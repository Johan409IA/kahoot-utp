package com.devkahoot.kahoot.model;

public class ResultadoRespuesta {

	private boolean correcta;
	private int puntosObtenidos;
	private String retroalimentacion;

	public ResultadoRespuesta() {
	}

	public ResultadoRespuesta(boolean correcta, int puntosObtenidos, String retroalimentacion) {
		this.correcta = correcta;
		this.puntosObtenidos = puntosObtenidos;
		this.retroalimentacion = retroalimentacion;
	}

	public boolean isCorrecta() {
		return correcta;
	}

	public void setCorrecta(boolean correcta) {
		this.correcta = correcta;
	}

	public int getPuntosObtenidos() {
		return puntosObtenidos;
	}

	public void setPuntosObtenidos(int puntosObtenidos) {
		this.puntosObtenidos = puntosObtenidos;
	}

	public String getRetroalimentacion() {
		return retroalimentacion;
	}

	public void setRetroalimentacion(String retroalimentacion) {
		this.retroalimentacion = retroalimentacion;
	}
}
