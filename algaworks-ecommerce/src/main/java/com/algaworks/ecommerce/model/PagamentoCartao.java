package com.algaworks.ecommerce.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@DiscriminatorValue(value = "cartao")
@Table(name = "pagamento_cartao")
public class PagamentoCartao extends Pagamento {

	private String numero;

	public PagamentoCartao() {
		super();
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

}
