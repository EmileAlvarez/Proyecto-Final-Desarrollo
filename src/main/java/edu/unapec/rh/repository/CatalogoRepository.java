package edu.unapec.rh.repository;

import edu.unapec.rh.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CatalogoRepository<T extends CatalogoBase>
    extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
  boolean existsByClaveAndIdNot(String clave, Long id);

  List<T> findByEstadoOrderByNombreAsc(Estado estado);

  long countByEstado(Estado estado);
}
