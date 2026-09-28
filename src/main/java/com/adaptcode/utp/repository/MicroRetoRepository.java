package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.MicroReto;
import com.adaptcode.utp.model.NivelMicroReto;

public interface MicroRetoRepository extends JpaRepository<MicroReto, Long> {

	boolean existsByCodigoIgnoreCase(String codigo);

	List<MicroReto> findAllByTema_Curso_IdAndTema_ActivoTrueAndActivoTrueOrderByEnunciadoAsc(Long cursoId);

	List<MicroReto> findAllByTema_Curso_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(
			Long cursoId, NivelMicroReto nivel);

	List<MicroReto> findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueOrderByEnunciadoAsc(
			Long cursoId, Long temaId);

	List<MicroReto> findAllByTema_Curso_IdAndTema_IdAndTema_ActivoTrueAndActivoTrueAndNivelOrderByEnunciadoAsc(
			Long cursoId, Long temaId, NivelMicroReto nivel);

	Optional<MicroReto> findByIdAndActivoTrueAndTema_ActivoTrueAndTema_Curso_ActivoTrue(Long id);
}
