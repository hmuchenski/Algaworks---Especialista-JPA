# find() e getReference()

## find()

Busca uma entidade pelo ID.

### Fluxo

```text
find()
↓
Verifica contexto de persistência
↓
Se encontrar:
    retorna a entidade existente

Senão:
    SELECT
    ↓
    Entidade carregada
```

## getReference()

Obtém uma referência para uma entidade.

### Fluxo

```text
getReference()
↓
Verifica contexto de persistência
↓
Se encontrar:
    retorna a entidade existente

Senão:
    cria proxy
```

[← Voltar](./README.md)
