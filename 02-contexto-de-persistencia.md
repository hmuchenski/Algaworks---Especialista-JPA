# Contexto de Persistência

O EntityManager mantém um **Contexto de Persistência** (cache de primeiro nível).

Enquanto uma entidade está nesse contexto, ela é considerada **managed**.

Para cada entidade + ID existe apenas uma representação gerenciada dentro do mesmo EntityManager.

[← Voltar](./README.md)
