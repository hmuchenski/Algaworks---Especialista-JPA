package com.algaworks.ecommerce.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class Atributo {

	private String nome;
	private String valor;

	public Atributo() {
		super();
	}

	public Atributo(String nome, String valor) {
		super();
		this.nome = nome;
		this.valor = valor;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

}
