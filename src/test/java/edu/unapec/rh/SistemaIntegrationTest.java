package edu.unapec.rh;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.exception.*;
import edu.unapec.rh.repository.*;
import edu.unapec.rh.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import edu.unapec.rh.util.Validacion;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin123", roles = "ADMIN")
class SistemaIntegrationTest {
  @Autowired CatalogoService catalogos;
  @Autowired CandidatoService candidatos;
  @Autowired ContratacionService contratacion;
  @Autowired EmpleadoService empleados;
  @Autowired CandidatoRepository candidatoRepo;
  @Autowired EmpleadoRepository empleadoRepo;
  @Autowired JdbcTemplate jdbc;
  @Autowired MockMvc mvc;
  Long puestoId, competenciaId, idiomaId, capacitacionId;

  @BeforeEach
  void preparar() {
    // Solo se ejecuta contra la base aislada del perfil test.
    for (String tabla :
        List.of(
            "empleado",
            "experiencia_laboral",
            "candidato_idioma",
            "candidato_competencia",
            "candidato_capacitacion",
            "candidato",
            "competencia",
            "idioma",
            "capacitacion",
            "puesto")) jdbc.update("DELETE FROM " + tabla);
    CatalogoForm puesto = cat("Desarrollador");
    puesto.setRiesgo(Riesgo.BAJO);
    puesto.setSalarioMinimo(new BigDecimal("30000"));
    puesto.setSalarioMaximo(new BigDecimal("150000"));
    puestoId = catalogos.guardar(TipoCatalogo.PUESTOS, null, puesto).getId();
    competenciaId = catalogos.guardar(TipoCatalogo.COMPETENCIAS, null, cat("Java")).getId();
    idiomaId = catalogos.guardar(TipoCatalogo.IDIOMAS, null, cat("Inglés")).getId();
    CatalogoForm cap = cat("Ingeniería");
    cap.setNivel(Nivel.GRADO);
    cap.setInstitucion("UNAPEC");
    cap.setFechaDesde(LocalDate.of(2020, 1, 1));
    cap.setFechaHasta(LocalDate.of(2024, 1, 1));
    capacitacionId = catalogos.guardar(TipoCatalogo.CAPACITACIONES, null, cap).getId();
  }

  CatalogoForm cat(String nombre) {
    CatalogoForm f = new CatalogoForm();
    f.setNombre(nombre);
    return f;
  }

  CandidatoForm candidato(String cedula) {
    CandidatoForm f = new CandidatoForm();
    f.setCedula(cedula);
    f.setNombre("Ana");
    f.setApellidos("Prueba");
    f.setEmail("ana@example.com");
    f.setPuestoId(puestoId);
    f.setDepartamento("Tecnología");
    f.setSalarioAspirado(new BigDecimal("80000"));
    f.setCompetenciaIds(List.of(competenciaId));
    f.setCapacitacionIds(List.of(capacitacionId));
    IdiomaSeleccion i = new IdiomaSeleccion();
    i.setIdiomaId(idiomaId);
    i.setSeleccionado(true);
    i.setNivel(NivelIdioma.AVANZADO);
    f.getIdiomas().add(i);
    ExperienciaForm e = new ExperienciaForm();
    e.setEmpresa("Empresa demo");
    e.setPuestoOcupado("Programadora");
    e.setFechaDesde(LocalDate.of(2021, 1, 1));
    e.setFechaHasta(LocalDate.of(2023, 12, 31));
    e.setSalario(new BigDecimal("45000"));
    f.getExperiencias().add(e);
    return f;
  }

  String cedulaValida(int consecutivo) {
    String base = String.format("%010d", consecutivo);
    for (int digito = 0; digito <= 9; digito++) {
      String candidata = base + digito;
      if (Validacion.validaCedula(candidata)) return candidata;
    }
    throw new IllegalStateException("No fue posible generar una cédula de prueba válida.");
  }

  Candidato crear() {
    return candidatos.guardar(null, candidato("00000000018"));
  }

  ContratacionForm contrato(LocalDate fecha) {
    ContratacionForm f = new ContratacionForm();
    f.setPuestoId(puestoId);
    f.setDepartamento("Tecnología");
    f.setFechaIngreso(fecha);
    f.setSalarioMensual(new BigDecimal("85000"));
    return f;
  }

  ReporteFiltro rango(String desde, String hasta) {
    ReporteFiltro f = new ReporteFiltro();
    f.setDesde(LocalDate.parse(desde));
    f.setHasta(LocalDate.parse(hasta));
    return f;
  }

  @Test
  void cedulaDuplicadaSeRechaza() {
    crear();
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("00000000018")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cédula");
    assertThat(candidatoRepo.count()).isEqualTo(1);
  }

  @Test
  void cedulaSoloAceptaOnceDigitosEnBackend() {
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("00123ABC456")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cédula");
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("000-0000000-1")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cédula");
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("12345")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cédula");
  }

  @Test
  void cedulaConChecksumInvalidoSeRechaza() {
    assertThat(Validacion.validaCedula("00000000018")).isTrue();
    assertThat(Validacion.validaCedula("00000000001")).isFalse();
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("00000000001")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("cédula");
  }

  @Test
  void correoDebeTenerFormatoValido() {
    CandidatoForm f = candidato("00000000018");
    f.setEmail("correo-sin-arroba");
    assertThatThrownBy(() -> candidatos.guardar(null, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("correo");

    f.setEmail("ana@dominio");
    assertThatThrownBy(() -> candidatos.guardar(null, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("correo");
  }

  @Test
  void telefonoSoloAceptaNumeros() {
    CandidatoForm f = candidato("00000000018");
    f.setTelefono("809-555-0000");
    assertThatThrownBy(() -> candidatos.guardar(null, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("teléfono");

    f.setTelefono("8095550000");
    Candidato c = candidatos.guardar(null, f);
    assertThat(c.getTelefono()).isEqualTo("8095550000");
  }

  @Test
  void cedulaUniqueEnBaseDeDatos() {
    Candidato c = crear();
    assertThatThrownBy(
            () ->
                jdbc.update(
                    "INSERT INTO candidato"
                        + " (cedula,nombre,apellidos,email,puesto_id,departamento,salario_aspirado,fecha_registro,estado)"
                        + " SELECT"
                        + " cedula,nombre,apellidos,email,puesto_id,departamento,salario_aspirado,fecha_registro,estado"
                        + " FROM candidato WHERE id=?",
                    c.getId()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void contratacionCreaEmpleadoYCambiaEstado() {
    Candidato c = crear();
    Empleado e = contratacion.contratar(c.getId(), contrato(LocalDate.now()));
    assertThat(e.getCedula()).isEqualTo(c.getCedula());
    assertThat(e.getNombre()).isEqualTo(c.getNombreCompleto());
    assertThat(e.getEstado()).isEqualTo(Estado.ACTIVO);
    assertThat(candidatos.obtener(c.getId()).getEstado()).isEqualTo(EstadoCandidato.CONTRATADO);
    assertThat(empleadoRepo.count()).isEqualTo(1);
  }

  @Test
  void candidatoNoContratadoSePuedeEliminar() throws Exception {
    Candidato c = crear();
    Long id = c.getId();

    mvc.perform(
            post("/candidatos/" + id + "/eliminar")
                .with(csrf())
                .param("version", String.valueOf(c.getVersion())))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/candidatos"));

    assertThat(candidatoRepo.existsById(id)).isFalse();
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM experiencia_laboral WHERE candidato_id=?", Integer.class, id))
        .isZero();
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM candidato_idioma WHERE candidato_id=?", Integer.class, id))
        .isZero();
  }

  @Test
  void candidatoContratadoNoSePuedeEliminar() {
    Candidato c = crear();
    contratacion.contratar(c.getId(), contrato(LocalDate.now()));
    Candidato contratado = candidatos.obtener(c.getId());

    assertThatThrownBy(() -> candidatos.eliminar(contratado.getId(), contratado.getVersion()))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("vinculado a un empleado");
    assertThat(candidatoRepo.existsById(c.getId())).isTrue();
    assertThat(empleadoRepo.existsByCandidatoId(c.getId())).isTrue();
  }

  @Test
  void noSePuedeContratarDosVeces() {
    Candidato c = crear();
    contratacion.contratar(c.getId(), contrato(LocalDate.now()));
    assertThatThrownBy(() -> contratacion.contratar(c.getId(), contrato(LocalDate.now())))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("ya fue contratado");
    assertThat(empleadoRepo.count()).isEqualTo(1);
  }

  @Test
  void contratacionConcurrenteSoloCreaUnEmpleado() throws Exception {
    Long id = crear().getId();
    CountDownLatch start = new CountDownLatch(1);
    try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> job =
          () -> {
            start.await();
            try {
              contratacion.contratar(id, contrato(LocalDate.now()));
              return true;
            } catch (BusinessException e) {
              return false;
            }
          };
      Future<Boolean> a = pool.submit(job), b = pool.submit(job);
      start.countDown();
      assertThat(List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS)))
          .containsExactlyInAnyOrder(true, false);
    }
    assertThat(empleadoRepo.count()).isEqualTo(1);
    assertThat(candidatos.obtener(id).getEstado()).isEqualTo(EstadoCandidato.CONTRATADO);
  }

  @Test
  void falloAlInsertarEmpleadoRevierteEstado() {
    Candidato c = crear();
    ContratacionForm f = contrato(LocalDate.now());
    // Una restricción real falla después de mutar el candidato: ambas escrituras deben revertirse.
    jdbc.execute("ALTER TABLE empleado ADD CONSTRAINT test_salario CHECK(salario_mensual < 1)");
    try {
      assertThatThrownBy(() -> contratacion.contratar(c.getId(), f))
          .isInstanceOf(DataIntegrityViolationException.class);
    } finally {
      jdbc.execute("ALTER TABLE empleado DROP CONSTRAINT test_salario");
    }
    assertThat(empleadoRepo.count()).isZero();
    assertThat(candidatos.obtener(c.getId()).getEstado()).isEqualTo(EstadoCandidato.REGISTRADO);
  }

  @Test
  void noSePuedeForzarEstadoContratado() {
    Candidato c = crear();
    assertThatThrownBy(
            () -> candidatos.cambiarEstado(c.getId(), EstadoCandidato.CONTRATADO, c.getVersion()))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void candidatoRechazadoNoSeContrata() {
    Candidato c = crear();
    candidatos.cambiarEstado(c.getId(), EstadoCandidato.RECHAZADO, c.getVersion());
    assertThatThrownBy(() -> contratacion.contratar(c.getId(), contrato(LocalDate.now())))
        .isInstanceOf(BusinessException.class);
    assertThat(empleadoRepo.count()).isZero();
  }

  @Test
  void salarioNegativoInvalido() {
    CandidatoForm f = candidato("00000000018");
    f.setSalarioAspirado(new BigDecimal("-1"));
    assertThatThrownBy(() -> candidatos.guardar(null, f)).isInstanceOf(BusinessException.class);
  }

  @Test
  void salarioCeroContratacionInvalido() {
    Candidato c = crear();
    ContratacionForm f = contrato(LocalDate.now());
    f.setSalarioMensual(BigDecimal.ZERO);
    assertThatThrownBy(() -> contratacion.contratar(c.getId(), f))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void fechaIngresoObligatoria() {
    Candidato c = crear();
    assertThatThrownBy(() -> contratacion.contratar(c.getId(), contrato(null)))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void capacitacionNoAceptaFechasInvertidas() {
    CatalogoForm f = catalogos.formulario(TipoCatalogo.CAPACITACIONES, capacitacionId);
    f.setFechaHasta(LocalDate.of(2019, 1, 1));
    assertThatThrownBy(() -> catalogos.guardar(TipoCatalogo.CAPACITACIONES, capacitacionId, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("fecha final");
  }

  @Test
  void experienciaNoAceptaFechasInvertidas() {
    CandidatoForm f = candidato("00000000018");
    f.getExperiencias().getFirst().setFechaHasta(LocalDate.of(2019, 1, 1));
    assertThatThrownBy(() -> candidatos.guardar(null, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("fecha final");
  }

  @Test
  void puestoNoAceptaMaximoMenorAlMinimo() {
    CatalogoForm f = catalogos.formulario(TipoCatalogo.PUESTOS, puestoId);
    f.setSalarioMaximo(new BigDecimal("20000"));
    assertThatThrownBy(() -> catalogos.guardar(TipoCatalogo.PUESTOS, puestoId, f))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("salario máximo");
  }

  @Test
  void catalogoEvitaDuplicadosConEspaciosMayusculasYAcentos() {
    assertThatThrownBy(() -> catalogos.guardar(TipoCatalogo.IDIOMAS, null, cat("  INGLES  ")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void catalogoEditaInactivaYReactiva() {
    CatalogoForm f = catalogos.formulario(TipoCatalogo.COMPETENCIAS, competenciaId);
    assertThat(f.getVersion()).isNotNull();
    f.setNombre("Java 21");
    CatalogoBase c = catalogos.guardar(TipoCatalogo.COMPETENCIAS, competenciaId, f);
    catalogos.cambiarEstado(TipoCatalogo.COMPETENCIAS, c.getId(), c.getVersion());
    c = catalogos.obtener(TipoCatalogo.COMPETENCIAS, c.getId());
    assertThat(c.getEstado()).isEqualTo(Estado.INACTIVO);
    catalogos.cambiarEstado(TipoCatalogo.COMPETENCIAS, c.getId(), c.getVersion());
    assertThat(catalogos.obtener(TipoCatalogo.COMPETENCIAS, c.getId()).getEstado())
        .isEqualTo(Estado.ACTIVO);
  }

  @Test
  void opcionesInactivasNoSeAgreganEnRegistro() {
    catalogos.cambiarEstado(TipoCatalogo.COMPETENCIAS, competenciaId, 0);
    assertThatThrownBy(() -> candidatos.guardar(null, candidato("00000000018")))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("activas");
  }

  @Test
  void puestoInactivoNoSeAsigna() {
    catalogos.cambiarEstado(TipoCatalogo.PUESTOS, puestoId, 0);
    assertThatThrownBy(() -> crear())
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("activo");
  }

  @Test
  void idiomaInactivoNoSeAsigna() {
    catalogos.cambiarEstado(TipoCatalogo.IDIOMAS, idiomaId, 0);
    assertThatThrownBy(() -> crear())
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("activos");
  }

  @Test
  void capacitacionInactivaNoSeAsigna() {
    catalogos.cambiarEstado(TipoCatalogo.CAPACITACIONES, capacitacionId, 0);
    assertThatThrownBy(() -> crear())
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("activas");
  }

  @Test
  void edicionConservaRelacionesInactivasHistoricas() {
    Candidato c = crear();
    catalogos.cambiarEstado(TipoCatalogo.COMPETENCIAS, competenciaId, 0);
    CandidatoForm f = candidatos.formulario(c.getId());
    f.setNombre("Ana editada");
    candidatos.guardar(c.getId(), f);
    assertThat(candidatos.obtener(c.getId()).getCompetencias()).hasSize(1);
  }

  @Test
  void experienciaYRelacionesPersistenSeEditanYSeEliminan() {
    Candidato c = crear();
    Candidato perfil = candidatos.obtener(c.getId());
    assertThat(perfil.getCompetencias()).hasSize(1);
    assertThat(perfil.getCapacitaciones()).hasSize(1);
    assertThat(perfil.getIdiomas()).hasSize(1);
    assertThat(perfil.getExperiencias()).hasSize(1);
    CandidatoForm f = candidatos.formulario(c.getId());
    f.getIdiomas().getFirst().setNivel(NivelIdioma.NATIVO);
    f.getExperiencias().getFirst().setEmpresa("Empresa editada");
    candidatos.guardar(c.getId(), f);
    perfil = candidatos.obtener(c.getId());
    assertThat(perfil.getIdiomas().getFirst().getNivel()).isEqualTo(NivelIdioma.NATIVO);
    assertThat(perfil.getExperiencias().getFirst().getEmpresa()).isEqualTo("Empresa editada");
    f = candidatos.formulario(c.getId());
    f.setIdiomas(new ArrayList<>());
    f.setExperiencias(new ArrayList<>());
    f.setCompetenciaIds(List.of());
    f.setCapacitacionIds(List.of());
    candidatos.guardar(c.getId(), f);
    assertThat(candidatos.obtener(c.getId()).getExperiencias()).isEmpty();
    assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM candidato_idioma", Long.class)).isZero();
  }

  @Test
  void consultaCombinaTodosLosCriterios() {
    Candidato c = crear();
    CandidatoFiltro f = new CandidatoFiltro();
    f.setNombre("Ana Prueba");
    f.setCedula("00000000018");
    f.setPuestoId(puestoId);
    f.setDepartamento("Tecnología");
    f.setEstado(EstadoCandidato.REGISTRADO);
    f.setCompetenciaId(competenciaId);
    f.setCapacitacionId(capacitacionId);
    f.setIdiomaId(idiomaId);
    f.setSalarioMin(new BigDecimal("75000"));
    f.setSalarioMax(new BigDecimal("100000"));
    assertThat(candidatos.buscar(f, 0, "nombre").getContent())
        .extracting(BaseEntity::getId)
        .containsExactly(c.getId());
    f.setSalarioMax(new BigDecimal("79000"));
    assertThat(candidatos.buscar(f, 0, "nombre").getTotalElements()).isZero();
  }

  @Test
  void paginacionYOrdenamiento() {
    for (int i = 1; i <= 12; i++) candidatos.guardar(null, candidato(cedulaValida(i)));
    assertThat(candidatos.buscar(new CandidatoFiltro(), 0, "recientes").getContent()).hasSize(10);
    assertThat(candidatos.buscar(new CandidatoFiltro(), 1, "recientes").getContent()).hasSize(2);
  }

  @Test
  void versionObsoletaNoSobrescribe() {
    Candidato c = crear();
    CandidatoForm a = candidatos.formulario(c.getId()), b = candidatos.formulario(c.getId());
    a.setNombre("Nuevo nombre");
    candidatos.guardar(c.getId(), a);
    assertThatThrownBy(() -> candidatos.guardar(c.getId(), b))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Otro usuario");
  }

  @Test
  void reporteIncluyeLimitesYExcluyeFueraDelRango() {
    LocalDate[] fechas = {
      LocalDate.of(2026, 1, 1),
      LocalDate.of(2026, 1, 31),
      LocalDate.of(2025, 12, 31),
      LocalDate.of(2026, 2, 1)
    };
    for (int i = 0; i < 4; i++)
      contratacion.contratar(
          candidatos.guardar(null, candidato(cedulaValida(i + 1))).getId(),
          contrato(fechas[i]));
    var lista = empleados.reporte(rango("2026-01-01", "2026-01-31"));
    assertThat(lista).hasSize(2);
    assertThat(
            lista.stream()
                .map(Empleado::getSalarioMensual)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
        .isEqualByComparingTo("170000");
  }

  @Test
  void reporteRechazaRangoInvertido() {
    assertThatThrownBy(() -> empleados.reporte(rango("2026-02-01", "2026-01-01")))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void empleadoEditaFiltraEInactivaSinRomperVinculo() {
    Empleado e = contratacion.contratar(crear().getId(), contrato(LocalDate.now()));
    EmpleadoForm f = empleados.formulario(e.getId());
    f.setDepartamento("Calidad");
    f.setSalarioMensual(new BigDecimal("90000"));
    empleados.actualizar(e.getId(), f);
    EmpleadoFiltro filtro = new EmpleadoFiltro();
    filtro.setDepartamento("Calidad");
    filtro.setCedula("00000000018");
    filtro.setNombre("Ana");
    filtro.setPuestoId(puestoId);
    filtro.setEstado(Estado.ACTIVO);
    assertThat(empleados.buscar(filtro, 0).getTotalElements()).isEqualTo(1);
    e = empleados.obtener(e.getId());
    empleados.cambiarEstado(e.getId(), e.getVersion());
    assertThat(empleados.obtener(e.getId()).getEstado()).isEqualTo(Estado.INACTIVO);
    assertThat(candidatos.obtener(e.getCandidato().getId()).getEstado())
        .isEqualTo(EstadoCandidato.CONTRATADO);
  }

  @Test
  void csvEscapaComillasYNeutralizaFormulas() {
    ContratacionForm f = contrato(LocalDate.of(2026, 1, 1));
    f.setDepartamento("=SUM(1,2) \"demo\"");
    contratacion.contratar(crear().getId(), f);
    String csv = empleados.csv(rango("2026-01-01", "2026-01-01"));
    assertThat(csv)
        .startsWith("\uFEFFID,Cédula")
        .contains("\"'=SUM(1,2) \"\"demo\"\"\"")
        .contains("85000.00");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/",
        "/competencias",
        "/idiomas",
        "/capacitaciones",
        "/puestos",
        "/competencias/nuevo",
        "/idiomas/nuevo",
        "/capacitaciones/nuevo",
        "/puestos/nuevo",
        "/candidatos",
        "/consultas",
        "/candidatos/nuevo",
        "/empleados",
        "/reportes/nuevos-ingresos"
      })
  void paginasPrincipalesRenderizan(String ruta) throws Exception {
    mvc.perform(get(ruta))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Talento RH")));
  }

  @Test
  void vistasConDatosDetallesYEdicionRenderizan() throws Exception {
    Candidato c = crear();
    Long id = c.getId();
    for (String ruta :
        List.of(
            "/candidatos/" + id,
            "/candidatos/" + id + "/editar",
            "/candidatos/" + id + "/contratar",
            "/competencias/" + competenciaId,
            "/competencias/" + competenciaId + "/editar",
            "/idiomas/" + idiomaId,
            "/idiomas/" + idiomaId + "/editar",
            "/capacitaciones/" + capacitacionId,
            "/capacitaciones/" + capacitacionId + "/editar",
            "/puestos/" + puestoId,
            "/puestos/" + puestoId + "/editar",
            "/candidatos",
            "/consultas")) mvc.perform(get(ruta)).andExpect(status().isOk());
    Empleado e = contratacion.contratar(id, contrato(LocalDate.now()));
    for (String ruta :
        List.of(
            "/",
            "/empleados",
            "/empleados/" + e.getId(),
            "/empleados/" + e.getId() + "/editar",
            "/reportes/nuevos-ingresos")) mvc.perform(get(ruta)).andExpect(status().isOk());
  }

  @Test
  void formulariosInvalidosConservanValoresYErrores() throws Exception {
    mvc.perform(
            post("/puestos")
                .with(csrf())
                .param("nombre", "Prueba inválida")
                .param("riesgo", "BAJO")
                .param("salarioMinimo", "200")
                .param("salarioMaximo", "100"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "rangoValido"))
        .andExpect(content().string(containsString("Prueba inv&aacute;lida")));
    mvc.perform(
            post("/candidatos")
                .with(csrf())
                .param("nombre", "Nombre conservado")
                .param("email", "correo-invalido"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "email"))
        .andExpect(content().string(containsString("Nombre conservado")));
    mvc.perform(
            get("/reportes/nuevos-ingresos")
                .param("desde", "2026-02-01")
                .param("hasta", "2026-01-01"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("filtro", "periodoValido"));
  }

  @Test
  void cedulaInvalidaSeRechazaAlCrearYBuscar() throws Exception {
    mvc.perform(
            post("/candidatos")
                .with(csrf())
                .param("cedula", "00123ABC456")
                .param("nombre", "Ana")
                .param("apellidos", "Prueba")
                .param("email", "ana@example.com")
                .param("puestoId", puestoId.toString())
                .param("departamento", "Tecnología")
                .param("salarioAspirado", "80000"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "cedula"));

    mvc.perform(get("/candidatos").param("cedula", "ABC123"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("filtro", "cedula"));

    mvc.perform(get("/empleados").param("cedula", "ABC123"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("filtro", "cedula"));
  }

  @Test
  void formulariosMuestranMascaraDeCedula() throws Exception {
    mvc.perform(get("/candidatos/nuevo"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("000-0000000-0")))
        .andExpect(content().string(containsString("data-cedula-mask")));
    mvc.perform(get("/candidatos"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-cedula-mask")));
    mvc.perform(get("/empleados"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-cedula-mask")));
  }

  @Test
  void telefonoInvalidoSeRechazaEnMvc() throws Exception {
    mvc.perform(
            post("/candidatos")
                .with(csrf())
                .param("cedula", "00000000018")
                .param("nombre", "Ana")
                .param("apellidos", "Prueba")
                .param("email", "ana@example.com")
                .param("telefono", "809-555-0000")
                .param("puestoId", puestoId.toString())
                .param("departamento", "Tecnología")
                .param("salarioAspirado", "80000"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "telefono"));
  }

  @Test
  void postCatalogoRealYProteccionCsrf() throws Exception {
    mvc.perform(post("/competencias").param("nombre", "Sin CSRF"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/competencias").with(csrf()).param("nombre", "Pensamiento crítico"))
        .andExpect(status().is3xxRedirection());
    assertThat(
            catalogos
                .buscar(TipoCatalogo.COMPETENCIAS, "Pensamiento", null, null, null, null, 0)
                .getTotalElements())
        .isEqualTo(1);
  }

  @Test
  void contratacionMvcYExportacionReal() throws Exception {
    Long id = crear().getId();
    mvc.perform(
            post("/candidatos/" + id + "/contratar")
                .with(csrf())
                .param("fechaIngreso", "2026-01-15")
                .param("departamento", "Tecnología")
                .param("puestoId", puestoId.toString())
                .param("salarioMensual", "90000"))
        .andExpect(status().is3xxRedirection());
    mvc.perform(
            get("/reportes/nuevos-ingresos/csv")
                .param("desde", "2026-01-01")
                .param("hasta", "2026-01-31"))
        .andExpect(status().isOk())
        .andExpect(
            header().string("Content-Disposition", "attachment; filename=\"nuevos-ingresos.csv\""))
        .andExpect(content().string(containsString("Ana Prueba")));
  }


  @Test
  @WithAnonymousUser
  void loginEsPublicoYProtegeElDashboard() throws Exception {
    mvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Bienvenido de nuevo")));

    mvc.perform(get("/"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithAnonymousUser
  void loginIncorrectoMuestraError() throws Exception {
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", "admin123")
                .param("password", "incorrecta"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login?error"));
  }

  @Test
  void recursoInexistenteDevuelve404() throws Exception {
    mvc.perform(get("/candidatos/99999999")).andExpect(status().isNotFound());
    mvc.perform(get("/ruta-inexistente")).andExpect(status().isNotFound());
  }
}
