package com.algaworks.ecommerce.iniciandocomjpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.algaworks.ecommerce.EntityManagerTest;
import com.algaworks.ecommerce.model.Produto;

public class ConsultandoRegistrosTest extends EntityManagerTest {

	@Test
	public void procurarPorIdentificador() {
		
		Produto produto = entityManager.find(Produto.class, 1);
		// Produto produto = entityManager.getReference(Produto.class, 1);

		assertNotNull(produto);
		assertEquals("Kindle", produto.getNome());
	}

	@Test
	public void atualizarReferencia() {
		
		Produto produto = entityManager.find(Produto.class, 1);

		produto.setNome("Nome alterado");

		entityManager.refresh(produto); // Volta ao estado do banco

		assertEquals("Kindle", produto.getNome());
	}

}
