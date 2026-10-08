# Diseño e implementación

La carpeta de destino estaba vacía. Se construye un monolito MVC Java 21 / Spring Boot 3, con vistas Thymeleaf y PostgreSQL. H2 persistente se reserva para demostraciones.

- Controladores: navegación, binding, validación y mensajes.
- DTO: formularios para evitar asignación de campos protegidos.
- Servicios transaccionales: catálogos, candidatos, contratación, empleados y reportes.
- Repositorios JPA y Specifications: persistencia, consultas combinables y paginación.
- Flyway: esquema reproducible con claves, restricciones e índices.
  Entidades: Competencia, Idioma, Capacitacion, Puesto, Candidato, CandidatoIdioma, ExperienciaLaboral, Empleado.
  Relaciones: puesto 1:N candidatos/empleados; candidato N:M competencias/capacitaciones; candidato 1:N idiomas con nivel y experiencias; candidato 1:0..1 empleado.
  Rutas: /, /competencias, /idiomas, /capacitaciones, /puestos, /candidatos, /consultas, /empleados, /reportes/nuevos-ingresos.
  Secuencia: infraestructura y modelo; servicios y validaciones; MVC e interfaz; semillas; pruebas de reglas y navegación; ejecución, empaquetado y documentación.
  La inactivación sustituye la eliminación de catálogos relacionados. Las experiencias se eliminan dentro de la edición del candidato. La contratación bloquea el candidato y actualiza candidato/empleado en una única transacción.
