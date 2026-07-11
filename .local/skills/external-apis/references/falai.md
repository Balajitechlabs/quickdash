# fal.ai

Proxy requests to fal.ai via Replit-managed billing.

## Callback

Use `externalApi__falai` in `codeExecution`.

## Allowed operations

- `POST` `/fal-ai/bria/background/remove{/}?` - Bria RMBG 2.0 background remove (queue submit).
- `GET` | `PUT` `/fal-ai/bria/requests/:request_id{/:action(status|cancel)}?` - Bria queue lifecycle (model-family-scoped): fetch result, poll status, cancel.

Authorization is handled automatically by Replit. Do not pass an `Authorization` header.

## Skill

## fal.ai background removal quickstart

Bria RMBG background removal through fal.ai passthrough billing.
Submit a queue job, poll status, then fetch the result:

```javascript
const submit = await externalApi__falai({
  path: '/fal-ai/bria/background/remove',
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: {image_url: 'https://upload.wikimedia.org/wikipedia/commons/3/3a/Cat03.jpg'},
})

const requestId = submit.body.request_id
let result
for (let attempt = 0; attempt < 30; attempt++) {
  await new Promise((resolve) => setTimeout(resolve, 2000))
  const status = await externalApi__falai({
    path: '/fal-ai/bria/requests/' + requestId + '/status',
    method: 'GET',
  })
  if (status.body.status === 'COMPLETED') {
    result = await externalApi__falai({
      path: '/fal-ai/bria/requests/' + requestId,
      method: 'GET',
    })
    break
  }
}

console.log(result.body.image.url)
```

`image_url` must be a publicly fetchable image. Authorization is
managed by passthrough billing. Do not set an `Authorization`
header manually.
