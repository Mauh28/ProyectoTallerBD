package com.example.proyectotbd;

public class ReporteCoachItem {
    private String equipo;
    private String categoria;
    private String evento;
    private String ptsDiseno;
    private String ptsProg;
    private String ptsConst;
    private String total;

    public ReporteCoachItem(String equipo, String categoria, String evento, String ptsDiseno, String ptsProg, String ptsConst, String total) {
        this.equipo = equipo;
        this.categoria = categoria;
        this.evento = evento;
        this.ptsDiseno = ptsDiseno;
        this.ptsProg = ptsProg;
        this.ptsConst = ptsConst;
        this.total = total;
    }

    // Getters necesarios para la Tabla
    public String getEquipo() { return equipo; }
    public String getCategoria() { return categoria; }
    public String getEvento() { return evento; }
    public String getPtsDiseno() { return ptsDiseno; }
    public String getPtsProg() { return ptsProg; }
    public String getPtsConst() { return ptsConst; }
    public String getTotal() { return total; }
}