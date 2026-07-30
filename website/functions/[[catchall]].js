export async function onRequest(context) {
  const url = new URL(context.request.url);
  if (/\.[a-z0-9]+$/i.test(url.pathname)) return context.next();
  return context.env.ASSETS.fetch(new Request(new URL("/index.html", context.request.url), context.request));
}
