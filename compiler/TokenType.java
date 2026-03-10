package compiler;

enum TokenType {
    //Single-char tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    //One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER_EQUAL, GREATER, LESS_EQUAL, LESS,

    //Literals
    IDENTIFIER, STRINGS, NUMBER,

    //Keywords
    AND, CLASS, ELSE, FALSE, TRUE, IF, FUN, FOR, NIL, OR, PRINT, RETURN, SUPER, THIS, VAR, WHILE,
    
    EOF,
}
