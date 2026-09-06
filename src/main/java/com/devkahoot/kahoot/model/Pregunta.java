package com.devkahoot.kahoot.model;
import java.util.List;
public class Pregunta {

	private int id;
	private String enunciado;
	private List<String> opciones;
	private String respuestaCorrecta;
	private String explicacion;
	private String categoria;
	private int orden;

	public Pregunta() {
	}

	public Pregunta(String enunciado, java.util.List<String> opciones, String respuestaCorrecta,
			String explicacion, String categoria) {
		this.enunciado = enunciado;
		this.opciones = opciones;
		this.respuestaCorrecta = respuestaCorrecta;
		this.explicacion = explicacion;
		this.categoria = categoria;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getEnunciado() {
		return enunciado;
	}

	public void setEnunciado(String enunciado) {
		this.enunciado = enunciado;
	}

	public java.util.List<String> getOpciones() {
		return opciones;
	}

	public void setOpciones(java.util.List<String> opciones) {
		this.opciones = opciones;
	}

	public String getRespuestaCorrecta() {
		return respuestaCorrecta;
	}

	public void setRespuestaCorrecta(String respuestaCorrecta) {
		this.respuestaCorrecta = respuestaCorrecta;
	}

	public String getExplicacion() {
		return explicacion;
	}

	public void setExplicacion(String explicacion) {
		this.explicacion = explicacion;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}
}
