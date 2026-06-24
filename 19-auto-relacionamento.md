# JPA - Autorelacionamento, Árvores, Grafos e `mappedBy`

## O que me confundiu

Considere a entidade:

```java
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_pai_id")
    private Categoria categoriaPai;

    @OneToMany(mappedBy = "categoriaPai")
    private List<Categoria> categorias; // Categorias filhas
}
```

Ao ler:

```java
@OneToMany(mappedBy = "categoriaPai")
private List<Categoria> categorias;
```

é comum interpretar incorretamente que:

> "categorias representa as filhas do atributo categoriaPai"

Mas isso está errado.

## O que `mappedBy = "categoriaPai"` realmente significa

### NÃO significa:

> "Pegue as filhas do atributo categoriaPai"

### Significa:

> "Pegue todas as Categorias cuja propriedade `categoriaPai` aponta para ESTA instância"

A palavra importante aqui é:

**ESTA INSTÂNCIA**

JPA trabalha no nível dos objetos (instâncias), não da classe.

---

## Como o Hibernate enxerga isso

Quando temos:

```java
@OneToMany(mappedBy = "categoriaPai")
private List<Categoria> categorias;
```

o Hibernate interpreta aproximadamente como:

```java
categorias =
    SELECT c
    FROM Categoria c
    WHERE c.categoriaPai = this;
```

Observe o:

```java
this
```

O Hibernate está procurando todas as categorias cujo pai é a instância atual.

---

# Exemplo Prático

Tabela:

| id | nome         | categoria_pai_id |
| -- | ------------ | ---------------- |
| 1  | Rock         | null             |
| 2  | Heavy Metal  | 1                |
| 3  | Hard Rock    | 1                |
| 4  | Thrash Metal | 2                |

---

## Instância Rock

```java
Categoria rock = categoriaRepository.findById(1);
```

Objeto carregado:

```java
rock.id = 1;
rock.nome = "Rock";
rock.categoriaPai = null;
```

Quando executamos:

```java
rock.getCategorias();
```

o Hibernate faz algo equivalente a:

```sql
SELECT *
FROM categoria
WHERE categoria_pai_id = 1;
```

Resultado:

```text
Heavy Metal
Hard Rock
```

---

## Instância Heavy Metal

```java
Categoria heavyMetal = categoriaRepository.findById(2);
```

Objeto carregado:

```java
heavyMetal.id = 2;
heavyMetal.nome = "Heavy Metal";
heavyMetal.categoriaPai = rock;
```

Quando executamos:

```java
heavyMetal.getCategorias();
```

o Hibernate faz algo equivalente a:

```sql
SELECT *
FROM categoria
WHERE categoria_pai_id = 2;
```

Resultado:

```text
Thrash Metal
```

---

# Nomenclatura Mais Clara

Uma modelagem mais intuitiva seria:

```java
@Entity
public class Categoria {

    @Id
    private Integer id;

    private String nome;

    @ManyToOne
    @JoinColumn(name = "categoria_pai_id")
    private Categoria pai;

    @OneToMany(mappedBy = "pai")
    private List<Categoria> filhos;
}
```

Agora a leitura fica natural:

```java
heavyMetal.getPai();
```

Resultado:

```text
Rock
```

---

```java
rock.getFilhos();
```

Resultado:

```text
Heavy Metal
Hard Rock
```

---

# Árvore x Grafo

Foi justamente essa diferença que causou a confusão inicial.

---

## Árvore

Em uma árvore, cada nó possui no máximo um pai direto.

Exemplo:

```text
Rock
├─ Heavy Metal
│  ├─ Thrash Metal
│  └─ Power Metal
└─ Hard Rock
```

Pergunta:

```text
Quem é o pai de Heavy Metal?
```

Resposta:

```text
Rock
```

Existe apenas uma resposta possível.

Por isso o relacionamento é:

```java
@ManyToOne
private Categoria pai;
```

Muitas categorias podem apontar para um mesmo pai.

---

## Estrutura Relacional

```text
Muitas Categorias -> Um Pai
```

ou

```text
N : 1
```

Logo:

```java
@ManyToOne
```

---

# Grafo

Em um grafo um nó pode possuir múltiplos pais.

Exemplo:

```text
             Informática
                 ↑
                 |
Notebook Gamer ←→ Promoções
                 |
                 ↓
            Black Friday
```

ou:

```text
Heavy Metal
   ↑     ↑
Rock    Música dos Anos 80
```

Agora a pergunta:

```text
Quem é o pai de Heavy Metal?
```

Possui múltiplas respostas:

```text
Rock
Música dos Anos 80
```

Portanto:

```text
Muitos ↔ Muitos
```

e o relacionamento se torna:

```java
@ManyToMany
private Set<Categoria> categoriasPai;
```

---

# Regra Mental Rápida

## Árvore

Pergunta:

```text
Um nó pode ter mais de um pai direto?
```

Resposta:

```text
Não
```

Então:

```java
@ManyToOne
```

Exemplos:

* Funcionário → Chefe
* Categoria → Categoria Pai
* Plano de Contas
* Centro de Custo
* Comentário → Comentário Pai

---

## Grafo

Pergunta:

```text
Um nó pode ter vários pais diretos?
```

Resposta:

```text
Sim
```

Então:

```java
@ManyToMany
```

Exemplos:

* Usuário segue Usuário
* Produtos Relacionados
* Categorias com múltiplos pais
* Rede de amizades

---

# Loops Infinitos

## Árvore

Uma árvore válida não possui ciclos.

Exemplo:

```text
Rock
↑
Heavy Metal
↑
Thrash Metal
```

Ao subir pelos pais:

```text
Thrash Metal
→ Heavy Metal
→ Rock
→ null
```

A navegação termina.

---

## Grafo

Grafos podem possuir ciclos.

Exemplo:

```text
A → B → C → A
```

Se percorrermos recursivamente:

```text
A
B
C
A
B
C
A
B
C
...
```

Teremos loop infinito.

---

## Atenção

Mesmo usando:

```java
@ManyToOne
private Categoria pai;
```

é possível criar ciclos por erro de dados:

```text
A → B
B → C
C → A
```

O banco aceita isso porque todas as chaves estrangeiras são válidas.

Por isso sistemas hierárquicos normalmente validam se o novo pai não é descendente da própria categoria antes de salvar.

---

# Resumo Final

## Árvore

```java
@ManyToOne
private Categoria pai;

@OneToMany(mappedBy = "pai")
private List<Categoria> filhos;
```

Características:

* Cada nó possui apenas um pai.
* Pode possuir vários filhos.
* Não deveria possuir ciclos.
* Estrutura hierárquica.

---

## Grafo

```java
@ManyToMany
private Set<Categoria> pais;

@ManyToMany(mappedBy = "pais")
private Set<Categoria> filhos;
```

Características:

* Um nó pode possuir vários pais.
* Um nó pode possuir vários filhos.
* Pode possuir ciclos.
* Estrutura de rede.

---

## Principal Aprendizado

Quando ler:

```java
@OneToMany(mappedBy = "categoriaPai")
private List<Categoria> categorias;
```

não pense:

> "filhas do atributo categoriaPai"

Pense:

> "todas as Categorias cujo campo categoriaPai aponta para ESTA instância"

Essa mudança de perspectiva (classe → instância) é o que faz o autorelacionamento do JPA ficar intuitivo.
