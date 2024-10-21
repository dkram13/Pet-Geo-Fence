import aiocoap
import aiocoap.resource as resource
import asyncio

class ObservableResourceWithPUT(resource.ObservableResource):
    def __init__(self):
        super().__init__()
        self.content = "Initial content"  # The resource's initial state

    async def render_get(self, request):
        """Handle GET requests, returning the current content."""
        return aiocoap.Message(payload=self.content.encode('utf8'))

    async def render_put(self, request):
        """Handle PUT requests, updating the content and notifying observers."""
        self.content = request.payload.decode('utf8')
        print(f"Resource updated: {self.content}")
        self.updated_state()  # Notify observers of the updated content
        return aiocoap.Message(code=aiocoap.CHANGED, payload=b"Resource updated")

    def notify(self):
        """Notify observers manually if needed."""
        self.updated_state()

async def main():
    # Resource tree creation
    root = resource.Site()

    # Adding the ObservableResourceWithPUT at the path /observe
    observable_resource = ObservableResourceWithPUT()
    root.add_resource(['observe'], observable_resource)

    # Create a server context to listen on all interfaces (IPv6 loopback here)
    await aiocoap.Context.create_server_context(root, bind=('::1', 5683))

    # Wait indefinitely for incoming requests
    await asyncio.get_running_loop().create_future()

if __name__ == "__main__":
    asyncio.run(main())