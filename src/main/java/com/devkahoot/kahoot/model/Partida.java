package com.devkahoot.kahoot.model;

import java.util.ArrayList;
import java.util.List;

public class Partida {

	private String codigo;
	private String nombre;
	private EstadoPartida estado;
	private List<Integer> idsPreguntas = new ArrayList<>();
	private List<Integer> idsEstudiantes = new ArrayList<>();

	public Partida() {
		this.estado = EstadoPartida.ESPERANDO;
	}

	public Partida(String codigo, String nombre) {
		this.codigo = codigo;
		this.nombre = nombre;
		this.estado = EstadoPartida.ESPERANDO;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public EstadoPartida getEstado() {
		return estado;
	}

	public void setEstado(EstadoPartida estado) {
		this.estado = estado;
	}

	public List<Integer> getIdsPreguntas() {
		return idsPreguntas;
	}

	public void setIdsPreguntas(List<Integer> idsPreguntas) {
		this.idsPreguntas = idsPreguntas;
	}

	public List<Integer> getIdsEstudiantes() {
		return idsEstudiantes;
	}

	public void setIdsEstudiantes(List<Integer> idsEstudiantes) {
		this.idsEstudiantes = idsEstudiantes;
	}
}
