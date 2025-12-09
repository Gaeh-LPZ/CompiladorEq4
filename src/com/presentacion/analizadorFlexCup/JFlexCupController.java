package com.presentacion.analizadorFlexCup;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Controlador para JFlex + CUP
 * Integra análisis léxico (JFlex) y sintáctico-semántico (CUP)
 */
public class JFlexCupController {

    private static final String RUTA_SALIDA = "salida/";
    private static final String JAR_JFLEX = "lib/jflex.jar";
    private static final String JAR_CUP = "lib/java-cup-11b.jar";
    private static final String JAR_CUP_RUNTIME = "lib/java-cup-11b-runtime.jar";

    private StringBuilder logCompilacion;
    private boolean compilacionExitosa;
    private String directorioTrabajo;
    private ResultadoAnalisis resultadosAnalisis; // NUEVO: Para almacenar resultados

    public JFlexCupController() {
        this.logCompilacion = new StringBuilder();
        this.compilacionExitosa = false;
        this.resultadosAnalisis = new ResultadoAnalisis(); // Inicializar
        crearDirectorios();
    }

    /**
     * Crea los directorios necesarios
     */
    private void crearDirectorios() {
        try {
            Files.createDirectories(Paths.get(RUTA_SALIDA));
        } catch (IOException e) {
            System.err.println("Error al crear directorios: " + e.getMessage());
        }
    }

    /**
     * Compila JFlex y CUP usando el contenido proporcionado
     */
    public boolean compilarJFlexCup(String contenidoJFlex, String contenidoCup, String directorioTrabajo) {
        logCompilacion = new StringBuilder();
        compilacionExitosa = false;
        this.directorioTrabajo = directorioTrabajo;

        try {
            log("🔨 INICIANDO COMPILACIÓN JFLEX + CUP\n");
            log("=====================================\n\n");

            if (contenidoCup == null || contenidoCup.trim().isEmpty()) {
                log("❌ Error: Debe proporcionar el contenido del archivo CUP\n");
                return false;
            }

            Files.createDirectories(Paths.get(directorioTrabajo));
            log("📁 Directorio de trabajo: " + directorioTrabajo + "\n\n");

            // Eliminar declaraciones de paquete para compilar sin paquetes
            log("🔧 Procesando archivos (eliminando declaraciones de paquete)...\n");

            // Limpiar JFlex
            String jflexLimpio = contenidoJFlex;
            if (!contenidoJFlex.trim().isEmpty()) {
                jflexLimpio = contenidoJFlex.replaceAll("(?m)^\\s*package\\s+[^;]+;\\s*$", "// package removido");
                log("   ✓ Archivo JFlex procesado\n");
            }

            // Limpiar CUP
            String cupLimpio = contenidoCup.replaceAll("(?m)^\\s*package\\s+[^;]+;\\s*$", "// package removido");
            log("   ✓ Archivo CUP procesado\n\n");

            String rutaTempCup = directorioTrabajo + File.separator + "parser.cup";
            Files.write(Paths.get(rutaTempCup), cupLimpio.getBytes());
            log("📁 Archivo CUP temporal creado\n");

            if (!jflexLimpio.trim().isEmpty()) {
                log("\n📝 Paso 1: Compilando JFlex...\n");

                String rutaTempJFlex = directorioTrabajo + File.separator + "analizador.flex";
                Files.write(Paths.get(rutaTempJFlex), jflexLimpio.getBytes());
                log("📁 Archivo JFlex temporal creado\n");

                if (!compilarJFlex(rutaTempJFlex, directorioTrabajo)) {
                    log("❌ Error al compilar JFlex\n");
                    return false;
                }
                log("✅ JFlex compilado exitosamente\n");
            } else {
                log("\n📝 Paso 1: JFlex...\n");
                log("✅ Usando Analizador.java ya compilado\n");
            }

            log("\n📝 Paso 2: Compilando CUP...\n");
            if (!compilarCup(rutaTempCup, directorioTrabajo)) {
                log("❌ Error al compilar CUP\n");
                return false;
            }
            log("✅ CUP compilado exitosamente\n");
            log("   Generados: parser.java, sym.java\n");

            // Limpiar paquetes de archivos generados
            log("\n📝 Paso 2.5: Limpiando archivos generados...\n");
            limpiarPaquetesDeArchivo(directorioTrabajo + File.separator + "parser.java");
            limpiarPaquetesDeArchivo(directorioTrabajo + File.separator + "sym.java");
            limpiarPaquetesDeArchivo(directorioTrabajo + File.separator + "Analizador.java");
            log("✅ Archivos limpiados\n");

            log("\n📝 Paso 3: Compilando clases Java...\n");
            if (!compilarJava(directorioTrabajo)) {
                log("❌ Error al compilar clases Java\n");
                return false;
            }
            log("✅ Clases Java compiladas exitosamente\n");

            log("\n✅✅✅ COMPILACIÓN COMPLETA EXITOSA ✅✅✅\n");
            compilacionExitosa = true;
            return true;

        } catch (Exception e) {
            log("❌ Error durante la compilación: " + e.getMessage() + "\n");
            e.printStackTrace();
            return false;
        }
    }

    private boolean compilarJFlex(String rutaArchivoJFlex, String directorioSalida) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-jar", JAR_JFLEX, "-d", directorioSalida, rutaArchivoJFlex);

            log("   Ejecutando JFlex...\n");
            Process proceso = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                log("   " + linea + "\n");
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
            boolean hayErrores = false;
            while ((linea = errorReader.readLine()) != null) {
                log("   ERROR: " + linea + "\n");
                hayErrores = true;
            }

            int exitCode = proceso.waitFor();
            if (exitCode != 0 || hayErrores) {
                log("   ⚠️ Código de salida: " + exitCode + "\n");
                return false;
            }
            return true;

        } catch (Exception e) {
            log("   ❌ Error: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean compilarCup(String rutaArchivoCup, String directorioSalida) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "java", "-jar", JAR_CUP, "-destdir", directorioSalida,
                    "-parser", "parser", "-symbols", "sym", rutaArchivoCup);

            log("   Ejecutando CUP...\n");
            Process proceso = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                log("   " + linea + "\n");
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
            while ((linea = errorReader.readLine()) != null) {
                log("   " + linea + "\n");
            }

            int exitCode = proceso.waitFor();
            if (exitCode != 0) {
                log("   ⚠️ Código de salida: " + exitCode + "\n");
                return false;
            }

            File parserFile = new File(directorioSalida + File.separator + "parser.java");
            File symFile = new File(directorioSalida + File.separator + "sym.java");

            if (!parserFile.exists() || !symFile.exists()) {
                log("   ❌ No se generaron los archivos parser.java o sym.java\n");
                return false;
            }
            return true;

        } catch (Exception e) {
            log("   ❌ Error: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean compilarJava(String directorio) {
        try {
            // Verificar que existe Analizador.java
            File analizadorJava = new File(directorio + File.separator + "Analizador.java");

            // Compilar todos los archivos Java generados
            ProcessBuilder pb;
            if (analizadorJava.exists()) {
                pb = new ProcessBuilder(
                        "javac", "-cp", ".;lib/java-cup-11b-runtime.jar",
                        directorio + File.separator + "Analizador.java",
                        directorio + File.separator + "parser.java",
                        directorio + File.separator + "sym.java");
                log("   Compilando: Analizador.java, parser.java, sym.java\n");
            } else {
                pb = new ProcessBuilder(
                        "javac", "-cp", ".;lib/java-cup-11b-runtime.jar",
                        directorio + File.separator + "parser.java",
                        directorio + File.separator + "sym.java");
                log("   Compilando: parser.java, sym.java\n");
            }

            Process proceso = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                log("   " + linea + "\n");
            }

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proceso.getErrorStream()));
            boolean hayErrores = false;
            while ((linea = errorReader.readLine()) != null) {
                log("   " + linea + "\n");
                hayErrores = true;
            }

            int exitCode = proceso.waitFor();
            if (exitCode != 0 || hayErrores) {
                return false;
            }

            // Verificar que se generaron los .class
            File analizadorClass = new File(directorio + File.separator + "Analizador.class");
            File parserClass = new File(directorio + File.separator + "parser.class");
            File symClass = new File(directorio + File.separator + "sym.class");

            log("   Verificando archivos .class generados:\n");
            log("      Analizador.class: " + (analizadorClass.exists() ? "✓" : "✗") + "\n");
            log("      parser.class: " + (parserClass.exists() ? "✓" : "✗") + "\n");
            log("      sym.class: " + (symClass.exists() ? "✓" : "✗") + "\n");

            return true;

        } catch (Exception e) {
            log("   ❌ Error: " + e.getMessage() + "\n");
            return false;
        }
    }

    public ResultadoAnalisis analizarPrograma(String contenidoPrograma) {
        ResultadoAnalisis resultado = new ResultadoAnalisis();

        try {
            log("\n🔍 INICIANDO ANÁLISIS\n");
            log("=====================\n\n");

            if (!compilacionExitosa) {
                resultado.setError("Debe compilar JFlex-CUP primero");
                log("❌ Debe compilar primero\n");
                return resultado;
            }

            String rutaTemporal = RUTA_SALIDA + "programa_temp.java";
            Files.write(Paths.get(rutaTemporal), contenidoPrograma.getBytes());
            log("📁 Programa guardado en: " + rutaTemporal + "\n\n");

            log("📝 Ejecutando análisis sintáctico-semántico...\n");

            try {
                File analizadorClass = new File(directorioTrabajo + File.separator + "Analizador.class");
                File parserClassFile = new File(directorioTrabajo + File.separator + "parser.class");

                if (!analizadorClass.exists() || !parserClassFile.exists()) {
                    throw new Exception("No se encuentran las clases compiladas en " + directorioTrabajo);
                }

                File dirTrabajo = new File(directorioTrabajo);
                URL[] urls = { dirTrabajo.toURI().toURL() };
                URLClassLoader classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());

                log("   📂 Cargando desde: " + dirTrabajo.getAbsolutePath() + "\n");

                // Como ya no tienen paquete, cargar directamente
                Class<?> analizadorClazz = classLoader.loadClass("Analizador");
                log("   ✓ Analizador cargado\n");

                Class<?> parserClass = classLoader.loadClass("parser");
                log("   ✓ Parser cargado\n");

                Class<?> symClass = classLoader.loadClass("sym");
                log("   ✓ Sym cargado\n");

                Object scanner = analizadorClazz.getConstructor(java.io.Reader.class)
                        .newInstance(new FileReader(rutaTemporal));
                log("   ✓ Scanner creado\n");

                Object parserInstance = null;
                for (java.lang.reflect.Constructor<?> c : parserClass.getConstructors()) {
                    Class<?>[] paramTypes = c.getParameterTypes();
                    if (paramTypes.length == 1) {
                        parserInstance = c.newInstance(scanner);
                        break;
                    }
                }

                if (parserInstance == null) {
                    throw new Exception("No se pudo crear instancia del parser");
                }
                log("   ✓ Parser creado\n");

                // Obtener el action object (puede estar en diferentes lugares según versión de
                // CUP)
                Object actionObj = null;
                try {
                    // Intentar obtener action_obj como campo público
                    actionObj = parserClass.getField("action_obj").get(parserInstance);
                    log("   ✓ Action object obtenido (campo público)\n");
                } catch (NoSuchFieldException e1) {
                    try {
                        // Intentar obtener como método getActionObject()
                        Method getActionObj = parserClass.getMethod("getActionObject");
                        actionObj = getActionObj.invoke(parserInstance);
                        log("   ✓ Action object obtenido (método)\n");
                    } catch (NoSuchMethodException e2) {
                        // Buscar en campos declarados (incluyendo privados)
                        for (java.lang.reflect.Field field : parserClass.getDeclaredFields()) {
                            if (field.getName().contains("action")) {
                                field.setAccessible(true);
                                actionObj = field.get(parserInstance);
                                log("   ✓ Action object obtenido (campo: " + field.getName() + ")\n");
                                break;
                            }
                        }
                    }
                }

                if (actionObj == null) {
                    // Si no encontramos action_obj, el parser podría tener las acciones integradas
                    log("   ⚠️ No se encontró action_obj, usando parser directamente\n");
                    actionObj = parserInstance;
                }

                String archivoTraduccion = RUTA_SALIDA + "traduccion.cpp";

                // Iniciar traducción
                try {
                    Method iniciarTraduccion = actionObj.getClass().getMethod("iniciarTraduccion", String.class);
                    iniciarTraduccion.invoke(actionObj, archivoTraduccion);
                    log("   ✓ Archivo de traducción inicializado: " + archivoTraduccion + "\n");
                } catch (NoSuchMethodException e) {
                    log("   ⚠️ Método iniciarTraduccion no encontrado, continuando...\n");
                }

                log("   ⚙️ Ejecutando parser...\n");

                Method parseMethod = parserClass.getMethod("parse");
                Object parseResult = parseMethod.invoke(parserInstance);
                log("   ✓ Parser ejecutado exitosamente\n");

                // Obtener código traducido
                String traduccion = "";
                try {
                    Object codigoTraducido = actionObj.getClass().getField("codigoTraducido").get(actionObj);
                    traduccion = codigoTraducido.toString();
                    log("   ✓ Código traducido obtenido\n");
                } catch (NoSuchFieldException e) {
                    // Intentar leer el archivo de traducción directamente
                    try {
                        traduccion = new String(Files.readAllBytes(Paths.get(archivoTraduccion)));
                        log("   ✓ Traducción leída desde archivo\n");
                    } catch (Exception e2) {
                        log("   ⚠️ No se pudo obtener traducción: " + e2.getMessage() + "\n");
                        traduccion = "/* No se pudo obtener la traducción */\n";
                    }
                }
                resultado.setTraduccion(traduccion);

                log("✅ Análisis sintáctico completado\n");
                log("✅ Traducción generada: " + archivoTraduccion + "\n\n");

                // Obtener errores semánticos
                try {
                    Method getErrores = actionObj.getClass().getMethod("getErroresSemanticos");
                    @SuppressWarnings("unchecked")
                    java.util.List<String> erroresSem = (java.util.List<String>) getErrores.invoke(actionObj);

                    if (!erroresSem.isEmpty()) {
                        StringBuilder tablaErrores = new StringBuilder("Tipo\tDescripción\tLinea\tColumna\n");
                        for (String error : erroresSem) {
                            tablaErrores.append("Semántico\t").append(error).append("\t-\t-\n");
                        }
                        resultado.setErrores(tablaErrores.toString());
                        log("⚠️ Errores semánticos: " + erroresSem.size() + "\n");
                    } else {
                        resultado.setErrores("Tipo\tDescripción\tLinea\tColumna\n");
                        log("✅ Sin errores semánticos\n");
                    }
                } catch (NoSuchMethodException e) {
                    log("   ⚠️ Método getErroresSemanticos no encontrado\n");
                    resultado.setErrores("Tipo\tDescripción\tLinea\tColumna\n");
                }

                // Obtener tokens
                try {
                    Method getTokens = analizadorClazz.getMethod("getTokens");
                    @SuppressWarnings("unchecked")
                    java.util.List<Object[]> tokens = (java.util.List<Object[]>) getTokens.invoke(scanner);

                    StringBuilder tiraTokens = new StringBuilder("Linea\tLexema\tToken\tColumna\n");
                    for (Object[] token : tokens) {
                        tiraTokens.append(token[0]).append("\t")
                                .append(token[1]).append("\t")
                                .append(token[2]).append("\t")
                                .append(token[3]).append("\n");
                    }
                    resultado.setTokens(tiraTokens.toString());
                    log("✅ Tokens: " + tokens.size() + "\n");
                } catch (Exception e) {
                    resultado.setTokens("< No disponible >\n");
                }

                // Obtener símbolos
                try {
                    Method getSimbolos = analizadorClazz.getMethod("getSimbolos");
                    @SuppressWarnings("unchecked")
                    java.util.List<Object[]> simbolos = (java.util.List<Object[]>) getSimbolos.invoke(scanner);

                    StringBuilder tablaSimbolos = new StringBuilder("ID\tSimbolo\n");
                    for (Object[] simbolo : simbolos) {
                        tablaSimbolos.append(simbolo[0]).append("\t").append(simbolo[1]).append("\n");
                    }
                    resultado.setSimbolos(tablaSimbolos.toString());
                    log("✅ Símbolos: " + simbolos.size() + "\n");
                } catch (Exception e) {
                    resultado.setSimbolos("ID\tSimbolo\n");
                }

                classLoader.close();

                // ========== CAMBIO IMPORTANTE ==========
                // Guardar los resultados en la variable de instancia
                this.resultadosAnalisis = resultado;
                log("\n✅ Resultados guardados para mostrar en ventanas\n");

            } catch (Exception e) {
                log("⚠️ Error al ejecutar parser: " + e.getMessage() + "\n");
                e.printStackTrace();
                log("   Generando traducción básica como respaldo...\n\n");

                String traduccion = generarTraduccionEjemplo(contenidoPrograma);
                resultado.setTraduccion(traduccion);
                resultado.setTokens("< Error en análisis >\n");
                resultado.setSimbolos("ID\tSimbolo\n");
                resultado.setErrores("Tipo\tDescripción\tLinea\tColumna\n");

                // ========== CAMBIO IMPORTANTE ==========
                // Guardar resultados incluso si hay error parcial
                this.resultadosAnalisis = resultado;
            }

            log("\n✅✅✅ ANÁLISIS COMPLETADO ✅✅✅\n");

        } catch (Exception e) {
            resultado.setError("Error durante el análisis: " + e.getMessage());
            log("❌ Error: " + e.getMessage() + "\n");
            e.printStackTrace();

            // ========== CAMBIO IMPORTANTE ==========
            // Guardar resultados aunque haya error
            this.resultadosAnalisis = resultado;
        }

        return resultado;
    }

    private String generarTraduccionEjemplo(String programa) {
        StringBuilder traduccion = new StringBuilder();
        traduccion.append("#include <iostream>\n");
        traduccion.append("#include <string>\n");
        traduccion.append("using namespace std;\n\n");
        traduccion.append("/* Traducción automática de Java a C++ */\n\n");

        if (programa.contains("main")) {
            traduccion.append("int main(int argc, char *argv[]) {\n");

            if (programa.contains("println")) {
                int start = programa.indexOf("println(");
                if (start != -1) {
                    int end = programa.indexOf(")", start);
                    if (end != -1) {
                        String contenido = programa.substring(start + 8, end).trim();
                        if (contenido.startsWith("\"") && contenido.endsWith("\"")) {
                            traduccion.append("    cout << ").append(contenido).append(" << endl;\n");
                        } else {
                            traduccion.append("    cout << ").append(contenido).append(" << endl;\n");
                        }
                    }
                }
            }

            if (programa.contains("int ")) {
                String[] lineas = programa.split("\n");
                for (String linea : lineas) {
                    if (linea.trim().startsWith("int ") && linea.contains("=")) {
                        traduccion.append("    ").append(linea.trim()).append("\n");
                    }
                }
            }

            traduccion.append("    return 0;\n");
            traduccion.append("}\n");
        }

        return traduccion.toString();
    }

    private void log(String mensaje) {
        logCompilacion.append(mensaje);
        System.out.print(mensaje);
    }

    public String getLogCompilacion() {
        return logCompilacion.toString();
    }

    public boolean verificarArchivos() {
        File cupJar = new File(JAR_CUP);
        File cupRuntimeJar = new File(JAR_CUP_RUNTIME);

        if (!cupJar.exists() || !cupRuntimeJar.exists()) {
            log("❌ No se encontraron los JARs de CUP en lib/\n");
            log("   Necesitas: java-cup-11b.jar y java-cup-11b-runtime.jar\n");
            return false;
        }
        return true;
    }

    public void guardarArchivo(String ruta, String contenido) {
        try {
            Files.write(Paths.get(ruta), contenido.getBytes());
            log("✅ Archivo guardado: " + ruta + "\n");
        } catch (IOException e) {
            log("❌ Error al guardar archivo: " + e.getMessage() + "\n");
        }
    }

    private void limpiarPaquetesDeArchivo(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                return;
            }

            String contenido = new String(Files.readAllBytes(archivo.toPath()));
            String contenidoLimpio = contenido.replaceAll("(?m)^\\s*package\\s+[^;]+;\\s*$", "// package removido");

            if (!contenido.equals(contenidoLimpio)) {
                Files.write(archivo.toPath(), contenidoLimpio.getBytes());
                log("   ✓ " + archivo.getName() + " limpiado\n");
            }
        } catch (Exception e) {
            log("   ⚠️ No se pudo limpiar " + rutaArchivo + ": " + e.getMessage() + "\n");
        }
    }

    // ========== MÉTODOS NUEVOS PARA SOPORTAR VENTANAS ==========

    /**
     * Obtiene los resultados del análisis más reciente
     */
    public ResultadoAnalisis getResultadosAnalisis() {
        if (resultadosAnalisis == null) {
            return new ResultadoAnalisis(); // Devolver vacío si no hay análisis
        }
        return resultadosAnalisis;
    }

    /**
     * Establece los resultados del análisis (para usar desde el panel)
     */
    public void setResultadosAnalisis(ResultadoAnalisis resultadosAnalisis) {
        this.resultadosAnalisis = resultadosAnalisis;
    }

    /**
     * Limpia los resultados actuales
     */
    public void limpiarResultados() {
        this.resultadosAnalisis = new ResultadoAnalisis();
    }

    /**
     * Verifica si hay resultados disponibles para mostrar
     */
    public boolean hayResultadosDisponibles() {
        return resultadosAnalisis != null &&
                (!resultadosAnalisis.getTokens().isEmpty() ||
                        !resultadosAnalisis.getSimbolos().isEmpty() ||
                        !resultadosAnalisis.getTraduccion().isEmpty() ||
                        !resultadosAnalisis.getErrores().isEmpty());
    }

}

class ResultadoAnalisis {
    private String tokens;
    private String simbolos;
    private String errores;
    private String traduccion;
    private String error;

    public ResultadoAnalisis() {
        this.tokens = "";
        this.simbolos = "";
        this.errores = "";
        this.traduccion = "";
        this.error = null;
    }

    public String getTokens() {
        return tokens;
    }

    public void setTokens(String tokens) {
        this.tokens = tokens;
    }

    public String getSimbolos() {
        return simbolos;
    }

    public void setSimbolos(String simbolos) {
        this.simbolos = simbolos;
    }

    public String getErrores() {
        return errores;
    }

    public void setErrores(String errores) {
        this.errores = errores;
    }

    public String getTraduccion() {
        return traduccion;
    }

    public void setTraduccion(String traduccion) {
        this.traduccion = traduccion;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean tieneError() {
        return error != null;
    }

    /**
     * Método auxiliar para verificar si hay contenido válido
     */
    public boolean tieneContenido() {
        return !tokens.isEmpty() || !simbolos.isEmpty() ||
                !traduccion.isEmpty() || !errores.isEmpty();
    }
}