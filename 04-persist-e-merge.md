# persist() e merge()

## persist()

* Principalmente para inserção.
* A própria entidade passada torna-se managed.

## merge()

* Principal uso: reanexar entidades detached.
* Pode inserir ou atualizar.
* A entidade passada continua detached e o id da entidade criada não é carregado.
* A entidade retornada é managed e vem com o id carregado.

## Comparação

|Característica|persist()|merge()|
|-|-|-|
|Entidade passada vira managed|Sim|Não|
|Retorna entidade managed|Não|Sim|
|Pode inserir|Sim|Sim|
|Pode atualizar|Não|Sim|
|Usado com detached|Não|Sim|

[← Voltar](./README.md)

