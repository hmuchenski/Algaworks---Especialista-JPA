### Tópico: Ciclo de Inicialização do Hibernate (Bootstrap)

**1. Objetivo**
Estabelecer a ponte entre a aplicação e o banco de dados, inicializando a `EntityManagerFactory` (que encapsula a `SessionFactory`), o que envolve carregar configurações, validar o mapeamento de entidades, configurar o `ServiceRegistry` e preparar o pool de conexões.

**2. O que é?**
É o processo de *bootstrapping* do provedor de persistência (Hibernate). É a fase em que o Hibernate lê as configurações (seja do `persistence.xml` ou `application.properties`), escaneia as classes anotadas com `@Entity` e compila o metadado que será utilizado em tempo de execução.

**3. Por que existe?**
A JPA não pode simplesmente começar a salvar objetos; ela necessita de um contexto configurado. Este processo existe para que o custo de processamento (leitura de XML, validação de mapeamentos, detecção de dialeto, criação do *metamodel*) ocorra apenas uma vez no *startup* da aplicação, evitando latência em cada transação.

**4. Como funciona?**
Através da classe `jakarta.persistence.Persistence`, a aplicação invoca `createEntityManagerFactory`. O Hibernate entra em cena, lê a unidade de persistência, constrói o `ServiceRegistry` (barramento de serviços) e compila a fábrica.

**5. Funcionamento interno**
O processo segue uma ordem hierárquica rigorosa:

* **Leitura de Metadados:** Processamento de anotações e arquivos XML.
* **ServiceRegistry:** Criação do ambiente de serviços que gerencia JDBC, transações e JNDI.
* **MetadataSources:** Consolidação de todas as classes de entidade.
* 
**SessionFactoryImpl:** A compilação final que gera o *metamodel* e os planos de execução de consulta (Query Plan Cache).



**6. Quando usar?**
Sempre no início do ciclo de vida da aplicação ( *startup*).

**7. Quando NÃO usar?**
Nunca durante o processamento de uma requisição. Criar uma `SessionFactory` (ou `EntityManagerFactory`) dentro de um método de *controller* ou serviço é um erro grave de arquitetura ( *rebootstrapping*), causando perda de performance e estouro de memória.

**8. O que influencia este conceito?**

* Arquivos de configuração (`persistence.xml`, `application.properties`).
* O *classpath* (onde as entidades são escaneadas).
* O *JDBC Driver* presente no ambiente.

**9. O que este conceito influencia?**
Diretamente a estabilidade do sistema, o tempo de inicialização (*slow startup*) e a integridade da validação das entidades antes mesmo da primeira *query* ser executada.

**10. Configurações que alteram seu comportamento**

* `hibernate.hbm2ddl.auto`: Define o que fazer com o esquema do banco durante o *startup*.
* `javax.persistence.provider`: Define a implementação a ser usada.

**11. O que a especificação define?**
Define a interface `Persistence.createEntityManagerFactory` e o contrato de uma `EntityManagerFactory` como o ponto de entrada único para o ecossistema.

**12. Como isso é implementado na prática?**
No Hibernate, a `SessionFactory` (a implementação da `EntityManagerFactory`) utiliza um padrão de projeto *Singleton* para garantir que todos os metadados sejam compilados uma única vez.

**13. Casos especiais**

* **JTA vs RESOURCE_LOCAL:** O ciclo de inicialização difere se o container (EE) está gerenciando as transações ou se a aplicação (SE/Boot) está no controle.

**14. Erros ou exceções relacionadas**

* `PersistenceException`: Falha genérica durante o bootstrap.
* `ClassNotFoundException`: Provedor não encontrado no *classpath*.
* Erros de mapeamento (`AnnotationException`): Detectados durante a compilação do *metamodel*.

**15. Modelos mentais incorretos**
Achar que o *bootstrap* é um processo "leve". Ele é um processo *heavyweight* (pesado) e caro.

**16. Exemplos práticos**

```java
// Em Java SE
EntityManagerFactory emf = Persistence.createEntityManagerFactory("meu-pu");

```

**17. Impactos e consequências**

* **Mal configurado:** Tempo de resposta inicial lento, consumo excessivo de memória, falhas catastróficas de *deploy*.
* 
**Bem configurado:** Inicialização rápida, pool de conexões pronto, mapeamento validado em tempo de desenvolvimento.



**18. Fluxograma**
Aplicação → `Persistence.createEntityManagerFactory` → Provedor (Hibernate) → Leitura XML/Annotations → `MetadataSources` → `ServiceRegistry` → Conexão JDBC → `SessionFactory` (Ready).

**19. Tabela resumo**

| Característica | Bootstrap (Java SE) | Bootstrap (Container/EE) |
| --- | --- | --- |
| **Responsabilidade** | Desenvolvedor | Servidor de Aplicação |
| **Ciclo de Vida** | Manual (Open/Close) | Gerenciado automaticamente |
| **Configuração** | `persistence.xml` | `persistence.xml` + JNDI |

**20. Checklist mental**

* [ ] O `persistence-unit` está com o nome correto?
* [ ] O driver JDBC está incluído no projeto?
* [ ] O `persistence.xml` está no diretório `META-INF/`?
* [ ] Estou injetando o `EntityManagerFactory` (Singleton) em vez de criar um novo por requisição? 



**21. Conceitos relacionados**

* 
**Persistence Unit:** O escopo configurado no bootstrap.


* 
**ServiceRegistry:** O barramento interno de serviços da fábrica.


* 
**Metamodel:** O mapa das entidades gerado durante o bootstrap.