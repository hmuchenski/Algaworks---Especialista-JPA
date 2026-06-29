### Tópico: EntityManagerFactory (A Fábrica do Contexto de Persistência)

### 1. Objetivo

Atuar como uma fábrica (*factory*) imutável e *thread-safe* responsável pela criação e gerenciamento de instâncias de `EntityManager`. Centraliza o carregamento de metadados, a validação de mapeamentos objeto-relacionais e a inicialização de infraestruturas pesadas para que esse custo ocorra uma única vez no arranque (*startup*) do sistema.

### 2. O que é?

É a interface principal definida pela especificação Jakarta Persistence (JPA) que representa a personificação em tempo de execução de uma Unidade de Persistência (*Persistence Unit*). Trata-se de um componente de peso pesado (*heavyweight*), projetado para possuir escopo global dentro do ciclo de vida da aplicação (padrão *Singleton*).

### 3. Por que existe?

Porque os processos de ler arquivos de configuração (`persistence.xml`), escanear classes anotadas com `@Entity`, validar chaves e relacionamentos, detetar dialetos de banco de dados e inicializar pools de conexões exigem um processamento extremamente custoso. Sem a `EntityManagerFactory`, a aplicação teria que sofrer esse enorme impacto de latência a cada transação ou consulta.

### 4. Como funciona?

No arranque (*bootstrap*) do sistema, a aplicação invoca a classe utilitária da especificação: `Persistence.createEntityManagerFactory("nome-da-unidade")`. A JPA localiza a configuração, transfere a responsabilidade para o provedor de persistência (Hibernate Core) e este compila e devolve a fábrica inteiramente inicializada e pronta para gerar instâncias leves de `EntityManager` sob demanda.

### 5. Funcionamento interno

Ao ser instanciada, a fábrica encapsula o `ServiceRegistry` do Hibernate (o ecossistema de serviços como JDBC e transações) e o cache de planos de consulta (*Query Plan Cache*). Ela cria internamente um metamodelo detalhado de todas as tabelas e colunas mapeadas, mantendo em memória uma árvore sintática abstrata (AST) para otimizar a tradução rápida de JPQL/HQL para SQL nativo em tempo de execução.

### 6. Quando usar?

Deve ser obrigatoriamente utilizada em qualquer aplicação Java que implemente o padrão de mapeamento objeto-relacional através da JPA. É ideal para arquiteturas corporativas de longa duração que lidam com requisições simultâneas concorrentes e necessitam de isolamento transacional.

### 7. Quando NÃO usar?

Não deve ser utilizada se a intenção for instanciar uma nova fábrica a cada requisição HTTP ou ciclo transacional, dado o seu alto custo de criação. Também se torna obsoleta em microsserviços puramente reativos e não relacionais que operam sem o ecossistema ORM clássico.

### 8. O que influencia este conceito?

* **Mapeamento de Entidades:** A quantidade de classes `@Entity` e a complexidade de seus relacionamentos definem o tamanho do metamodelo em memória.
* **Arquivos de Configuração:** O conteúdo declarativo presente no `persistence.xml` ou nos prefixos `spring.jpa.*` do `application.properties`.
* **Infraestrutura de Rede:** A estabilidade e configuração do `DataSource` (como o pool do HikariCP) afetam diretamente o bootstrap da fábrica.

### 9. O que este conceito influencia?

* **Tempo de Boot do Sistema:** Um metamodelo gigante ou validações rígidas aumentam o tempo de startup da aplicação.
* **Consumo de Heap Memory:** Por reter todos os metadados estruturais estáticos da persistência de forma global.
* **Desempenho de Queries:** A existência do cache de planos compilados na fábrica dita a velocidade de entrega das consultas ao banco de dados.

### 10. Configurações que alteram seu comportamento

* `hibernate.hbm2ddl.auto`: Define se a fábrica irá validar (`validate`), atualizar (`update`) ou criar (`create`) o esquema do banco de dados ao inicializar.
* `hibernate.jdbc.batch_size`: Altera a capacidade da fábrica de otimizar inserções em lote nas sessões geradas.
* `exclude-unlisted-classes`: Configuração no XML que determina se a fábrica deve escanear todo o *classpath* ou restringir-se estritamente às classes listadas.

### 11. O que a especificação (ou teoria) define?

A especificação Jakarta Persistence determina que a `EntityManagerFactory` é o contrato abstrato e agnóstico de fornecimento. Ela isola o desenvolvedor de acoplamentos proprietários, garantindo que o código interaja estritamente com o pacote `jakarta.persistence.*`.

### 12. Como isso é implementado na prática?

O Hibernate implementa este contrato através da classe `SessionFactoryImpl`. Desse modo, no ecossistema do Hibernate, a `SessionFactory` herda e estende nativamente as capacidades da `EntityManagerFactory` da JPA, agindo como o verdadeiro "cérebro" operacional.

### 13. Casos especiais

Em ecossistemas modernos baseados em Spring Boot, a manipulação direta da `EntityManagerFactory` é abstraída. O framework injeta programaticamente um `LocalContainerEntityManagerFactoryBean` que atua como um *proxy* inteligente para gerenciar o ciclo de vida da fábrica sem a necessidade do arquivo `persistence.xml`.

### 14. Erros ou exceções relacionadas

* `MappingException`: Ocorre no startup se houver um erro de sintaxe ou inconsistência nos mapeamentos das entidades.
* `PersistenceException`: Lançada se a unidade de persistência nomeada não for localizada no *classpath*.
* `OutOfMemoryError`: Se o volume de entidades mapeadas e caches estáticos da fábrica estourarem o espaço físico alocado para a JVM (*Heap*).

### 15. Modelos mentais incorretos

* **Achar que EntityManagerFactory e SessionFactory são rivais:** Na verdade, a `SessionFactory` do Hibernate **é** a própria `EntityManagerFactory` por baixo dos panos.
* **Confundir Fábrica com Contexto:** Pensar que a fábrica monitora entidades sujas (*Dirty Checking*), quando na verdade essa é uma função exclusiva e isolada do `EntityManager` (Contexto de Persistência).

### 16. Exemplos práticos

Inicialização manual nativa da especificação JPA em ambientes Java SE:

```java
// Inicialização única no startup da aplicação (Singleton)
EntityManagerFactory emf = Persistence.createEntityManagerFactory("ProducaoPU");

// Criação de uma instância leve e transacional por Thread/Requisição
EntityManager em = emf.createEntityManager();

// Fechamento da fábrica apenas no encerramento da aplicação
emf.close();

```

### 17. Impactos e consequências

* **Bem configurado:** Inicialização segura, validação prévia de falhas no deployment e geração estável de conexões otimizadas em tempo de execução.
* **Mal configurado:** Travamentos catastróficos no startup, vazamentos de conexões (*connection leaks*) ou alocação ineficiente de memória.

### 18. Fluxograma

```
Configurações (XML/Properties) ──> Persistence.createEntityManagerFactory()
                                                   │
                                                   ▼
                                      Bootstrapping do Provedor
                               (ServiceRegistry + Metamodelo + Pool)
                                                   │
                                                   ▼
                                      EntityManagerFactory (Pronta)
                                                   │
                                                   ▼
                                        createEntityManager()
                                                   │
                                                   ▼
                                         EntityManager (Leve)

```

### 19. Tabela resumo

| Característica | EntityManagerFactory | EntityManager |
| --- | --- | --- |
| **Peso Operacional** | Pesado (*Heavyweight*) | Leve (*Lightweight*) |
| **Escopo / Ciclo de Vida** | Escopo Global da Aplicação (*Singleton*) | Escopo Transacional (*Request/Thread*) |
| **Thread-Safety** | Sim, é totalmente *Thread-Safe* | Não, é estritamente *Thread-Unsafe* |
| **Finalidade Primária** | Compilar metadados e fabricar conexões | Executar operações de CRUD e gerir estados |

### 20. Checklist mental

* [ ] Certifiquei-me de que a minha aplicação possui apenas **uma única** instância de `EntityManagerFactory` ativa por banco de dados?
* [ ] Se utilizo Spring Boot, compreendo perfeitamente que o `EntityManagerFactory` injetado é, nativamente, a `SessionFactory` do Hibernate e evito instanciar fábricas paralelas?
* [ ] O encerramento (`.close()`) da fábrica está configurado estritamente atrelado aos ganchos (*hooks*) de desligamento final do servidor?

### 21. Conceitos relacionados

* **Persistence Unit (Unidade de Persistência):** O agrupamento declarativo que define as fronteiras e dados da fábrica.
* **Persistence Context (Contexto de Persistência):** O ambiente isolado de runtime onde as entidades são gerenciadas pelo produto da fábrica.
* **ServiceRegistry:** O barramento interno de serviços consumido pela infraestrutura do Hibernate para alimentar a fábrica.