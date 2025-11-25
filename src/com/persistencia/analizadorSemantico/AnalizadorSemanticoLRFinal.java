package com.persistencia.analizadorSemantico;

import com.persistencia.analizadorLexico.lexer;
import com.persistencia.analizadorLexico.tipoToken;
import com.persistencia.analizadorLexico.token;
import com.persistencia.analizadorSintacticoLR.tablaLR.lr0Table;
import com.persistencia.analizadorSintacticoLR.coleccionCanonica.grammar;
import com.persistencia.analizadorSintacticoLR.coleccionCanonica.production;
import com.persistencia.analizadorSemantico.PasoAnalisis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Analizador Semántico LR Final - Proyecto Compiladores
 * Integra: Analizador Léxico -> Sintáctico LR -> Semántico -> Traducción a C++
 */
public class AnalizadorSemanticoLRFinal {

    // RUTAS HARDCODEADAS (según instrucciones del proyecto)
    private static final String RUTA_GRAMATICA = "pruebas/Semantico/final/gramatica.txt";
    private static final String RUTA_ACCIONES = "pruebas/Semantico/final/acciones_semanticas.txt";

    private Map<String, AccionSemantica> acciones;
    private List<PasoAnalisis> corrida;
    private Stack<Object> pilaSemantica;
    private TablaSimbolos tablaSimbolos;
    private List<String> erroresSemanticos;
    private StringBuilder codigoObjeto;
    private int indentLevel = 0;

    public AnalizadorSemanticoLRFinal() {
        this.acciones = new HashMap<>();
        this.corrida = new ArrayList<>();
        this.pilaSemantica = new Stack<>();
        this.tablaSimbolos = new TablaSimbolos();
        this.erroresSemanticos = new ArrayList<>();
        this.codigoObjeto = new StringBuilder();
    }

    /**
     * Análisis completo: Léxico -> Sintáctico -> Semántico -> Traducción
     */
    public ResultadoAnalisisCompleto analizar(String rutaCodigo) throws Exception {
        System.out.println("\n🚀 ========== INICIANDO ANÁLISIS COMPLETO ==========");

        // ✅ DECLARAR VARIABLES AL INICIO DEL MÉTODO
        lr0Table.Result tableLR;
        List<TokenSemantico> tokensSemanticos;

        // 1. Cargar gramática y acciones (hardcodeadas)
        System.out.println("\n📖 FASE 1: Cargando gramática y acciones semánticas...");
        cargarAcciones(RUTA_ACCIONES);

        // 2. Construir tabla LR
        System.out.println("\n📊 FASE 2: Construyendo tabla de análisis sintáctico LR(0)...");
        tableLR = lr0Table.buildFromFile(RUTA_GRAMATICA);
        System.out.println("   ✅ Tabla LR construida exitosamente");

        // 3. Análisis léxico REAL (usando tu lexer del primer parcial)
        System.out.println("\n🔤 FASE 3: Análisis Léxico...");
        String programa = Files.readString(Path.of(rutaCodigo));
        lexer analizadorLexico = new lexer(programa);
        List<token> tokensReales = analizadorLexico.scanTokens();
        System.out.println("   ✅ Tokens generados: " + tokensReales.size());

        // 4. Construir tabla de símbolos
        System.out.println("\n📋 FASE 4: Construyendo tabla de símbolos...");
        construirTablaSimbolos(tokensReales);
        System.out.println("   ✅ Símbolos únicos: " + tablaSimbolos.size());

        // 5. Detectar errores léxicos
        System.out.println("\n⚠️  FASE 5: Verificando errores léxicos...");
        detectarErroresLexicos(tokensReales);

        // 6. Convertir tokens a formato para análisis sintáctico
        System.out.println("\n🔄 FASE 6: Convirtiendo tokens para análisis sintáctico...");
        tokensSemanticos = convertirTokens(tokensReales);

        // 7. Cargar producciones
        System.out.println("\n📝 FASE 7: Cargando producciones de la gramática...");
        grammar gAug = grammar.parseAugmentedGrammar(RUTA_GRAMATICA);
        List<production> rules = obtenerProducciones(gAug);
        System.out.println("   ✅ Producciones cargadas: " + rules.size());

        // 8. Análisis sintáctico-semántico
        System.out.println("\n⚙️  FASE 8: Análisis Sintáctico-Semántico LR...");
        analizarConSemantica(tableLR, tokensSemanticos, rules);
        System.out.println("   ✅ Análisis completado - Pasos: " + corrida.size());

        // ============ DEBUG: VER QUÉ HAY EN LA PILA ============
        System.out.println("\n🔍 DEBUG: Contenido de la pila semántica:");
        System.out.println("   Tamaño: " + pilaSemantica.size());
        if (!pilaSemantica.isEmpty()) {
            System.out.println("   Tope de la pila: " + pilaSemantica.peek());
            System.out.println("   Contenido completo:");
            for (int i = pilaSemantica.size() - 1; i >= 0; i--) {
                Object elemento = pilaSemantica.get(i);
                String preview = elemento.toString();
                if (preview.length() > 100) {
                    preview = preview.substring(0, 100) + "...";
                }
                System.out.println("      [" + i + "]: " + preview);
            }
        }

        System.out.println("\n🔍 DEBUG: Contenido de codigoObjeto StringBuilder:");
        System.out.println("   Longitud: " + codigoObjeto.length());
        if (codigoObjeto.length() > 0) {
            System.out.println("   Contenido:");
            System.out.println(codigoObjeto.toString());
        }

        System.out.println("\n🔍 DEBUG: Últimos 10 pasos con salida semántica:");
        int inicio = Math.max(0, corrida.size() - 10);
        for (int i = inicio; i < corrida.size(); i++) {
            PasoAnalisis paso = corrida.get(i);
            if (paso.getSalida() != null && !paso.getSalida().isEmpty()) {
                System.out.println("   Paso " + paso.getPaso() + ": " + paso.getSalida());
            }
        }

        // 9. Generar código objeto C++
        System.out.println("\n🎯 FASE 9: Generando código objeto (C++)...");

        String codigoGenerado = "";

        // El tope de la pila contiene el resultado final (ya debería estar evaluado)
        if (!pilaSemantica.isEmpty()) {
            Object topePila = pilaSemantica.peek();
            codigoGenerado = safeString(topePila);

            System.out.println("   ✅ Código extraído de la pila semántica");
            System.out.println("   📏 Longitud: " + codigoGenerado.length() + " caracteres");
        } else {
            System.out.println("   ❌ ERROR: Pila semántica vacía");
        }

        // Limpiar duplicados de includes
        if (!codigoGenerado.isEmpty()) {
            String[] lineas = codigoGenerado.split("\n");
            Set<String> includesVistos = new HashSet<>();
            StringBuilder codigoLimpio = new StringBuilder();

            for (String linea : lineas) {
                String lineaTrim = linea.trim();

                if (lineaTrim.startsWith("#include")) {
                    if (!includesVistos.contains(lineaTrim)) {
                        includesVistos.add(lineaTrim);
                        codigoLimpio.append(linea).append("\n");
                    }
                } else if (lineaTrim.equals("using namespace std;")) {
                    if (!includesVistos.contains("using")) {
                        includesVistos.add("using");
                        codigoLimpio.append(linea).append("\n");
                    }
                } else {
                    codigoLimpio.append(linea).append("\n");
                }
            }

            codigoGenerado = codigoLimpio.toString().trim();
        }

        if (codigoGenerado.isEmpty() || codigoGenerado.contains("IDENTIFICADOR.") || codigoGenerado.contains(".trad")) {
            System.out.println("   ⚠️  WARNING: El código contiene referencias sin evaluar");
            System.out.println("   🔍 Muestra: " + codigoGenerado.substring(0, Math.min(200, codigoGenerado.length())));
            codigoGenerado = "// Código C++ generado\n// ERROR: Referencias no evaluadas correctamente\n// Verificar evaluarExpresion()";
            System.out.println("   ❌ No se pudo generar código C++ válido");
        } else {
            System.out.println("   ✅ Código generado exitosamente");
        }

        System.out.println("\n   📄 CÓDIGO C++ GENERADO:");
        System.out.println("   " + "=".repeat(60));
        System.out.println(codigoGenerado);
        System.out.println("   " + "=".repeat(60));

        // ✅ RETURN AL FINAL DEL MÉTODO (mismo nivel que las declaraciones iniciales)
        return new ResultadoAnalisisCompleto(
                tokensSemanticos,
                corrida,
                tablaSimbolos,
                erroresSemanticos,
                codigoGenerado,
                tableLR);
    }

    /**
     * Cargar acciones semánticas desde archivo
     */
    private void cargarAcciones(String rutaArchivo) throws Exception {
        String contenido = Files.readString(Path.of(rutaArchivo));
        String[] lineas = contenido.split("\n");

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("//"))
                continue;

            int idx = linea.indexOf('{');
            if (idx == -1)
                continue;

            String produccion = linea.substring(0, idx).trim();
            String codigo = linea.substring(idx + 1, linea.lastIndexOf('}')).trim();

            produccion = normalizarPrima(produccion);
            codigo = normalizarPrima(codigo);

            acciones.put(produccion, new AccionSemantica(produccion, codigo));
        }

        System.out.println("   ✅ Acciones semánticas cargadas: " + acciones.size());
    }

    /**
     * Construir tabla de símbolos desde tokens - VERSIÓN MEJORADA
     * Detecta tipos, valores y contexto de las variables
     */
    private void construirTablaSimbolos(List<token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            token t = tokens.get(i);

            // Solo procesar identificadores
            if (t.tipo != tipoToken.IDENTIFICADOR) {
                continue;
            }

            // Si ya existe, saltar
            if (tablaSimbolos.existe(t.lexema)) {
                continue;
            }

            // Intentar detectar el tipo y valor
            String tipo = detectarTipo(tokens, i);
            String valor = detectarValor(tokens, i);

            tablaSimbolos.agregar(t.lexema, tipo, valor, t.linea);
        }
    }

    /**
     * Detectar el valor inicial de una variable
     */
    private String detectarValor(List<token> tokens, int posIdentificador) {
        // Buscar hacia adelante un símbolo de asignación
        for (int i = posIdentificador + 1; i < tokens.size() && i < posIdentificador + 10; i++) {
            token t = tokens.get(i);

            // Si encontramos punto y coma o llave, no hay asignación
            if (t.tipo == tipoToken.PUNTO_Y_COMA || t.tipo == tipoToken.LLAVE_IZQ) {
                break;
            }

            // Si encontramos asignación, obtener el valor
            if (t.tipo == tipoToken.ASIGNACION) {
                // El siguiente token es el valor
                if (i + 1 < tokens.size()) {
                    return extraerValorAsignado(tokens, i + 1);
                }
            }
        }

        return "---";
    }

    /**
     * Extraer el valor asignado (puede ser una expresión)
     */
    private String extraerValorAsignado(List<token> tokens, int inicio) {
        StringBuilder valor = new StringBuilder();

        for (int i = inicio; i < tokens.size() && i < inicio + 20; i++) {
            token t = tokens.get(i);

            // Terminar en punto y coma o coma
            if (t.tipo == tipoToken.PUNTO_Y_COMA || t.tipo == tipoToken.COMA) {
                break;
            }

            // Terminar en paréntesis derecho (en caso de parámetros)
            if (t.tipo == tipoToken.PARENTESIS_DER) {
                break;
            }

            // Agregar el lexema
            if (valor.length() > 0) {
                // Agregar espacio entre tokens (excepto para operadores)
                if (t.tipo == tipoToken.PUNTO || t.tipo == tipoToken.CORCHETE_IZQ ||
                        t.tipo == tipoToken.PARENTESIS_IZQ) {
                    valor.append(t.lexema);
                } else {
                    valor.append(" ").append(t.lexema);
                }
            } else {
                valor.append(t.lexema);
            }
        }

        String resultado = valor.toString().trim();

        // Limitar longitud para que no sea demasiado largo
        if (resultado.length() > 50) {
            resultado = resultado.substring(0, 47) + "...";
        }

        return resultado.isEmpty() ? "---" : resultado;
    }

    /**
     * Detectar el tipo de dato de un identificador
     */
    private String detectarTipo(List<token> tokens, int posIdentificador) {
        // Buscar hacia atrás para encontrar el tipo
        for (int i = posIdentificador - 1; i >= 0 && i >= posIdentificador - 5; i--) {
            token t = tokens.get(i);

            // Tipos primitivos
            switch (t.tipo) {
                case INT:
                    return "int";
                case FLOAT:
                    return "float";
                case DOUBLE:
                    return "double";
                case BOOLEAN:
                    return "boolean";
                case CHAR:
                    return "char";
                case BYTE:
                    return "byte";
                case SHORT:
                    return "short";
                case LONG:
                    return "long";
            }

            // Tipos por referencia (String, clases)
            if (t.tipo == tipoToken.IDENTIFICADOR) {
                String lexema = t.lexema;
                // Tipos comunes
                if (lexema.equals("String") || lexema.equals("List") ||
                        lexema.equals("ArrayList") || lexema.equals("Map")) {
                    return lexema;
                }
                // Podría ser una clase (si viene después de modificadores)
                if (i > 0) {
                    token anterior = tokens.get(i - 1);
                    if (anterior.tipo == tipoToken.PUBLIC ||
                            anterior.tipo == tipoToken.PRIVATE ||
                            anterior.tipo == tipoToken.PROTECTED ||
                            anterior.tipo == tipoToken.STATIC ||
                            anterior.tipo == tipoToken.FINAL) {
                        return lexema;
                    }
                }
            }

            // Si encontramos una palabra clave que indica fin de búsqueda
            if (t.tipo == tipoToken.LLAVE_IZQ || t.tipo == tipoToken.PUNTO_Y_COMA) {
                break;
            }
        }

        // Verificar si es un método (tiene paréntesis después)
        if (posIdentificador + 1 < tokens.size()) {
            token siguiente = tokens.get(posIdentificador + 1);
            if (siguiente.tipo == tipoToken.PARENTESIS_IZQ) {
                // Buscar tipo de retorno del método
                for (int i = posIdentificador - 1; i >= 0 && i >= posIdentificador - 5; i--) {
                    token t = tokens.get(i);
                    if (t.tipo == tipoToken.VOID) {
                        return "método void";
                    }
                    if (t.tipo == tipoToken.INT || t.tipo == tipoToken.BOOLEAN ||
                            t.tipo == tipoToken.FLOAT || t.tipo == tipoToken.DOUBLE) {
                        return "método " + t.lexema;
                    }
                    if (t.tipo == tipoToken.IDENTIFICADOR) {
                        return "método " + t.lexema;
                    }
                }
                return "método";
            }
        }

        // Verificar si es una clase (viene después de 'class')
        if (posIdentificador - 1 >= 0) {
            token anterior = tokens.get(posIdentificador - 1);
            if (anterior.tipo == tipoToken.CLASS) {
                return "clase";
            }
            if (anterior.tipo == tipoToken.INTERFACE) {
                return "interfaz";
            }
            if (anterior.tipo == tipoToken.EXTENDS || anterior.tipo == tipoToken.IMPLEMENTS) {
                return "clase/interfaz";
            }
        }

        return "IDENTIFICADOR";
    }

    /**
     * Detectar errores léxicos
     */
    private void detectarErroresLexicos(List<token> tokens) {
        for (token t : tokens) {
            if (t.tipo == tipoToken.DESCONOCIDO) {
                String error = "Línea " + t.linea + ": Token desconocido '" + t.lexema + "'";
                erroresSemanticos.add(error);
                System.out.println("   ❌ " + error);
            }
            if (t.tipo == tipoToken.ERROR_DE_CADENA) {
                String error = "Línea " + t.linea + ": Cadena sin cerrar '" + t.lexema + "'";
                erroresSemanticos.add(error);
                System.out.println("   ❌ " + error);
            }
        }

        if (erroresSemanticos.isEmpty()) {
            System.out.println("   ✅ No se encontraron errores léxicos");
        }
    }

    /**
     * Convertir tokens del lexer a tokens semánticos
     * CON SOPORTE PARA System.out.println Y main
     */
    private List<TokenSemantico> convertirTokens(List<token> tokensReales) {
        List<TokenSemantico> resultado = new ArrayList<>();

        int i = 0;
        while (i < tokensReales.size()) {
            token t = tokensReales.get(i);

            // ============ DETECTAR System.out.println() ============
            // En convertirTokens(), después de crear el token:
            if (t.tipo == tipoToken.LITERAL_CADENA) {
                System.out.println("   🔍 DEBUG LITERAL_CADENA: [" + t.lexema + "]");
            }
            if (t.tipo == tipoToken.SYSTEM && i + 4 < tokensReales.size()) {
                token t1 = tokensReales.get(i + 1);
                token t2 = tokensReales.get(i + 2);
                token t3 = tokensReales.get(i + 3);
                token t4 = tokensReales.get(i + 4);

                if (t1.tipo == tipoToken.PUNTO &&
                        t2.tipo == tipoToken.OUT &&
                        t3.tipo == tipoToken.PUNTO &&
                        t4.tipo == tipoToken.PRINTLN) {

                    resultado.add(new TokenSemantico("IDENTIFICADOR", "println", "println", t.linea));
                    System.out.println("   ✅ System.out.println → println (línea " + t.linea + ")");

                    i += 5;
                    continue;
                }
            }

            // ============ CONVERTIR MAIN A IDENTIFICADOR ============
            if (t.tipo == tipoToken.MAIN) {
                resultado.add(new TokenSemantico("IDENTIFICADOR", "main", "main", t.linea));
                System.out.println("   ✅ main (palabra reservada) → main (identificador) (línea " + t.linea + ")");
                i++;
                continue;
            }

            // Token normal
            String tokenNombre = obtenerNombreToken(t);
            String lexema = t.lexema;
            Object valor = obtenerValorToken(t);

            resultado.add(new TokenSemantico(tokenNombre, lexema, valor, t.linea));
            i++;
        }

        // Agregar EOF si no existe
        if (resultado.isEmpty() || !resultado.get(resultado.size() - 1).token.equals("EOF")) {
            resultado.add(new TokenSemantico("EOF", "$", "$", -1));
        }

        System.out.println("   📊 Total tokens convertidos: " + resultado.size());

        return resultado;
    }

    /**
     * Obtener nombre del token según la gramática
     */
    private String obtenerNombreToken(token t) {
        switch (t.tipo) {
            // Tokens con lexema
            case IDENTIFICADOR:
                return "IDENTIFICADOR";
            case LITERAL_ENTERA:
                return "LITERAL_ENTERA";
            case LITERAL_FLOTANTE:
                return "LITERAL_FLOTANTE";
            case LITERAL_CADENA:
                return "LITERAL_CADENA";
            case EOF:
                return "EOF";

            // ===== TOKENS ESPECIALES DE TU LEXER =====
            case SYSTEM:
                return "SYSTEM";
            case OUT:
                return "OUT";
            case PRINTLN:
                return "PRINTLN";
            case MAIN:
                return "MAIN";
            // ==========================================

            // Palabras reservadas
            case PACKAGE:
                return "PACKAGE";
            case IMPORT:
                return "IMPORT";
            case CLASS:
                return "CLASS";
            case INTERFACE:
                return "INTERFACE";
            case ENUM:
                return "ENUM";
            case PUBLIC:
                return "PUBLIC";
            case PRIVATE:
                return "PRIVATE";
            case PROTECTED:
                return "PROTECTED";
            case ABSTRACT:
                return "ABSTRACT";
            case STATIC:
                return "STATIC";
            case FINAL:
                return "FINAL";
            case SYNCHRONIZED:
                return "SYNCHRONIZED";
            case NATIVE:
                return "NATIVE";
            case TRANSIENT:
                return "TRANSIENT";
            case VOLATILE:
                return "VOLATILE";
            case EXTENDS:
                return "EXTENDS";
            case IMPLEMENTS:
                return "IMPLEMENTS";
            case VOID:
                return "VOID";
            case THROWS:
                return "THROWS";
            case IF:
                return "IF";
            case ELSE:
                return "ELSE";
            case SWITCH:
                return "SWITCH";
            case CASE:
                return "CASE";
            case DEFAULT:
                return "DEFAULT";
            case WHILE:
                return "WHILE";
            case DO:
                return "DO";
            case FOR:
                return "FOR";
            case BREAK:
                return "BREAK";
            case CONTINUE:
                return "CONTINUE";
            case RETURN:
                return "RETURN";
            case THROW:
                return "THROW";
            case TRY:
                return "TRY";
            case CATCH:
                return "CATCH";
            case FINALLY:
                return "FINALLY";
            case BOOLEAN:
                return "BOOLEAN";
            case BYTE:
                return "BYTE";
            case SHORT:
                return "SHORT";
            case INT:
                return "INT";
            case LONG:
                return "LONG";
            case CHAR:
                return "CHAR";
            case FLOAT:
                return "FLOAT";
            case DOUBLE:
                return "DOUBLE";
            case NEW:
                return "NEW";
            case THIS:
                return "THIS";
            case SUPER:
                return "SUPER";
            case NULL:
                return "NULL";
            case TRUE:
                return "TRUE";
            case FALSE:
                return "FALSE";
            case INSTANCEOF:
                return "INSTANCEOF";

            // Símbolos
            case PUNTO:
                return "PUNTO";
            case PUNTO_Y_COMA:
                return "PUNTO_Y_COMA";
            case COMA:
                return "COMA";
            case LLAVE_IZQ:
                return "LLAVE_IZQ";
            case LLAVE_DER:
                return "LLAVE_DER";
            case PARENTESIS_IZQ:
                return "PARENTESIS_IZQ";
            case PARENTESIS_DER:
                return "PARENTESIS_DER";
            case CORCHETE_IZQ:
                return "CORCHETE_IZQ";
            case CORCHETE_DER:
                return "CORCHETE_DER";
            case ASIGNACION:
                return "ASIGNACION";
            case SUMA:
                return "SUMA";
            case RESTA:
                return "RESTA";
            case ASTERISK:
                return "ASTERISK";
            case DIVISION:
                return "DIVISION";
            case MOD:
                return "MOD";
            case IGUAL:
                return "IGUAL";
            case DIFERENTE:
                return "DIFERENTE";
            case MENOR_QUE:
                return "MENOR_QUE";
            case MAYOR_QUE:
                return "MAYOR_QUE";
            case MENOR_IGUAL:
                return "MENOR_IGUAL";
            case MAYOR_IGUAL:
                return "MAYOR_IGUAL";
            case AND:
                return "AND";
            case OR:
                return "OR";
            case NOT:
                return "NOT";
            case BITAND:
                return "BITAND";
            case BITOR:
                return "BITOR";
            case BITXOR:
                return "BITXOR";
            case TILDE:
                return "TILDE";
            case LSHIFT:
                return "LSHIFT";
            case RSHIFT:
                return "RSHIFT";
            case URSHIFT:
                return "URSHIFT";
            case QUESTION:
                return "QUESTION";
            case DOS_PUNTOS:
                return "DOS_PUNTOS";
            case ELLIPSIS:
                return "ELLIPSIS";

            default:
                return t.tipo.toString();
        }
    }

    /**
     * Obtener valor del token para la pila semántica
     */
    private Object obtenerValorToken(token t) {
        switch (t.tipo) {
            case LITERAL_ENTERA:
                try {
                    return Integer.parseInt(t.lexema);
                } catch (Exception e) {
                    return t.lexema;
                }
            case LITERAL_FLOTANTE:
                try {
                    return Double.parseDouble(t.lexema);
                } catch (Exception e) {
                    return t.lexema;
                }
            case LITERAL_CADENA:
                // ✅ FIX: Agregar comillas si no las tiene
                String lexema = t.lexema;
                if (!lexema.startsWith("\"")) {
                    lexema = "\"" + lexema + "\"";
                }
                return lexema;
            case IDENTIFICADOR:
                return t.lexema;
            default:
                return t.lexema;
        }
    }

    /**
     * Obtener producciones de la gramática
     */
    private List<production> obtenerProducciones(grammar gAug) {
        List<production> rules = new ArrayList<>();
        for (String A : gAug.byLeft.keySet()) {
            if (A.endsWith("'"))
                continue;
            rules.addAll(gAug.byLeft.get(A));
        }
        return rules;
    }

    /**
     * Motor del análisis LR con pila semántica - VERSIÓN CORREGIDA
     */
    private void analizarConSemantica(lr0Table.Result table, List<TokenSemantico> tokens,
            List<production> rules) throws Exception {

        Deque<Object> pilaSintactica = new ArrayDeque<>();
        pilaSintactica.push(0);
        pilaSemantica.clear();
        corrida.clear();

        int ptr = 0;
        int paso = 1;

        while (true) {
            int estado = (int) pilaSintactica.peek();
            String simboloActual = (ptr < tokens.size()) ? tokens.get(ptr).token : "$";

            String accion = table.action.getOrDefault(estado, Map.of()).get(simboloActual);

            String entradaStr = construirCadenaEntrada(tokens, ptr);
            String pilaStr = renderPila(pilaSintactica);

            if (accion == null || accion.isBlank()) {
                String lexema = (ptr < tokens.size()) ? tokens.get(ptr).lexema : "$";
                int linea = (ptr < tokens.size()) ? tokens.get(ptr).linea : -1;

                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr,
                        "ERROR", "Error sintáctico"));

                String error = "Error sintáctico en línea " + linea + ": inesperado '" + lexema + "'";
                erroresSemanticos.add(error);
                throw new Exception(error);
            }

            if (accion.startsWith("d")) {
                // DESPLAZAMIENTO
                // DESPLAZAMIENTO
                int j = Integer.parseInt(accion.substring(1));

                // ============ FIX: Empujar el VALOR del token (con comillas para strings)
                // ============
                Object valor = tokens.get(ptr).valor; // ✅ Usar valor que ya tiene las comillas
                pilaSemantica.push(valor);

                pilaSintactica.push(simboloActual);
                pilaSintactica.push(j);

                corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr,
                        "Desplazar " + j, ""));

                ptr++;
                continue;
            }

            if (accion.startsWith("r")) {
                // REDUCCIÓN
                int k = Integer.parseInt(accion.substring(1));
                production prod = rules.get(k - 1);

                int betaLen = calcularLongitud(prod.right);

                // Pop de pila sintáctica
                for (int i = 0; i < betaLen; i++) {
                    pilaSintactica.pop(); // símbolo
                    pilaSintactica.pop(); // estado
                }

                // Ejecutar acción semántica (con protección de pila)
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
                        "Aceptar", "✅ Análisis completado exitosamente"));
                break;
            }

            corrida.add(new PasoAnalisis(paso++, pilaStr, entradaStr,
                    accion, ""));
            break;
        }
    }

    /**
     * Convertir println(args) a cout << args << endl
     */
    private String convertirPrintlnACout(String traduccion) {
        // Si contiene println, convertirlo a cout
        if (traduccion.contains("println(")) {
            // Extraer el contenido entre paréntesis
            int inicio = traduccion.indexOf("println(") + 8;
            int fin = traduccion.lastIndexOf(")");

            if (inicio > 8 && fin > inicio) {
                String argumentos = traduccion.substring(inicio, fin);
                return "cout << " + argumentos + " << endl";
            }
        }

        return traduccion;
    }

    /**
     * Ejecutar acción semántica - VERSIÓN CORREGIDA Y COMPLETA
     */
    private String ejecutarAccion(production prod, int betaLen) {
        String produccionStr = prod.left + " -> " + String.join(" ", prod.right);
        produccionStr = normalizarPrima(produccionStr);

        AccionSemantica accion = acciones.get(produccionStr);

        // Protección contra pila insuficiente
        if (pilaSemantica.size() < betaLen) {
            System.err.println("\n   ⚠️  WARNING: Pila semántica insuficiente");
            System.err.println("      Producción: " + produccionStr);
            System.err.println("      Se necesitan: " + betaLen + " elementos");
            System.err.println("      Disponibles: " + pilaSemantica.size());

            List<Object> valores = new ArrayList<>();
            int disponibles = Math.min(pilaSemantica.size(), betaLen);

            for (int i = 0; i < disponibles; i++) {
                valores.add(0, pilaSemantica.pop());
            }

            for (int i = disponibles; i < betaLen; i++) {
                valores.add(0, "");
            }

            Object valorConservar = obtenerValorPredeterminado(valores);
            pilaSemantica.push(valorConservar);
            return "";
        }

        // Obtener valores de la pila
        List<Object> valores = new ArrayList<>();
        for (int i = 0; i < betaLen; i++) {
            valores.add(0, pilaSemantica.pop());
        }

        // Si no hay acción, conservar primer valor significativo
        if (accion == null) {
            Object valorConservar = obtenerValorPredeterminado(valores);
            pilaSemantica.push(valorConservar);
            return "";
        }

        // ============ EVALUAR LA ACCIÓN SEMÁNTICA ============
        String codigo = accion.getCodigo();
        String resultado = evaluarAccionSemantica(codigo, prod.right, valores);

        // ============ CRÍTICO: Pushear resultado evaluado ============
        pilaSemantica.push(resultado);

        // ============ RETORNAR PARA LA CORRIDA (con formato) ============
        // Formato: "NoTerminal.trad = valor"
        String salidaParaCorrida = prod.left + ".trad = " + resultado;

        return salidaParaCorrida;
    }

    /**
     * Evaluar acción semántica - VERSIÓN MEJORADA CON DEBUG
     */
    private String evaluarAccionSemantica(String codigo, List<String> simbolos, List<Object> valores) {
        // Crear contexto con los valores de los símbolos
        Map<String, Object> contexto = crearContexto(simbolos, valores);

        // Extraer expresión (remover LHS si existe: "X.trad = ...")
        String expresion = codigo;
        if (codigo.contains(" = ")) {
            String[] partes = codigo.split(" = ", 2);
            if (partes.length > 1) {
                expresion = partes[1].trim();
            }
        }

        // Evaluar la expresión
        String resultado = evaluarExpresion(expresion, contexto);

        return resultado;
    }

    /**
     * Crear contexto - VERSIÓN MEJORADA
     */
    private Map<String, Object> crearContexto(List<String> simbolos, List<Object> valores) {
        Map<String, Object> contexto = new HashMap<>();
        Map<String, Integer> contadores = new HashMap<>();

        for (int i = 0; i < simbolos.size() && i < valores.size(); i++) {
            String simbolo = normalizarPrima(simbolos.get(i));

            // Ignorar epsilon
            if (simbolo.equals("ε") || simbolo.equalsIgnoreCase("epsilon")) {
                continue;
            }

            Object valor = valores.get(i);
            String valorString = (valor != null) ? valor.toString() : "";

            int count = contadores.getOrDefault(simbolo, 0);
            contadores.put(simbolo, count + 1);

            // ============ CLAVE: Guardar con TODOS los formatos posibles ============

            // Formato base (primera ocurrencia)
            if (count == 0) {
                contexto.put(simbolo + ".trad", valorString);
                contexto.put(simbolo + ".val", valorString);
                contexto.put(simbolo + ".lexema", valorString);
            }

            // Formato con número (segunda+ ocurrencia)
            if (count > 0) {
                String sufijo = String.valueOf(count + 1);
                contexto.put(simbolo + sufijo + ".trad", valorString);
                contexto.put(simbolo + sufijo + ".val", valorString);
                contexto.put(simbolo + sufijo + ".lexema", valorString);
            }

            // Formato con prima (segunda ocurrencia específicamente)
            if (count == 1) {
                contexto.put(simbolo + "'.trad", valorString);
                contexto.put(simbolo + "'.val", valorString);
                contexto.put(simbolo + "'.lexema", valorString);
            }
        }

        return contexto;
    }

    /**
     * Evaluar expresión - VERSIÓN TOTALMENTE CORREGIDA
     */
    private String evaluarExpresion(String expresion, Map<String, Object> contexto) {
        expresion = expresion.trim();

        // Caso 1: String literal puro ("texto")
        if (expresion.startsWith("\"") && expresion.endsWith("\"") &&
                expresion.length() >= 2 && !expresion.substring(1, expresion.length() - 1).contains("\"")) {
            String contenido = expresion.substring(1, expresion.length() - 1);
            return procesarEscapes(contenido);
        }

        // Caso 2: Referencia directa (X.trad, X.lexema, X2.trad, X'.trad)
        if (expresion.matches("[a-zA-Z_][a-zA-Z0-9_']*\\.[a-zA-Z_][a-zA-Z0-9_]*")) {
            Object valor = contexto.get(expresion);
            if (valor != null) {
                return safeString(valor);
            }
            // Si no existe, retornar vacío
            return "";
        }

        // Caso 3: Concatenación con +
        if (expresion.contains("+")) {
            return evaluarConcatenacion(expresion, contexto);
        }

        // Caso 4: Texto plano
        return expresion;
    }

    /**
     * Evaluar concatenación - VERSIÓN MEJORADA CON MEJOR PARSING
     */
    private String evaluarConcatenacion(String expresion, Map<String, Object> contexto) {
        StringBuilder resultado = new StringBuilder();
        List<String> partes = splitPorPlusMejorado(expresion);

        for (String parte : partes) {
            parte = parte.trim();
            if (parte.isEmpty())
                continue;

            // Evaluar recursivamente cada parte
            String valorEvaluado = evaluarExpresion(parte, contexto);
            resultado.append(valorEvaluado);
        }

        return resultado.toString();
    }

    /**
     * Procesar escapes en strings
     */
    private String procesarEscapes(String texto) {
        return texto.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    /**
     * Split por + MEJORADO - Maneja strings con espacios y referencias
     */
    private List<String> splitPorPlusMejorado(String texto) {
        List<String> partes = new ArrayList<>();
        StringBuilder actual = new StringBuilder();
        boolean dentroComillas = false;
        boolean escape = false;
        int nivel = 0; // Para paréntesis anidados

        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);

            // Manejar escape
            if (escape) {
                actual.append(ch);
                escape = false;
                continue;
            }

            if (ch == '\\') {
                escape = true;
                actual.append(ch);
                continue;
            }

            // Comillas
            if (ch == '"') {
                dentroComillas = !dentroComillas;
                actual.append(ch);
                continue;
            }

            // Paréntesis (para manejar expresiones complejas)
            if (!dentroComillas) {
                if (ch == '(') {
                    nivel++;
                    actual.append(ch);
                    continue;
                }
                if (ch == ')') {
                    nivel--;
                    actual.append(ch);
                    continue;
                }
            }

            // Operador + FUERA de comillas y paréntesis
            if (ch == '+' && !dentroComillas && nivel == 0) {
                if (actual.length() > 0) {
                    partes.add(actual.toString().trim());
                    actual = new StringBuilder();
                }
                continue;
            }

            actual.append(ch);
        }

        // Agregar última parte
        if (actual.length() > 0) {
            partes.add(actual.toString().trim());
        }

        return partes;
    }

    /**
     * Obtener valor predeterminado - MEJORADO
     */
    private Object obtenerValorPredeterminado(List<Object> valores) {
        // Buscar el primer valor no vacío y significativo
        for (Object v : valores) {
            if (v != null) {
                String s = v.toString().trim();
                if (!s.isEmpty() &&
                        !s.equals(",") &&
                        !s.equals(";") &&
                        !s.equals("{") &&
                        !s.equals("}") &&
                        !s.equals("(") &&
                        !s.equals(")")) {
                    return v;
                }
            }
        }
        return ""; // Retornar string vacío si no hay nada útil
    }

    private int calcularLongitud(List<String> right) {
        if (right == null || right.isEmpty())
            return 0;
        if (right.size() == 1) {
            String s = right.get(0);
            if (s != null && (s.trim().equals("ε") || s.trim().equalsIgnoreCase("epsilon"))) {
                return 0;
            }
        }
        return right.size();
    }

    private String renderPila(Deque<Object> pila) {
        List<Object> lista = new ArrayList<>(pila);
        Collections.reverse(lista);
        StringBuilder sb = new StringBuilder("$");
        for (Object o : lista) {
            sb.append(" ").append(o);
        }
        return sb.toString();
    }

    private String construirCadenaEntrada(List<TokenSemantico> tokens, int desde) {
        StringBuilder sb = new StringBuilder();
        for (int i = desde; i < Math.min(desde + 10, tokens.size()); i++) {
            sb.append(tokens.get(i).token).append(" ");
        }
        if (desde + 10 < tokens.size()) {
            sb.append("...");
        }
        return sb.toString().trim();
    }

    private String safeString(Object obj) {
        return (obj == null) ? "" : String.valueOf(obj);
    }

    // ==================== CLASES INTERNAS ====================

    /**
     * Token con información semántica
     */
    public static class TokenSemantico {
        private String token;
        private String lexema;
        private Object valor;
        private int linea;

        public TokenSemantico(String token, String lexema, Object valor, int linea) {
            this.token = token;
            this.lexema = lexema;
            this.valor = valor;
            this.linea = linea;
        }

        // Getters
        public String getToken() {
            return token;
        }

        public String getLexema() {
            return lexema;
        }

        public Object getValor() {
            return valor;
        }

        public int getLinea() {
            return linea;
        }

        @Override
        public String toString() {
            return lexema + " " + token + " " + linea;
        }
    }

    /**
     * Tabla de símbolos
     */
    public static class TablaSimbolos {
        private List<Simbolo> simbolos = new ArrayList<>();
        private Map<String, Simbolo> mapa = new HashMap<>();

        public void agregar(String nombre, String tipo, String valor, int linea) {
            if (!mapa.containsKey(nombre)) {
                Simbolo s = new Simbolo(simbolos.size() + 1, nombre, tipo, valor, linea);
                simbolos.add(s);
                mapa.put(nombre, s);
            }
        }

        public boolean existe(String nombre) {
            return mapa.containsKey(nombre);
        }

        public Simbolo obtener(String nombre) {
            return mapa.get(nombre);
        }

        public int size() {
            return simbolos.size();
        }

        public List<Simbolo> getSimbolos() {
            return simbolos;
        }

        public static class Simbolo {
            int id;
            String nombre;
            String tipo;
            String valor;
            int linea;

            Simbolo(int id, String nombre, String tipo, String valor, int linea) {
                this.id = id;
                this.nombre = nombre;
                this.tipo = tipo;
                this.valor = valor;
                this.linea = linea;
            }

            public Object[] toArray() {
                return new Object[] { id, nombre, tipo, valor, linea };
            }
        }
    }

    /**
     * Resultado del análisis completo
     */
    public static class ResultadoAnalisisCompleto {
        private List<TokenSemantico> tokens;
        private List<PasoAnalisis> corrida;
        private TablaSimbolos tablaSimbolos;
        private List<String> errores;
        private String codigoObjeto;
        private lr0Table.Result tablaLR;

        public ResultadoAnalisisCompleto(List<TokenSemantico> tokens, List<PasoAnalisis> corrida,
                TablaSimbolos tablaSimbolos, List<String> errores,
                String codigoObjeto, lr0Table.Result tablaLR) {
            this.tokens = tokens;
            this.corrida = corrida;
            this.tablaSimbolos = tablaSimbolos;
            this.errores = errores;
            this.codigoObjeto = codigoObjeto;
            this.tablaLR = tablaLR;
        }

        // Getters
        public lr0Table.Result getTablaLR() {
            return tablaLR;
        }

        public List<TokenSemantico> getTokens() {
            return tokens;
        }

        public List<String> getTokensString() {
            List<String> result = new ArrayList<>();
            for (TokenSemantico t : tokens) {
                result.add(t.toString());
            }
            return result;
        }

        public List<PasoAnalisis> getCorrida() {
            return corrida;
        }

        public TablaSimbolos getTablaSimbolos() {
            return tablaSimbolos;
        }

        public List<String> getErrores() {
            return errores;
        }

        public String getCodigoObjeto() {
            return codigoObjeto;
        }

        public boolean tieneErrores() {
            return !errores.isEmpty();
        }
    }

    // ==================== AGREGAR ESTAS CLASES Y MÉTODOS ====================

    /**
     * Normalizar caracteres prima/apostrofe a estándar
     */
    private String normalizarPrima(String texto) {
        return texto
                .replace("′", "'")
                .replace("´", "'")
                .replace("ʹ", "'")
                .replace("ˊ", "'")
                .replace("'", "'");
    }

    /**
     * Clase para representar una acción semántica
     */
    private static class AccionSemantica {
        private String produccion;
        private String codigo;

        public AccionSemantica(String produccion, String codigo) {
            this.produccion = produccion;
            this.codigo = codigo;
        }

        public String getProduccion() {
            return produccion;
        }

        public String getCodigo() {
            return codigo;
        }

        @Override
        public String toString() {
            return produccion + " { " + codigo + " }";
        }
    }

    // ==================== FIN DEL CÓDIGO FALTANTE ====================
}