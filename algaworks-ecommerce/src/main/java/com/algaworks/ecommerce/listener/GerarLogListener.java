package com.algaworks.ecommerce.listener;

import jakarta.persistence.PostLoad;

public class GerarLogListener {

	@PostLoad
	public void logCarregamento(Object obj) {
		System.out.println("Entidade " + obj.getClass().getSimpleName() + " foi carregada.");
	}

}
