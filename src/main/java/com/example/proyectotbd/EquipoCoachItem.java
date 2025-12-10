package com.example.proyectotbd;

public class EquipoCoachItem {
    private int id;
    private String nombre;
    private String institucion;
    private String categoria;
    private String evento;
    private String integrantes;

    public EquipoCoachItem(int id, String nombre, String institucion, String categoria, String evento, String integrantes) {
        this.id = id;
        this.nombre = nombre;
        this.institucion = institucion;
        this.categoria = categoria;
        this.evento = evento;
        this.integrantes = integrantes;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getInstitucion() { return institucion; }
    public String getCategoria() { return categoria; }
    public String getEvento() { return evento; }
    public String getIntegrantes() { return integrantes; } // <--- NUEVO GETTER
}