### Tópico: Persistence Unit vs. Persistence Context

#### 1. Objetivo

Estabelecer a fronteira entre a definição estática (configuração) e o ambiente de execução (runtime), garantindo que você saiba exatamente onde configurar o comportamento (PU) e onde manipular os dados (PC).

#### 2. O que é?

* **Persistence Unit (PU):** É uma configuração nomeada, definida no `persistence.xml` (ou programaticamente), que agrupa as entidades, configurações de conexão e propriedades do provedor.
* **Persistence Context (PC):** É a "área de trabalho" temporária, um conjunto de instâncias de entidades gerenciadas, vinculadas a um `EntityManager` (JPA) ou `Session` (Hibernate) durante uma transação.

#### 3. Por que existe?

Para separar a infraestrutura (o ambiente/regras) da execução (a transação/operação). Sem essa separação, não haveria como garantir que configurações globais não interfiram na integridade de transações isoladas.

#### 4. Como funciona?

O `EntityManagerFactory` (produto da PU) é criado uma vez no *startup*. Quando você precisa de uma operação, você solicita um `EntityManager`. Ao iniciar uma transação no `EntityManager`, ele abre ou associa um `Persistence Context`.

#### 5. Funcionamento interno

A PU define os metadados (quem é a entidade, qual o dialeto). O PC utiliza esses metadados para realizar o *Dirty Checking*, mantendo um snapshot (cópia de segurança) do estado das entidades para comparar com o banco no final da transação.

#### 6. Quando usar?

* **PU:** Quando você precisa alterar configurações de banco, pool de conexões ou adicionar/remover classes de entidade.
* **PC:** Quando você precisa realizar operações de CRUD (persist, merge, find).

#### 7. Quando NÃO usar?

Nunca tente usar o `Persistence Context` como um cache de longa duração (como um banco de dados em memória persistente). Ele é efêmero e transacional.

#### 8. O que influencia este conceito?

* **PU:** O arquivo `persistence.xml` e a classe `EntityManagerFactory`.
* **PC:** A interface `EntityManager`, a transação ativa (`JTA` ou `RESOURCE_LOCAL`) e o ciclo de vida das entidades.

#### 9. O que este conceito influencia?

* **PU:** Performance de startup (quanto mais entidades, mais lento o escaneamento).
* **PC:** Performance de runtime e uso de memória (entidades gerenciadas consomem memória RAM do JVM).

#### 10. Configurações que alteram seu comportamento

* **PU:** `hibernate.hbm2ddl.auto`, propriedades de cache de segundo nível, pool de conexões.
* **PC:** `FlushMode` (quando o PC sincroniza com o banco) e bloqueios de transação.

#### 11. O que a especificação (ou teoria) define?

A especificação Jakarta Persistence define que a PU é o escopo lógico da aplicação, enquanto o PC é o contrato de monitoramento de estado obrigatório para garantir a integridade dos dados durante uma transação.

#### 12. Como isso é implementado na prática?

A `SessionFactory` (Hibernate) compila a PU em um ServiceRegistry pesado. O `EntityManager` (Hibernate Session) instancia o `PersistenceContext` (ActionQueue, First Level Cache) para cada unidade de trabalho.

#### 13. Casos especiais

* **Extended Persistence Context:** O PC sobrevive a múltiplas transações (comum em estados de conversação), o que aumenta drasticamente o risco de *Memory Leaks*.

#### 14. Erros ou exceções relacionadas

* `LazyInitializationException`: Ocorre quando você tenta acessar uma entidade fora do `Persistence Context` (que já foi fechado).
* `PersistenceException`: Erros genéricos de configuração da `Persistence Unit`.

#### 15. Modelos mentais incorretos

Acreditar que o `Persistence Context` é persistente. Ele é volátil; se a transação acaba, o contexto é descartado (a menos que a entidade seja detached).

#### 16. Exemplos práticos

```java
// A Fábrica (Persistence Unit - Configuração)
EntityManagerFactory emf = Persistence.createEntityManagerFactory("minha-pu");

// A Sessão (Persistence Context - Execução)
EntityManager em = emf.createEntityManager(); // O PC nasce aqui
em.getTransaction().begin();
// Operações ocorrem dentro do PC
em.getTransaction().commit();
em.close(); // O PC morre aqui

```

#### 17. Impactos e consequências

* **PU mal configurada:** Erros de startup, "Entity not found".
* **PC mal gerenciado:** `LazyInitializationException` ou estouro de memória (entidades demais no contexto).

#### 18. Fluxograma

`Persistence.xml` → `Persistence Unit` → `EntityManagerFactory` → (criação) → `EntityManager` → `Persistence Context` → `Database`.

#### 19. Tabela resumo

| Característica | Persistence Unit (PU) | Persistence Context (PC) |
| --- | --- | --- |
| **Escopo** | Global (Aplicação) | Transacional (Requisito) |
| **Vida** | Desde o startup até o shutdown | Apenas durante a transação |
| **Natureza** | Configuração Estática | Cache/Área de Trabalho Dinâmica |
| **Responsabilidade** | Definir regras/conectividade | Gerenciar estados das entidades |

#### 20. Checklist mental

* [ ] O `persistence-unit` está com o nome correto no `persistence.xml`? (PU)
* [ ] O `EntityManager` foi fechado após o uso para liberar o `Persistence Context`? (PC)
* [ ] Entendo que o PC é onde reside o *Dirty Checking*? (PC)

#### 21. Conceitos relacionados

* **EntityManagerFactory:** A fábrica da PU.
* **EntityManager:** O gerente do PC.
* **First Level Cache:** Outro nome para o PC.
* **Flush:** O momento em que o PC sincroniza com o banco.
