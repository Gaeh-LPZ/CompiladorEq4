// package com.persistencia.analizadorLexicoFlex;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java_cup.runtime.*;

%%

%class Analizador
%public
%line
%column
%cup
%{
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

    public List<Object[]> getTokens() {
        return listaTokens;
    }

    public List<Object[]> getErrores() {
        return listaErrores;
    }

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
    
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

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

"+=" { escribirTiraTokens("ASIG_SUMA", yytext()); return symbol(sym.ASIG_SUMA); }
"-=" { escribirTiraTokens("ASIG_RESTA", yytext()); return symbol(sym.ASIG_RESTA); }
"/=" { escribirTiraTokens("ASIG_DIV", yytext()); return symbol(sym.ASIG_DIV); }
"*=" { escribirTiraTokens("ASIG_MULT", yytext()); return symbol(sym.ASIG_MULT); }
"++" { escribirTiraTokens("INCREMENTO", yytext()); return symbol(sym.INCREMENTO); }
"--" { escribirTiraTokens("DECREMENTO", yytext()); return symbol(sym.DECREMENTO); }

"int" { escribirTiraTokens("INT", yytext()); return symbol(sym.INT); }
"public" { escribirTiraTokens("PUBLIC", yytext()); return symbol(sym.PUBLIC); }
"class" { escribirTiraTokens("CLASS", yytext()); return symbol(sym.CLASS); }
"static" { escribirTiraTokens("STATIC", yytext()); return symbol(sym.STATIC); }
"if" { escribirTiraTokens("IF", yytext()); return symbol(sym.IF); }
"for" { escribirTiraTokens("FOR", yytext()); return symbol(sym.FOR); }
"while" { escribirTiraTokens("WHILE", yytext()); return symbol(sym.WHILE); }
"boolean" { escribirTiraTokens("BOOLEAN", yytext()); return symbol(sym.BOOLEAN); }
"float" { escribirTiraTokens("FLOAT", yytext()); return symbol(sym.FLOAT); }
"main" { escribirTiraTokens("MAIN", yytext()); return symbol(sym.MAIN); }
"System" { escribirTiraTokens("SYSTEM", yytext()); return symbol(sym.SYSTEM); }
"out" { escribirTiraTokens("OUT", yytext()); return symbol(sym.OUT); }
"println" { escribirTiraTokens("PRINTLN", yytext()); return symbol(sym.PRINTLN); }
"String" { escribirTiraTokens("STRING", yytext()); return symbol(sym.STRING); }
"void" { escribirTiraTokens("VOID", yytext()); return symbol(sym.VOID); }
"do" { escribirTiraTokens("DO", yytext()); return symbol(sym.DO); }
"else" { escribirTiraTokens("ELSE", yytext()); return symbol(sym.ELSE); }
"package" { escribirTiraTokens("PACKAGE", yytext()); return symbol(sym.PACKAGE); }
"import" { escribirTiraTokens("IMPORT", yytext()); return symbol(sym.IMPORT); }
"return" { escribirTiraTokens("RETURN", yytext()); return symbol(sym.RETURN); }
"switch" { escribirTiraTokens("SWITCH", yytext()); return symbol(sym.SWITCH); }
"case" { escribirTiraTokens("CASE", yytext()); return symbol(sym.CASE); }
"default" { escribirTiraTokens("DEFAULT", yytext()); return symbol(sym.DEFAULT); }
"break" { escribirTiraTokens("BREAK", yytext()); return symbol(sym.BREAK); }

"{" { escribirTiraTokens("LLAVE_IZQ", yytext()); return symbol(sym.LLAVE_IZQ); }
"}" { escribirTiraTokens("LLAVE_DER", yytext()); return symbol(sym.LLAVE_DER); }
";" { escribirTiraTokens("PUNTO_COMA", yytext()); return symbol(sym.PUNTO_COMA); }
"." { escribirTiraTokens("PUNTO", yytext()); return symbol(sym.PUNTO); }
"[" { escribirTiraTokens("CORCHETE_IZQ", yytext()); return symbol(sym.CORCHETE_IZQ); }
"]" { escribirTiraTokens("CORCHETE_DER", yytext()); return symbol(sym.CORCHETE_DER); }
"(" { escribirTiraTokens("PARENTESIS_IZQ", yytext()); return symbol(sym.PARENTESIS_IZQ); }
")" { escribirTiraTokens("PARENTESIS_DER", yytext()); return symbol(sym.PARENTESIS_DER); }
"," { escribirTiraTokens("COMA", yytext()); return symbol(sym.COMA); }
":" { escribirTiraTokens("DOS_PUNTOS", yytext()); return symbol(sym.DOS_PUNTOS); }

"==" { escribirTiraTokens("IGUAL_IGUAL", yytext()); return symbol(sym.IGUAL_IGUAL); }
"!=" { escribirTiraTokens("DIFERENTE", yytext()); return symbol(sym.DIFERENTE); }
"<" { escribirTiraTokens("MENOR_QUE", yytext()); return symbol(sym.MENOR_QUE); }
">" { escribirTiraTokens("MAYOR_QUE", yytext()); return symbol(sym.MAYOR_QUE); }

"+" { escribirTiraTokens("SUMA", yytext()); return symbol(sym.SUMA); }
"-" { escribirTiraTokens("RESTA", yytext()); return symbol(sym.RESTA); }
"/" { escribirTiraTokens("DIVISION", yytext()); return symbol(sym.DIVISION); }
"%" { escribirTiraTokens("MODULO", yytext()); return symbol(sym.MODULO); }
"*" { escribirTiraTokens("MULTIPLICACION", yytext()); return symbol(sym.MULTIPLICACION); }
"=" { escribirTiraTokens("ASIGNACION", yytext()); return symbol(sym.ASIGNACION); }

"&&" { escribirTiraTokens("AND", yytext()); return symbol(sym.AND); }
"||" { escribirTiraTokens("OR", yytext()); return symbol(sym.OR); }
"!" { escribirTiraTokens("NOT", yytext()); return symbol(sym.NOT); }

{ESPACIO}+ { /* Ignorar */ }
{COMENTARIO} { /* Ignorar */ }
{NUEVALINEA} { /* Ignorar */ }

{IDENTIFICADOR} {
    escribirTiraTokens("IDENTIFICADOR", yytext());
    escribirSimbolos(yytext());
    return symbol(sym.IDENTIFICADOR, yytext());
}

{NUMERO_ENTERO} { 
    escribirTiraTokens("NUMERO_ENTERO", yytext()); 
    return symbol(sym.NUMERO_ENTERO, Integer.parseInt(yytext()));
}

{NUMERO_FLOTANTE} { 
    escribirTiraTokens("NUMERO_FLOTANTE", yytext()); 
    return symbol(sym.NUMERO_FLOTANTE, Float.parseFloat(yytext()));
}

{LITERAL} { 
    escribirTiraTokens("LITERAL", yytext()); 
    return symbol(sym.LITERAL, yytext());
}

<<EOF>> { return symbol(sym.EOF); }

. { 
    escribirError(yytext());
}