package com.persistencia.analizadorLexico;

import java.util.ArrayList;
import java.util.List;

public class lexer {
    private String codigo;
    private int inicio = 0;
    private int actual = 0;
    private int linea = 1;
    private final List<token> tokens = new ArrayList<>();
    
    // Constructor
    public lexer(String codigo){
        this.codigo = codigo;
    }

    public List<token> scanTokens(){
        while(!estaAlFinal()){
            this.inicio = this.actual;
            scanToken();
        }
        // Añadimos los tokens al final del archivo
        tokens.add(new token(tipoToken.EOF, "", linea, inicio));
        return tokens;
    }

    // Escaneamos los tokens
    private void scanToken(){
        char c = avanzar();
        switch (c) {
            case '.':
                aniadirToken(tipoToken.PUNTO);
                break;
            case '[':
                aniadirToken(tipoToken.CORCHETE_IZQ);
                break;
            case ']':
                aniadirToken(tipoToken.CORCHETE_DER);
                break;
            case '(':
                aniadirToken(tipoToken.PARENTESIS_IZQ);
                break;
            case ')':
                aniadirToken(tipoToken.PARENTESIS_DER);
                break;
            case '{':
                aniadirToken(tipoToken.LLAVE_IZQ);
                break;
            case '}':
                aniadirToken(tipoToken.LLAVE_DER);
                break;
            case ',':
                aniadirToken(tipoToken.COMA);
                break;
            case '=':
                aniadirToken(tipoToken.ASIGNACION);
                break;
            case ';':
                aniadirToken(tipoToken.PUNTO_Y_COMA);
                break;
            case '<':
                if(match('='))
                    aniadirToken(tipoToken.MENOR_IGUAL);
                else
                    aniadirToken(tipoToken.MENOR_QUE);
                break;
            case '>':
                if (match('='))
                    aniadirToken(tipoToken.MAYOR_IGUAL);
                else
                    aniadirToken(tipoToken.MAYOR_QUE);
                break;
            case '+':
                if (match('='))
                    aniadirToken(tipoToken.MAS_IGUAL);
                else if (match('+')) 
                    aniadirToken(tipoToken.INCREMENTO);
                else
                    aniadirToken(tipoToken.SUMA);
                break;
            case '-':
                if (match('='))
                    aniadirToken(tipoToken.DECREMENTO);
                else
                    aniadirToken(tipoToken.RESTA);
                break;
            case '*':
                if (match('='))
                    aniadirToken(tipoToken.MULTIPLICACION_ASIGNACION);
                else
                    aniadirToken(tipoToken.MULTIPLICACION);
                break;
            case '/':
                if (match('/')) 
                    // es un comentario, entonces avanza hasta el final de la línea
                    while (mirar() != '\n' && !estaAlFinal()) avanzar();
                else if(match('='))
                    aniadirToken(tipoToken.DIVISION_ASIGNACION);
                else
                    aniadirToken(tipoToken.DIVISION);
                break;
            // Ignoramos espacios en blanco, saltos de linea y tabulaciones
            case ' ':
            case '\r':
            case '\t':
                break;
            // Si hay una nueva linea agregamos
            case '\n':
                linea++;
                break;
            // Detectamos literales de cadena
            case '"':
                literal();
                break;    
            default:
                if (esDigito(c)) 
                    numero();
                else if (esLetra(c))
                    identificador();
                else
                    aniadirToken(tipoToken.DESCONOCIDO);
                break;
        }
    }

    // Funcion para reconocer identificadores
    public void identificador(){
        while (esAlfaNumerico(mirar())) {
            avanzar();
        }
        String texto = codigo.substring(inicio, actual);
        tipoToken tipo;
        try {
            tipo = tipoToken.valueOf(texto.toUpperCase());
        }catch (IllegalArgumentException e){
            tipo = tipoToken.IDENTIFICADOR;
        }
        aniadirToken(tipo);
    }

    private boolean esAlfaNumerico(char letra) {
        return esLetra(letra) || esDigito(letra);
    }

    public void numero(){
        while (esDigito(mirar()))
            avanzar();
        // Analizar si es un numero flotante
        if (mirar() == '.' && esDigito(mirarSiguiente())){
            avanzar(); // consumir '.'
            while (esDigito(mirar()))
                avanzar();
            aniadirToken(tipoToken.FLOAT);
        } else {
            aniadirToken(tipoToken.INT);
        }
    }

    // Funcion para detectar literales de cadena
    public void literal(){
        while (mirar() != '"' && !estaAlFinal()) {
            if (mirar() == '\n' || mirar() == '\r') {
                tokens.add(new token(tipoToken.COMILLA, "\"", linea, actual));
                return;
            }
            avanzar();
        }
        if(estaAlFinal()){
            String textoErrorneo = codigo.substring(inicio,actual);
            tokens.add(new token(tipoToken.ERROR_DE_CADENA, textoErrorneo, linea, actual));
            System.out.println("Error en la linea " + linea + ": Cadena sin cerrar");
            return;
        }
        avanzar();
        String valor = codigo.substring(inicio + 1, actual -1);
        aniadirToken(tipoToken.CADENA, valor);
    }

    private char avanzar(){
        return codigo.charAt(actual++);
    }
    // Verificamos si el codigo esta al final
    private boolean estaAlFinal(){
        return actual >= codigo.length();
    }
    // Función para añadir un token
    private void aniadirToken(tipoToken tipo){
        aniadirToken(tipo, null);
    }

    private void aniadirToken(tipoToken tipo, Object literal){
        String texto = codigo.substring(inicio, actual);
        // si el texto actual no es nulo, se usa ese para cadenas
        String lexema = (literal == null) ? texto : literal.toString();
        tokens.add(new token(tipo, lexema, linea, actual));
    }

    private boolean match(char caracter_esperado){
        if(estaAlFinal() || codigo.charAt(actual) != caracter_esperado)
            return false;
        actual++;
        return true;
    }

    private char mirar() {
        if(estaAlFinal())
            return 0;
        return codigo.charAt(actual);
    }

    private boolean esDigito(char digito){
        return digito >= '0' && digito <= '9';
    }

    private boolean esLetra(char letra){
        return (letra >= 'a' && letra <= 'z') || (letra >= 'A' && letra <= 'Z');
    }
    
    private char mirarSiguiente() {
        if (actual + 1 >= codigo.length())
            return '\0';
        return codigo.charAt(actual + 1);
    }
}