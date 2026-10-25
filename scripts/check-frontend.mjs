import { readdir, readFile } from 'node:fs/promises';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
const directory = new URL('../src/main/resources/static/assets/',import.meta.url);
const files = (await readdir(directory)).filter(f => f.endsWith('.js'));
for (const file of files) {
  const path = new URL(file,directory);
  execFileSync(process.execPath,['--check',fileURLToPath(path)],{stdio:'inherit'});
  const code = await readFile(path,'utf8');
  for (const match of code.matchAll(/(?:from\s+|import\()(['"])(\.\/[^'"]+)\1/g)) await readFile(new URL(match[2],directory));
}
console.log(`${files.length} módulos verificados: sintaxis e imports locales correctos.`);
