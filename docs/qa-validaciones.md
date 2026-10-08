# QA de validaciones de entrada

## Alcance

Se revisaron los formularios y filtros de **Candidatos**, **Empleados**, **Contratación**, **Catálogos**, **Experiencia laboral** y **Reportes**, con especial atención a datos personales y búsquedas.

## Correcciones aplicadas

| Caso | Regla aplicada | Frontend | Backend |
| --- | --- | --- | --- |
| Cédula de candidato | Obligatoria, 11 dígitos; máscara visual `000-0000000-0`; se envía y almacena sin guiones | Sí | Sí |
| Búsqueda de candidato por cédula | Máscara visual `000-0000000-0`; antes de buscar se normaliza a máximo 11 dígitos | Sí | Sí |
| Búsqueda de empleado por cédula | Máscara visual `000-0000000-0`; antes de buscar se normaliza a máximo 11 dígitos | Sí | Sí |
| Correo electrónico | Obligatorio, máximo 180 caracteres y formato `usuario@dominio.ext` | Sí | Sí |
| Teléfono | Opcional; si existe, únicamente 7–15 dígitos | Sí | Sí |
| Salario aspirado | Obligatorio, numérico, no negativo y máximo 2 decimales | Ya existía | Ya existía |
| Salario mensual | Obligatorio, mayor que cero y máximo 2 decimales | Ya existía | Ya existía |
| Rangos salariales | Máximo no menor que mínimo | Ya existía | Ya existía |
| Fechas de experiencia/capacitación | Fecha final no anterior a inicial | Ya existía | Ya existía |
| Campos obligatorios y longitudes | Nombre, apellidos, departamento, puesto y demás DTO | Ya existía | Ya existía |
| Filtro de empleados | Ahora ejecuta Bean Validation antes de consultar | N/A | Corregido |
| Eliminación de candidatos | Disponible con confirmación para candidatos no contratados; protege los vinculados a empleados | Sí | Sí |

## Comportamiento esperado

- La **cédula** se enmascara automáticamente como `000-0000000-0`; al escribir o pegar letras/símbolos, se conservan únicamente los dígitos válidos.
- En **teléfono**, el navegador elimina letras, símbolos, espacios y guiones.
- Aunque JavaScript se desactive o se envíe una petición manual, el backend rechaza valores inválidos.
- La cédula se almacena como 11 dígitos y se muestra con guiones tanto en alta/edición como en búsquedas y vistas.
- Los candidatos no contratados pueden eliminarse desde el listado o el perfil, siempre con confirmación. Los candidatos ya contratados no se eliminan para no romper el historial del empleado asociado.
- El correo `correo-sin-arroba` o `ana@dominio` es inválido; `ana@dominio.com` es válido.
- El teléfono `809-555-0000` es inválido; `8095550000` es válido.
- Los filtros de cédula con letras muestran error y no ejecutan la consulta.

## Pruebas automatizadas añadidas

Se agregaron casos para:

1. cédula duplicada numérica;
2. rechazo backend de letras, guiones y longitud incorrecta; la máscara visual se normaliza en frontend antes del envío;
3. rechazo de correo sin formato completo;
4. rechazo de teléfono con guiones y aceptación de teléfono numérico;
5. rechazo MVC de cédula inválida al registrar;
6. rechazo MVC de cédula inválida al buscar candidatos;
7. rechazo MVC de cédula inválida al buscar empleados;
8. rechazo MVC de teléfono inválido;
9. presencia de la máscara de cédula en alta y búsquedas;
10. eliminación de candidato no contratado y limpieza de sus relaciones;
11. protección contra eliminación de un candidato ya contratado.

La suite incluye estos casos adicionales de regresión; el conteo exacto se obtiene al ejecutar `mvnw.cmd test`.

## Nota de ejecución en este entorno

La validación estática de los archivos Java se realizó con `google-java-format` (que también analiza la sintaxis) y `app.js` pasó `node --check`. No fue posible ejecutar Maven en este sandbox porque el Maven Wrapper necesita descargar Maven 3.9.9 y el acceso de descarga está bloqueado. En un equipo con Internet, ejecutar:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Después del `clean package`, usar el JAR recién generado en `target/`.
