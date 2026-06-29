### Tópico: Metadados de Mapeamento (Mapping Metadata)

**1. Objetivo**
Declarar explicitamente (ou implicitamente via convenções) a estrutura, os relacionamentos e as restrições de um modelo de domínio, permitindo que o provedor de persistência (Hibernate) realize a tradução objeto-relacional (ORM) e valide a integridade estrutural entre Java e o SGBD.

**2. O que é?**
É o conjunto de definições que descreve a estrutura dos dados. No ecossistema JPA/Hibernate, estes metadados podem ser fornecidos de duas formas:

* **Anotações (Annotations):** A forma mais comum (JPA API `jakarta.persistence.*`), intrínseca ao código Java (`@Entity`, `@Table`, `@Column`, `@Id`).
* **XML:** Arquivos de mapeamento externos (`orm.xml` ou específicos do Hibernate `*.hbm.xml`), úteis para configurações que não podem ser alteradas no código fonte.

**3. Por que existe?**
Para resolver o *Impedance Mismatch*. O Hibernate não consegue "adivinhar" que a classe `Usuario` deve ser persistida na tabela `TB_USER` ou que o campo `nome` tem um limite de 255 caracteres. Os metadados fornecem a "bula" que o framework utiliza para construir o *Metamodel* interno.

**4. Como funciona?**
Durante o *Bootstrap* (inicialização da `EntityManagerFactory`), o Hibernate escaneia as classes anotadas (ou lê os arquivos XML). Ele interpreta essas instruções, valida se fazem sentido (ex: uma entidade sem `@Id` lançará erro) e constrói o *Metamodel* em memória, que é o mapa utilizado em tempo de execução para gerar SQL.

**5. Funcionamento interno**
O Hibernate transforma os metadados em uma estrutura de dados interna chamada *Metamodel*. Esta estrutura contém o mapeamento de propriedades de classes para colunas de banco, estratégias de geração de ID, definições de *fetch types* e restrições de unicidade. É a "tradução" do que o programador escreveu para o que o motor entende.

**6. Quando usar?**
Sempre. Toda entidade, componente ou coleção que precise ser gerenciada pela persistência deve ter metadados definidos, seja via anotações explícitas ou através de convenções padrão da JPA.

**7. Quando NÃO usar?**
Classes que são puramente DTOs ou objetos de domínio que não devem ser persistidos no banco de dados não devem conter metadados de mapeamento JPA.

**8. O que influencia este conceito?**

* **Arquitetura do Modelo de Domínio:** O design das suas classes.
* **Especificação JPA:** Define quais anotações são válidas (standard).
* **Versão do Hibernate:** Novas versões podem suportar novas anotações ou alterar o comportamento de convenções padrão.

**9. O que este conceito influencia?**

* **Geração de Schema (`hbm2ddl`):** Se permitido, o Hibernate cria as tabelas baseando-se nestes metadados.
* **SQL Gerado:** A complexidade das *queries* e os *joins* são determinados pelo mapeamento definido.
* **Validação de Runtime:** Erros de mapeamento são detectados no *startup* do sistema.

**10. Configurações que alteram seu comportamento**

* `hibernate.mapping.precedence`: Define a precedência entre XML e Anotações.
* `hibernate.implicit_naming_strategy`: Define como o Hibernate nomeia colunas/tabelas se você não as declarar.
* `hibernate.physical_naming_strategy`: Como os nomes lógicos do Java são convertidos para nomes físicos no banco.

**11. O que a especificação (ou teoria) define?**
Define os contratos (interfaces e anotações) no pacote `jakarta.persistence`. A teoria ORM dita que o mapeamento deve ser transparente ao modelo de negócio, mas rigoroso na estrutura.

**12. Como isso é implementado na prática?**
Através da leitura dos *classpaths* e reflexão (Reflection API). O Hibernate usa refletores para inspecionar classes, ler anotações de tempo de execução (Runtime Annotations) e processar as restrições ali contidas.

**13. Casos especiais**

* **XML Overriding:** Você pode definir um mapeamento por anotação e "sobrescrevê-lo" via `orm.xml` sem recompilar o código (útil em ambientes de produção para *hotfixes*).
* **@MappedSuperclass:** Herança de mapeamento onde a classe pai não é uma entidade, mas provê metadados para as filhas.

**14. Erros ou exceções relacionadas**

* `AnnotationException`: Erro ao processar uma anotação (ex: falta de `@Id`).
* `MappingException`: Erro na montagem do mapa de persistência.
* `PropertyNotFoundException`: Erro ao tentar mapear um atributo que não existe no POJO.

**15. Modelos mentais incorretos**

* "Anotações são apenas comentários": Errado. Elas são diretrizes de configuração cruciais.
* "Posso mudar o nome da coluna no banco e o Hibernate descobre sozinho": Errado. Se você mudar a coluna no banco, deve atualizar os metadados (ou o mapeamento) sob risco de `SQLGrammarException`.

**16. Exemplos práticos**

```java
@Entity
@Table(name = "TB_USUARIO")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nm_usuario", nullable = false)
    private String nome;
}

```

**17. Impactos e consequências**

* **Bem configurado:** Garantia de integridade, mapeamento performático e código limpo.
* **Mal configurado:** "Dangling pointers" entre Java e SQL, colunas que não mapeiam corretamente, perda de dados ou falhas de inicialização que impedem o sistema de subir.

**18. Fluxograma**
`Código (Annotations/XML)` → `Bootstrap` → `Scanning` → `Processamento de Metadados` → `Metamodel` → `Schema Validation/SQL Generation`.

**19. Tabela resumo**

| Característica | Annotations | XML (orm.xml) |
| --- | --- | --- |
| **Localização** | Dentro do código Java | Arquivo externo (META-INF) |
| **Manutenção** | Fácil, próxima ao código | Exige edição de arquivo separado |
| **Flexibilidade** | Baixa (exige recompile) | Alta (permite mudanças sem recompile) |
| **Uso Principal** | Configurações padrão/estáveis | Sobrescrita de comportamento/Hotfixes |

**20. Checklist mental**

* [ ] As minhas entidades possuem um `@Id` único definido?
* [ ] O mapeamento reflete fielmente as restrições do banco (nullability, length)?
* [ ] Estou utilizando a *Naming Strategy* correta para evitar conflitos de nomes reservados?
* [ ] Em caso de herança, usei a estratégia de mapeamento correta (`@Inheritance`)?

**21. Conceitos relacionados**

* **Metamodel API:** A API que o JPA fornece para consultar metadados programaticamente.
* **Reflection:** A tecnologia base que o Hibernate usa para ler estes metadados.
* **Persistence Unit:** O escopo que agrupa todos esses metadados.