# PGF CoAP Server

## Build CoAP Docker Container
```
docker build -t coap-server:latest .
```

## Run Docker Container
```
docker run -d -p 5683:5683/udp coap-server:latest

```
