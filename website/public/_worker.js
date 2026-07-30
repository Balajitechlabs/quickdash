export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (/\.[a-z0-9]+$/i.test(url.pathname) || url.pathname === "/") {
      return env.ASSETS.fetch(request);
    }
    return env.ASSETS.fetch(new Request(new URL("/index.html", request.url), request));
  }
};
