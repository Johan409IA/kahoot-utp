package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.InsigniaObtenida;
import com.adaptcode.utp.model.TipoInsignia;

public interface InsigniaObtenidaRepository extends JpaRepository<InsigniaObtenida, Long> {

	List<InsigniaObtenida> findAllByUsuario_Id(Long usuarioId);

	boolean existsByUsuario_IdAndTipo(Long usuarioId, TipoInsignia tipo);
}
