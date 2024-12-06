const coap = require('coap');

// Map to store resources for each IMEI
const resources = {};

// Notify observers function
function notifyObservers(imei) {
  const resource = resources[imei];
  if (resource && resource.observers) {
    resource.observers.forEach((observer) => {
      console.log(`Notifying observer for IMEI: ${imei}`);
      observer.res.write(resource.batchContent, () => {
        observer.lastSeen = Date.now(); // Update timestamp after notification
      });
    });
  }
}

// Remove stale observers periodically
setInterval(() => {
  Object.keys(resources).forEach((imei) => {
    const resource = resources[imei];
    resource.observers = resource.observers.filter((observer) => {
      const isAlive = Date.now() - observer.lastSeen < 60000; // 60 seconds timeout
      if (!isAlive) {
        console.log(`Removing stale observer for IMEI: ${imei}`);
      }
      return isAlive;
    });
  });
}, 10 * 60 * 1000); // Run cleanup every 30 seconds

// Handle /.well-known/core for resource discovery
function handleDiscovery(req, res) {
  const discoveryResponse = Object.keys(resources).map(imei =>
    `</${imei}/batch>;rt="observe",</${imei}/boundary>;rt="data"`
  ).join(',');
  res.setOption('Content-Format', 'application/link-format');
  res.end(discoveryResponse);
}

// Handle requests for IMEI-based resources
function handleResourceRequest(req, res) {
  const urlParts = req.url.split('/');
  const imei = urlParts[1];
  const resourceType = urlParts[2];

  if (!imei || !resourceType) {
    res.statusCode = 400; // Bad Request
    res.end('Invalid URL format. Expected /IMEI/resource.');
    return;
  }

  // Initialize resources for new IMEI
  if (!resources[imei]) {
    resources[imei] = {
      batchContent: 'Initial content',
      boundaryContent: 'POLYGON EMPTY', // Default empty WKT polygon
      observers: []
    };
    console.log(`Created resources for IMEI: ${imei}`);
  }

  const resource = resources[imei];

  if (resourceType === 'batch') {
    // Handle /IMEI/batch requests
    switch (req.method) {
      case 'GET':
        if (req.headers.Observe === 0) {
          const existingObserver = resource.observers.find((observer) =>
            observer.token.equals(req._packet.token)
          );
        
          if (!existingObserver) {
            const newObserver = {
              token: req._packet.token,
              res,
              lastSeen: Date.now(),
            };
            resource.observers.push(newObserver);
            console.log(
              `Observer added for IMEI: ${imei}, Token: ${newObserver.token.toString('hex')}, Time: ${new Date(newObserver.lastSeen).toISOString()}`
            );
          } else {
            console.log(
              `Duplicate observer attempt detected for IMEI: ${imei}, Token: ${existingObserver.token.toString('hex')}`
            );
          }
        
          res.setOption('Observe', 1);
          res.write(resource.batchContent);
        } else if (req.headers.Observe === 1) {
          const index = resource.observers.findIndex(observer =>
            observer.token.equals(req._packet.token)
          );
          if (index !== -1) {
            const removedObserver = resource.observers.splice(index, 1)[0];
            console.log(
              `Observer removed for IMEI: ${imei}, Token: ${removedObserver.token.toString('hex')}, Time: ${new Date(Date.now()).toISOString()}`
            );
          } else {
            console.log(
              `No observer found to remove for IMEI: ${imei}, Token: ${req._packet.token.toString('hex')}`
            );
          }
          res.end('Stopped observing');
        }
        
        break;

      case 'PUT':
        resource.batchContent = req.payload.toString();
        console.log(`Batch content updated for IMEI: ${imei}`);
        notifyObservers(imei);
        res.end('Batch updated successfully');
        break;

      default:
        res.statusCode = 405;
        res.end('Method Not Allowed');
        break;
    }
  } else if (resourceType === 'boundary') {
    // Handle /IMEI/boundary requests
    switch (req.method) {
      case 'GET':
        res.setOption('Content-Format', 'text/plain');
        res.end(resource.boundaryContent);
        break;

      case 'PUT':
        const newBoundary = req.payload.toString();
        if (
          newBoundary === 'POLYGON EMPTY' || (newBoundary.startsWith('POLYGON(') && newBoundary.endsWith(')'))
        ) {
          resource.boundaryContent = newBoundary;
          console.log(`Boundary updated for IMEI: ${imei}`);
          res.end('Boundary updated successfully');
        } else {
          res.statusCode = 400;
          res.end('Invalid WKT format for POLYGON');
        }
        break;

      default:
        res.statusCode = 405;
        res.end('Method Not Allowed');
        break;
    }
  } else {
    res.statusCode = 404;
    res.end('Resource not found');
  }
}

// Main request handler
function handleRequest(req, res) {
  if (req.url === '/.well-known/core') {
    handleDiscovery(req, res);
  } else {
    handleResourceRequest(req, res);
  }
}

// Create and start the CoAP server
const server = coap.createServer(handleRequest);

server.on('error', (err) => {
  console.error('Server error:', err);
});

// Graceful shutdown
process.on('SIGINT', () => {
  console.log('Shutting down server...');
  Object.keys(resources).forEach((imei) => {
    resources[imei].observers.forEach((observer) => {
      observer.res.end();
    });
  });
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});

server.listen(() => {
  console.log('CoAP Server listening on port 5683');
});