package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.ProgresoMicroReto;

public interface ProgresoMicroRetoRepository extends JpaRepository<ProgresoMicroReto, Long> {

	Optional<ProgresoMicroReto> findByUsuario_IdAndMicroReto_Id(Long usuarioId, Long microRetoId);

	List<ProgresoMicroReto> findAllByUsuario_Id(Long usuarioId);

	long countByUsuario_IdAndCompletadoTrue(Long usuarioId);
}
