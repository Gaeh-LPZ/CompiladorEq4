package com.persistencia.analizadorLexico;
public enum tipoToken {
    // Palabras reservadas
    PACKAGE, IMPORT, CLASS, INTERFACE, ENUM, 
    PUBLIC, PRIVATE, PROTECTED, ABSTRACT, STATIC, FINAL, SYNCHRONIZED, NATIVE, TRANSIENT, VOLATILE,
    EXTENDS, IMPLEMENTS, VOID, THROWS, 
    IF, ELSE, SWITCH, CASE, DEFAULT, WHILE, DO, FOR, BREAK, CONTINUE, RETURN, THROW, TRY, CATCH, FINALLY,
    BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE,
    NEW, THIS, SUPER, NULL, TRUE, FALSE, INSTANCEOF,
    ASSERT,
    
    // Extras (aunque no son palabras reservadas estándar de Java)
    MAIN, CADENA, SYSTEM, OUT, PRINTLN,

    // Identificadores y literales
    IDENTIFICADOR,
    LITERAL_ENTERA,
    LITERAL_FLOTANTE,
    LITERAL_CADENA,

    // Operadores aritméticos
    SUMA,           // +
    RESTA,          // -
    MULTIPLICACION, // *
    DIVISION,       // /
    MOD,            // %
    
    // Operadores de asignación y comparación
    ASIGNACION,     // =
    IGUAL,          // ==
    DIFERENTE,      // !=
    MENOR_QUE,      // 
    MAYOR_QUE,      // >
    MENOR_IGUAL,    // <=
    MAYOR_IGUAL,    // >=
    
    // Operadores lógicos
    AND,            // &&
    OR,             // ||
    NOT,            // !
    
    // Operadores bit a bit
    BITAND,         // &
    BITOR,          // |
    BITXOR,         // ^
    TILDE,          // ~ (complemento bit a bit)
    
    // Operadores de desplazamiento
    LSHIFT,         // 
    RSHIFT,         // >>
    URSHIFT,        // >>>

    // Símbolos y Delimitadores
    CORCHETE_IZQ,   // [
    CORCHETE_DER,   // ]
    PARENTESIS_IZQ, // (
    PARENTESIS_DER, // )
    LLAVE_IZQ,      // {
    LLAVE_DER,      // }
    PUNTO_Y_COMA,   // ;
    COMA,           // ,
    PUNTO,          // .
    DOS_PUNTOS,     // :
    COLON,          // : (alias para la gramática)
    QUESTION,       // ?
    ASTERISK,       // * (para imports y genéricos)
    MENOR,          // < (para genéricos)
    MAYOR,          // > (para genéricos)
    ELLIPSIS,       // ... (varargs)
    ARROBA,         // @ (anotaciones)
    
    COMILLA,        // "

    // Token especial para el fin del archivo
    EOF,

    // Tokens para elementos desconocidos o errores
    DESCONOCIDO,
    ERROR_DE_CADENA
}