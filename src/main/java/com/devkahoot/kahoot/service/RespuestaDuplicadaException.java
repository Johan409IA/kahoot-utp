package com.devkahoot.kahoot.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RespuestaDuplicadaException extends RuntimeException {

	// metodo reutilizable para lanzar una excepcion de respuesta duplicada
	public RespuestaDuplicadaException(String mensaje) {
		super(mensaje);
	}
}
