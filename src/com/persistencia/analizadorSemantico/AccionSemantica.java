package com.persistencia.analizadorSemantico;

public class AccionSemantica {
    private String produccion;  // "E → E + T"
    private String codigo;      // "E.val = E1.val + T.val"
    
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