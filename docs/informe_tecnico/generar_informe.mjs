import fs from 'node:fs/promises';
import path from 'node:path';
import { pathToFileURL } from 'node:url';
import { createRequire } from 'node:module';

const dependencyRoot = process.argv[2];
const outputDirectory = path.dirname(new URL(import.meta.url).pathname.replace(/^\/([A-Za-z]:)/, '$1'));
const require = createRequire(path.join(dependencyRoot, 'package.json'));
const { marked } = await import(pathToFileURL(require.resolve('marked')).href);
const { chromium } = require('playwright');
const escapeHtml = value => value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;');
const stylesheet = `
:root { color-scheme: light; --ink:#172332; --accent:#175d8e; }
* { box-sizing:border-box; }
body { margin:0; color:var(--ink); background:#edf1f5; font:16px/1.65 'Segoe UI',Arial,sans-serif; }
.layout { max-width:1440px; margin:auto; display:grid; grid-template-columns:265px minmax(0,1fr); }
nav { padding:30px 20px; position:sticky; top:0; height:100vh; overflow:auto; font-size:13px; }
nav a { display:block; margin:0 0 9px; text-decoration:none; color:#244d70; }
nav strong { display:block; margin-bottom:18px; }
main { padding:50px 62px 80px; background:white; min-width:0; }
h1 { color:#111; font-size:38px; line-height:1.18; margin:0 0 28px; }
h2 { font-size:25px; margin-top:46px; line-height:1.3; color:#173951; scroll-margin-top:20px; }
h3 { font-size:19px; margin-top:30px; color:#244d70; }
p { margin:12px 0; }
a { color:#175d8e; overflow-wrap:anywhere; }
table { width:100%; border-collapse:collapse; margin:20px 0; font-size:13px; line-height:1.45; table-layout:auto; }
th,td { border:1px solid #cbd5df; padding:9px 10px; text-align:left; vertical-align:top; overflow-wrap:anywhere; }
th { background:#e8f0f6; color:#143650; }
tr:nth-child(even) td { background:#f8fafc; }
pre { background:#f2f5f8; padding:18px; white-space:pre-wrap; overflow-wrap:anywhere; font:13px/1.6 Consolas,monospace; }
code { font:0.88em Consolas,monospace; overflow-wrap:anywhere; }
li { margin:8px 0; }
.meta { font-size:13px; color:#4c6275; margin-bottom:24px; }
.related { border:1px solid #ccd8e2; padding:14px; background:#f6f9fb; }
@media(max-width:950px){ .layout{display:block;} nav{position:static;height:auto;} main{padding:28px 22px;} }
@media print {
 @page { size:A4; margin:16mm 16mm 18mm; }
 body { background:white; font:10pt/1.45 Arial,sans-serif; }
 .layout { display:block; max-width:none; }
 nav { display:none; }
 main { padding:0; }
 h1 { font-size:25pt; }
 h2 { font-size:16pt; margin-top:24pt; break-after:avoid; }
 h3 { font-size:12pt; break-after:avoid; }
 p,li { orphans:3; widows:3; }
 table { font-size:8pt; margin:10pt 0; }
 th,td { padding:5pt; }
 tr { break-inside:avoid; }
 thead { display:table-header-group; }
 pre { font-size:8pt; break-inside:avoid; }
 a { text-decoration:none; }
 .meta { font-size:9pt; }
}
`;
const documents = [
    { stem:'INFORME_AIKUKISNA', title:'Informe técnico de Aikukisna', other:'DICCIONARIO_DATOS', link:'Consultar el diccionario completo de datos' },
    { stem:'DICCIONARIO_DATOS', title:'Diccionario de datos de Aikukisna', other:'INFORME_AIKUKISNA', link:'Volver al informe técnico' }
];
for (const document of documents) {
    const source = await fs.readFile(path.join(outputDirectory, `${document.stem}.md`), 'utf8');
    let html = await marked.parse(source);
    let headingNumber = 0;
    const sections = [];
    html = html.replace(/<h2>(.*?)<\/h2>/g, (_, title) => {
        const id = `seccion-${++headingNumber}`;
        sections.push(`<a href="#${id}">${title}</a>`);
        return `<h2 id="${id}">${title}</h2>`;
    });
    const output = `<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>${escapeHtml(document.title)}</title><style>${stylesheet}</style></head><body><div class="layout"><nav aria-label="Contenido"><strong>AIKUKISNA<br>Documentación técnica</strong>${sections.join('')}</nav><main><p class="meta">Corte técnico 1 de octubre de 2026 · Diseño y desarrollo · Lectura y defensa</p>${html}<p class="related"><a href="${document.other}.html">${document.link}</a></p></main></div></body></html>`;
    await fs.writeFile(path.join(outputDirectory, `${document.stem}.html`), output);
}
const browser = await chromium.launch({ headless:true, executablePath:process.argv[3] });
for (const document of documents) {
    const page = await browser.newPage({ viewport:{width:1440,height:1000} });
    await page.goto(pathToFileURL(path.join(outputDirectory, `${document.stem}.html`)).href);
    await page.pdf({
        path:path.join(outputDirectory, `${document.stem}.pdf`),
        format:'A4', preferCSSPageSize:true, printBackground:true, displayHeaderFooter:true,
        headerTemplate:'<span></span>',
        footerTemplate:'<div style="font-family:Arial;font-size:8px;width:100%;text-align:center;color:#667788">Aikukisna · Referencia técnica 2026-10-01 · <span class="pageNumber"></span> / <span class="totalPages"></span></div>'
    });
    await page.screenshot({ path:path.join(outputDirectory, `${document.stem}_vista.png`) });
    console.log(JSON.stringify({document:document.stem,headings:await page.locator('h2').count(),tables:await page.locator('table').count(),horizontalOverflow:await page.evaluate(()=>document.documentElement.scrollWidth>window.innerWidth)}));
    await page.close();
}
await browser.close();
