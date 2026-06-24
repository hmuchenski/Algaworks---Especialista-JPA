# JPA Entity Lifecycle - Operações x Estados

| Operação | Transient | Managed | Detached | Removed |
|-----------|-----------|----------|----------|----------|
| persist() | ✅ Vira Managed | Ignora | ❌ Exceção | ❌ |
| merge() | ✅ Cria/retorna Managed | ✅ Retorna Managed | ✅ Reatacha (retorna Managed) | ❌ |
| remove() | ❌ Exceção | ✅ Vira Removed | ❌ Exceção | Ignora |
| refresh() | ❌ Exceção | ✅ Recarrega do banco | ❌ Exceção | ❌ |
| detach() | Ignora | ✅ Vira Detached | Ignora | Ignora |
| contains() | false | true | false | true* |
| flush() | Sem efeito | ✅ Sincroniza | Sem efeito | ✅ Executa DELETE |
| commit() | Sem efeito | ✅ Sincroniza | Sem efeito | ✅ Executa DELETE |
| lock() | ❌ | ✅ | ⚠️ Algumas variantes permitem reassociação | ❌ |
| clear() | N/A | Todas viram Detached | N/A | N/A |
| find() | N/A | Retorna mesma instância Managed | Retorna Managed do contexto atual | N/A |
| getReference() | N/A | Retorna mesma instância Managed (se já existir) | Retorna Managed Proxy (não reatacha a Detached) | N/A |
| getReference(id inexistente) | N/A | Proxy criado; erro ao acessar atributos | Proxy criado; erro ao acessar atributos | N/A |
| refresh(entity, LockModeType) | ❌ | ✅ | ❌ | ❌ |
| evict() (Hibernate Session) | Ignora | ✅ Vira Detached | Ignora | Ignora |

\* Enquanto a entidade Removed ainda estiver sendo gerenciada pelo contexto de persistência.