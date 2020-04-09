# Klox Grammar

This contains the representation of the syntax grammar which is updated through the book and the questions
so thought I would keep it in a small group.

Some of the grammar is used to create error productions.

```
expression     -> comma;
comma          -> ternary ("," ternary)*
ternary        -> equality ("?" equality ":" (equality | ternary))*
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> addition ( ( ">" | ">=" | "<" | "<=" ) addition )* ;
addition       -> multiplication ( ( "-" | "+" ) multiplication )* ;
multiplication -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" | "+" | "==" | "!=" | "<" | "<=" | ">" | ">=" | "*" | "/" ) unary
               | primary ;
primary        -> NUMBER | STRING | "false" | "true" | "nil"
               | "(" expression ")" ;
```
