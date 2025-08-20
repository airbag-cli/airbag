grammar ValidationTree;

validationTree
    : node EOF
    ;

tokenList
    : token* EOF
    ;

node
    : rule
    | token
    | error
    ;

rule
    : '(' RULE node+ ')'
    ;

token
    : '(' TOKEN STRING ')'
    | STRING
    | EOF_KEYWORD
    ;

error
    : '(' ERROR token ')'
    ;

// --- Lexer Rules ---
EOF_KEYWORD: 'EOF';
ERROR: '<error>';
RULE: [a-z][a-zA-Z0-9_]*;
TOKEN: [A-Z][a-zA-Z0-9_]*;
INT: '-'?[0-9]+;
STRING: '\'' (ESC | .)*? '\'';
WS: [ \t\r\n\f]+ -> skip;

// --- Fragments ---
fragment ESC: '\\'['tnfr];