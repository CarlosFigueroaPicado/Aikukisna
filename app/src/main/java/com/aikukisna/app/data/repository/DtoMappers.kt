package com.aikukisna.app.data.repository

import com.aikukisna.app.data.remote.dto.FuenteDocumentoDto
import com.aikukisna.app.data.remote.dto.LeccionDto
import com.aikukisna.app.data.remote.dto.PalabraDto
import com.aikukisna.app.data.remote.dto.TraduccionDto
import com.aikukisna.app.domain.model.Categoria
import com.aikukisna.app.domain.model.FuenteDocumento
import com.aikukisna.app.domain.model.Idioma
import com.aikukisna.app.domain.model.Leccion
import com.aikukisna.app.domain.model.Palabra
import com.aikukisna.app.domain.model.Traduccion

internal fun FuenteDocumentoDto.toDomain() = FuenteDocumento(
    id = id,
    titulo = titulo,
    autor = autor,
    anio = anio,
    institucion = institucion
)

internal fun PalabraDto.toDomain() = Palabra(
    id = id,
    idioma = Idioma(idioma.id, idioma.codigo, idioma.nombre),
    texto = texto,
    categoria = categoria?.let { Categoria(it.id, it.nombre) },
    fuente = fuente.toDomain()
)

internal fun TraduccionDto.toDomain() = Traduccion(
    id = id,
    palabraOrigen = palabraOrigen.toDomain(),
    palabraDestino = palabraDestino.toDomain(),
    nota = nota
)

internal fun LeccionDto.toDomain() = Leccion(
    id = id,
    titulo = titulo,
    capituloNumero = capituloNumero,
    nivel = nivel,
    categoria = categoria?.let { Categoria(it.id, it.nombre) }
)