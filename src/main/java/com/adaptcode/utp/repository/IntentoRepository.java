package com.adaptcode.utp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.adaptcode.utp.model.Intento;

public interface IntentoRepository extends JpaRepository<Intento, Long> {

	long countByUsuario_IdAndMicroReto_Id(Long usuarioId, Long microRetoId);

	// ! CONSULTA PARA OBTENER LOS INTENTOS DE UN USUARIO POR MICRORETO ORDENADOS POR NRO DE INTENTO
	@Query("SELECT i FROM Intento i WHERE i.usuario.id = :usuarioId AND i.microReto.id = :microRetoId ORDER BY i.nroIntento ASC")
	List<Intento> listarPorUsuarioYReto(
			@Param("usuarioId") Long usuarioId, @Param("microRetoId") Long microRetoId);
}
