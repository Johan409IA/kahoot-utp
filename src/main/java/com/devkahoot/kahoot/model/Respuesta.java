package com.devkahoot.kahoot.model;

public class Respuesta {

	private int estudianteId;
	private int preguntaId;
	private String opcionSeleccionada;

	public Respuesta() {
	}

	public Respuesta(int estudianteId, int preguntaId, String opcionSeleccionada) {
		this.estudianteId = estudianteId;
		this.preguntaId = preguntaId;
		this.opcionSeleccionada = opcionSeleccionada;
	}

	public int getEstudianteId() {
		return estudianteId;
	}

	public void setEstudianteId(int estudianteId) {
		this.estudianteId = estudianteId;
	}

	public int getPreguntaId() {
		return preguntaId;
	}

	public void setPreguntaId(int preguntaId) {
		this.preguntaId = preguntaId;
	}

	public String getOpcionSeleccionada() {
		return opcionSeleccionada;
	}

	public void setOpcionSeleccionada(String opcionSeleccionada) {
		this.opcionSeleccionada = opcionSeleccionada;
	}
}
