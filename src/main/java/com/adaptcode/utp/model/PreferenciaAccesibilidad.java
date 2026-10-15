package com.adaptcode.utp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "preferencias_accesibilidad")
public class PreferenciaAccesibilidad {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private Usuario usuario;

	@Size(max = 32, message = "El tema no debe superar los 32 caracteres")
	@Column(name = "tema", nullable = false, length = 32)
	private String tema = "NORMAL";

	@Size(max = 32, message = "El tamaño no debe superar los 32 caracteres")
	@Column(name = "tamano_texto", nullable = false, length = 32)
	private String tamanoTexto = "NORMAL";

	@Column(name = "reduccion_movimiento", nullable = false)
	private boolean reduccionMovimiento;

	@Column(name = "sonido_activado", nullable = false)
	private boolean sonidoActivado = true;

	@Column(name = "modo_daltonismo", nullable = false)
	private boolean modoDaltonismo;

	public PreferenciaAccesibilidad(Usuario usuario) {
		this.usuario = usuario;
	}
}
