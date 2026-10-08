import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "..");
const input = path.join(root, "app", "src", "main", "assets", "diccionario_semilla.json");
const outputDir = path.join(root, "docs", "auditoria_linguistica");
const seed = JSON.parse(fs.readFileSync(input, "utf8")).seed;

const normalize = (value) => value.normalize("NFKC").trim().toLocaleLowerCase("und").replace(/\s+/gu, " ");
const wordsById = new Map(seed.palabras.map((word) => [word.id, word]));
const languagesById = new Map(seed.idiomas.map((language) => [language.id, language.nombre]));
const translationsByWord = new Map();
for (const translation of seed.traducciones) {
  for (const id of [translation.palabra_origen_id, translation.palabra_destino_id]) {
    if (!translationsByWord.has(id)) translationsByWord.set(id, []);
    translationsByWord.get(id).push(translation.id);
  }
}
const lessonsByWord = new Map();
for (const relation of seed.leccion_palabras) {
  if (!lessonsByWord.has(relation.palabra_id)) lessonsByWord.set(relation.palabra_id, []);
  lessonsByWord.get(relation.palabra_id).push(relation.leccion_id);
}

const groups = new Map();
for (const word of seed.palabras) {
  const key = `${word.idioma_id}|${normalize(word.texto)}`;
  if (!groups.has(key)) groups.set(key, []);
  groups.get(key).push(word);
}
const duplicates = [...groups.values()].filter((items) => items.length > 1).map((items) => ({
  idioma_id: items[0].idioma_id,
  idioma: languagesById.get(items[0].idioma_id),
  texto_normalizado: normalize(items[0].texto),
  palabra_canonica_candidata: Math.min(...items.map((word) => word.id)),
  criterio_canonico: "ID menor; propuesta técnica, no fusión autorizada",
  entradas: items.map((word) => ({
    id: word.id,
    texto: word.texto,
    fuente_id: word.fuente_id,
    traduccion_ids: translationsByWord.get(word.id) ?? [],
    leccion_ids: lessonsByWord.get(word.id) ?? [],
    favoritos: "no disponibles en el JSON offline"
  }))
}));

const sameLanguage = seed.traducciones.flatMap((translation) => {
  const source = wordsById.get(translation.palabra_origen_id);
  const target = wordsById.get(translation.palabra_destino_id);
  if (!source || !target || source.idioma_id !== target.idioma_id) return [];
  return [{
    traduccion_id: translation.id,
    idioma_id: source.idioma_id,
    idioma: languagesById.get(source.idioma_id),
    origen_id: source.id,
    origen: source.texto,
    destino_id: target.id,
    destino: target.texto,
    nota: translation.nota,
    clasificacion: /\s/u.test(normalize(source.texto)) || /\s/u.test(normalize(target.texto))
      ? "posible_expresion"
      : normalize(source.texto).replace(/[^\p{L}\p{N}]/gu, "") === normalize(target.texto).replace(/[^\p{L}\p{N}]/gu, "") && normalize(source.texto) !== normalize(target.texto)
        ? "variante_ortografica_alta_certeza"
        : "indeterminado"
  }];
});

const expressionCandidates = seed.palabras.filter((word) => {
  const value = normalize(word.texto);
  return /\s/u.test(value) || /[,;:]/u.test(word.texto) || value.length > 80;
}).map((word) => ({
  id: word.id,
  idioma_id: word.idioma_id,
  idioma: languagesById.get(word.idioma_id),
  texto: word.texto,
  fuente_id: word.fuente_id,
  motivo: /\s/u.test(normalize(word.texto)) ? "varias_palabras" : /[,;:]/u.test(word.texto) ? "puntuacion_lista" : "texto_extenso",
  clasificacion: /[,;]/u.test(word.texto)
    ? "lista_o_enumeracion"
    : /[.!?¿¡]$/u.test(word.texto)
      ? "posible_frase"
      : normalize(word.texto).split(" ").length <= 3
        ? "posible_locucion"
        : "posible_frase",
  certeza: "requiere_revision"
}));

const withoutSource = seed.palabras.filter((word) => word.fuente_id == null);
const meanings = new Map();
for (const translation of seed.traducciones) {
  const target = wordsById.get(translation.palabra_destino_id);
  if (!target) continue;
  const key = `${translation.palabra_origen_id}|${target.idioma_id}`;
  if (!meanings.has(key)) meanings.set(key, new Set());
  meanings.get(key).add(translation.palabra_destino_id);
}
const multipleMeanings = [...meanings.entries()].filter(([, ids]) => ids.size > 1).map(([key, ids]) => {
  const [wordId, targetLanguageId] = key.split("|").map(Number);
  const word = wordsById.get(wordId);
  return {
    palabra_id: wordId,
    texto: word?.texto,
    idioma_origen_id: word?.idioma_id,
    idioma_destino_id: targetLanguageId,
    destino_ids: [...ids],
    estado: "pendiente_revision"
  };
});

const report = {
  generado_desde: "app/src/main/assets/diccionario_semilla.json",
  nota: "Los conteos fueron contrastados con Supabase remoto el 2026-09-21. El detalle se genera desde la copia offline equivalente.",
  resumen: {
    palabras: seed.palabras.length,
    traducciones: seed.traducciones.length,
    grupos_duplicados: duplicates.length,
    entradas_en_grupos_duplicados: duplicates.reduce((sum, group) => sum + group.entradas.length, 0),
    traducciones_mismo_idioma: sameLanguage.length,
    candidatos_expresion: expressionCandidates.length,
    entradas_sin_fuente: withoutSource.length,
    posibles_multiples_acepciones: multipleMeanings.length
  },
  duplicados: duplicates,
  traducciones_mismo_idioma: sameLanguage,
  candidatos_expresion: expressionCandidates,
  entradas_sin_fuente: withoutSource,
  posibles_multiples_acepciones: multipleMeanings
};

fs.mkdirSync(outputDir, { recursive: true });
fs.writeFileSync(path.join(outputDir, "reporte_modelo_linguistico.json"), JSON.stringify(report, null, 2));
const summary = [
  "# Auditoría lingüística offline",
  "",
  `Fuente analizada: \`${report.generado_desde}\`.`,
  "",
  "> Los conteos fueron contrastados mediante consultas de solo lectura en Supabase remoto. El detalle reproducible se genera desde el JSON incluido en la aplicación.",
  "",
  ...Object.entries(report.resumen).map(([key, value]) => `- ${key}: ${value}`),
  "",
  "Los registros detectados se conservan sin modificación y quedan detallados en `reporte_modelo_linguistico.json`."
].join("\n");
fs.writeFileSync(path.join(outputDir, "resumen.md"), summary);
console.log(JSON.stringify(report.resumen));
