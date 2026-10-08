# Corpus controlado de Cámara

## Estado

El corpus contextual contiene 30 conceptos visuales revisados por acepción:

- 29 conceptos tienen selección aprobada para Miskitu, Español, Inglés Kriol e
  Inglés Estándar.
- `cangrejo` tiene selección aprobada para Miskitu, Español e Inglés Estándar.
- No existe selección Kriol genérica para `cangrejo`: `rahti` queda excluida
  porque la evidencia disponible la describe como una especie de cangrejo.

La selección se guarda localmente en
`seleccion_traduccion_camara_cache`. Cada fila conserva los IDs de la palabra
española, palabra destino, relación, acepción de origen y acepción de destino.
No se alteran los estados de las relaciones originales de la réplica.

## Conceptos cubiertos

1. mesa
2. libro
3. lápiz
4. cuaderno
5. árbol
6. bebé
7. pájaro
8. pez
9. tigre
10. puerta
11. agua
12. arroz
13. barco
14. cabeza
15. carne
16. casa
17. cuchillo
18. dedo
19. mango — únicamente la acepción fruto
20. mano
21. mono — únicamente la acepción animal
22. mujer
23. niña
24. niño
25. ropa
26. tortuga
27. papaya
28. palmera
29. cangrejo — Kriol genérico no aprobado
30. canoa

## Comportamiento

1. La etiqueta inglesa o el concepto español detectado se normaliza.
2. Cámara consulta la selección contextual en Room.
3. La palabra española y la palabra destino se recuperan por sus IDs desde Room.
4. Si el concepto está controlado pero no tiene destino aprobado, no se usa una
   traducción alternativa ambigua.
5. Los conceptos fuera de este corpus mantienen el flujo conservador general.

## Conteos esperados

- 30 identidades en Español.
- 30 selecciones Miskitu.
- 29 selecciones Kriol.
- 30 selecciones de Inglés Estándar.
- Total: 119 filas.
- `cangrejo + Kriol`: 0 filas.

## Prueba física pendiente

La compilación, las pruebas unitarias y el ensamblado de las pruebas
instrumentadas finalizaron correctamente. La ejecución física de la migración
11→12 y del sembrado queda pendiente hasta que ADB vuelva a mostrar un
dispositivo conectado.

