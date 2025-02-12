export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    
    // Serve static assets from /_next
    if (url.pathname.startsWith('/_next/')) {
      return env.ASSETS.fetch(request);
    }

    // Try to serve the requested path
    try {
      const response = await env.ASSETS.fetch(request);
      if (response.status === 404) {
        // If the exact path is not found, try serving index.html
        return env.ASSETS.fetch(`${url.origin}/index.html`);
      }
      return response;
    } catch (e) {
      // If there's an error, serve index.html
      return env.ASSETS.fetch(`${url.origin}/index.html`);
    }
  }
}
