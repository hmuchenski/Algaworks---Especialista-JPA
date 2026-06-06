# Guia Avançado - Geração de IDs no JPA/Hibernate (MySQL e PostgreSQL)

## Objetivo

Entender não apenas as anotações do JPA, mas também como o Hibernate gera IDs internamente, quais otimizações existem, como elas impactam performance e quais armadilhas podem aparecer em produção.

---

# Estratégias Relevantes

Na prática:

```text
MySQL
↓
IDENTITY

PostgreSQL
↓
SEQUENCE

Microsserviços
↓
UUID
```

Ranking de relevância:

```text
1. IDENTITY
2. SEQUENCE
3. UUID
4. AUTO
5. TABLE
```

---

# GenerationType.IDENTITY

## Conceito

O banco de dados gera automaticamente o identificador.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## Fluxo

```text
persist()
↓
INSERT
↓
Banco gera ID
↓
Hibernate recebe ID
```

## Exemplo MySQL

```sql
CREATE TABLE produto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);
```

## Consequência importante

O Hibernate não conhece o ID antes do INSERT.

```text
persist()
↓
INSERT obrigatório
↓
ID disponível
```

## Vantagens

```text
Configuração simples

Pouca manutenção

Muito comum
```

## Desvantagens

```text
Hibernate não conhece o ID previamente

Menos otimizações para batch inserts
```

## Quando usar

```text
MySQL
↓
Quase sempre
```

---

# GenerationType.SEQUENCE

## Conceito

Utiliza uma sequence do banco para gerar IDs.

```java
@Id
@GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "seq_produto"
)
@SequenceGenerator(
    name = "seq_produto",
    sequenceName = "seq_produto"
)
private Long id;
```

---

# Entendendo generator

```java
@GeneratedValue(generator = "seq")
```

↓

```java
@SequenceGenerator(name = "seq")
```

Regra:

```text
generator
↓
aponta para
↓
name
```

---

# Entendendo sequenceName

```java
@SequenceGenerator(
    name = "seq",
    sequenceName = "seq_produto"
)
```

Regra:

```text
name
↓
identificador interno do JPA

sequenceName
↓
nome real da sequence
```

---

# Fluxo

```text
persist()
↓
nextval(sequence)
↓
Hibernate recebe ID
↓
INSERT
```

---

# Diferença para IDENTITY

IDENTITY:

```text
INSERT
↓
Banco gera ID
```

SEQUENCE:

```text
nextval()
↓
Hibernate recebe ID
↓
INSERT
```

Consequência:

```text
Hibernate conhece o ID antes do INSERT
```

---

# PostgreSQL

Exemplo:

```sql
CREATE SEQUENCE seq_produto;
```

Uso:

```sql
SELECT nextval('seq_produto');
```

---

# allocationSize

O conceito mais importante de Sequence.

## Sem allocationSize

```text
Persist
↓
nextval()

Persist
↓
nextval()

Persist
↓
nextval()
```

---

## allocationSize = 50

```java
@SequenceGenerator(
    allocationSize = 50
)
```

Fluxo:

```text
Banco
↓
Entrega bloco
1..50
```

Persistências:

```text
1
2
3
...
50
```

Sem consultar novamente.

---

# Objetivo do allocationSize

Reduzir consultas.

Exemplo:

```text
10000 inserts
```

Sem cache:

```text
10000 consultas
```

Com:

```java
allocationSize = 50
```

```text
200 consultas
```

---

# initialValue

Valor inicial da sequence.

```java
initialValue = 1
```

Equivalente conceitual:

```sql
START WITH 1
```

---

# Shared Sequence

Possível:

```text
Produto
↓
seq_global

Cliente
↓
seq_global

Pedido
↓
seq_global
```

Resultado:

```text
Produto -> 1
Cliente -> 2
Pedido -> 3
```

---

# AllocationSize diferentes

Evite.

Exemplo:

```text
Produto
↓
allocationSize = 50

Cliente
↓
allocationSize = 1
```

Possível:

```text
1
51
2
3
52
4
```

---

# Colisão?

```text
Não
```

---

# Confusão?

```text
Sim
```

---

# Boa prática

```text
Mesma sequence
↓
Mesmo allocationSize
```

---

# Convenção mais comum

```text
Produto
↓
seq_produto

Cliente
↓
seq_cliente

Pedido
↓
seq_pedido
```

Regra prática:

```text
1 tabela
↓
1 sequence
```

---

# UUID

## Conceito

Hibernate gera o identificador.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

---

## Fluxo

```text
persist()
↓
Hibernate gera UUID
↓
INSERT
```

---

## Exemplo

```text
550e8400-e29b-41d4-a716-446655440000
```

---

## Vantagens

```text
Não depende do banco

Pode gerar antes do INSERT

Excelente para microsserviços

Excelente para sistemas distribuídos
```

---

## Desvantagens

```text
Índices maiores

Mais memória

Menos legível
```

---

# AUTO

```java
@GeneratedValue(
    strategy = GenerationType.AUTO
)
```

Fluxo:

```text
AUTO
↓
Hibernate escolhe
↓
IDENTITY
ou
SEQUENCE
```

Problema:

```text
Menos previsível
```

---

# TABLE

Fluxo:

```text
Tabela
↓
Lê valor
↓
Incrementa
↓
Atualiza
↓
Novo ID
```

Hoje:

```text
Pouco utilizada
```

---

# O Problema das Sequences

Imagine:

```java
for (int i = 0; i < 10000; i++) {
    entityManager.persist(produto);
}
```

Sem otimização:

```text
Persist
↓
nextval()

Persist
↓
nextval()

Persist
↓
nextval()
```

10.000 consultas.

---

# Otimizadores do Hibernate

O Hibernate possui estratégias internas para reduzir consultas.

Principais:

```text
none
pooled
pooled-lo
legacy-hilo
```

---

# none

Sem otimização.

Fluxo:

```text
Persist
↓
nextval()

Persist
↓
nextval()

Persist
↓
nextval()
```

Maior quantidade de consultas.

---

# pooled

Reserva blocos inteiros.

Exemplo:

Sequence retorna:

```text
50
```

Hibernate interpreta:

```text
1..50
```

como disponíveis.

Depois:

```text
51..100
```

e assim por diante.

---

# pooled-lo

Mais moderno.

Utiliza:

```text
valor da sequence
+
contador local
```

Exemplo conceitual:

Banco:

```text
5
```

Hibernate:

```text
201..250
```

IDs disponíveis localmente.

---

# Benefício

```text
Menos consultas

Mais performance

Melhor escalabilidade
```

---

# Hi/Lo

Antecessor do pooled-lo.

Ideia:

```text
Hi
↓
valor vindo do banco

Lo
↓
contador local
```

Gera vários IDs usando apenas um acesso à sequence.

---

# Por que pooled-lo substituiu hi/lo?

```text
Mais simples

Mais previsível

Menos problemas históricos
```

---

# Gaps de IDs

Muita gente estranha isso.

Exemplo:

```text
1
2
3
4
50
51
52
```

ou

```text
1
2
100
101
```

---

# Causas

```text
Rollback

allocationSize

pooled

pooled-lo
```

---

# Rollback

Exemplo:

```text
nextval()
↓
ID = 10
↓
rollback()
```

O ID não volta.

Resultado:

```text
9
11
12
```

---

# Importante

```text
Sequences não participam da transação.
```

---

# Batch Inserts

SEQUENCE é mais amigável para lotes.

IDENTITY:

```text
INSERT
↓
recebe ID
```

SEQUENCE:

```text
recebe ID
↓
INSERT
```

---

# Regra prática

Grandes volumes:

```text
SEQUENCE
>
IDENTITY
```

---

# Mercado

## MySQL

```text
IDENTITY
↓
Escolha padrão
```

## PostgreSQL

```text
SEQUENCE
↓
Mais comum em projetos que usam Hibernate intensivamente
```

## Microsserviços

```text
UUID
```

---

# Resumo Final

```text
MySQL
↓
IDENTITY

PostgreSQL
↓
SEQUENCE

Microsserviços
↓
UUID
```

E a regra mais importante para PostgreSQL:

```text
generator
↓
name
↓
sequenceName
↓
allocationSize
```

Se você dominar esses quatro pontos, entenderá a maioria dos cenários reais envolvendo geração de IDs com Hibernate.
