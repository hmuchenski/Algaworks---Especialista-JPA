# Tópico: ORM (Object-Relational Mapping)

### 1. Objetivo

O objetivo fundamental do ORM no ecossistema JPA/Hibernate é **abstrair o modelo de dados relacional** (tabelas, chaves estrangeiras, colunas) e apresentá-lo como um **modelo de objetos orientados a objetos** (classes, instâncias, relacionamentos de herança e composição), permitindo a persistência automática dos estados dos objetos no banco de dados.

### 2. O que é?

É uma técnica de programação que automatiza a conversão de dados entre sistemas de tipos incompatíveis (Orientação a Objetos vs. Modelo Relacional). No JPA, isso é alcançado através de metadados (anotações ou XML) que descrevem como uma classe Java (e seus atributos) se mapeia para uma estrutura de banco de dados.

### 3. Por que existe?

Existe para resolver o chamado **"Impedance Mismatch"** (Descompasso de Impedância). Objetos possuem identidade, comportamento e encapsulamento; bancos de dados relacionais baseiam-se em valores, relações tabulares e normalização rígida. O ORM resolve isso para evitar que o desenvolvedor tenha que escrever manualmente SQL boilerplate para operações CRUD.

### 4. Como funciona?

Ele atua como uma camada intermediária. Quando você interage com um objeto no Java, o Hibernate (a implementação do JPA) traduz essas operações em comandos SQL apropriados. Ele mantém um **Persistence Context** (cache de primeiro nível) que rastreia o estado de cada objeto carregado.

### 5. Funcionamento interno

O Hibernate utiliza **Reflexão (Reflection)** para ler as anotações das classes e **Proxying** (ou Bytecode Enhancement) para implementar funcionalidades como *Lazy Loading* (carregamento sob demanda). Ele cria um "mapa" em memória das metadados da entidade que orienta a geração do SQL em tempo de execução.

### 6. Quando usar?

* Sistemas de alta complexidade de domínio (DDD).
* Aplicações onde a produtividade e a manutenção do código são prioridades.
* Cenários onde o modelo de dados é rico em relacionamentos complexos.

### 7. Quando NÃO usar?

* Relatórios analíticos massivos (que demandam leitura de milhões de linhas de uma só vez).
* Cenários onde você precisa de otimização extrema de queries específicas (onde o SQL nativo é mais performático).
* Sistemas que utilizam stored procedures complexas como regra de negócio principal.

### 8. O que influencia este conceito?

* **Modelo de Domínio:** O design das suas classes Java dita a complexidade do mapeamento.
* 
**Dialetos de Banco de Dados:** O ORM precisa entender o dialeto específico do banco (Postgres, MySQL, Oracle) para traduzir o SQL corretamente.



### 9. O que este conceito influencia?

* **Ciclo de vida da Entidade:** O ORM controla se um objeto está `Managed`, `Detached`, `New` ou `Removed`.
* **Performance:** A forma como o ORM é configurado influencia diretamente o número de queries executadas (problema N+1).

### 10. Configurações que alteram seu comportamento

* `hibernate.dialect`: Define como traduzir HQL/JPQL para o SQL do seu banco.
* `hibernate.hbm2ddl.auto`: Define se o ORM deve validar ou criar o esquema de banco automaticamente.
* `hibernate.show_sql`: Fundamental para o aprendizado, pois revela o SQL gerado pelo ORM.

### 11. O que a especificação (ou teoria) define?

A especificação Jakarta Persistence (JPA) define a interface padrão que o Hibernate implementa. Ela estabelece regras sobre como deve ser o gerenciamento do *EntityManager*, as transações e o mapeamento de entidades.

### 12. Como isso é implementado na prática?

Utilizamos anotações de nível de classe e atributo:

```java
@Entity // Define a classe como uma entidade persistente
@Table(name = "usuarios")
public class Usuario {
    @Id // Define a chave primária
    private Long id;
}

```

### 13. Casos especiais

* **Herança:** O ORM precisa decidir como mapear classes pai/filho (Single Table, Joined, Table per Class).
* **Coleções:** O mapeamento de listas, sets e mapas exige atenção especial ao ciclo de vida (`CascadeTypes`).

### 14. Erros ou exceções relacionadas

* `MappingException`: Ocorrem quando o ORM não entende o mapeamento (ex: propriedade sem getter/setter).
* `LazyInitializationException`: Quando tentamos acessar uma entidade fora de uma sessão ativa.

### 15. Modelos mentais incorretos

* **"ORM é apenas um gerador de SQL":** Errado. Ele é um gerenciador de estado e identidade.
* **"ORM é transparente":** Errado. Você *precisa* entender o que está acontecendo "debaixo do capô" para não criar gargalos de performance.

### 16. Exemplos práticos

Em um sistema de e-commerce, o ORM permite que você faça `pedido.getItens().add(item)` e, ao final da transação, ele automaticamente executa o `INSERT` na tabela de itens, mantendo a integridade referencial.

### 17. Impactos e consequências

* **Positivo:** Aumento drástico na velocidade de desenvolvimento.
* **Negativo:** Abstração excessiva pode esconder queries ineficientes se não monitoradas.

### 18. Fluxograma

1. `Persistência` -> 2. `Verificação de Cache (Persistence Context)` -> 3. `Detecção de Mudanças (Dirty Checking)` -> 4. `Geração de SQL` -> 5. `Execução no Banco`.

### 19. Tabela resumo

| Característica | ORM (JPA/Hibernate) | JDBC (Manual) |
| --- | --- | --- |
| **Abstração** | Alta (POO) | Baixa (SQL) |
| **Produtividade** | Alta | Baixa |
| **Controle de SQL** | Controlado via Framework | Totalmente manual |
| **Manutenção** | Fácil (focada em objetos) | Difícil (focada em queries) |

### 20. Checklist mental

* [ ] A entidade possui um `@Id`?
* [ ] Os relacionamentos possuem fetch types adequados (Lazy/Eager)?
* [ ] Estou usando coleções adequadas para evitar duplicidade (`Set` vs `List`)?
* [ ] O mapeamento reflete fielmente o modelo de negócio?

### 21. Conceitos relacionados

* **Persistence Context:** O "espaço de trabalho" do Hibernate.
* **Dirty Checking:** A capacidade do Hibernate de detectar alterações em objetos sem você chamar um `save()`.
* **JDBC:** A tecnologia que o ORM utiliza por baixo dos panos para conversar com o banco.