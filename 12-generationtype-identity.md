# GenerationType.IDENTITY

## Conceito
O banco gera o ID.

## Fluxo

persist()
↓
INSERT
↓
Banco gera ID
↓
Hibernate recebe ID

## Exemplo

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

## SQL típico

INSERT INTO produto (...) VALUES (...)

## Vantagens
- Simples
- Pouca configuração
- Muito comum em MySQL

## Desvantagens
- Hibernate não conhece o ID antes do INSERT
- Menos otimizações para batch inserts

## Quando usar
- MySQL
- MariaDB
