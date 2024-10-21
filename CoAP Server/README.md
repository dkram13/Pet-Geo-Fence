# PGF CoAP Server
Using the [aiocoap-client](https://aiocoap.readthedocs.io/en/latest/module/aiocoap.cli.client.html), we can interact with the CoAP server over the command line.

## Observing Resources
```
python -m aiocoap.cli.client --observe coap://[::1]/observe
```

## Putting Resources
```
python -m aiocoap.cli.client -m put coap://[::1]/observe --payload "New content"
```
