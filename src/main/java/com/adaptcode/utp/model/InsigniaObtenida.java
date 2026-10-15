package com.adaptcode.utp.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "insignias_obtenidas", uniqueConstraints = {
		@UniqueConstraint(name = "uk_insignias_usuario_tipo", columnNames = {
				"usuario_id", "tipo" })
})
public class InsigniaObtenida {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 32)
	private TipoInsignia tipo;

	@Column(name = "obtenido_en", nullable = false, updatable = false)
	private Instant obtenidoEn;

	public InsigniaObtenida(Usuario usuario, TipoInsignia tipo) {
		this.usuario = usuario;
		this.tipo = tipo;
	}

	@PrePersist
	private void fijarFechaObtencion() {
		if (obtenidoEn == null) {
			obtenidoEn = Instant.now();
		}
	}
}
