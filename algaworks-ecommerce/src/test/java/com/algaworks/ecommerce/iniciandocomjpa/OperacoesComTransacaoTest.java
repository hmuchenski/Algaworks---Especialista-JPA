package com.algaworks.ecommerce.iniciandocomjpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.algaworks.ecommerce.EntityManagerTest;
import com.algaworks.ecommerce.model.Produto;

public class OperacoesComTransacaoTest extends EntityManagerTest {

	@Test
	public void impedirOperacaoComBancoDeDados() {

		Produto produto = entityManager.find(Produto.class, 1l);
		entityManager.detach(produto);
		
		entityManager.getTransaction().begin();
		produto.setNome("Kindle PaperWhite");
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoAlterado = entityManager.find(Produto.class, produto.getId());

		assertEquals("Kindle", produtoAlterado.getNome());
	}

	@Test
	public void mostrarDifencaPersistMerge() {
		Produto produtoPersist = new Produto();

		produtoPersist.setId(6);
		produtoPersist.setNome("Smartphone One Plus");
		produtoPersist.setDescricao("O processador mais rápido.");
		produtoPersist.setPreco(new BigDecimal(2000));

		entityManager.getTransaction().begin();
		entityManager.persist(produtoPersist); // Make an instance managed and persistent.
		produtoPersist.setNome("Smartphone Two Plus");
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoVerificacaoPersist = entityManager.find(Produto.class, produtoPersist.getId());
		assertNotNull(produtoVerificacaoPersist);

		Produto produtoMerge = new Produto();

		produtoMerge.setId(7);
		produtoMerge.setNome("Notebook Dell");
		produtoMerge.setDescricao("O melhor da categoria.");
		produtoMerge.setPreco(new BigDecimal(2000));

		entityManager.getTransaction().begin();
		produtoMerge = entityManager.merge(produtoMerge); // Returns: the managed instance that the state was merged to
		produtoMerge.setNome("Notebook Dell 2");
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoVerificacaoMerge = entityManager.find(Produto.class, produtoMerge.getId());
		assertNotNull(produtoVerificacaoMerge);
	}

	@Test
	public void inserirObjetoComMerge() {

		Produto produto = new Produto(4, "Microfone", "Melhor qualidade de som", new BigDecimal(1000.0));

		entityManager.getTransaction().begin();
		entityManager.merge(produto);
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoCadastrado = entityManager.find(Produto.class, produto.getId());

		assertEquals(produto.getNome(), produtoCadastrado.getNome());
	}

	@Test
	public void atualizarObjetoGerenciado() {

		Produto produto = entityManager.getReference(Produto.class, 1l);

		entityManager.getTransaction().begin();
		produto.setNome("Kindle PaperWhite 2");
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoAlterado = entityManager.find(Produto.class, produto.getId());

		assertEquals(produto.getNome(), produtoAlterado.getNome());
	}

	@Test
	public void atualizarObjetoNaoGerenciado() {

		Produto produto = new Produto(2, "Câmera Canon", "A melhor definição para suas fotos", new BigDecimal(5000.0));

		produto.setNome("Câmera Canon 2.0");

		entityManager.getTransaction().begin();
		entityManager.merge(produto);
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoAlterado = entityManager.find(Produto.class, produto.getId());

		assertEquals(produto.getNome(), produtoAlterado.getNome());
	}

	@Test
	public void removerObjeto() {

		Produto produto = entityManager.getReference(Produto.class, 1l);

		entityManager.getTransaction().begin();
		entityManager.remove(produto);
		entityManager.getTransaction().commit();

		produto = entityManager.find(Produto.class, 1l);

		assertNull(produto);
	}

	@Test
	public void inserirObjeto() {

		Produto produto = new Produto(5, "Câmera Canon", "A melhor definição para suas fotos", new BigDecimal(5000.0));

		entityManager.getTransaction().begin();
		entityManager.persist(produto);
		entityManager.getTransaction().commit();

		entityManager.clear();

		Produto produtoCadastrado = entityManager.find(Produto.class, produto.getId());

		assertEquals(produto.getNome(), produtoCadastrado.getNome());
	}

}
