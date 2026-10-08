import { createClient } from "npm:@supabase/supabase-js@2";

const CABECERAS = {
  "Content-Type": "application/json",
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};

const MENSAJE_INVALIDO = "Usuario/correo o contraseña incorrectos.";
const CORREO_NEUTRO = "cuenta-inexistente@invalid.aikukisna";
const LONGITUD_MAXIMA_IDENTIFICADOR = 80;
const LONGITUD_MAXIMA_CONTRASENA = 256;

function responder(cuerpo: Record<string, unknown>, estado: number): Response {
  return new Response(JSON.stringify(cuerpo), { status: estado, headers: CABECERAS });
}

function solicitudDeClienteValido(solicitud: Request, claveAnonima: string): boolean {
  const apiKey = solicitud.headers.get("apikey");
  const autorizacion = solicitud.headers.get("authorization");
  return apiKey === claveAnonima || autorizacion === `Bearer ${claveAnonima}`;
}

function escaparPatronLike(valor: string): string {
  return valor.replaceAll("\\", "\\\\").replaceAll("%", "\\%").replaceAll("_", "\\_");
}

Deno.serve(async (solicitud) => {
  if (solicitud.method === "OPTIONS") {
    return new Response("ok", { headers: CABECERAS });
  }
  if (solicitud.method !== "POST") {
    return responder({ error: MENSAJE_INVALIDO }, 401);
  }

  try {
    const url = Deno.env.get("SUPABASE_URL");
    const claveAnonima = Deno.env.get("SUPABASE_ANON_KEY");
    const claveServicio = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");
    if (!url || !claveAnonima || !claveServicio) {
      return responder({ error: "Servicio de autenticación no disponible." }, 503);
    }
    if (!solicitudDeClienteValido(solicitud, claveAnonima)) {
      return responder({ error: MENSAJE_INVALIDO }, 401);
    }

    const cuerpo = await solicitud.json();
    const identificador = typeof cuerpo.identificador === "string" ? cuerpo.identificador.trim() : "";
    const contrasena = typeof cuerpo.contrasena === "string" ? cuerpo.contrasena : "";
    if (
      !identificador ||
      !contrasena ||
      identificador.includes("@") ||
      identificador.length > LONGITUD_MAXIMA_IDENTIFICADOR ||
      contrasena.length > LONGITUD_MAXIMA_CONTRASENA
    ) {
      return responder({ error: MENSAJE_INVALIDO }, 401);
    }

    const administrador = createClient(url, claveServicio, {
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const { data: perfil, error: errorPerfil } = await administrador
      .from("usuario")
      .select("correo")
      .ilike("nombre_usuario", escaparPatronLike(identificador))
      .maybeSingle();
    if (errorPerfil) {
      return responder({ error: "Servicio de autenticación no disponible." }, 503);
    }

    // Incluso si no existe el username se ejecuta Auth con un correo neutro para
    // mantener la misma forma de respuesta y no confirmar la existencia de cuentas.
    const autenticacion = createClient(url, claveAnonima, {
      auth: { persistSession: false, autoRefreshToken: false },
    });
    const { data, error } = await autenticacion.auth.signInWithPassword({
      email: typeof perfil?.correo === "string" && perfil.correo ? perfil.correo : CORREO_NEUTRO,
      password: contrasena,
    });
    if (error || !data.session || !data.user) {
      return responder({ error: MENSAJE_INVALIDO }, 401);
    }

    return responder({
      access_token: data.session.access_token,
      refresh_token: data.session.refresh_token,
      expires_in: data.session.expires_in,
      token_type: data.session.token_type,
      usuario_id: data.user.id,
    }, 200);
  } catch {
    return responder({ error: MENSAJE_INVALIDO }, 401);
  }
});
