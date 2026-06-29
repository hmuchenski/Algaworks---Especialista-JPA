### Tópico: TransactionType (RESOURCE_LOCAL e JTA)

### 1. Objetivo

Definir a estratégia de gerenciamento e demarcação de transações que a Unidade de Persistência adotará, estabelecendo se o controle do ciclo de vida das transações será feito manualmente pela aplicação (RESOURCE_LOCAL) ou delegado ao servidor de aplicação/container (JTA).

### 2. O que é?

O `TransactionType` é um atributo configurável na definição de uma `persistence-unit` (no arquivo `persistence.xml` ou programaticamente). Ele aceita dois valores definidos pela especificação Jakarta Persistence:

* **RESOURCE_LOCAL**: Transações nativas do recurso (diretamente no JDBC/Banco de Dados).
* **JTA (Java Transaction API)**: Transações distribuídas e gerenciadas globalmente por um gerenciador de transações do ecossistema Jakarta EE.

### 3. Por que existe?

Existe para permitir que a mesma especificação JPA funcione em arquiteturas radicalmente distintas: desde microsserviços leves e aplicações Java SE (onde não há um container complexo) até grandes sistemas corporativos monolíticos que exigem coordenação atômica de operações envolvendo múltiplos bancos de dados distintos, filas de mensageria (JMS) ou múltiplos sistemas na mesma transação.

### 4. Como funciona?

A escolha do `TransactionType` altera o comportamento de como o `EntityManager` obtém e lida com as transações:

* No **RESOURCE_LOCAL**, a aplicação interage explicitamente com a interface `EntityTransaction` via `entityManager.getTransaction()`.
* No **JTA**, o `EntityManager` se integra ao gerenciador de transações do container. O controle é feito geralmente via anotações declarativas (como `@Transactional`) ou pela interface `UserTransaction`. Tentar chamar `entityManager.getTransaction()` em uma unidade configurada como JTA causará uma exceção imediata.

### 5. Funcionamento interno

* **RESOURCE_LOCAL**: O Hibernate Core encapsula uma conexão JDBC tradicional. Quando `transaction.begin()` é invocado, o Hibernate executa um `setAutoCommit(false)` na conexão JDBC subjacente. Ao fazer o `commit()`, o comando `connection.commit()` é enviado ao driver JDBC.
* **JTA**: O Hibernate atua como um participante em uma transação global orquestrada por um *Transaction Manager* (como Narayana, Atomikos ou o gerenciador embutido no JBoss/WildFly). O processo utiliza commits de duas fases (Two-Phase Commit - 2PC) para garantir a atomicidade global, registrando o `EntityManager` como um recurso sincronizado no ciclo de vida da transação global.

### 6. Quando usar?

* **RESOURCE_LOCAL**: Aplicações Spring Boot padronizadas (que gerenciam transações em nível de aplicação), aplicações Java SE puras, ferramentas de linha de comando (CLI) ou arquiteturas de microsserviços simples com um único banco de dados relacional.
* **JTA**: Aplicações implantadas em servidores de aplicação Full Jakarta EE (WildFly, WebSphere, WebLogic) ou sistemas distribuídos complexos onde uma única operação de negócio precisa garantir que a gravação no Banco A e o envio de uma mensagem na Fila B funcionem em esquema de "tudo ou nada".

### 7. Quando NÃO usar?

* **RESOURCE_LOCAL**: Nunca use se você precisar realizar transações distribuídas (XA transactions) entre múltiplos bancos de dados ou coordenar o banco com sistemas de mensageria na mesma transação.
* **JTA**: Não use em aplicações puras Java SE ou ambientes de microsserviços altamente simplificados devido ao alto overhead de performance do protocolo 2PC (Two-Phase Commit) e à complexidade de configuração fora de um container.

---

### 8. O que influencia este conceito?

* O ambiente de execução (Java SE vs. Jakarta EE Profile / Container).
* A infraestrutura de banco de dados (se suporta e está configurada para conexões XA).
* O tipo de `EntityManager` utilizado (gerenciado pelo container ou pela aplicação).

### 9. O que este conceito influencia?

* A forma como as conexões são adquiridas do Pool de Conexões.
* O ciclo de vida e o escopo do `Persistence Context` (Extended vs. Transaction-scoped).
* Quais métodos e interfaces do JPA são válidos ou geram exceções em runtime.

### 10. Configurações que alteram seu comportamento

* Atributo `transaction-type` na tag `<persistence-unit>` do `persistence.xml`.
* Configuração da propriedade `hibernate.transaction.jta.platform` para especificar qual servidor de aplicação está fornecendo o gerenciador JTA.
* A alteração entre `javax.sql.DataSource` (para JTA/Managed) e propriedades diretas de driver JDBC (frequente em RESOURCE_LOCAL).

---

### 11. O que a especificação (ou teoria) define?

A especificação do Jakarta Persistence dita que, por padrão:

* Em ambientes Java EE / Jakarta EE, o valor default de `transaction-type` é `JTA`.
* Em ambientes Java SE, o valor default é `RESOURCE_LOCAL`.
A especificação proíbe terminantemente o uso simultâneo de estratégias de controle manual em contextos declarados como gerenciados globalmente.

### 12. Como isso é implementado na prática?

No `persistence.xml`:

```xml
<persistence-unit name="meuPU_Local" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <non-jta-data-source>java:comp/env/jdbc/MeuDBLocal</non-jta-data-source>
</persistence-unit>

<persistence-unit name="meuPU_JTA" transaction-type="JTA">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <jta-data-source>java:jboss/datasources/MeuDBGlobal</jta-data-source>
</persistence-unit>

```

---

### 13. Casos especiais

Aplicações Spring Boot que utilizam `RESOURCE_LOCAL` por baixo dos panos, mas mascaram isso fornecendo uma abstração declarativa semelhante ao JTA através do seu próprio `@Transactional`. O Spring gerencia o `RESOURCE_LOCAL` simulando uma transação declarativa global em nível de aplicação.

### 14. Erros ou exceções relacionadas

* `java.lang.IllegalStateException: A JTA EntityManager cannot use getTransaction()`: Ocorre quando o desenvolvedor tenta chamar `em.getTransaction().begin()` em uma unidade de persistência configurada como `transaction-type="JTA"`.
* `PersistenceException: Connection JDBC ... setAutoCommit(true) failed`: Pode acontecer ao tentar forçar comportamentos locais em conexões controladas pelo escopo global JTA.

### 15. Modelos mentais incorretos

* *Incorrecto:* "Anotar meu método com `@Transactional` significa que estou obrigatoriamente usando JTA".
* *Correto:* Frameworks como Spring conseguem interceptar e gerenciar transações `RESOURCE_LOCAL` de forma declarativa sem usar a infraestrutura JTA pesada.


* *Incorreto:* "JTA é sempre melhor porque resolve tudo automaticamente".
* *Correto:* JTA adiciona complexidade e perda severa de vazão e performance devido ao overhead de coordenação global. Só deve ser usado sob real necessidade técnica de distribuição.



---

### 16. Exemplos práticos

**Demarcação Manual (RESOURCE_LOCAL):**

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("meuPU_Local");
EntityManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();

try {
    tx.begin();
    Produto produto = em.find(Produto.class, 1L);
    produto.setPreco(150.00);
    tx.commit();
} catch (Exception e) {
    if (tx.isActive()) tx.rollback();
} finally {
    em.close();
}

```

**Demarcação Gerenciada (JTA com injeção de dependência e controle declarativo):**

```java
@Stateless // Componente EJB/Jakarta EE
public class ProdutoService {

    @PersistenceContext(unitName = "meuPU_JTA")
    private EntityManager em;

    // Transação inicia e encerra automaticamente nas bordas do método
    public void atualizarPreco(Long id, Double novoPreco) {
        Produto produto = em.find(Produto.class, id);
        produto.setPreco(novoPreco);
    }
}

```

---

### 17. Impactos e consequências

* **RESOURCE_LOCAL bem ajustado:** Máxima performance local, controle cirúrgico do desenvolvedor sobre o ciclo de vida das transações, isolamento de problemas de conexão localizados.
* **RESOURCE_LOCAL mal ajustado:** Vazamentos de conexões (se esquecer de fechar o `EntityManager` ou dar `rollback` em falhas), falta de sincronismo em operações distribuídas.
* **JTA bem ajustado:** Segurança total transacional em ambientes heterogêneos corporativos, código limpo sem boilerplate de try/catch/rollback.
* **JTA mal ajustado:** Gargalos de performance globais causados pelo mecanismo de trava (locking) do 2PC, transações longas bloqueando recursos cruciais do servidor.

---

### 18. Fluxograma

```
RESOURCE_LOCAL:
Aplicação ──> em.getTransaction().begin() ──> Hibernate Core ──> Conexão JDBC (Local DB)

JTA:
Aplicação ──> Interceptor (@Transactional) ──> Transaction Manager ──> Coordenador XA ──> Múltiplos Recursos (DB / JMS)

```

---

### 19. Tabela resumo

| Característica | RESOURCE_LOCAL | JTA |
| --- | --- | --- |
| **Ambiente de Origem** | Java SE / Spring Boot nativo | Jakarta EE Containers / Servidores de Aplicação |
| **Controle de Transação** | Manual (via `EntityTransaction`) | Declarativo (`@Transactional`) ou Programático Global |
| **Múltiplos Recursos** | Não suporta de forma nativa (apenas 1 DB) | Suporta nativamente (Transações distribuídas XA) |
| **Interface de Controle** | `jakarta.persistence.EntityTransaction` | `jakarta.transaction.UserTransaction` / Injeção |
| **Overhead** | Mínimo / Alta Performance | Alto (Devido ao gerenciamento global e 2PC) |

---

### 20. Checklist mental

* [ ] O ambiente de deploy final possui um Transaction Manager corporativo? (Se não, use `RESOURCE_LOCAL`).
* [ ] Minha transação precisa tocar mais de um banco de dados ou integrar com mensageria atomicamente? (Se sim, use `JTA`).
* [ ] Se usei `JTA`, certifiquei-me de remover chamadas para `em.getTransaction()` do código legível?
* [ ] No arquivo `persistence.xml`, as tags `<jta-data-source>` ou `<non-jta-data-source>` estão coerentes com o `transaction-type` escolhido?

---

### 21. Conceitos relacionados

* **XA Transactions**: O padrão de protocolo que permite transações distribuídas (essencial sob o escopo JTA).
* **Two-Phase Commit (2PC)**: O algoritmo interno usado por gerenciadores JTA para garantir atomicidade em múltiplos nós.
* **Persistence Context**: O ciclo de vida do contexto de persistência estende-se ou vincula-se diretamente ao ciclo da transação ativa.