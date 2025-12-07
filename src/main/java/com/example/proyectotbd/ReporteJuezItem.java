package com.example.proyectotbd;

public class ReporteJuezItem {
    private String equipo;
    private String categoria;
    private String diseno;
    private String prog;
    private String construc;
    private String total;

    public ReporteJuezItem(String e, String c, String d, String p, String co, String t) {
        this.equipo = e;
        this.categoria = c;
        this.diseno = d;
        this.prog = p;
        this.construc = co;
        this.total = t;
    }

    public String getEquipo() { return equipo; }
    public String getCategoria() { return categoria; }
    public String getDiseno() { return diseno; }
    public String getProg() { return prog; }
    public String getConstruc() { return construc; }
    public String getTotal() { return total; }
}