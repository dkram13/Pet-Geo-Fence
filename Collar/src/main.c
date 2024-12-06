#include <stdio.h>
#include <string.h>
#include <zephyr/kernel.h>
#include <nrf_modem_at.h>
#include <modem/lte_lc.h>
#include <modem/location.h>
#include <modem/nrf_modem_lib.h>
#include <modem/modem_battery.h>
#include <date_time.h>
#include <ncs_version.h>
#include <zephyr/net/coap.h>
#include <zephyr/net/socket.h>
#include <dk_buttons_and_leds.h>
#include <zephyr/random/rand32.h>
#include "tg.h"
#include <zephyr/drivers/pwm.h>
#include <zephyr/devicetree.h>
#include <zephyr/logging/log.h>
#include <zephyr/drivers/gpio.h>
#include <zephyr/device.h>

#define SEC_TAG 12
#define APP_COAP_SEND_INTERVAL_MS 60000
#define APP_COAP_MAX_MSG_LEN 1280
#define APP_COAP_VERSION 1
#define MAX_WKT_SIZE 1280

static int sock;
static struct sockaddr_storage server;
static uint16_t next_token;
static uint8_t coap_buf[APP_COAP_MAX_MSG_LEN];
static uint8_t coap_sendbug[64];
static char wkt_polygon[1280];
const struct pwm_dt_spec sBuzzer = PWM_DT_SPEC_GET(DT_ALIAS(buzzer_pwm));
static struct location_event_data location_batch;

static K_SEM_DEFINE(location_event, 0, 1);
static K_SEM_DEFINE(lte_connected, 0, 1);
static K_SEM_DEFINE(time_update_finished, 0, 1);

static void buzzer_beep_one_second(void) {
    uint32_t period_ns = 500000;
    uint32_t pulse_ns = period_ns / 2;

    int ret = pwm_set_dt(&sBuzzer, period_ns, pulse_ns);
    if (ret < 0) {
        printk("Failed to turn on the buzzer: %d\n", ret);
        return;
    }
    printk("Buzzer turned on\n");

    k_sleep(K_SECONDS(1));

    ret = pwm_set_dt(&sBuzzer, period_ns, 0);
    if (ret < 0) {
        printk("Failed to turn off the buzzer: %d\n", ret);
        return;
    }
    printk("Buzzer turned off\n");
}

static void date_time_evt_handler(const struct date_time_evt *evt)
{
	k_sem_give(&time_update_finished);
}

static void lte_event_handler(const struct lte_lc_evt *const evt)
{
	switch (evt->type) {
	case LTE_LC_EVT_NW_REG_STATUS:
		if ((evt->nw_reg_status == LTE_LC_NW_REG_REGISTERED_HOME) ||
		     (evt->nw_reg_status == LTE_LC_NW_REG_REGISTERED_ROAMING)) {
			printk("Connected to LTE\n");
			k_sem_give(&lte_connected);
		}
		break;
	default:
		break;
	}
}

static void location_event_handler(const struct location_event_data *event_data)
{
	switch (event_data->id) {
	case LOCATION_EVT_LOCATION:
		memcpy(&location_batch, event_data, sizeof(struct location_event_data));

		printk("Got location:\n");
		printk("  method: %s\n", location_method_str(event_data->method));
		printk("  latitude: %.06f\n", event_data->location.latitude);
		printk("  longitude: %.06f\n", event_data->location.longitude);
		printk("  accuracy: %.01f m\n", event_data->location.accuracy);

		if (event_data->location.datetime.valid) {
			printk("  date: %04d-%02d-%02d\n",
				event_data->location.datetime.year,
				event_data->location.datetime.month,
				event_data->location.datetime.day);
			printk("  time: %02d:%02d:%02d.%03d UTC\n",
				event_data->location.datetime.hour,
				event_data->location.datetime.minute,
				event_data->location.datetime.second,
				event_data->location.datetime.ms);
		}
		printk("  Google maps URL: https://maps.google.com/?q=%.06f,%.06f\n\n",
			event_data->location.latitude, event_data->location.longitude);
		break;

	case LOCATION_EVT_TIMEOUT:
		printk("Getting location timed out\n\n");
		break;

	case LOCATION_EVT_ERROR:
		printk("Getting location failed\n\n");
		break;

	case LOCATION_EVT_GNSS_ASSISTANCE_REQUEST:
		printk("Getting location assistance requested (A-GNSS). Not doing anything.\n\n");
		break;

	case LOCATION_EVT_GNSS_PREDICTION_REQUEST:
		printk("Getting location assistance requested (P-GPS). Not doing anything.\n\n");
		break;

	default:
		printk("Getting location: Unknown event\n\n");
		break;
	}

	k_sem_give(&location_event);
}

static void location_event_wait(void)
{
	k_sem_take(&location_event, K_FOREVER);
}

static void location_with_fallback_get(void)
{
	int err;
	struct location_config config;
	enum location_method methods[] = {LOCATION_METHOD_GNSS, LOCATION_METHOD_CELLULAR};

	location_config_defaults_set(&config, ARRAY_SIZE(methods), methods);
	config.methods[0].gnss.timeout = 240 * MSEC_PER_SEC;
	config.methods[1].cellular.timeout = 40 * MSEC_PER_SEC;

	printk("Requesting location with short GNSS timeout to trigger fallback to cellular...\n");

	err = location_request(&config);
	if (err) {
		printk("Requesting location failed, error: %d\n", err);
		return;
	}

	location_event_wait();
}

static int server_resolve(void)
{
	int err;
	struct addrinfo *result;
	struct addrinfo hints = {
		.ai_family = AF_INET,
		.ai_socktype = SOCK_DGRAM
	};
	char ipv4_addr[NET_IPV4_ADDR_LEN];

	err = getaddrinfo(CONFIG_COAP_SERVER_HOSTNAME, NULL, &hints, &result);
	if (err != 0) {
		printk("ERROR: getaddrinfo failed %d\n", err);
		return -EIO;
	}

	if (result == NULL) {
		printk("ERROR: Address not found\n");
		return -ENOENT;
	}

	struct sockaddr_in *server4 = ((struct sockaddr_in *)&server);

	server4->sin_addr.s_addr =
		((struct sockaddr_in *)result->ai_addr)->sin_addr.s_addr;
	server4->sin_family = AF_INET;
	server4->sin_port = htons(CONFIG_COAP_SERVER_PORT);

	inet_ntop(AF_INET, &server4->sin_addr.s_addr, ipv4_addr,
		  sizeof(ipv4_addr));
	printk("IPv4 Address found %s\n", ipv4_addr);

	freeaddrinfo(result);

	return 0;
}

static int server_connect(void)
{
	int err;

	sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	if (sock < 0) {
		printk("Failed to create CoAP socket: %d.\n", errno);
		return -errno;
	}

	err = connect(sock, (struct sockaddr *)&server,
		      sizeof(struct sockaddr_in));
	if (err < 0) {
		printk("Connect failed : %d\n", errno);
		return -errno;
	}

	return 0;
}

static int get_battery_level(void) {
    int ret, battery_voltage;

    ret = modem_battery_voltage_get(&battery_voltage);
    if (ret == 0) {
        return battery_voltage;
    } else {
        printk("Error retrieving battery voltage: %d\n", ret);
        return -1; 
    }
}

static void batch_prep(char *json_buffer, size_t buffer_size) {
    int battery_level = get_battery_level();
    if (battery_level < 0) {
        strcpy(json_buffer, "{\"error\": \"Failed to get battery level\"}");
        return;
    }

    char wkt_point[256];
    snprintf(wkt_point, sizeof(wkt_point), "POINT(%.06f %.06f)", 
             location_batch.location.longitude, 
             location_batch.location.latitude);

    int in_bounds = check_in_bounds(wkt_point, wkt_polygon);

    snprintf(json_buffer, buffer_size, "{"
        "\"method\": \"%s\","
        "\"latitude\": %.06f,"
        "\"longitude\": %.06f,"
        "\"accuracy\": %.01f,"
        "\"battery\": %d,"
        "\"in_bounds\": %d"
        "}",
        location_method_str(location_batch.method),
        location_batch.location.latitude,
        location_batch.location.longitude,
        location_batch.location.accuracy,
        battery_level,
        in_bounds
    );
}

static int client_put_send(void)
{
    int err;
    char payload_buffer[256];
    struct coap_packet request;

    batch_prep(payload_buffer, sizeof(payload_buffer));
    next_token = sys_rand32_get();

    char uri_path[128];
    snprintf(uri_path, sizeof(uri_path), "%s/%s", CONFIG_DEVICE_IMEI, CONFIG_COAP_PUT_RESOURCE);

    err = coap_packet_init(&request, coap_buf, sizeof(coap_buf),
                           APP_COAP_VERSION, COAP_TYPE_NON_CON,
                           sizeof(next_token), (uint8_t *)&next_token,
                           COAP_METHOD_PUT, coap_next_id());
    if (err < 0) {
        printk("Failed to create CoAP request, %d\n", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)uri_path, strlen(uri_path));
    if (err < 0) {
        printk("Failed to encode CoAP option, %d\n", err);
        return err;
    }

    const uint8_t text_plain = COAP_CONTENT_FORMAT_TEXT_PLAIN;
    err = coap_packet_append_option(&request, COAP_OPTION_CONTENT_FORMAT,
                                    &text_plain, sizeof(text_plain));
    if (err < 0) {
        printk("Failed to encode CoAP option, %d\n", err);
        return err;
    }

    err = coap_packet_append_payload_marker(&request);
    if (err < 0) {
        printk("Failed to append payload marker, %d\n", err);
        return err;
    }

    err = coap_packet_append_payload(&request, (uint8_t *)payload_buffer, strlen(payload_buffer));
    if (err < 0) {
        printk("Failed to append payload, %d\n", err);
        return err;
    }

    err = send(sock, request.data, request.offset, 0);
    if (err < 0) {
        printk("Failed to send CoAP request, %d\n", errno);
        return -errno;
    }

    printk("CoAP PUT request sent: Token 0x%04x\n", next_token);

    return 0;
}

static int client_get_send(void)
{
    struct coap_packet request;
    next_token = sys_rand32_get();

    char uri_path[128];
    snprintf(uri_path, sizeof(uri_path), "%s/%s", CONFIG_DEVICE_IMEI, CONFIG_COAP_GET_RESOURCE);

    int err = coap_packet_init(&request, coap_buf, sizeof(coap_buf),
                               APP_COAP_VERSION, COAP_TYPE_NON_CON,
                               sizeof(next_token), (uint8_t *)&next_token,
                               COAP_METHOD_GET, coap_next_id());

    if (err < 0) {
        printk("Failed to create CoAP request, %d\n", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)uri_path, strlen(uri_path));
    if (err < 0) {
        printk("Failed to encode CoAP option, %d\n", err);
        return err;
    }

    err = send(sock, request.data, request.offset, 0);
    if (err < 0) {
        printk("Failed to send CoAP request, %d\n", errno);
        return -errno;
    }

    printk("CoAP GET request sent: Token 0x%04x\n", next_token);

    return 0;
}

static int client_handle_put_response(uint8_t *buf, int received)
{
	int err;
	struct coap_packet reply;
	const uint8_t *payload;
	uint16_t payload_len;
	uint8_t token[8];
	uint16_t token_len;
	static uint8_t temp_buf[1024];

	err = coap_packet_parse(&reply, buf, received, NULL, 0);
	if (err < 0) {
		printk("Malformed response received: %d\n", err);
		return err;
	}

	payload = coap_packet_get_payload(&reply, &payload_len);
	token_len = coap_header_get_token(&reply, token);

	if ((token_len != sizeof(next_token)) ||
	    (memcmp(&next_token, token, sizeof(next_token)) != 0)) {
		printk("Invalid token received: 0x%02x%02x\n",
		       token[1], token[0]);
		return 0;
	}

	if (payload_len > 0) {
		snprintf(temp_buf, MIN(payload_len + 1, sizeof(temp_buf)), "%s", payload);
	} else {
		strcpy(temp_buf, "EMPTY");
	}

	printk("CoAP response: Code 0x%x, Token 0x%02x%02x, Payload: %s\n",
	       coap_header_get_code(&reply), token[1], token[0], temp_buf);

	return 0;
}

static int client_handle_get_response(uint8_t *buf, int received)
{
	int err;
	struct coap_packet reply;
	const uint8_t *payload;
	uint16_t payload_len;
	uint8_t token[8];
	uint16_t token_len;

	err = coap_packet_parse(&reply, buf, received, NULL, 0);
	if (err < 0) {
		printk("Malformed response received: %d\n", err);
		return err;
	}

	payload = coap_packet_get_payload(&reply, &payload_len);
	token_len = coap_header_get_token(&reply, token);

	if ((token_len != sizeof(next_token)) ||
	    (memcmp(&next_token, token, sizeof(next_token)) != 0)) {
		printk("Invalid token received: 0x%02x%02x\n",
		       token[1], token[0]);
		return 0;
	}

	if (payload_len > 0) {
		snprintf(wkt_polygon, MIN(payload_len + 1, MAX_WKT_SIZE), "%s", payload);
	} else {
		strcpy(wkt_polygon, "EMPTY");
	}

	printk("CoAP response: Code 0x%x, Token 0x%02x%02x, Payload: %s\n",
	       coap_header_get_code(&reply), token[1], token[0], wkt_polygon);

	return 0;
}

int check_in_bounds(const char *wkt_point, const char *wkt_polygon) {
    if (strcmp(wkt_polygon, "POLYGON EMPTY") == 0) {
        printk("Polygon is empty. Skipping bounds check.\n");
        return 1;
    }
	
    struct tg_geom *a = tg_parse_wkt(wkt_point);
    if (tg_geom_error(a)) {
        printk("Error parsing geometry POINT\n");
        return 0;
    }

    struct tg_geom *b = tg_parse_wkt(wkt_polygon);
    if (tg_geom_error(b)) {
        printk("Error parsing geometry POLYGON\n");
        tg_geom_free(a); 
        return 0;
    }

    int result = tg_geom_intersects(a, b);
    if (result) {
        printk("IN BOUNDS\n");
    } else {
        printk("OUT OF BOUNDS\n");
		buzzer_beep_one_second();
    }

    tg_geom_free(a);
    tg_geom_free(b);

    return result;
}

int main(void)
{
	buzzer_beep_one_second();
	
	int err, received;

	err = nrf_modem_lib_init();
	if (err) {
		printk("Modem library initialization failed, error: %d\n", err);
		return err;
	}

	if (IS_ENABLED(CONFIG_DATE_TIME)) {
		date_time_register_handler(date_time_evt_handler);
	}

	printk("Connecting to LTE...\n");

	lte_lc_init();
	lte_lc_register_handler(lte_event_handler);

	lte_lc_psm_req(true);
	lte_lc_connect();

	k_sem_take(&lte_connected, K_FOREVER);

	if (IS_ENABLED(CONFIG_DATE_TIME)) {
		printk("Waiting for current time\n");

		k_sem_take(&time_update_finished, K_MINUTES(10));

		if (!date_time_is_valid()) {
			printk("Failed to get current time. Continuing anyway.\n");
		}
	}

	err = location_init(location_event_handler);
	if (err) {
		printk("Initializing the Location library failed, error: %d\n", err);
		return -1;
	}

	while(1) {
		location_with_fallback_get();

		if (server_resolve() != 0) {
			printk("Failed to resolve server name\n");
			return 0;
		}

		printk("Connecting to CoAP server...\r\n");
		if (server_connect() != 0) {
			printk("Failed to initialize CoAP client\n");
			return 0;
		}

		printk("Sending get request to CoAP server...\r\n");
		if (client_get_send() != 0) {
			printk("Failed to send GET request, exit...\n");
			return 0;
		}

		printk("Waiting for response from CoAP server...\r\n");
		received = recv(sock, coap_buf, sizeof(coap_buf), 0);
		if (received < 0) {
			printk("Error reading response\n");
			return 0;
		} else if (received == 0) {
			printk("Disconnected\n");
			return 0;
		}

		err = client_handle_get_response(coap_buf, received);
		if (err < 0) {
			printk("Invalid response, exit...\n");
			return 0;
		}

		printk("Sending PUT request to CoAP server...\r\n");
		if (client_put_send() != 0) {
			printk("Failed to send PUT request, exit...\n");
			return 0;
		}

		printk("Waiting for response from CoAP server...\r\n");
		received = recv(sock, coap_buf, sizeof(coap_buf), 0);
		if (received < 0) {
			printk("Error reading response\n");
			return 0;
		} else if (received == 0) {
			printk("Disconnected\n");
			return 0;
		}

		err = client_handle_put_response(coap_buf, received);
		if (err < 0) {
			printk("Invalid response, exit...\n");
			return 0;
		}

		(void)close(sock);
		printk("Sleeping...");
		k_sleep(K_MSEC(1000));
	}
	return 0;
}
