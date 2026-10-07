package com.adaptcode.utp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adaptcode.utp.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByCorreoIgnoreCase(String correo);

	boolean existsByCorreoIgnoreCase(String correo);
}
