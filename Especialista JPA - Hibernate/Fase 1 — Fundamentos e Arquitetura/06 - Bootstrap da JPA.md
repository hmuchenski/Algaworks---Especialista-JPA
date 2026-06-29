### Tópico: Bootstrap da JPA (Inicialização do Ambiente de Persistência)

#### 1. Objetivo

Estabelecer a ponte entre o código da aplicação e o banco de dados, inicializando a `EntityManagerFactory` (fábrica de `EntityManager`), o que envolve carregar configurações, validar o mapeamento das entidades e criar o pool de conexões.

#### 2. O que é?

É o processo de inicialização do provedor de persistência (Hibernate, neste caso). É o momento em que a JPA "lê" o arquivo `persistence.xml` ou as configurações programáticas, escaneia as classes anotadas com `@Entity` e prepara o metadado que será usado em tempo de execução.

#### 3. Por que existe?

A JPA precisa de um contexto. Ela não pode simplesmente começar a salvar objetos; ela precisa saber *onde* salvar (banco de dados), *como* se conectar (JDBC), *qual* dialeto SQL usar e *quais* classes fazem parte da unidade de persistência (`Persistence Unit`).

#### 4. Como funciona?

Através da classe `jakarta.persistence.Persistence`, a aplicação invoca `createEntityManagerFactory`. O provedor (Hibernate) entra em cena, lê o `persistence.xml`, processa o modelo de entidades (Metamodel) e inicializa a fábrica.

#### 5. Funcionamento interno

1. **Descoberta do Provedor:** A JPA procura por implementações através da Service Provider Interface (SPI).
2. **Leitura de Metadados:** Processamento das anotações `@Entity`, `@Table`, `@Column`, etc.
3. **Validação:** Verificação se todas as dependências (Driver JDBC) estão no classpath.
4. **Criação do Pool:** Estabelecimento das conexões iniciais.
5. **Criação da Fábrica:** Instanciação do objeto `EntityManagerFactory` (que é *thread-safe* e pesado).

#### 6. Quando usar?

Deve ser executado exatamente uma vez durante o ciclo de vida da aplicação (no *startup* ou *deployment*).

#### 7. Quando NÃO usar?

Nunca chame o bootstrap (criação da `EntityManagerFactory`) dentro de um método de negócio ou a cada requisição HTTP. É uma operação extremamente custosa em termos de processamento e memória.

#### 8. O que influencia este conceito?

* O conteúdo do `persistence.xml` (ou `hibernate.properties`).
* Classes anotadas no classpath.
* Driver JDBC disponibilizado.
* Configurações de servidor (se rodando em Jakarta EE).

#### 9. O que este conceito influencia?

* O tempo de "subida" (startup) da aplicação.
* A disponibilidade da `EntityManager`.
* A integridade do Metamodel.

#### 10. Configurações que alteram seu comportamento

* `javax.persistence.schema-generation.database.action`: Define se o Hibernate cria/valida tabelas no bootstrap.
* `hibernate.hbm2ddl.auto`: Define a estratégia de atualização do schema.
* Propriedades de cache (2º nível).

#### 11. O que a especificação (ou teoria) define?

A especificação JPA exige que a classe `Persistence` seja o ponto de entrada único para obter uma `EntityManagerFactory`, garantindo que o desenvolvedor não precise acoplar o código diretamente ao Hibernate na fase de inicialização.

#### 12. Como isso é implementado na prática?

O Hibernate estende a especificação com o seu próprio `BootstrapServiceRegistryBuilder`, que permite configurar loggers, classloaders e outras otimizações antes mesmo da fábrica ser criada.

#### 13. Casos especiais

* **Java SE:** Você é responsável por fechar a fábrica (`emf.close()`) quando a aplicação encerra.
* **Jakarta EE:** O servidor de aplicação gerencia o ciclo de vida do bootstrap.

#### 14. Erros ou exceções relacionadas

* `PersistenceException`: Erro genérico de persistência.
* `NoPersistenceUnitException`: Quando o nome da unidade de persistência não é encontrado.
* `ClassNotFoundException` (nas entidades): Erro de scan de classpath.
* Falha na conexão JDBC (Timeout no bootstrap).

#### 15. Modelos mentais incorretos

Achar que `EntityManagerFactory` é leve. Muitas pessoas criam uma fábrica nova para cada operação de banco, o que destrói a performance da aplicação devido ao custo de inicialização.

#### 16. Exemplos práticos

```java
// O custo desta linha é alto. Deve ser estática/Singleton.
EntityManagerFactory emf = Persistence.createEntityManagerFactory("meu-banco-pu");

```

#### 17. Impactos e consequências

* **Se mal configurado:** Tempo de resposta inicial lento, consumo excessivo de memória (se carregar metadados desnecessários), falhas no deploy.
* **Se bem configurado:** Inicialização rápida, pool de conexões pronto, mapeamento validado em tempo de desenvolvimento.

#### 18. Fluxograma

`Aplicação` -> `Persistence.createEntityManagerFactory` -> `Provedor (Hibernate)` -> `Leitura XML/Annotations` -> `Metamodel` -> `Conexão JDBC` -> `EntityManagerFactory (Ready)`.

#### 19. Tabela resumo

| Característica | Bootstrap (Java SE) | Bootstrap (Container/EE) |
| --- | --- | --- |
| **Responsabilidade** | Desenvolvedor | Servidor (JBoss, WildFly) |
| **Ciclo de Vida** | Manual (Open/Close) | Gerenciado automaticamente |
| **Configuração** | `persistence.xml` | `persistence.xml` + JNDI |

#### 20. Checklist mental

* [ ] O `persistence-unit` está com o nome correto?
* [ ] O driver JDBC está incluído no projeto?
* [ ] O arquivo `persistence.xml` está no diretório `META-INF`?
* [ ] Estou injetando o `EntityManager` ou utilizando o Singleton de `EntityManagerFactory` em vez de criar um novo por requisição?

#### 21. Conceitos relacionados

* **Persistence Unit:** O escopo configurado no bootstrap.
* **EntityManagerFactory:** O produto final do bootstrap.
* **Metamodel:** O mapa das entidades gerado durante o bootstrap.