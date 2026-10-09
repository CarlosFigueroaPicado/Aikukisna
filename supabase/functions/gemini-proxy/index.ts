import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "jsr:@supabase/supabase-js@2";

const GEMINI_API_KEY = Deno.env.get("GEMINI_API_KEY");
const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SUPABASE_ANON_KEY = Deno.env.get("SUPABASE_ANON_KEY")!;
const GEMINI_URL =
  "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent";

const LARGO_MAXIMO_PROMPT = 4000;
const LARGO_MAXIMO_CONTEXTO = 2000;
// La app envía la personalidad de Tuki + contexto del estudiante + datos verificados.
const LARGO_MAXIMO_CONTEXTO_TUKI = 6000;
const MAXIMO_MENSAJES_HISTORIAL = 30;
const TAMANO_MAXIMO_ARCHIVO_BASE64 = 6_000_000;

const MIME_TYPES_IMAGEN_PERMITIDOS = new Set([
  "image/jpeg", "image/png", "image/webp", "image/heic", "image/heif",
]);

const MIME_TYPES_AUDIO_PERMITIDOS = new Set([
  "audio/wav", "audio/mp3", "audio/mpeg", "audio/aac",
  "audio/ogg", "audio/flac", "audio/aiff",
]);

const ALCANCE_TUKI =
  "Tuki está especializado en Miskito, Español, Inglés Kriol e Inglés Estándar. " +
  "Puedo ayudarte con vocabulario, traducción documentada, expresiones, pronunciación, " +
  "gramática, cultura y aprendizaje relacionados con estos cuatro idiomas.";

function jsonResponse(body: Record<string, unknown>, status: number): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function recortarContexto(valor: unknown, maximo = 12000): string {
  const texto = typeof valor === "string" ? valor : JSON.stringify(valor);
  return texto.length <= maximo ? texto : texto.slice(0, maximo);
}

function limpiarTexto(valor: string): string {
  return valor
    .replace(/^[¿¡"']+|[?!"']+$/g, "")
    .trim();
}

function idiomaMencionado(texto: string): number | null {
  const q = texto.toLowerCase();
  if (/\bmiskit[ou]\b/.test(q)) return 1;
  if (/\b(español|spanish)\b/.test(q)) return 2;
  if (/\b(kriol|creole)\b/.test(q)) return 3;
  if (/\b(inglés estándar|ingles estandar|standard english)\b/.test(q)) return 4;
  if (/\b(inglés|ingles|english)\b/.test(q)) return 4;
  return null;
}

function extraerTextoTraducible(texto: string): string | null {
  const q = texto.trim();
  const patrones = [
    /(?:¿?cómo se dice)\s+(.+?)(?:\s+en\s+(?:miskit[ou]|kriol|creole|inglés(?: estándar)?|ingles(?: estandar)?|english|español|spanish))?[?]?$/i,
    /(?:¿?qué significa)\s+(.+?)(?:\s+en\s+(?:miskit[ou]|kriol|creole|inglés(?: estándar)?|ingles(?: estandar)?|english|español|spanish))?[?]?$/i,
    /(?:traduce|traducir)\s+(.+?)(?:\s+(?:al|a|en)\s+(?:miskit[ou]|kriol|creole|inglés(?: estándar)?|ingles(?: estandar)?|english|español|spanish))?[?.!]?$/i,
    /(?:how do you say)\s+(.+?)(?:\s+in\s+(?:miskito|kriol|creole|spanish|english))?[?]?$/i,
    /(?:what does)\s+(.+?)\s+mean(?:\s+in\s+(?:miskito|kriol|creole|spanish|english))?[?]?$/i,
  ];

  for (const patron of patrones) {
    const match = q.match(patron);
    if (match?.[1]) return limpiarTexto(match[1]);
  }
  return null;
}

function esSolicitudTraduccion(texto: string): boolean {
  return /(cómo se dice|qué significa|traduce|traducir|translate|how do you say|what does .+ mean)/i.test(texto);
}

function valoresDestino(datos: any): string[] {
  const salidas: string[] = [];
  const agregar = (valor: unknown) => {
    if (typeof valor === "string" && valor.trim() && !salidas.includes(valor.trim())) {
      salidas.push(valor.trim());
    }
  };

  if (Array.isArray(datos?.resultados)) {
    for (const r of datos.resultados) {
      agregar(r?.texto_destino);
      if (Array.isArray(r?.tramos) && r.tramos.length > 0) {
        agregar(r?.texto_destino);
        agregar(r.tramos[r.tramos.length - 1]?.texto_destino);
      }
    }
  }

  if (Array.isArray(datos?.componentes)) {
    for (const c of datos.componentes) {
      const eq = c?.equivalencias;
      if (Array.isArray(eq?.resultados)) {
        for (const r of eq.resultados) agregar(r?.texto_destino);
      }
      if (Array.isArray(eq?.sugerencias) && salidas.length === 0) {
        for (const s of eq.sugerencias) agregar(s?.texto);
      }
    }
  }

  return salidas.slice(0, 6);
}

function formatearRespaldoTuki(datos: any, perfil: any): string {
  if (!datos) return ALCANCE_TUKI;
  if (datos?.tipo === "fuera_alcance") return datos?.respuesta || ALCANCE_TUKI;

  const destinos = valoresDestino(datos);
  if (destinos.length > 0) {
    const prefijo = destinos.length === 1
      ? "La equivalencia documentada es"
      : "Las equivalencias documentadas incluyen";
    return `${prefijo}: ${destinos.map((x) => `“${x}”`).join(", ")}.`;
  }

  if (Array.isArray(datos?.resultados) && datos.resultados.length > 0) {
    const partes: string[] = [];
    for (const r of datos.resultados.slice(0, 5)) {
      if (typeof r?.texto === "string") {
        const detalle = typeof r?.contenido_adicional === "string" && r.contenido_adicional.trim()
          ? `: ${r.contenido_adicional}`
          : "";
        partes.push(`${r.texto}${detalle}`);
      }
    }
    if (partes.length > 0) {
      return `Encontré información documentada en Aikukisna: ${partes.join(" | ")}.`;
    }
  }

  const nombre = perfil?.nombre_usuario || perfil?.nombre;
  if (datos?.respuesta) {
    return nombre ? `${datos.respuesta}` : datos.respuesta;
  }
  return ALCANCE_TUKI;
}

Deno.serve(async (req: Request) => {
  if (req.method !== "POST") {
    return jsonResponse({ error: "Método no permitido, usar POST" }, 405);
  }

  if (!GEMINI_API_KEY) {
    console.error("GEMINI_API_KEY no está configurada como secreto del proyecto");
    return jsonResponse({ error: "El servidor no tiene configurada la clave del proveedor" }, 500);
  }

  const authHeader = req.headers.get("Authorization");
  if (!authHeader) {
    return jsonResponse({ error: "Falta autenticación" }, 401);
  }

  const supabaseCliente = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
    global: { headers: { Authorization: authHeader } },
  });

  const token = authHeader.replace(/^Bearer\s+/i, "");
  const { data: userData, error: userError } = await supabaseCliente.auth.getUser(token);
  if (userError || !userData?.user) {
    console.error("Token sin usuario real asociado:", userError?.message);
    return jsonResponse({ error: "Necesitás iniciar sesión para usar esta función" }, 401);
  }

  let body: any;
  try {
    body = await req.json();
  } catch {
    return jsonResponse({ error: "El cuerpo de la petición debe ser JSON válido" }, 400);
  }

  let contents: Array<{ role?: string; parts: Array<Record<string, unknown>> }>;
  let esTuki = false;
  let respaldoTuki: any = null;
  let perfilTuki: any = null;
  let contextoInternoTuki = "";
  let respuestaLocalTuki = "";

  if (Array.isArray(body?.mensajes)) {
    esTuki = true;
    const mensajes = body.mensajes as unknown[];

    if (mensajes.length === 0) {
      return jsonResponse({ error: "'mensajes' no puede estar vacío" }, 400);
    }
    if (mensajes.length > MAXIMO_MENSAJES_HISTORIAL) {
      return jsonResponse(
        { error: `El historial supera el máximo de ${MAXIMO_MENSAJES_HISTORIAL} mensajes` },
        400,
      );
    }

    const rolesValidos = new Set(["user", "model"]);
    for (const m of mensajes) {
      const mensaje = m as { rol?: unknown; texto?: unknown };
      if (
        typeof mensaje?.rol !== "string" || !rolesValidos.has(mensaje.rol) ||
        typeof mensaje?.texto !== "string" || mensaje.texto.trim().length === 0
      ) {
        return jsonResponse(
          { error: "Cada mensaje necesita 'rol' (user|model) y 'texto' no vacío" },
          400,
        );
      }
      if (mensaje.texto.length > LARGO_MAXIMO_PROMPT) {
        return jsonResponse(
          { error: `Un mensaje supera el máximo de ${LARGO_MAXIMO_PROMPT} caracteres` },
          400,
        );
      }
    }

    const mensajesTipados = mensajes as Array<{ rol: string; texto: string }>;
    const ultimoUsuario = [...mensajesTipados].reverse().find((m) => m.rol === "user");

    const { data: perfil, error: errorPerfil } = await supabaseCliente.rpc(
      "obtener_contexto_tuki_usuario",
    );
    if (!errorPerfil && perfil) perfilTuki = perfil;

    if (ultimoUsuario) {
      let idiomaOrigenId =
        Number.isInteger(body?.idiomaOrigenId) ? Number(body.idiomaOrigenId) : null;
      let idiomaDestinoId =
        Number.isInteger(body?.idiomaDestinoId) ? Number(body.idiomaDestinoId) : null;

      if (!idiomaDestinoId) {
        idiomaDestinoId = idiomaMencionado(ultimoUsuario.texto)
          ?? (Number.isInteger(perfilTuki?.idioma_meta_id) ? Number(perfilTuki.idioma_meta_id) : null);
      }

      const textoTraducible = extraerTextoTraducible(ultimoUsuario.texto);
      if (!idiomaOrigenId && textoTraducible && /[áéíóúñ¿¡]/i.test(ultimoUsuario.texto)) {
        idiomaOrigenId = 2;
      }
      if (!idiomaOrigenId && esSolicitudTraduccion(ultimoUsuario.texto)) {
        idiomaOrigenId = 2;
      }

      if (
        textoTraducible &&
        idiomaOrigenId &&
        idiomaDestinoId &&
        idiomaOrigenId !== idiomaDestinoId
      ) {
        const { data: datosTraductor, error: errorTraductor } = await supabaseCliente.rpc(
          "traducir_texto_seguro",
          {
            p_texto: textoTraducible,
            p_idioma_origen: idiomaOrigenId,
            p_idioma_destino: idiomaDestinoId,
          },
        );

        if (!errorTraductor && datosTraductor) {
          respaldoTuki = datosTraductor;
          respuestaLocalTuki = formatearRespaldoTuki(datosTraductor, perfilTuki);
          contextoInternoTuki = recortarContexto({
            perfil: perfilTuki,
            consulta: ultimoUsuario.texto,
            texto_traducible: textoTraducible,
            datos: datosTraductor,
          });
        }
      }

      if (!respaldoTuki) {
        const { data: datosTuki, error: errorTuki } = await supabaseCliente.rpc(
          "responder_tuki",
          {
            p_pregunta: ultimoUsuario.texto,
            p_idioma_origen: idiomaOrigenId,
            p_idioma_destino: idiomaDestinoId,
          },
        );

        if (errorTuki) {
          console.error("No se pudo consultar la capa lingüística de Tuki:", errorTuki.message);
        } else if (datosTuki) {
          respaldoTuki = datosTuki;
          respuestaLocalTuki = formatearRespaldoTuki(datosTuki, perfilTuki);
          // "fuera_alcance" ya no corta la conversación: Tuki responde con su personalidad
          // y, cuando encaja, lleva el tema de vuelta al aprendizaje.
          if (datosTuki?.tipo !== "fuera_alcance") {
            contextoInternoTuki = recortarContexto({
              perfil: perfilTuki,
              datos: datosTuki,
            });
          }
        }
      }
    }

    contents = mensajesTipados.map((m) => ({
      role: m.rol,
      parts: [{ text: m.texto }],
    }));
  } else if (typeof body?.prompt === "string") {
    const prompt = body.prompt;
    if (prompt.trim().length === 0) {
      return jsonResponse({ error: "Falta el campo 'prompt' (texto no vacío)" }, 400);
    }
    if (prompt.length > LARGO_MAXIMO_PROMPT) {
      return jsonResponse(
        { error: `El prompt supera el máximo de ${LARGO_MAXIMO_PROMPT} caracteres` },
        400,
      );
    }

    const parts: Array<Record<string, unknown>> = [];

    if (typeof body?.imagenBase64 === "string" && body.imagenBase64.length > 0) {
      const mimeType = typeof body?.mimeType === "string" ? body.mimeType : "image/jpeg";
      if (!MIME_TYPES_IMAGEN_PERMITIDOS.has(mimeType)) {
        return jsonResponse({ error: `mimeType '${mimeType}' no soportado para imagen` }, 400);
      }
      if (body.imagenBase64.length > TAMANO_MAXIMO_ARCHIVO_BASE64) {
        return jsonResponse({ error: "La imagen es demasiado grande" }, 400);
      }
      parts.push({ inline_data: { mime_type: mimeType, data: body.imagenBase64 } });
    }

    if (typeof body?.audioBase64 === "string" && body.audioBase64.length > 0) {
      const audioMimeType =
        typeof body?.audioMimeType === "string" ? body.audioMimeType : "audio/aac";
      if (!MIME_TYPES_AUDIO_PERMITIDOS.has(audioMimeType)) {
        return jsonResponse({ error: `audioMimeType '${audioMimeType}' no soportado` }, 400);
      }
      if (body.audioBase64.length > TAMANO_MAXIMO_ARCHIVO_BASE64) {
        return jsonResponse({ error: "El audio es demasiado grande" }, 400);
      }
      parts.push({ inline_data: { mime_type: audioMimeType, data: body.audioBase64 } });
    }

    parts.push({ text: prompt });
    contents = [{ parts }];
  } else {
    return jsonResponse({ error: "Falta 'prompt' (texto) o 'mensajes' (lista)" }, 400);
  }

  let systemInstruction: { parts: Array<{ text: string }> } | undefined;

  if (esTuki) {
    const nombrePerfil =
      perfilTuki?.nombre_usuario || perfilTuki?.nombre || "estudiante";
    const idiomaMeta =
      perfilTuki?.idioma_meta || "el idioma meta configurado";

    const reglasTuki =
      "Eres Tuki, asistente educativo de Aikukisna. " +
      `El estudiante se identifica como ${nombrePerfil} y su idioma meta es ${idiomaMeta}. ` +
      "Tu ámbito principal es Miskito, Español, Inglés Kriol e Inglés Estándar, " +
      "incluyendo vocabulario, traducción, expresiones, pronunciación, gramática, cultura y aprendizaje. " +
      "Si la pregunta es de otro tema, responde con brevedad y, cuando encaje, conéctala con el aprendizaje. " +
      "Para Miskito y Kriol nunca inventes traducciones, pronunciaciones, variantes ni reglas. " +
      "Usa primero el contexto documental de Aikukisna. " +
      "Si una equivalencia exacta no está documentada, ofrece la información documentada disponible: acepciones, componentes, rutas entre idiomas, expresiones, reglas, ejemplos o una reformulación útil. " +
      "Distingue claramente una traducción directa de una ruta mediante otro idioma. " +
      "No presentes contenido pendiente de revisión como validado. " +
      "Cuando el contexto incluya una traducción documentada, responde con esa traducción de forma directa antes de explicar detalles. " +
      "Responde siempre en español salvo que el usuario pida explícitamente usar uno de los otros tres idiomas.";

    // La app manda la personalidad de Tuki, el progreso real del estudiante y los datos
    // verificados del diccionario local; antes este campo se ignoraba en modo Tuki.
    const contextoCliente = typeof body?.contexto === "string"
      ? body.contexto.trim().slice(0, LARGO_MAXIMO_CONTEXTO_TUKI)
      : "";
    const base = contextoCliente.length > 0
      ? `${contextoCliente}\n\nEl estudiante se identifica como ${nombrePerfil} y su idioma meta es ${idiomaMeta}.`
      : reglasTuki;

    const contexto =
      contextoInternoTuki.length > 0
        ? `${base}\n\nCONTEXTO DOCUMENTAL DE AIKUKISNA:\n${contextoInternoTuki}`
        : base;

    systemInstruction = { parts: [{ text: contexto }] };
  } else if (typeof body?.contexto === "string" && body.contexto.trim().length > 0) {
    if (body.contexto.length > LARGO_MAXIMO_CONTEXTO) {
      return jsonResponse(
        { error: `El contexto supera el máximo de ${LARGO_MAXIMO_CONTEXTO} caracteres` },
        400,
      );
    }
    systemInstruction = { parts: [{ text: body.contexto }] };
  }

  try {
    const geminiBody: Record<string, unknown> = { contents };
    if (systemInstruction) {
      geminiBody.system_instruction = systemInstruction;
    }

    const geminiResponse = await fetch(GEMINI_URL, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": GEMINI_API_KEY,
      },
      body: JSON.stringify(geminiBody),
    });

    if (!geminiResponse.ok) {
      const detalle = await geminiResponse.text();
      console.error("Error del proveedor:", geminiResponse.status, detalle);

      if (esTuki && (respuestaLocalTuki || respaldoTuki?.respuesta)) {
        return jsonResponse(
          {
            respuesta: respuestaLocalTuki || respaldoTuki.respuesta,
            datos: respaldoTuki,
            origen: "aikukisna_respaldo",
          },
          200,
        );
      }

      return jsonResponse({ error: "El proveedor externo no respondió correctamente" }, 502);
    }

    const data = await geminiResponse.json();
    const texto = data?.candidates?.[0]?.content?.parts?.[0]?.text;

    if (!texto) {
      console.error("Respuesta del proveedor sin texto");

      if (esTuki && (respuestaLocalTuki || respaldoTuki?.respuesta)) {
        return jsonResponse(
          {
            respuesta: respuestaLocalTuki || respaldoTuki.respuesta,
            datos: respaldoTuki,
            origen: "aikukisna_respaldo",
          },
          200,
        );
      }

      return jsonResponse({ error: "El proveedor no devolvió texto utilizable" }, 502);
    }

    return jsonResponse(
      {
        respuesta: texto,
        ...(esTuki
          ? {
              datos: respaldoTuki,
              respuesta_local: respuestaLocalTuki || null,
              perfil: perfilTuki,
              origen: "tuki_aikukisna",
            }
          : {}),
      },
      200,
    );
  } catch (e) {
    console.error("Error interno llamando al proveedor:", e);

    if (esTuki && (respuestaLocalTuki || respaldoTuki?.respuesta)) {
      return jsonResponse(
        {
          respuesta: respuestaLocalTuki || respaldoTuki.respuesta,
          datos: respaldoTuki,
          origen: "aikukisna_respaldo",
        },
        200,
      );
    }

    return jsonResponse({ error: "Error interno del servidor" }, 500);
  }
});
