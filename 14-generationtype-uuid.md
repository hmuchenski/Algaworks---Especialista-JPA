# GenerationType.UUID

## Conceito

Hibernate gera UUID.

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

## Fluxo

persist()
↓
Hibernate gera UUID
↓
INSERT

## Uso comum
- Microsserviços
- APIs públicas
- Sistemas distribuídos
