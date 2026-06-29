# Tópico: SessionFactory (A implementação do EntityManagerFactory)

### 1. Objetivo

Atuar como uma fábrica (*factory*) thread-safe e imutável para a criação de instâncias de `Session` (a interface primária de trabalho do Hibernate). Ela centraliza, processa e armazena em cache todos os metadados de mapeamento objeto-relacional, definições de dialetos de banco de dados, planos de execução de consultas estruturadas compiladas e as configurações da infraestrutura de conexão. O objetivo fim é otimizar drasticamente a performance da camada de persistência, garantindo que o pesado custo de inicialização de metadados ocorra uma única vez no arranque do sistema.

### 2. O que é?

É a implementação concreta, robusta e proprietária da interface `jakarta.persistence.EntityManagerFactory` definida pela especificação Jakarta Persistence (JPA). Enquanto a especificação JPA dita o contrato abstrato de fábrica de gerenciadores, o Hibernate entrega a sua inteligência operacional através da classe `SessionFactoryImpl` (que implementa as interfaces `SessionFactory` e `EntityManagerFactory`). Trata-se de um componente de peso pesado (*heavyweight*), projetado para possuir escopo global dentro do ciclo de vida da aplicação (padrão *Singleton*).

### 3. Por que existe?

Existe para mitigar o altíssimo custo computacional envolvido na leitura, parsing e validação de configurações de persistência. Construir um ambiente ORM requer que o framework faça o parsing de arquivos estruturados (como o `persistence.xml`), escaneie exaustivamente o *classpath* à procura de classes anotadas com `@Entity`, valide chaves, heranças e relacionamentos, resolva o dialeto SQL nativo via metadados JDBC e inicialize pools de conexão complexos. Se esse processo de compilação ocorresse a cada requisição ou transação, a aplicação sofreria gargalos extremos de CPU e latência insustentável. A `SessionFactory` encapsula todo esse estado imutável pós-inicialização para que sessões de curta duração possam ser geradas instantaneamente.

### 4. Como funciona?

No momento do arranque (*bootstrap*) do sistema, as configurações brutas da aplicação (sejam propriedades do Spring Boot ou tags do XML) são ingeridas. O Hibernate cria o `StandardServiceRegistry` para gerenciar serviços de infraestrutura (como entrega de conexões e transações) e compila os metadados das entidades (`MetadataSources`) em um mapa consolidado chamado `Metadata`.

A partir deste objeto de metadados, o `SessionFactoryBuilder` é invocado para erguer a instância definitiva da `SessionFactory`. Uma vez estabelecida na memória, a fábrica fica aguardando as demandas do código cliente. Quando a aplicação executa um bloco lógico de persistência, ela chama `sessionFactory.openSession()` (ou o Spring coordena essa entrega de forma transparente), recebendo uma `Session` leve e limpa que reaproveita instantaneamente toda a estrutura de metadados compilada pela fábrica. Ao encerrar a aplicação (*shutdown*), o método `close()` da fábrica deve ser disparado para desalocar os pools físicos e caches globais de forma limpa.

### 5. Funcionamento interno

Internamente, a `SessionFactory` gerencia subsistemas complexos que otimizam a execução do framework em runtime:

* **Metamodel (Metamodelo):** Um repositório em memória contendo o mapeamento exato de todas as classes Java para as tabelas físicas do SGBD, incluindo tipos de dados, chaves estrangeiras, restrições e estratégias de geração de ID.
* **Query Plan Cache (Cache de Planos de Consulta):** Traduzir strings de JPQL, HQL ou estruturas de Criteria em instruções SQL nativas do banco exige uma análise sintática pesada (geração de árvores abstratas - AST). Para evitar o reprocessamento, a `SessionFactory` mantém um cache interno desses planos compilados. Quando qualquer sessão executa uma query repetida, o plano é extraído do cache da fábrica instantaneamente.
* **Second-Level Cache (Cache de Segundo Nível - L2):** Enquanto a `Session` mantém um cache local e transacional (Primeiro Nível), a `SessionFactory` gerencia e coordena opcionalmente um cache global compartilhado por todas as threads da aplicação, integrando provedores corporativos (como Ehcache, Caffeine ou Infinispan) para evitar idas desnecessárias ao disco.
* **ServiceRegistry Integration:** Retém a árvore de serviços ativos do Hibernate, controlando diretamente o `ConnectionProvider` (que encapsula o pool físico do DataSource) e os `JdbcServices` (que envelopam o tratamento de exceções JDBC e o dialeto atual).
* **Imutabilidade e Concorrência:** Para ser 100% *thread-safe*, o estado interno da `SessionFactory` torna-se imutável após a sua construção. Múltiplas threads concorrentes podem ler suas propriedades concorrentemente sem causar condições de corrida (*race conditions*) ou demandar travas (*locks*) pesadas.

### 6. Quando usar?

Deve ser obrigatoriamente configurada e mantida viva em qualquer aplicação Java que utilize o ecossistema Hibernate ORM para gerenciar sua camada relacional. Ela deve ser instanciada exatamente uma única vez por base de dados/unidade de persistência (*Persistence Unit*) durante o carregamento inicial da aplicação. Em projetos Spring Boot estruturados, sua presença é automatizada através da configuração implícita do bean `LocalContainerEntityManagerFactoryBean`.

### 7. Quando NÃO usar?

* **Nunca crie instâncias locais ou dinâmicas:** Não deve ser instanciada dentro de escopos de métodos, DAOs, repositórios, ou a cada ciclo de requisição HTTP, dado o risco iminente de estouro de memória RAM (*Heap Memory OutOfMemoryError*) e exaustão de conexões físicas.
* **Aplicações JDBC puras sem mapeamento:** Se o seu sistema executa persistência puramente baseada em queries manuais escritas em SQL nativo e lidas via `ResultSet` manual (sem conversão automatizada de objetos), introduzir a complexidade de uma `SessionFactory` criará um overhead de memória e tempo de startup totalmente injustificável.

────────────────────────────

### 8. O que influencia este conceito?

* **Volume do Modelo de Entidades:** A quantidade total de classes anotadas com `@Entity` e a complexidade de suas associações aumentam linearmente o tamanho do Metamodelo residente na fábrica e o tempo de bootstrap.
* **Configurações Estruturais de Infraestrutura:** Propriedades declaradas no `persistence.xml` ou `application.properties`, tais como o dialeto do SGBD escolhido, ativação do cache L2 e dimensionamento dos limites do cache de queries.
* **Capacidade do DataSource:** O pool de conexões físicas subjacente (como o HikariCP) dita a capacidade de suprimento de conexões que a fábrica repassará para as sessões filhas.

### 9. O que este conceito influencia?

* **Desempenho Geral e Vazão (Throughput):** A agilidade na abertura de transações e a velocidade na resolução de consultas estão intrinsecamente ligadas à saúde estrutural da fábrica.
* **Pegada de Memória Permanente (Long-Term Heap Footprint):** Como permanece viva durante todo o ciclo da aplicação, o tamanho das suas estruturas internas (Metamodelo, planos acumulados e cache L2) afeta diretamente o consumo basal de RAM e a atividade do *Garbage Collector*.
* **Sintaxe do SQL Gerado:** Por deter a instância do dialeto específico do fornecedor, ela dita a formatação exata dos comandos SQL emitidos em tempo de execução.

### 10. Configurações que alteram seu comportamento

* `hibernate.query.plan_cache_max_size`: Controla o limite máximo de instruções e estruturas de queries parametrizadas armazenadas no cache de planos da fábrica (evitando estouro de memória em aplicações com volumosa geração de queries dinâmicas).
* `hibernate.cache.use_second_level_cache`: Chave booleana (`true`/`false`) que liga ou desliga a inteligência de Cache de Segundo Nível mapeada globalmente na fábrica.
* `hibernate.cache.region.factory_class`: Define a classe concreta responsável por fabricar as regiões de cache integradas (ex: `org.hibernate.cache.jcache.internal.JCacheRegionFactory`).
* `hibernate.generate_statistics`: Se configurada como `true`, a fábrica ativa um barramento de coleta de dados de performance em tempo real (contagem de sessões abertas, hits/misses de cache, queries mais lentas), crucial para monitoramento de infraestrutura.

────────────────────────────

### 11. O que a especificação (ou teoria) define?

A especificação oficial Jakarta Persistence (JPA) define o contrato da interface `jakarta.persistence.EntityManagerFactory` como a abstração oficial de uma Unidade de Persistência totalmente inicializada. A teoria dita que este objeto deve ser imutável após criado, obrigatoriamente *thread-safe* para acesso concorrente massivo, e responsável exclusivo por gerar instâncias isoladas de `EntityManager`. A especificação se isenta de padronizar componentes internos como gerenciamento de cache AST de queries ou mapeamento mecânico de dialetos, transferindo esses pormenores de engenharia para as ferramentas de implementação.

### 12. Como isso é implementado na prática?

O Hibernate implementa as diretrizes da especificação estendendo o contrato da JPA diretamente na sua interface `org.hibernate.SessionFactory`. Na prática:

* O objeto concreto que opera em memória sob o capô do seu framework é a classe `org.hibernate.internal.SessionFactoryImpl`.
* Em um código que utilize estritamente a especificação JPA através da injeção de dependência (`@PersistenceUnit EntityManagerFactory emf`), a instância injetada em tempo de execução é a própria `SessionFactoryImpl`.
* O desenvolvedor pode livremente realizar o desempacotamento (*unwrap*) do contrato padrão para acessar os métodos proprietários avançados do motor Hibernate:
```java
SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);

```



────────────────────────────

### 13. Casos especiais

* **Arquiteturas Multi-Tenant (Multi-inquilinato):** Quando o sistema precisa segmentar os dados de diferentes clientes corporativos (tenants) usando bases isoladas ou esquemas distintos, a `SessionFactory` assume uma configuração unificada especial. Ela mantém internamente um resolvedor dinâmico (`CurrentTenantIdentifierResolver`) e um provedor flexível de conexões (`MultiTenantConnectionProvider`), alternando o roteamento do banco de dados de maneira transparente no momento exato em que `openSession()` é acionado.
* **Múltiplas Unidades de Persistência simultâneas:** Cenários onde o sistema precisa interagir com bancos de dados heterogêneos simultaneamente (ex: uma base transacional Mysql e uma base de auditoria legada em Oracle) exigirão a criação de duas instâncias apartadas de `SessionFactory`. Cada uma conterá seu metamodelo isolado, seu dialeto correspondente e seu próprio pool físico de conexões.

### 14. Erros ou exceções relacionadas

* `org.hibernate.MappingException`: Disparada no startup caso a fábrica encontre inconsistências graves na compilação dos metadados (ex: propriedades de relacionamentos bidirecionais apontando para campos inexistentes ou entidades mapeadas sem um identificador `@Id` obrigatório).
* `org.hibernate.service.UnknownServiceException`: Ocorre na inicialização se a fábrica tentar consumir um serviço obrigatório (como um mecanismo de cache L2) cujas dependências ou caminhos de classe estejam ausentes ou incorretos.
* `java.lang.OutOfMemoryError: Java heap space` (Vazamento no Plan Cache): Se os desenvolvedores construírem queries dinâmicas concatenando strings brutas (ex: `where nome = '` + valor + `'`) em vez de utilizar parâmetros nomeados (*Binding Parameters*), o Hibernate interpretará cada string modificada como uma nova query estrutural. A `SessionFactory` tentará gerar e armazenar um plano único para cada variação no seu `QueryPlanCache`, expandindo linearmente o consumo de RAM até o colapso da JVM.
* `java.lang.IllegalStateException: EntityManagerFactory is closed`: Lançada se a aplicação tentar abrir uma sessão ou ler metadados da fábrica após o método `close()` ter sido executada durante o encerramento do contexto.

### 15. Modelos mentais incorretos

* **"Devo abrir e fechar a SessionFactory a cada operação de banco":** Erro de arquitetura gravíssimo. Confunde-se o ciclo de vida e peso computacional de uma `Session` (leve, descartável) com o da `SessionFactory` (pesada, perene). Inicializar uma fábrica por operação derrubará drasticamente o throughput da aplicação devido ao overhead de compilação constante.
* **"SessionFactory e EntityManagerFactory são duas coisas totalmente distintas em memória":** Mito. No ecossistema Hibernate, ambas as nomenclaturas representam rigorosamente a mesma instância física de objeto (`SessionFactoryImpl`). A distinção reside puramente na interface de contrato que seu código consome (padrão JPA vs. customizado do Hibernate).
* **"A SessionFactory atualiza seu metamodelo se eu alterar a tabela no banco dinamicamente":** Incorreto. Os metadados de uma fábrica são imutáveis e consolidados rigidamente durante o bootstrap. Mudanças estruturais em tabelas exigem um re-bootstrap ou o reinício completo do processo da aplicação.

────────────────────────────

### 16. Exemplos práticos

#### Exemplo 1: Bootstrap Programático Nativo (Java SE Clássico)

Abaixo está demonstrado como instanciar e centralizar manualmente a criação de uma `SessionFactory` de forma segura utilizando o padrão Singleton:

```java
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {
    private static final SessionFactory sessionFactory;

    static {
        // 1. Inicializa o Registro de Serviços acoplando propriedades estruturais
        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml") // Carrega arquivo estático se presente
                .applySetting("hibernate.query.plan_cache_max_size", "4096")
                .build();

        try {
            // 2. Acopla as fontes de metadados apontando as classes mapeadas
            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            metadataSources.addAnnotatedClass(com.projeto.modelo.Usuario.class);
            metadataSources.addAnnotatedClass(com.projeto.modelo.Pedido.class);

            // 3. Compila a árvore de Metadados estruturais do modelo
            Metadata metadata = metadataSources.getMetadataBuilder().build();

            // 4. Constrói a instância única e imutável da Fábrica
            sessionFactory = metadata.getSessionFactoryBuilder().build();
            
        } catch (Exception e) {
            // Em caso de falha crítica, destrói o registro para evitar vazamento de recursos
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
            throw new ExceptionInInitializerError("Falha crítica no bootstrap da SessionFactory: " + e.getMessage());
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close(); // Encerra pools e caches de forma limpa
        }
    }
}

```

#### Exemplo 2: Capturando a SessionFactory nativa em ambiente Spring Boot / JPA

Em projetos modernos que utilizam Spring Boot, a fábrica já é erguida automaticamente. Para extrair a API nativa do Hibernate, faz-se o unwrap do gerenciador padrão:

```java
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HibernateConfig {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Bean
    public SessionFactory sessionFactory() {
        // Desempacota a interface padronizada da JPA expondo o motor real do Hibernate
        SessionFactory nativeFactory = entityManagerFactory.unwrap(SessionFactory.class);
        if (nativeFactory == null) {
            throw new IllegalStateException("O provedor de persistência atual não é o Hibernate.");
        }
        return nativeFactory;
    }
}

```

### 17. Impactos e consequências

* **Se mal configurada ou mal dimensionada:** Pode gerar severos vazamentos de memória permanente (*heap space leak*) através de mau gerenciamento do Cache de Planos ou do Cache L2. Negligenciar a execução do método `close()` no encerramento da aplicação resultará em conexões JDBC órfãs presas no SGBD, inviabilizando deploys contínuos sem o restart forçado do banco de dados.
* **Se bem configurada e mantida como Singleton real:** Proporciona tempo de resposta virtualmente instantâneo para a geração de transações curtas, otimização extrema de uso do pool físico de conexões e economia drástica de CPU via reuso inteligente de estruturas de queries já mastigadas pelo compilador AST.

────────────────────────────

### 18. Fluxograma

O diagrama a seguir descreve a esteira de processamento que vai desde a leitura das configurações estáticas no startup até a instanciação da SessionFactory, contrastando o seu peso com a subsequente criação de sessões leves em runtime:

```
Propriedades de Configuração (XML / Properties)
                     │
                     ▼
         [StandardServiceRegistry] ──── (Aloca serviços base: Conexão, Dialeto, JTA)
                     │
                     ▼
            [MetadataSources] ───────── (Escaneia entidades @Entity e anotações)
                     │
                     ▼
                [Metadata] ──────────── (Compila o Metamodelo relacional em memória)
                     │
                     ▼
           [SessionFactoryBuilder]
                     │
                     ▼
      ┌─────────────────────────────────────────────────────────┐
      │          SESSIONFACTORY (Singleton - Pesado)            │
      │  - Retém o Metamodelo imutável das Entidades            │
      │  - Armazena o Query Plan Cache (Instruções AST)         │
      │  - Centraliza e gerencia o Second-Level Cache (L2)      │
      │  - Mantém referências ao Pool físico e Dialeto SGBD     │
      └─────────────────────────┬───────────────────────────────┘
                                │
               Invocação de .openSession() / Proxy
                                │
                                ▼
      ┌─────────────────────────────────────────────────────────┐
      │              SESSION (Lightweight - Leve)               │
      │  - Ciclo de vida curto (atrelado à Thread/Request)      │
      │  - Mantém o Cache de 1º Nível (Persistence Context)     │
      │  - Porta de entrada para operações transacionais CRUD  │
      └─────────────────────────────────────────────────────────┘

```

### 19. Tabela resumo

| Característica | SessionFactory (Hibernate) | Session (Hibernate) |
| --- | --- | --- |
| **Peso Computacional** | **Alto (Heavyweight):** Processo de inicialização lento, consome muita CPU e memória no startup. | **Baixo (Lightweight):** Criação extremamente rápida, barata e instantânea sob demanda. |
| **Escopo / Ciclo de Vida** | **Aplicação (Singleton):** Inicializada uma única vez no startup e destruída apenas no shutdown. | **Transacional:** Curto, escopado à thread de execução ou ciclo de request HTTP (descartada após commit/rollback). |
| **Thread-Safety** | **Sim:** Totalmente segura para acesso concorrente massivo de múltiplas threads simultâneas. | **Não (Thread-Unsafe):** Nunca deve ser compartilhada entre threads. Escopo estritamente local. |
| **Finalidade Principal** | Atuar como Fábrica de Sessions (consequentemente, de conexões), reter metadados compilados, Query Plan Cache e Cache L2. | Atuar como contexto de trabalho ativo para execução de operações CRUD e controle de transações. |
| **Custo de Criação** | Elevadíssimo (Parsing de arquivos, scan de classes, conexões com banco). | Praticamente nulo (Apenas aloca um mapa de persistência raso e solicita uma conexão aquecida do pool). |

### 20. Checklist mental

* [ ] Minha `SessionFactory` está de fato configurada como um Singleton real na aplicação, blindada contra reinstanciações acidentais por classes acessórias?
* [ ] Configurei o limite de tamanho máximo do `QueryPlanCache` condizente com o teto de memória RAM do ambiente para mitigar riscos de vazamento por queries dinâmicas?
* [ ] Amarrei a execução do método `close()` da fábrica de forma inequívoca aos ganchos (*hooks*) de desligamento ordenado do servidor da aplicação?
* [ ] Se estou trabalhando em ecossistema Spring Boot, compreendo que o `EntityManagerFactory` injetado pelo Spring é, por baixo dos panos, a minha própria `SessionFactory` e evito criar fábricas paralelas nativas?
* [ ] Certifiquei-me de que todas as queries construídas em laços de repetição de alta frequência utilizam parâmetros nomeados (`.setParameter()`) em vez de concatenação de strings brutas para não poluir o cache da fábrica?

### 21. Conceitos relacionados

* **Persistence Unit (Unidade de Persistência):** A barreira lógica declarativa que define o conjunto de entidades e configurações que servirão de insumo para compilar a fábrica.
* **ServiceRegistry:** O barramento hierárquico modularizado de serviços (JDBC, transações) que a fábrica consome para interagir com a infraestrutura do sistema.
* **EntityManagerFactory:** A interface padronizada pela especificação JPA cujo contrato a `SessionFactory` herda e implementa nativamente.
* **Query Plan Cache:** Subsistema interno da fábrica responsável por reter e reaproveitar os planos de execução compilados em formato de árvore sintática (AST).