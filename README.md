# Sistema de Reclutamiento y Selección de RH

Proyecto final universitario · Universidad APEC. Aplicación MVC funcional para administrar catálogos, postulantes, experiencia laboral, selección, empleados y reportes. Nombre visual: **Talento RH**.

## Tecnologías

- Java 21; Spring Boot 3.5.11; Spring MVC.
- Spring Data JPA / Hibernate; Jakarta Bean Validation.
- Thymeleaf; Bootstrap 5.3.8; Bootstrap Icons 1.13.1; JavaScript vanilla.
- PostgreSQL como base principal; H2 persistente para el perfil de demostración.
- Flyway para migraciones SQL; Maven Wrapper 3.9.9.
- Spring Security para autenticación por formulario, sesión, cierre de sesión, CSRF y cabeceras HTTP.
- JUnit 5, MockMvc y AssertJ. Perfil adicional con PostgreSQL real temporal para las pruebas.

Los recursos del frontend están incluidos localmente: una vez descargadas las dependencias Java no necesitas un CDN para presentar la aplicación.

## Requisitos

- JDK **21** en `PATH` o `JAVA_HOME`.
- PostgreSQL 14 o superior para el modo principal (el archivo Compose usa PostgreSQL 17).
- Internet en la primera compilación para descargar Maven y dependencias. La demo incluida ya trae un JAR precompilado y no necesita descargar Maven.
- Maven no es obligatorio: se incluye `mvnw` / `mvnw.cmd`.

Comprueba `java -version`. No uses un JRE sin compilador.

## Configuración de PostgreSQL — modo principal

### Opción A: PostgreSQL instalado

Ejecuta como administrador de PostgreSQL, desde psql o pgAdmin:

```sql
CREATE USER rh_user WITH PASSWORD 'rh_local';
CREATE DATABASE rh_reclutamiento OWNER rh_user;
```

Si utilizas otro usuario, debe poder crear tablas en el esquema `public` de esta base. Flyway crea las tablas, restricciones, relaciones e índices al iniciar; Hibernate comprueba que el esquema coincida con las entidades. No es necesario ejecutar el SQL de migración manualmente.

### Opción B: Docker Compose

Con Docker Desktop **iniciado y operativo**:

```powershell
docker compose up -d --wait
```

El contenedor publica PostgreSQL en `127.0.0.1:5432`, con base `rh_reclutamiento`, usuario `rh_user` y contraseña local `rh_local`. Los datos persisten en el volumen `rh_postgres`. Para detenerlo sin borrar datos: `docker compose down`.

### Variables de entorno

| Variable       | Valor predeterminado                              | Uso                                             |
| -------------- | ------------------------------------------------- | ----------------------------------------------- |
| DB_URL         | jdbc:postgresql://localhost:5432/rh_reclutamiento | Conexión PostgreSQL                             |
| DB_USERNAME    | rh_user                                           | Usuario                                         |
| DB_PASSWORD    | rh_local                                          | Contraseña local                                |
| DEMO_SEED      | true                                              | Cargar ejemplos solamente si la base está vacía |
| PORT           | 8080                                              | Puerto HTTP                                     |
| SERVER_ADDRESS | 127.0.0.1                                         | Dirección de escucha local                      |

Ejemplo en PowerShell:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/rh_reclutamiento"
$env:DB_USERNAME = "rh_user"
$env:DB_PASSWORD = "rh_local"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/rh_reclutamiento
export DB_USERNAME=rh_user
export DB_PASSWORD=rh_local
chmod +x mvnw
./mvnw spring-boot:run
```

`.env.example` documenta las variables. Spring Boot **no carga automáticamente archivos .env**; usa variables de entorno. Docker Compose sí puede usar `DB_PASSWORD` desde un `.env` para inicializar su contenedor. Cambiar esa variable no cambia la contraseña de un volumen PostgreSQL ya inicializado.

## Demostración inmediata con H2

No requiere instalar PostgreSQL, Docker ni Maven. La entrega incluye `target/reclutamiento-1.0.0.jar` ya compilado.

La forma más sencilla es ejecutar `iniciar-demo.cmd` con doble clic. El script usa primero el JAR precompilado y solo recurre a Maven Wrapper si ese archivo no existe.

También puedes iniciarlo manualmente desde PowerShell:

```powershell
cd "C:\Users\Ian\Documents\Proyecto final"
java -jar .\target\reclutamiento-1.0.0.jar --spring.profiles.active=dev
```

Si quieres recompilar el proyecto desde el código fuente:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Linux/macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

H2 guarda los datos en `data/reclutamiento.mv.db`, por lo que sobreviven a los reinicios. No ejecutes dos instancias simultáneas contra ese archivo. El perfil `dev` usa H2; ejecutar sin perfil usa PostgreSQL.

## Acceso

Abre [http://localhost:8080](http://localhost:8080).

La aplicación requiere inicio de sesión. Para la demostración académica usa **usuario `admin123`** y **contraseña `admin123`**. Si las credenciales son incorrectas, el formulario muestra un mensaje de error en rojo. Los POST continúan protegidos contra CSRF y el sistema incluye cierre de sesión.

## Módulos

| Módulo         | Funcionalidades                                                                          |
| -------------- | ---------------------------------------------------------------------------------------- |
| Dashboard      | Seis indicadores reales, últimos candidatos y últimas contrataciones                     |
| Competencias   | Crear, listar, buscar, filtrar por estado, consultar, editar e inactivar/reactivar       |
| Idiomas        | CRUD lógico y uso real en candidatos con nivel básico/intermedio/avanzado/nativo         |
| Capacitaciones | CRUD lógico; nivel, institución, fechas; filtros por nombre, estado, nivel e institución |
| Puestos        | CRUD lógico; riesgo y rango salarial; filtros por nombre, estado y riesgo                |
| Candidatos     | Datos personales, postulación, relaciones múltiples, detalle y edición                   |
| Experiencias   | Varias filas por candidato, agregar/editar/quitar y periodo validado                     |
| Selección      | Estados de evaluación, confirmación y contratación transaccional                         |
| Empleados      | Creación desde candidato; búsqueda y filtros; detalle, edición laboral e inactivación    |
| Consulta       | Diez criterios combinables, paginación de 10 y tres órdenes de resultados                |
| Reportes       | Fechas inclusivas, conteo, total salarial, impresión y CSV UTF-8                         |

Todos los formularios guardan en la base. No hay botones de acción decorativos.

## Reglas y decisiones

- Cédula de **exactamente 11 dígitos numéricos, sin letras, espacios ni guiones**. La misma regla se aplica al alta/edición y a los filtros de búsqueda; tiene UNIQUE en candidatos y empleados. No se valida el dígito verificador, pues los ejemplos del proyecto son ficticios.
- El correo es obligatorio y debe tener estructura de correo completa (por ejemplo `nombre@correo.com`). El teléfono es opcional, pero si se informa acepta únicamente entre 7 y 15 dígitos numéricos, sin símbolos, espacios ni guiones.
- Cédula y teléfono tienen validación preventiva en el navegador y validación obligatoria nuevamente en el backend; por tanto, las reglas no dependen de JavaScript.
- Los nombres de catálogos se normalizan con espacios, mayúsculas y acentos para detectar duplicados. La clave normalizada también es UNIQUE en SQL.
- Los catálogos comparten `ACTIVO / INACTIVO`. La inactivación es la eliminación lógica y conserva el historial.
- Nuevas relaciones solo aceptan opciones activas, también en el backend. Una relación histórica inactiva puede conservarse al editar, pero no añadirse a otro candidato.
- Los candidatos activos son REGISTRADO, EN_EVALUACION y PRESELECCIONADO. RECHAZADO permite archivar lógicamente un perfil y reabrirlo después.
- Puede contratarse a un candidato en cualquiera de los tres estados activos. Un rechazado debe reabrirse. CONTRATADO solo se obtiene mediante contratación.
- La contratación usa una transacción y bloqueo pesimista del candidato. Las restricciones UNIQUE de empleado por candidato y cédula añaden protección ante concurrencia.
- Nombre y cédula se copian al empleado; el candidato contratado queda como historial de solo lectura. Los datos laborales se editan desde Empleados.
- No se ofrece alta manual de empleados. Inactivar un empleado no revierte su contratación histórica.
- Salarios en `BigDecimal` / `NUMERIC(14,2)`. Mínimos y aspiraciones >= 0; máximo de puesto y salario mensual > 0. No se impone que la oferta coincida con el rango orientativo del puesto.
- Una fecha final puede omitirse en capacitación o experiencia en curso; si existe debe ser >= fecha inicial. Se aceptan fechas futuras de ingreso para incorporaciones programadas.
- `@Version` evita sobrescrituras silenciosas desde formularios desactualizados.
- Datos de prueba, ejemplos y QA se identifican como demostración. `DEMO_SEED=false` evita nuevas semillas; no elimina registros existentes.
- El CSV escapa comillas y neutraliza fórmulas de hoja de cálculo. Incluye BOM UTF-8 y separador coma. En Excel con configuración regional que espere otro separador, usa **Datos → Desde texto/CSV** y selecciona coma.
- Recursos frontend compartidos para los cuatro catálogos, sin duplicar la misma vista cuatro veces.

## Arquitectura y archivos

```text
src/main/java/edu/unapec/rh/
  config/           configuración HTTP, idioma y semillas
  controller/       navegación MVC, binding y mensajes
  dto/              formularios validados y filtros
  entity/           entidades JPA y enums
  exception/        errores de negocio y recurso inexistente
  repository/       repositorios y Specifications
  service/          contratos de servicios
  service/impl/     lógica transaccional
  util/             normalización, validación y formato
src/main/resources/
  db/migration/     esquema versionado con Flyway
  templates/        dashboard, catálogos, candidatos, empleados, reportes, errores
  static/           CSS, JavaScript, icono y dependencias locales
src/test/           suite H2 aislada
src/postgres-test/  la misma suite sobre PostgreSQL real
docs/              modelo relacional, plan y entrega
```

Flujo: **Controller → Service → Repository → Database**. La lógica de negocio se concentra en servicios. Los DTO evitan asignar campos protegidos mediante parámetros de formularios. Open Session in View está deshabilitado; las relaciones requeridas se cargan dentro del servicio.

## Rutas principales

| Ruta                                                | Página                          |
| --------------------------------------------------- | ------------------------------- |
| /                                                   | Dashboard                       |
| /competencias, /idiomas, /capacitaciones, /puestos  | Listas de catálogos             |
| /{catalogo}/nuevo                                   | Alta                            |
| /{catalogo}/{id}, /{catalogo}/{id}/editar           | Detalle / edición               |
| /candidatos, /candidatos/nuevo                      | Lista / registro                |
| /candidatos/{id}, /candidatos/{id}/editar           | Perfil / edición                |
| /candidatos/{id}/contratar                          | Proceso de contratación         |
| /consultas                                          | Consulta avanzada de candidatos |
| /empleados, /empleados/{id}, /empleados/{id}/editar | Empleados                       |
| /reportes/nuevos-ingresos                           | Reporte e impresión             |
| /reportes/nuevos-ingresos/csv                       | Exportación CSV                 |

Las mutaciones usan POST y CSRF, incluyendo rutas `/{catalogo}/{id}/estado`, `/candidatos/{id}/estado` y `/empleados/{id}/estado`.

## Datos de demostración

En una base completamente vacía se crean 8 competencias, 6 idiomas, 5 puestos, 3 capacitaciones, 8 candidatos y 3 empleados. Cinco candidatos permanecen disponibles para selección. Los empleados de ejemplo se crean llamando al mismo servicio de contratación de la aplicación, no insertándolos por una vía alternativa.

Correos `example.com`, cédulas comenzando por 000 y empresas de demostración son ficticios. Las fechas de los empleados se calculan respecto al día de inicio, para facilitar los reportes. La semilla es atómica e idempotente y no vuelve a poblar bases con información.

## Pruebas y compilación

Desde la raíz:

```powershell
.\mvnw.cmd test
.\mvnw.cmd clean package
```

Con Maven instalado, los equivalentes son `mvn test` y `mvn clean package`.

Validación opcional con **PostgreSQL real temporal**, sin Docker ni tocar tu base local:

```powershell
.\mvnw.cmd -Ppostgres-test test
```

El perfil descarga binarios de PostgreSQL mediante Zonky Embedded Postgres (dependencia solo de test), inicia una instancia aislada y la detiene al finalizar. El perfil normal de pruebas usa H2 en memoria. Nunca apuntes estas pruebas a una base con datos importantes: la suite limpia exclusivamente sus tablas de prueba antes de cada caso.

La suite verifica duplicados de cédula en aplicación y SQL, rangos de fechas y salarios, relaciones, edición/eliminación de experiencias, catálogos, asociaciones inactivas, concurrencia, rollback, estados, filtros, paginación, reportes inclusivos, CSV, CSRF, vistas MVC, conservación de formularios y 404.

Un error SQL de CHECK aparece **deliberadamente** en el caso que comprueba rollback; la prueba solo pasa si no queda contratación parcial.

## Ejecutar el JAR

```powershell
java -jar target\reclutamiento-1.0.0.jar
# Demostración H2:
java -jar target\reclutamiento-1.0.0.jar --spring.profiles.active=dev
```

La aplicación lee los mismos parámetros de entorno. Detén con Ctrl+C.

## Guion para presentar

1. Abrir Dashboard y explicar que los indicadores provienen de la base.
2. Crear una competencia, editarla, inactivarla y reactivarla.
3. Crear un candidato con puesto, competencias, idioma, capacitación y dos experiencias.
4. Editar una experiencia y quitar otra; guardar y mostrar el perfil.
5. Filtrar por puesto + competencia + idioma + salario máximo.
6. Cambiar estado a PRESELECCIONADO; contratar con datos definitivos.
7. Mostrar el empleado y el perfil original en CONTRATADO.
8. Filtrar Nuevos ingresos, imprimir o descargar CSV.
9. Mostrar las pruebas y [el diagrama relacional](docs/database.md).

## Solución de problemas

- **No se conecta a PostgreSQL:** comprueba el servicio, puerto, base, variables y permisos. Docker instalado no significa que el motor esté iniciado.
- **Puerto 8080 ocupado:** detén la otra instancia o usa `$env:PORT="8081"`.
- **Base H2 en uso:** cierra la otra instancia. No borres su archivo mientras esté abierta.
- **JAVA_HOME incorrecto:** debe apuntar a la carpeta del JDK 21, no a `bin`.
- **Cambios de SQL:** no edites una migración que ya se aplicó; crea una nueva `V2__...` para conservar el historial.
- **Sin datos iniciales:** confirma DEMO_SEED=true y que la base estuviera vacía. El sistema no reemplaza datos existentes.
- **403 al enviar un POST manual:** los formularios incluyen automáticamente el token CSRF. Usa la interfaz o proporciona un token de sesión válido.
- **Pruebas PostgreSQL en Windows:** puede ser necesario el runtime Visual C++ indicado por Zonky; las pruebas se ejecutaron correctamente en este equipo.

## Documentación adicional

- [Modelo relacional y restricciones](docs/database.md).
- [Reporte de entrega y checklist](docs/entrega.md).
- [Plan de implementación](docs/plan.md).
- [Requisitos de Spring Boot 3.5](https://docs.spring.io/spring-boot/3.5/system-requirements.html).
- [PostgreSQL temporal para pruebas: documentación de Zonky](https://github.com/zonkyio/embedded-postgres).
