package com.example.proyectotbd;

public class EquipoCoachItem {
    private String nombre, institucion, categoria, evento;

    public EquipoCoachItem(String nombre, String institucion, String categoria, String evento) {
        this.nombre = nombre;
        this.institucion = institucion;
        this.categoria = categoria;
        this.evento = evento;
    }
    // Getters necesarios para la TableView
    public String getNombre() { return nombre; }
    public String getInstitucion() { return institucion; }
    public String getCategoria() { return categoria; }
    public String getEvento() { return evento; }
}