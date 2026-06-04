package com.algaworks.ecommerce;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerTest {

	protected static EntityManagerFactory entityManagerFactory;

	protected EntityManager entityManager;

	@BeforeAll
	public static void setupBeforeClass() {
		entityManagerFactory = Persistence.createEntityManagerFactory("Ecommerce-PU");
	}

	@AfterAll
	public static void tearDownAfterClass() {
		entityManagerFactory.close();
	}

	@BeforeEach
	public void setup() {
		entityManager = entityManagerFactory.createEntityManager();
	}

	@AfterEach
	public void tearDown() {
		entityManager.close();
	}

}
