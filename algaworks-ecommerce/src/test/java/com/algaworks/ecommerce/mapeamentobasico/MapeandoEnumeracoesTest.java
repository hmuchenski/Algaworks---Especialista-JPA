package com.algaworks.ecommerce.mapeamentobasico;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.algaworks.ecommerce.EntityManagerTest;
import com.algaworks.ecommerce.model.Cliente;
import com.algaworks.ecommerce.model.Sexo;

public class MapeandoEnumeracoesTest extends EntityManagerTest {

	@Test
	public void testarEnum() {

		Cliente cliente = new Cliente(null, "José Mineiro", Sexo.MASCULINO, List.of());

		entityManager.getTransaction().begin();
		entityManager.persist(cliente);
		entityManager.getTransaction().commit();

		entityManager.clear();

		Cliente clienteVerificacao = entityManager.find(Cliente.class, cliente.getId());
		assertNotNull(clienteVerificacao);
	}
}
