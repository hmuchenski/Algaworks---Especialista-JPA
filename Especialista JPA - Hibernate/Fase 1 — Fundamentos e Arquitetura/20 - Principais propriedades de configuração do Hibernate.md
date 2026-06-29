### Tópico: Principais Propriedades de Configuração do Hibernate

**1. Objetivo**
Personalizar o comportamento do provedor de persistência (Hibernate) para adaptar a aplicação ao ambiente de execução, otimizar performance (IO e CPU) e definir a estratégia de gerenciamento do esquema do banco de dados.

**2. O que é?**
São elementos de configuração definidos como pares chave-valor. No `persistence.xml`, ficam dentro da tag `<properties>`. No `application.properties` do Spring Boot, são declaradas sob os prefixos `spring.jpa.properties.hibernate.*` ou `spring.jpa.*`.

**3. Por que existe?**
Para permitir o desacoplamento entre o código Java e a infraestrutura. Você altera o dialeto, o pool de conexões ou a estratégia de log alterando apenas o arquivo de configuração, sem precisar recompilar o código fonte.

**4. Como funciona?**
Durante o *bootstrap*, o Hibernate lê essas propriedades e as injeta no seu ambiente de persistência, configurando os serviços internos (como o `DialectResolver` ou o `BatchingConnectionCoordinator`) antes da inicialização da `SessionFactory`.

**5. Funcionamento interno**
As propriedades alimentam o `ServiceRegistry`. Por exemplo, `hibernate.dialect` orienta o *SQL Translator*, enquanto `hibernate.jdbc.batch_size` orienta o *BatchingConnectionCoordinator* para agrupar comandos SQL antes de enviá-los ao driver JDBC.

**6. Quando usar?**
Sempre que precisar ajustar o comportamento padrão. Exemplos: trocar o dialeto ao mudar de banco (ex: de H2 para PostgreSQL), ativar logs para debug ou configurar otimizações de inserção em lote.

**7. Quando NÃO usar?**
Não use propriedades obscuras ou experimentais em ambientes de produção sem testes de performance rigorosos. Evite também configurar propriedades de desenvolvimento (como `hbm2ddl.auto=update`) em ambientes produtivos.

**8. O que influencia este conceito?**
O `DataSource` (define a fonte física), o driver JDBC (versão do protocolo) e a `Persistence Unit` (escopo da configuração).

**9. O que este conceito influencia?**
O consumo de memória (cache), o tráfego de rede (batching), o tempo de inicialização (scanning de entidades) e a estrutura final do banco de dados (DDL).

**10. Configurações que alteram seu comportamento**

* `hibernate.dialect`: Define a tradução de SQL.
* `hibernate.hbm2ddl.auto`: Define se o Hibernate cria/valida o schema (validate, update, none).
* `hibernate.jdbc.batch_size`: Controla o tamanho dos lotes de comandos SQL.
* 
`hibernate.show_sql` / `hibernate.format_sql`: Controle de log de consultas.



**11. O que a especificação (ou teoria) define?**
A JPA define contratos de configuração para o `persistence.xml`, mas o Hibernate estende isso com centenas de propriedades proprietárias (prefixo `hibernate.*`) para tuning fino.

**12. Como isso é implementado na prática?**
Através de leitura direta das propriedades no `Persistence.createEntityManagerFactory()` ou via injeção de dependência no Spring Boot que mapeia `application.properties` para o *Configuration* do Hibernate.

**13. Casos especiais**
Propriedades de otimização como `batch_size` falham silenciosamente se o dialeto não for compatível ou se a entidade usar `IDENTITY` para geração de ID (pois isso desabilita o batching).

**14. Erros ou exceções relacionadas**
`ClassNotFoundException` (provider não encontrado), erros de tradução SQL (dialeto incorreto) ou `OutMemoryError` (se o *batching* estiver descontrolado).

**15. Modelos mentais incorretos**
Achar que `hbm2ddl.auto=update` é uma forma válida de gerenciar migrações de banco em produção (é perigoso e instável).

**16. Exemplos práticos**

```properties
# Exemplo em application.properties
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.hibernate.ddl-auto=validate

```

**17. Impactos e consequências**

* 
*Bem configurado:* Aplicação performática, sem gargalos de conexão e com SQL otimizado.


* 
*Mal configurado:* Degradação de performance, N+1 queries não otimizadas por falta de batching ou erros de corrupção de schema.



**18. Fluxograma**
`Arquivo de Configuração` → `ServiceRegistry` → `SessionFactory` (compilação das propriedades) → `EntityManager` (aplicação das regras em runtime).

**19. Tabela resumo**

| Propriedade | Função | Risco |
| --- | --- | --- |
| `hibernate.dialect` | Tradução SQL | Baixo (se correto) |
| `hbm2ddl.auto` | Gerenciamento de Schema | **Crítico em Produção** |
| `jdbc.batch_size` | Performance de escrita | Médio (depende do ID) |

**20. Checklist mental**

* [ ] `hibernate.hbm2ddl.auto` está como `validate` ou `none` em produção?
* [ ] O `batch_size` foi configurado e testado se a estratégia de ID permite?
* [ ] Os logs de `show_sql` estão desativados em ambiente produtivo?
* [ ] As propriedades com prefixo `hibernate.*` foram verificadas quanto a erros de digitação? (elas falham silenciosamente) .



**21. Conceitos relacionados**

* *Execution Plan Cache:* Beneficiado pelo *padding* de cláusulas IN via configurações.
* *Sequence Allocation Size:* Atua simbioticamente com o `batch_size`.
* *Connection Provider:* Gerenciado pelas configurações de Datasource.