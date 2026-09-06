package com.devkahoot.kahoot.model;

public class Estudiante {

	private int id;
	private String apodo;
	private String codigoPartida;
	private int puntaje;

	public Estudiante() {
	}

	public Estudiante(String apodo) {
		this.apodo = apodo;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getApodo() {
		return apodo;
	}

	public void setApodo(String apodo) {
		this.apodo = apodo;
	}

	public String getCodigoPartida() {
		return codigoPartida;
	}

	public void setCodigoPartida(String codigoPartida) {
		this.codigoPartida = codigoPartida;
	}

	public int getPuntaje() {
		return puntaje;
	}

	public void setPuntaje(int puntaje) {
		this.puntaje = puntaje;
	}
}
