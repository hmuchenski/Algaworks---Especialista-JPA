# GenerationType.SEQUENCE

## Conceito
Utiliza uma sequence do banco.

## Estrutura

generator
↓
name
↓
sequenceName
↓
Sequence do banco

## Exemplo

```java
@Id
@GeneratedValue(
 strategy = GenerationType.SEQUENCE,
 generator = "seq"
)
@SequenceGenerator(
 name = "seq",
 sequenceName = "seq_produto",
 allocationSize = 50
)
private Long id;
```

## Fluxo

persist()
↓
nextval(sequence)
↓
Hibernate recebe ID
↓
INSERT

## allocationSize

Reserva blocos de IDs.

allocationSize = 50

Banco
↓
Reserva 50 IDs
↓
1..50

## Vantagens
- Excelente performance
- Hibernate conhece o ID antes do INSERT

## Bancos
- Oracle
- PostgreSQL
