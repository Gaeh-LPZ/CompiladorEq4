package com.persistencia.analizadorLexico;

public enum tipoToken {
    // Palabras reservadas
     
    ELSE, SWITCH, CASE, WHILE, DO, FOR, BREAK, CONTINUE, RETURN, PUBLIC, PRIVATE, PROTECTED, DEFAULT, ABSTRACT, CLASS, EXTENDS, FINAL, MAIN, IMPLEMENTS, INTERFACE, STATIC, CHAR, BOOLEAN, NEW, THIS, TRY, IMPORT, CATCH, PACKAGE, SUPER, NULL, TRUE, FALSE, IF, LONG, VOID, FINALLY, THROWS, ASSERT, BYTE, DOUBLE, SHORT, ENUM, VOLATILE, INSTANCEOF, NATIVE, TRANSIENT, SYNCHRONIZED, THROW, INT, FLOAT, CADENA, SYSTEM, OUT, PRINTLN, STRING, ARGS,

    //identificadores y literales
    IDENTIFICADOR,
    LITERAL_ENTERA,
    LITERAL_FLOTANTE,
    LITERAL_CADENA,


    //operadores 
    COMILLA,         // "
    SUMA,           // +
    RESTA,          // -
    MULTIPLICACION, // *
    DIVISION,       // /
    MOD,            // %
    ASIGNACION,     // =
    IGUAL,          // ==
    DIFERENTE,      // !=
    MENOR_QUE,      // <
    MAYOR_QUE,      // >
    MENOR_IGUAL,    // <=
    MAYOR_IGUAL,    // >=
    AND,            // &&
    OR,             // ||
    INCREMENTO,     // ++ 
    MAS_IGUAL,      // +=
    DECREMENTO,     //-- | -=
    DIVISION_ASIGNACION, // /=
    MULTIPLICACION_ASIGNACION, // *=

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

    // Token especial para el fin del archivo
    EOF, // End Of File

    // Token para elementos desconocidos
    DESCONOCIDO,
    ERROR_DE_CADENA
}
