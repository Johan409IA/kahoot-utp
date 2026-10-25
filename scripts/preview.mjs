import { createServer } from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { resolve, sep, extname } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = fileURLToPath(new URL('../src/main/resources/static/', import.meta.url));
const port = Number(process.env.FRONTEND_PORT || 4173);
const apiOrigin = process.env.ADAPTCODE_API_ORIGIN || 'http://localhost:8080';
const types = {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8','.svg':'image/svg+xml'};
const server = createServer(async (req,res) => {
  try {
    const url = new URL(req.url,'http://localhost');
    if (url.pathname.startsWith('/api/')) {
      const buffers = []; for await (const part of req) buffers.push(part);
      const headers = { accept: 'application/json' };
      for (const name of ['authorization','content-type']) if (req.headers[name]) headers[name] = req.headers[name];
      const response = await fetch(new URL(url.pathname + url.search, apiOrigin), {method:req.method,headers,...(!['GET','HEAD'].includes(req.method) ? {body:Buffer.concat(buffers)} : {}),signal:AbortSignal.timeout(12000)});
      res.writeHead(response.status, {'content-type':response.headers.get('content-type') || 'application/json'});
      res.end(Buffer.from(await response.arrayBuffer())); return;
    }
    if (!['GET','HEAD'].includes(req.method)) { res.writeHead(405); res.end(); return; }
    const path = resolve(root, `.${decodeURIComponent(url.pathname === '/' ? '/index.html' : url.pathname)}`);
    if (!path.startsWith(resolve(root) + sep)) { res.writeHead(403); res.end(); return; }
    if (!(await stat(path)).isFile()) { res.writeHead(404); res.end(); return; }
    const contents = await readFile(path);
    res.writeHead(200, {'content-type':types[extname(path)] || 'application/octet-stream','cache-control':'no-store'});
    res.end(req.method === 'HEAD' ? undefined : contents);
  } catch (error) {
    const api = req.url.startsWith('/api/');
    res.writeHead(api ? 503 : 404, {'content-type':'application/json; charset=utf-8'});
    res.end(JSON.stringify({error:api ? 'La API no está disponible. Inicia Spring Boot en localhost:8080 o explora la demo.' : 'Archivo no encontrado.'}));
  }
});
server.listen(port,'127.0.0.1',() => console.log(`AdaptCode UTP: http://127.0.0.1:${port}\nDemo: http://127.0.0.1:${port}/?demo=1#/\nAPI proxy: ${apiOrigin}`));
for (const signal of ['SIGTERM','SIGINT']) process.on(signal, () => server.close(() => process.exit(0)));
