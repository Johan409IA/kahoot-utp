package com.devkahoot.kahoot.model;

import java.util.List;

public class PreguntaParaEstudiante {

	private int id;
	private String enunciado;
	private List<String> opciones;
	private String categoria;
	private int orden;

	public PreguntaParaEstudiante() {
	}

	public PreguntaParaEstudiante(int id, String enunciado, List<String> opciones, String categoria, int orden) {
		this.id = id;
		this.enunciado = enunciado;
		this.opciones = opciones;
		this.categoria = categoria;
		this.orden = orden;
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

	public List<String> getOpciones() {
		return opciones;
	}

	public void setOpciones(List<String> opciones) {
		this.opciones = opciones;
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
