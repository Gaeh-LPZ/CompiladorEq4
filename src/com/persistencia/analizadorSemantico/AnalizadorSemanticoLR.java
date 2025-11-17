package com.persistencia.analizadorSemantico;

import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import com.persistencia.analizadorSintacticoLR.tablaLR.lrParser;
import com.persistencia.analizadorSintacticoLR.coleccionCanonica.grammar;
import com.persistencia.analizadorSintacticoLR.coleccionCanonica.production;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class AnalizadorSemanticoLR {
    
    private Map<String, AccionSemantica> acciones;
    private List<PasoAnalisis> corrida;
    private Stack<Object> pilaSemantica;
    private Object resultadoFinal;
    
    public AnalizadorSemanticoLR() {
        this.acciones = new HashMap<>();
        this.corrida = new ArrayList<>();
        this.pilaSemantica = new Stack<>();
    }
    
    /**
     * Cargar acciones semánticas desde archivo
     */
    public void cargarAcciones(String rutaArchivo) throws Exception {
        String contenido = Files.readString(Path.of(rutaArchivo));
        String[] lineas = contenido.split("\n");
        
        System.out.println("\n📖 Cargando acciones semánticas...");
        
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("//")) continue;
            
            int idx = linea.indexOf('{');
            if (idx == -1) continue;
            
            String produccion = linea.substring(0, idx).trim();
            String codigo = linea.substring(idx + 1, linea.lastIndexOf('}')).trim();
            
            // Normalizar caracteres prima
            produccion = normalizarPrima(produccion);
            codigo = normalizarPrima(codigo);
            
            acciones.put(produccion, new AccionSemantica(produccion, codigo));
            System.out.println("   ✅ [" + produccion + "] -> " + codigo);
        }
        
        System.out.println("📊 Total acciones: " + acciones.size());
    }
    
    /**
     * Normalizar diferentes representaciones de prima a apóstrofo simple
     */
    private String normalizarPrima(String texto) {
        return texto
            .replace("′", "'")  // U+2032 PRIME
            .replace("´", "'")  // U+00B4 ACUTE ACCENT
            .replace("ʹ", "'")  // U+02B9 MODIFIER LETTER PRIME
            .replace("ˊ", "'")  // U+02CA MODIFIER LETTER ACUTE ACCENT
            .replace("'", "'"); // U+2019 RIGHT SINGLE QUOTATION MARK
    }
    
    /**
     * Ejecutar análisis sintáctico-semántico
     */
    public ResultadoAnalisis analizar(String rutaGramatica, String rutaCodigo) throws Exception {
        // 1. Construir tabla LR
        lr0Table.Result table = lr0Table.buildFromFile(rutaGramatica);
        
        // 2. Análisis léxico CON lexemas reales
        String programa = Files.readString(Path.of(rutaCodigo));
        List<TokenLexema> tokensConLexemas = analizarLexicoConLexemas(programa, table.terminals);
        
        // Extraer solo tokens para mostrar
        List<String> tokens = new ArrayList<>();
        for (TokenLexema tl : tokensConLexemas) {
            tokens.add(tl.token);
        }
        
        // 3. Cargar producciones
        grammar gAug = grammar.parseAugmentedGrammar(rutaGramatica);
        List<production> rules = new ArrayList<>();
        for (String A : gAug.byLeft.keySet()) {
            if (A.endsWith("'")) continue;
            rules.addAll(gAug.byLeft.get(A));
        }
        
        // 4. Análisis sintáctico-semántico
        analizarConSemantica(table, tokensConLexemas, rules);
        
        return new ResultadoAnalisis(tokens, corrida, resultadoFinal);
    }
    
    /**
     * Análisis léxico que conserva los lexemas originales
     */
    private List<TokenLexema> analizarLexicoConLexemas(String programa, List<String> terminales) {
        List<TokenLexema> resultado = new ArrayList<>();
        Set<String> keywords = new HashSet<>(Arrays.asList("int", "float", "char", "double", "if", "while", "return"));
        
        int i = 0, n = programa.length();
        while (i < n) {
            char ch = programa.charAt(i);
            
            if (Character.isWhitespace(ch)) { 
                i++; 
                continue; 
            }
            
            if (Character.isLetter(ch) || ch == '_') {
                int j = i + 1;
                while (j < n && (Character.isLetterOrDigit(programa.charAt(j)) || programa.charAt(j) == '_')) 
                    j++;
                String lexema = programa.substring(i, j);
                
                if (keywords.contains(lexema)) {
                    resultado.add(new TokenLexema(lexema, lexema));
                } else {
                    resultado.add(new TokenLexema("id", lexema));
                }
                i = j;
                continue;
            }
            
            if (Character.isDigit(ch)) {
                int j = i + 1;
                while (j < n && Character.isDigit(programa.charAt(j))) j++;
                String lexema = programa.substring(i, j);
                resultado.add(new TokenLexema("num", lexema));
                i = j;
                continue;
            }
            
            if (ch == '[' || ch == ']') {
                resultado.add(new TokenLexema(String.valueOf(ch), String.valueOf(ch)));
                i++;
                continue;
            }
            
            String simbolo = String.valueOf(ch);
            if (terminales.contains(simbolo)) {
                resultado.add(new TokenLexema(simbolo, simbolo));
                i++;
                continue;
            }
            
            i++;
        }
        
        resultado.add(new TokenLexema("$", "$"));
        return resultado;
    }
    
    /**
     * Motor del análisis LR con pila semántica
     */
    private void analizarConSemantica(lr0Table.Result table, List<TokenLexema> tokensLex, List<production> rules) 
            throws Exception {
        
        Deque<Object> pilaSintactica = new ArrayDeque<>();
        pilaSintactica.push(0);
        pilaSemantica.clear();
        corrida.clear();
        
        int ptr = 0;
        int paso = 1;
        
        while (true) {
            int estado = (int) pilaSintactica.peek();
            String simboloActual = (ptr < tokensLex.size()) ? tokensLex.get(ptr).token : "$";
            
            String accion = table.action.getOrDefault(estado, Map.of()).get(simboloActual);
            
            List<String> tokensRestantes = new ArrayList<>();
            for (int k = ptr; k < tokensLex.size(); k++) {
                tokensRestantes.add(tokensLex.get(k).token);
            }
            String entradaStr = String.join(" ", tokensRestantes);
            String pilaStr = renderPila(pilaSintactica);
            
            if (accion == null || accion.isBlank()) {
                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr, 
                    "ERROR", "Error sintáctico"));
                throw new Exception("Error sintáctico en: " + simboloActual);
            }
            
            if (accion.startsWith("d")) {
                int j = Integer.parseInt(accion.substring(1));
                
                String lexemaReal = tokensLex.get(ptr).lexema;
                Object valor = obtenerValorToken(simboloActual, lexemaReal);
                pilaSemantica.push(valor);
                
                pilaSintactica.push(simboloActual);
                pilaSintactica.push(j);
                
                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr, 
                    "Desplazar " + j, ""));
                
                ptr++;
                continue;
            }
            
            if (accion.startsWith("r")) {
                int k = Integer.parseInt(accion.substring(1));
                production prod = rules.get(k - 1);
                
                int betaLen = calcularLongitud(prod.right);
                
                for (int i = 0; i < betaLen; i++) {
                    pilaSintactica.pop();
                    pilaSintactica.pop();
                }
                
                String salidaSemantica = ejecutarAccion(prod, betaLen);
                
                int j = (int) pilaSintactica.peek();
                pilaSintactica.push(prod.left);
                Integer nuevoEstado = table.gotoTable.getOrDefault(j, Map.of()).get(prod.left);
                
                if (nuevoEstado == null) {
                    throw new Exception("GOTO[" + j + ", " + prod.left + "] no definido");
                }
                
                pilaSintactica.push(nuevoEstado);
                
                String produccionStr = prod.left + " → " + String.join(" ", prod.right);
                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr, 
                    "Reducir r" + k + ": " + produccionStr, salidaSemantica));
                
                continue;
            }
            
            if (accion.equalsIgnoreCase("acep") || accion.equalsIgnoreCase("aceptar")) {
                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr, 
                    "Aceptar", ""));
                
                if (!pilaSemantica.isEmpty()) {
                    resultadoFinal = pilaSemantica.peek();
                }
                break;
            }
            
            corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr, 
                accion, ""));
            break;
        }
    }
    
    /**
     * Ejecutar acción semántica con soporte para if-else
     */
    private String ejecutarAccion(production prod, int betaLen) {
        String produccionStr = prod.left + "->" + String.join(" ", prod.right);
        produccionStr = normalizarPrima(produccionStr);
        
        AccionSemantica accion = acciones.get(produccionStr);
        
        List<Object> valores = new ArrayList<>();
        for (int i = 0; i < betaLen; i++) {
            valores.add(0, pilaSemantica.pop());
        }
        
        if (accion == null) {
            Object valorConservar = obtenerValorPredeterminado(valores);
            pilaSemantica.push(valorConservar);
            return "";
        }
        
        String codigo = accion.getCodigo();
        
        try {
            Map<String, Object> contexto = crearContexto(prod.right, valores);
            
            if (codigo.contains("if")) {
                return ejecutarAccionCondicional(codigo, contexto, prod.left);
            } else {
                return ejecutarAccionSimple(codigo, contexto, prod.left);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            Object valorPredeterminado = obtenerValorPredeterminado(valores);
            pilaSemantica.push(valorPredeterminado);
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Crear contexto mapeando símbolos a valores
     */
    private Map<String, Object> crearContexto(List<String> simbolos, List<Object> valores) {
        Map<String, Object> contexto = new HashMap<>();
        Map<String, Integer> contadores = new HashMap<>();
        
        if (valores.isEmpty() || simbolos.isEmpty()) {
            return contexto;
        }
        
        for (int i = 0; i < simbolos.size() && i < valores.size(); i++) {
            String simbolo = normalizarPrima(simbolos.get(i));
            
            if (simbolo.equals("ε") || simbolo.equalsIgnoreCase("epsilon")) {
                continue;
            }
            
            Object valor = valores.get(i);
            int count = contadores.getOrDefault(simbolo, 0);
            contadores.put(simbolo, count + 1);
            
            contexto.put(simbolo + ".valex", valor);
            contexto.put(simbolo + ".lexval", valor);
            contexto.put(simbolo + ".trad", valor);
            contexto.put(simbolo + ".val", valor);
            
            if (count > 0) {
                contexto.put(simbolo + (count + 1) + ".valex", valor);
                contexto.put(simbolo + (count + 1) + ".lexval", valor);
                contexto.put(simbolo + (count + 1) + ".trad", valor);
                contexto.put(simbolo + (count + 1) + ".val", valor);
            }
            
            if (count == 1) {
                contexto.put(simbolo + "'.valex", valor);
                contexto.put(simbolo + "'.lexval", valor);
                contexto.put(simbolo + "'.trad", valor);
                contexto.put(simbolo + "'.val", valor);
            }
        }
        
        return contexto;
    }
    
    /**
     * Ejecutar acción con if-else
     */
    private String ejecutarAccionCondicional(String codigo, Map<String, Object> contexto, String ladoIzq) {
        Pattern pattern = Pattern.compile(
            "if\\s*\\((.+?)\\)\\s*\\{(.+?)\\}(?:\\s*else\\s*\\{(.+?)\\})?",
            Pattern.DOTALL
        );
        
        Matcher matcher = pattern.matcher(codigo);
        if (!matcher.find()) {
            throw new RuntimeException("No se pudo parsear if-else");
        }
        
        String condicion = matcher.group(1).trim();
        String accionTrue = matcher.group(2).trim();
        String accionFalse = matcher.group(3) != null ? matcher.group(3).trim() : "";
        
        boolean resultado = evaluarCondicion(condicion, contexto);
        String accionSeleccionada = resultado ? accionTrue : accionFalse;
        
        if (accionSeleccionada.isEmpty()) {
            pilaSemantica.push("");
            return "";
        }
        
        return ejecutarAsignacion(accionSeleccionada, contexto, ladoIzq, "");
    }
    
    /**
     * Evaluar condición booleana
     */
    private boolean evaluarCondicion(String condicion, Map<String, Object> contexto) {
        condicion = condicion.trim();
        
        String[] operadores = {"!=", "==", "<=", ">=", "<", ">"};
        
        for (String op : operadores) {
            if (condicion.contains(op)) {
                String[] partes = condicion.split(Pattern.quote(op), 2);
                String izq = evaluarExpresion(partes[0].trim(), contexto);
                String der = evaluarExpresion(partes[1].trim(), contexto);
                
                return compararValores(izq, der, op);
            }
        }
        
        String valor = evaluarExpresion(condicion, contexto);
        return !valor.isEmpty() && !valor.equals("false") && !valor.equals("0");
    }
    
    /**
     * Comparar valores
     */
    private boolean compararValores(String izq, String der, String operador) {
        switch (operador) {
            case "==": return izq.equals(der);
            case "!=": return !izq.equals(der);
            case "<": return compararNumericamente(izq, der) < 0;
            case ">": return compararNumericamente(izq, der) > 0;
            case "<=": return compararNumericamente(izq, der) <= 0;
            case ">=": return compararNumericamente(izq, der) >= 0;
            default: return false;
        }
    }
    
    private int compararNumericamente(String a, String b) {
        try {
            double numA = Double.parseDouble(a);
            double numB = Double.parseDouble(b);
            return Double.compare(numA, numB);
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }
    
    /**
     * Ejecutar acción simple
     */
    private String ejecutarAccionSimple(String codigo, Map<String, Object> contexto, String ladoIzq) {
        return ejecutarAsignacion(codigo, contexto, ladoIzq, "");
    }
    
    /**
     * Ejecutar asignación
     */
    private String ejecutarAsignacion(String codigo, Map<String, Object> contexto, String ladoIzq, String prefijo) {
        Pattern pattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_']*)\\.([a-zA-Z_][a-zA-Z0-9_]*)\\s*=\\s*(.+)");
        Matcher matcher = pattern.matcher(codigo);
        
        if (!matcher.find()) {
            pilaSemantica.push("");
            return codigo;
        }
        
        String simbolo = matcher.group(1);
        String atributo = matcher.group(2);
        String expresion = matcher.group(3).trim();
        
        if (expresion.endsWith(";")) {
            expresion = expresion.substring(0, expresion.length() - 1).trim();
        }
        
        System.out.println("🔧 Evaluando expresión: " + expresion);
        System.out.println("📋 Contexto disponible: " + contexto);
        
        String resultado = evaluarExpresion(expresion, contexto);
        
        System.out.println("✨ Resultado evaluado: \"" + resultado + "\"");
        
        pilaSemantica.push(resultado);
        
        return simbolo + "." + atributo + " = \"" + resultado + "\"";
    }
    
    /**
     * Evaluar expresión
     */
    private String evaluarExpresion(String expresion, Map<String, Object> contexto) {
        expresion = expresion.trim();
        
        System.out.println("      🔍 evaluarExpresion: \"" + expresion + "\"");
        
        // String literal: "texto"
        if (expresion.startsWith("\"") && expresion.endsWith("\"") && !expresion.substring(1, expresion.length()-1).contains("\"")) {
            String resultado = expresion.substring(1, expresion.length() - 1);
            System.out.println("      → String literal: \"" + resultado + "\"");
            return resultado;
        }
        
        // Expresión aritmética entre paréntesis: (num.valex-1)
        if (expresion.startsWith("(") && expresion.endsWith(")") && !expresion.substring(1, expresion.length()-1).contains("(")) {
            String interior = expresion.substring(1, expresion.length() - 1).trim();
            System.out.println("      → Paréntesis, evaluando interior: \"" + interior + "\"");
            return evaluarAritmetica(interior, contexto);
        }
        
        // Concatenación con + (PRIORIDAD: verificar ANTES de verificar si es referencia)
        if (expresion.contains("+")) {
            System.out.println("      → Contiene +, evaluando concatenación");
            return evaluarConcatenacion(expresion, contexto);
        }
        
        // Concatenación con ||
        if (expresion.contains("||")) {
            System.out.println("      → Contiene ||, convirtiendo a +");
            expresion = expresion.replace("||", "+");
            return evaluarConcatenacion(expresion, contexto);
        }
        
        // Referencia a atributo: id.valex, V'.trad, E1.val
        if (expresion.matches("[a-zA-Z_][a-zA-Z0-9_']*\\.[a-zA-Z_][a-zA-Z0-9_]*")) {
            Object valor = contexto.get(expresion);
            String resultado = safeString(valor);
            System.out.println("      → Referencia " + expresion + " = \"" + resultado + "\"");
            return resultado;
        }
        
        // Literal numérico
        if (expresion.matches("\\d+")) {
            System.out.println("      → Número literal: " + expresion);
            return expresion;
        }
        
        // Expresiones aritméticas simples
        if (expresion.matches(".+[+\\-*/].+")) {
            System.out.println("      → Aritmética detectada");
            return evaluarAritmetica(expresion, contexto);
        }
        
        System.out.println("      → Valor por defecto: \"" + expresion + "\"");
        return expresion;
    }
    
    /**
     * Evaluar concatenación
     */
    private String evaluarConcatenacion(String expresion, Map<String, Object> contexto) {
        StringBuilder resultado = new StringBuilder();
        List<String> partes = splitRespetandoDelimitadores(expresion, '+');
        
        for (String parte : partes) {
            parte = parte.trim();
            String valor = evaluarExpresion(parte, contexto);
            resultado.append(valor);
        }
        
        return resultado.toString();
    }
    
    /**
     * Split respetando comillas y paréntesis
     */
    private List<String> splitRespetandoDelimitadores(String texto, char delimitador) {
        List<String> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroComillas = false;
        int nivelParentesis = 0;
        
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            
            if (ch == '"') {
                dentroComillas = !dentroComillas;
                actual.append(ch);
            } else if (ch == '(' && !dentroComillas) {
                nivelParentesis++;
                actual.append(ch);
            } else if (ch == ')' && !dentroComillas) {
                nivelParentesis--;
                actual.append(ch);
            } else if (ch == delimitador && !dentroComillas && nivelParentesis == 0) {
                if (actual.length() > 0) {
                    partes.add(actual.toString());
                    actual = new StringBuilder();
                }
            } else {
                actual.append(ch);
            }
        }
        
        if (actual.length() > 0) {
            partes.add(actual.toString());
        }
        
        return partes;
    }
    
    /**
     * Evaluar aritmética
     */
    private String evaluarAritmetica(String expresion, Map<String, Object> contexto) {
        try {
            expresion = expresion.trim();
            
            if (expresion.startsWith("(") && expresion.endsWith(")")) {
                expresion = expresion.substring(1, expresion.length() - 1).trim();
            }
            
            char operador = ' ';
            int posOperador = -1;
            
            for (int i = expresion.length() - 1; i >= 0; i--) {
                char ch = expresion.charAt(i);
                if ((ch == '+' || ch == '-') && i > 0) {
                    operador = ch;
                    posOperador = i;
                    break;
                }
            }
            
            if (posOperador == -1) {
                for (int i = expresion.length() - 1; i >= 0; i--) {
                    char ch = expresion.charAt(i);
                    if (ch == '*' || ch == '/') {
                        operador = ch;
                        posOperador = i;
                        break;
                    }
                }
            }
            
            if (posOperador == -1) {
                String valor = evaluarExpresion(expresion, contexto);
                return valor;
            }
            
            String izqStr = expresion.substring(0, posOperador).trim();
            String derStr = expresion.substring(posOperador + 1).trim();
            
            String izq = evaluarExpresion(izqStr, contexto);
            String der = evaluarExpresion(derStr, contexto);
            
            int numIzq = toInt(izq);
            int numDer = toInt(der);
            int resultado;
            
            switch (operador) {
                case '+': resultado = numIzq + numDer; break;
                case '-': resultado = numIzq - numDer; break;
                case '*': resultado = numIzq * numDer; break;
                case '/': resultado = numIzq / numDer; break;
                default: return expresion;
            }
            
            return String.valueOf(resultado);
            
        } catch (Exception e) {
            return expresion;
        }
    }
    
    // Métodos auxiliares
    private Object obtenerValorPredeterminado(List<Object> valores) {
        Object valorConservar = "";
        for (Object v : valores) {
            if (v != null && !v.toString().isEmpty() && 
                !v.toString().equals(",") && !v.toString().equals(";") &&
                !v.toString().equals("[") && !v.toString().equals("]")) {
                valorConservar = v;
                break;
            }
        }
        return valorConservar;
    }
    
    private int toInt(Object val) {
        if (val instanceof Integer) return (Integer) val;
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String safeString(Object obj) {
        if (obj == null) return "";
        return String.valueOf(obj);
    }
    
    private Object obtenerValorToken(String tipo, String lexema) {
        if (tipo.equals("num") || lexema.matches("\\d+")) {
            try {
                return Integer.parseInt(lexema);
            } catch (Exception e) {
                return lexema;
            }
        }
        
        if (tipo.equals("id")) {
            return lexema;
        }
        
        if (tipo.equals("+") || tipo.equals("*") || tipo.equals("(") || tipo.equals(")") 
            || tipo.equals(",") || tipo.equals(";") || tipo.equals("[") || tipo.equals("]")) {
            return tipo;
        }
        
        return lexema;
    }
    
    private int calcularLongitud(List<String> right) {
        if (right == null || right.isEmpty()) return 0;
        if (right.size() == 1) {
            String s = right.get(0);
            if (s != null) {
                String t = s.trim();
                if (t.equals("ε") || t.equalsIgnoreCase("epsilon")) return 0;
            }
        }
        return right.size();
    }
    
    private String renderPila(Deque<Object> pila) {
        List<Object> lista = new ArrayList<>(pila);
        Collections.reverse(lista);
        StringBuilder sb = new StringBuilder();
        sb.append("$");
        for (Object o : lista) {
            sb.append(" ").append(o);
        }
        return sb.toString();
    }
    
    private static class TokenLexema {
        String token;
        String lexema;
        
        TokenLexema(String token, String lexema) {
            this.token = token;
            this.lexema = lexema;
        }
    }
}