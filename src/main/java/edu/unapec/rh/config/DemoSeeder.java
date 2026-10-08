package edu.unapec.rh.config;

import edu.unapec.rh.dto.*;
import edu.unapec.rh.entity.*;
import edu.unapec.rh.repository.CandidatoRepository;
import edu.unapec.rh.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DemoSeeder implements ApplicationRunner {
  private final CatalogoService catalogos;
  private final CandidatoService candidatos;
  private final ContratacionService contratacion;
  private final CandidatoRepository repository;

  public DemoSeeder(
      CatalogoService catalogos,
      CandidatoService candidatos,
      ContratacionService contratacion,
      CandidatoRepository repository) {
    this.catalogos = catalogos;
    this.candidatos = candidatos;
    this.contratacion = contratacion;
    this.repository = repository;
  }

  private Long simple(TipoCatalogo t, String nombre) {
    CatalogoForm f = new CatalogoForm();
    f.setNombre(nombre);
    return catalogos.guardar(t, null, f).getId();
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    // La semilla se ejecuta solamente en una base vacía y se revierte completa ante un error.
    if (repository.count() > 0
        || !catalogos.todos(TipoCatalogo.PUESTOS).isEmpty()
        || !catalogos.todos(TipoCatalogo.COMPETENCIAS).isEmpty()
        || !catalogos.todos(TipoCatalogo.IDIOMAS).isEmpty()
        || !catalogos.todos(TipoCatalogo.CAPACITACIONES).isEmpty()) return;
    List<Long> competencias = new ArrayList<>();
    for (String n :
        List.of(
            "Java",
            "SQL",
            "Liderazgo",
            "Comunicación efectiva",
            "Trabajo en equipo",
            "Resolución de problemas",
            "Orientación a resultados",
            "Gestión de proyectos")) competencias.add(simple(TipoCatalogo.COMPETENCIAS, n));
    List<Long> idiomas = new ArrayList<>();
    for (String n : List.of("Español", "Inglés", "Francés", "Portugués", "Alemán", "Italiano"))
      idiomas.add(simple(TipoCatalogo.IDIOMAS, n));
    List<Long> puestos = new ArrayList<>();
    for (String n :
        List.of(
            "Desarrollador de Software",
            "Analista de Sistemas",
            "QA Engineer",
            "Contador",
            "Especialista de Recursos Humanos")) {
      CatalogoForm f = new CatalogoForm();
      f.setNombre(n);
      f.setRiesgo(Riesgo.BAJO);
      f.setSalarioMinimo(new BigDecimal("45000"));
      f.setSalarioMaximo(new BigDecimal("150000"));
      puestos.add(catalogos.guardar(TipoCatalogo.PUESTOS, null, f).getId());
    }
    List<Long> caps = new ArrayList<>();
    String[] titulos = {
      "Ingeniería de Software", "Gestión de Talento Humano", "Contabilidad y Finanzas"
    };
    for (String n : titulos) {
      CatalogoForm f = new CatalogoForm();
      f.setNombre(n);
      f.setNivel(Nivel.GRADO);
      f.setInstitucion("UNAPEC · demostración");
      f.setFechaDesde(LocalDate.of(2020, 1, 15));
      f.setFechaHasta(LocalDate.of(2024, 8, 15));
      caps.add(catalogos.guardar(TipoCatalogo.CAPACITACIONES, null, f).getId());
    }
    String[] nombres = {
      "Laura", "Carlos", "María", "Andrés", "Paola", "Gabriel", "Sofía", "Daniel"
    };
    String[] apellidos = {
      "Martínez", "Pérez", "Gómez", "Castillo", "Ramírez", "Santos", "Reyes", "Díaz"
    };
    String[] deptos = {"Tecnología", "Tecnología", "Calidad", "Finanzas", "Recursos Humanos"};
    String[] cedulasDemo = {
      "00000000018",
      "00000000026",
      "00000000034",
      "00000000042",
      "00000000059",
      "00000000067",
      "00000000075",
      "00000000083"
    };
    for (int i = 0; i < nombres.length; i++) {
      CandidatoForm f = new CandidatoForm();
      f.setCedula(cedulasDemo[i]);
      f.setNombre(nombres[i]);
      f.setApellidos(apellidos[i]);
      f.setEmail("demo" + (i + 1) + "@example.com");
      f.setTelefono("80955501" + String.format("%02d", i + 1));
      f.setDepartamento(deptos[i % 5]);
      f.setPuestoId(puestos.get(i % 5));
      f.setSalarioAspirado(new BigDecimal(60000 + i * 5000));
      f.setRecomendadoPor("Datos ficticios de demostración");
      f.setCompetenciaIds(
          List.of(
              competencias.get(i % competencias.size()),
              competencias.get((i + 1) % competencias.size())));
      f.setCapacitacionIds(List.of(caps.get(i % 3)));
      for (int j = 0; j < 2; j++) {
        IdiomaSeleccion s = new IdiomaSeleccion();
        s.setIdiomaId(idiomas.get(j));
        s.setSeleccionado(true);
        s.setNivel(j == 0 ? NivelIdioma.NATIVO : NivelIdioma.AVANZADO);
        f.getIdiomas().add(s);
      }
      ExperienciaForm exp = new ExperienciaForm();
      exp.setEmpresa("Empresa de demostración " + (i + 1));
      exp.setPuestoOcupado("Asistente de área");
      exp.setFechaDesde(LocalDate.of(2023, 1, 1));
      exp.setFechaHasta(LocalDate.of(2025, 12, 31));
      exp.setSalario(new BigDecimal("40000"));
      f.getExperiencias().add(exp);
      Candidato c = candidatos.guardar(null, f);
      if (i == 1 || i == 3)
        candidatos.cambiarEstado(
            c.getId(),
            i == 1 ? EstadoCandidato.EN_EVALUACION : EstadoCandidato.PRESELECCIONADO,
            c.getVersion());
      if (i >= 5) {
        ContratacionForm h = new ContratacionForm();
        h.setFechaIngreso(LocalDate.now().minusDays((i - 5) * 18L));
        h.setDepartamento(f.getDepartamento());
        h.setPuestoId(f.getPuestoId());
        h.setSalarioMensual(f.getSalarioAspirado());
        contratacion.contratar(c.getId(), h);
      }
    }
  }
}
