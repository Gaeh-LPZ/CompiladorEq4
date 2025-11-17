package com.persistencia.analizadorSemantico;

public class PasoAnalisis {
    private int paso;
    private String pila;
    private String entrada;
    private String accion;
    private String salida;
    
    public PasoAnalisis(int paso, String pila, String entrada, String accion, String salida) {
        this.paso = paso;
        this.pila = pila;
        this.entrada = entrada;
        this.accion = accion;
        this.salida = salida;
    }
    
    public Object[] toArray() {
        return new Object[]{paso, pila, entrada, accion, salida};
    }
    
    // Getters
    public int getPaso() { 
        return paso; 
    }
    
    public String getPila() { 
        return pila; 
    }
    
    public String getEntrada() { 
        return entrada; 
    }
    
    public String getAccion() { 
        return accion; 
    }
    
    public String getSalida() { 
        return salida; 
    }
}