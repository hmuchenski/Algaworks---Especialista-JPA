package com.algaworks.ecommerce.mapeamentobasico;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.algaworks.ecommerce.EntityManagerTest;
import com.algaworks.ecommerce.model.Categoria;

public class EstrategiaChavePrimariaTest extends EntityManagerTest {

	@Test
	public void testeEstrategiaSequence() {

		Categoria categoria = new Categoria(null, "Eletronicos", null, null, null);
		
		entityManager.getTransaction().begin();
		entityManager.persist(categoria);
		entityManager.getTransaction().commit();
		
		entityManager.clear();

		Categoria categoriaVerificacao = entityManager.find(Categoria.class, categoria.getId());
		assertNotNull(categoriaVerificacao);
	}

}
