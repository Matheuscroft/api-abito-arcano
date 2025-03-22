# RESTful API - Abito Arcano

Java RESTful API - To Do List

## Diagrama de Classes

```mermaid
classDiagram
class Tarefa {
    -UUID id
    -String titulo
    -int pontuacao
    -List<Integer> diasSemana
}

class Area {
    -UUID id
    -String nome
    -String cor
}

class Subarea {
    -UUID id
    -String nome
}

Tarefa "1" *-- "1" Area : "pertence a"
Tarefa "1" *-- "1" Subarea : "pertence a"
Area "1" *-- "N" Subarea : "contém"
```
