# Estados da Entidade

```text
Transient
↓ persist()
Managed
↓ detach() / clear()
Detached
↓ remove()
Removed
```

## Transient
Entidade criada com `new`.

## Managed
Entidade gerenciada pelo EntityManager.

## Detached
Entidade removida do contexto de persistência.

## Removed
Entidade marcada para exclusão.

[← Voltar](./README.md)
