package com.persistencia.analizadorSintacticoLR.primerosYSiguientes;

import java.util.*;
import java.util.stream.Collectors;

public final class grammar {
    public static final String EPS = "ε";

    public final Map<String, List<List<String>>> prods = new LinkedHashMap<>();
    public String start; 
    public final Set<String> nonTerminals = new LinkedHashSet<>();
    public final Set<String> terminals = new LinkedHashSet<>();

    private grammar() {}

    public static grammar fromText(String text) {
        grammar g = new grammar();
        List<String> lines = Arrays.stream(text.split("\\R+"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#") && !s.startsWith("//"))
                .collect(Collectors.toList());

        if (lines.isEmpty()) throw new IllegalArgumentException("Texto vacío");

        boolean headerFormat = lines.size() >= 3
                && !lines.get(0).contains("->")
                && !lines.get(1).contains("->");

        int idx = 0;
        if (headerFormat) {
            // Línea 1: NTs
            for (String nt : splitBySpace(lines.get(idx++))) {
                if (nt.isEmpty()) continue;
                g.nonTerminals.add(nt);
                if (g.start == null) g.start = nt;
            }
            // Línea 2: Ts
            for (String t : splitBySpace(lines.get(idx++))) {
                if (t.isEmpty()) continue;
                if (!isEpsilon(t)) g.terminals.add(t);
            }
        }

        // Resto: producciones
        for (; idx < lines.size(); idx++) {
            String line = lines.get(idx);
            String[] sides = line.split("->");
            if (sides.length != 2) {
                throw new IllegalArgumentException("Línea inválida (se espera 'A->...'): " + line);
            }
            String lhs = sides[0].trim();
            if (lhs.isEmpty()) {
                throw new IllegalArgumentException("LHS vacío en línea: " + line);
            }
            if (g.start == null) g.start = lhs; // en caso de no usar encabezado
            g.nonTerminals.add(lhs);

            String[] alts = sides[1].trim().split("\\|");
            for (String alt : alts) {
                List<String> symbols = tokenize(alt.trim());
                if (symbols.isEmpty()) {
                    throw new IllegalArgumentException("Producción vacía en: " + line);
                }
                g.prods.computeIfAbsent(lhs, k -> new ArrayList<>()).add(symbols);
            }
        }

        if (!headerFormat) {
            Set<String> rhsSymbols = new LinkedHashSet<>();
            for (List<List<String>> alts : g.prods.values())
                for (List<String> prod : alts)
                    rhsSymbols.addAll(prod);

            for (String s : rhsSymbols) {
                if (!g.nonTerminals.contains(s) && !isEpsilon(s)) {
                    g.terminals.add(s);
                }
            }
        }

        return g;
    }

    private static List<String> splitBySpace(String s) {
        return Arrays.stream(s.trim().split("\\s+")).collect(Collectors.toList());
    }

    public static List<String> tokenize(String alt) {
        if (alt.contains(" ")) {
            return Arrays.stream(alt.split("\\s+"))
                    .map(grammar::normalizeEps)
                    .collect(Collectors.toList());
        }
        return Collections.singletonList(normalizeEps(alt));
    }

    public static boolean isEpsilon(String s) {
        String n = s.trim();
        return n.equals(EPS) || n.equalsIgnoreCase("EPS") || n.equalsIgnoreCase("epsilon");
    }

    public static String normalizeEps(String s) {
        return isEpsilon(s) ? EPS : s;
    }
}
