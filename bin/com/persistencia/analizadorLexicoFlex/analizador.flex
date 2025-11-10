package com.persistencia.analizadorLexicoFlex;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

%%

%class Analizador
%public
%line
%column
%type void
%eofval{
    return;
%eofval}

%{
    // ======== CAMBIO PRINCIPAL ========
    // Usamos LinkedHashMap para mantener el orden de inserción
    private java.util.LinkedHashMap<Integer,String> tablaSimbolos = new java.util.LinkedHashMap<>();
    private int nextId = 1;

    private List<Object[]> listaTokens = new ArrayList<>();
    private List<Object[]> listaErrores = new ArrayList<>();

    private boolean simboloExiste(String simbolo) {
        return tablaSimbolos.containsValue(simbolo);
    }

    private void agregarSimbolo(String simbolo) {
        if (!simboloExiste(simbolo)) {
            tablaSimbolos.put(nextId++, simbolo);
        }
    }

    private void escribirTiraTokens(String token, String lexema){
        listaTokens.add(new Object[]{yyline + 1, lexema, token, yycolumn + 1});
    }

    private void escribirSimbolos(String simbolo){
        agregarSimbolo(simbolo);
    }

    private void escribirError(String lexema){
        listaErrores.add(new Object[]{"Error Lexico", lexema, yyline + 1, yycolumn + 1});
    }

    // Recuperar listas
    public List<Object[]> getTokens() {
        return listaTokens;
    }

    public List<Object[]> getErrores() {
        return listaErrores;
    }

    // Devolvemos ID y nombre en el mismo orden de inserción
    public List<Object[]> getSimbolos() {
        List<Object[]> result = new ArrayList<>();
        for (java.util.Map.Entry<Integer,String> e : tablaSimbolos.entrySet()) {
            result.add(new Object[]{e.getKey(), e.getValue()});
        }
        return result;
    }

    public void limpiarArchivos() {
        listaTokens.clear();
        listaErrores.clear();
        tablaSimbolos.clear();
        nextId = 1;
    }
%}

// ---- EXPRESIONES REGULARES ---- //
LETRA = [a-zA-Z]
DIGITO = [0-9]
ESPACIO = [ \t\r]
NUEVALINEA = \n
IDENTIFICADOR = {LETRA}({LETRA}|{DIGITO})*
NUMERO_ENTERO = {DIGITO}+
NUMERO_FLOTANTE = {DIGITO}+(\.{DIGITO}+)?
LITERAL = \"([^\\\"]|\\.)*\"
COMENTARIO = "//".*

%%

// ----- OPERADORES DE ASIGNACION -----//
"+=" { escribirTiraTokens("ASIG_SUMA", yytext()); }
"-=" { escribirTiraTokens("ASIG_RESTA", yytext()); }
"/=" { escribirTiraTokens("ASIG_DIV", yytext()); }
"*=" { escribirTiraTokens("ASIG_MULT", yytext()); }
"++" { escribirTiraTokens("INCREMENTO", yytext()); }
"--" { escribirTiraTokens("DECREMENTO", yytext()); }

// ----- PALABRAS RESERVADAS -----//
"int" { escribirTiraTokens("INT", yytext()); }
"public" { escribirTiraTokens("PUBLIC", yytext()); }
"class" { escribirTiraTokens("CLASS", yytext()); }
"static" { escribirTiraTokens("STATIC", yytext()); }
"if" { escribirTiraTokens("IF", yytext()); }
"for" { escribirTiraTokens("FOR", yytext()); }
"while" { escribirTiraTokens("WHILE", yytext()); }
"boolean" { escribirTiraTokens("BOOLEAN", yytext()); }
"float" { escribirTiraTokens("FLOAT", yytext()); }
"main" { escribirTiraTokens("MAIN", yytext()); }
"System" { escribirTiraTokens("SYSTEM", yytext()); }
"out" { escribirTiraTokens("OUT", yytext()); }
"println" { escribirTiraTokens("PRINTLN", yytext()); }
"String" { escribirTiraTokens("STRING", yytext()); }
"void" { escribirTiraTokens("VOID", yytext()); }
"do" { escribirTiraTokens("DO", yytext()); }
"else" { escribirTiraTokens("ELSE", yytext()); }

// ----- DELIMITADORES -----//
"{" { escribirTiraTokens("LLAVE_IZQ", yytext()); }
"}" { escribirTiraTokens("LLAVE_DER", yytext()); }
";" { escribirTiraTokens("PUNTO_COMA", yytext()); }
"." { escribirTiraTokens("PUNTO", yytext()); }
"[" { escribirTiraTokens("CORCHETE_IZQ", yytext()); }
"]" { escribirTiraTokens("CORCHETE_DER", yytext()); }
"(" { escribirTiraTokens("PARENTESIS_IZQ", yytext()); }
")" { escribirTiraTokens("PARENTESIS_DER", yytext()); }
"," { escribirTiraTokens("COMA", yytext()); }
":" { escribirTiraTokens("DOS_PUNTOS", yytext()); }
"<" { escribirTiraTokens("MENOR_QUE", yytext()); }

// ----- OPERADORES ARITMETICOS -----//
"+" { escribirTiraTokens("SUMA", yytext()); }
"-" { escribirTiraTokens("RESTA", yytext()); }
"/" { escribirTiraTokens("DIVISION", yytext()); }
"%" { escribirTiraTokens("MODULO", yytext()); }
"*" { escribirTiraTokens("MULTIPLICACION", yytext()); }
"=" { escribirTiraTokens("ASIGNACION", yytext()); }

// ----- OPERADORES LOGICOS -----//
"&&" { escribirTiraTokens("AND", yytext()); }
"||" { escribirTiraTokens("OR", yytext()); }
"!" { escribirTiraTokens("NOT", yytext()); }

// ----- ESPACIOS Y COMENTARIOS ------ //
{ESPACIO}+ {/* Ignorar */ }
{COMENTARIO} {/* Ignorar */}
{NUEVALINEA} {/* Ignorar */ }

// ----- IDENTIFICADORES (SIMBOLOS) -----//
{IDENTIFICADOR} {
    escribirTiraTokens("IDENTIFICADOR", yytext());
    escribirSimbolos(yytext());
}

// ----- LITERALES Y NUMEROS -----//
{NUMERO_ENTERO} { escribirTiraTokens("NUMERO_ENTERO", yytext()); }
{NUMERO_FLOTANTE} { escribirTiraTokens("NUMERO_FLOTANTE", yytext()); }
{LITERAL} { escribirTiraTokens("LITERAL", yytext()); }

// ----- ERRORES ----- //
. { escribirError(yytext()); }