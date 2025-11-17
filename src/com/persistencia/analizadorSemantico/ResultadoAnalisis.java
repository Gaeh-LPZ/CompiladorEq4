package com.persistencia.analizadorSemantico;

import java.util.List;

public class ResultadoAnalisis {
    private List<String> tokens;
    private List<PasoAnalisis> corrida;
    private Object resultado;
    
    public ResultadoAnalisis(List<String> tokens, List<PasoAnalisis> corrida, Object resultado) {
        this.tokens = tokens;
        this.corrida = corrida;
        this.resultado = resultado;
    }
    
    public List<String> getTokens() { 
        return tokens; 
    }
    
    public List<PasoAnalisis> getCorrida() { 
        return corrida; 
    }
    
    public Object getResultado() { 
        return resultado; 
    }
}