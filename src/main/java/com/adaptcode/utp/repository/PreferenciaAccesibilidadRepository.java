package com.adaptcode.utp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.PreferenciaAccesibilidad;

public interface PreferenciaAccesibilidadRepository
		extends JpaRepository<PreferenciaAccesibilidad, Long> {

	Optional<PreferenciaAccesibilidad> findByUsuario_Id(Long usuarioId);
}
