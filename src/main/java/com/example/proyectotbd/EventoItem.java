package com.example.proyectotbd;

public class EventoItem {
    private int id;
    private String nombre;
    private String lugar;
    private String fecha;
    private String jueces; // <--- ESTE CAMPO ES EL QUE TE FALTA

    // Constructor actualizado para recibir 5 parámetros
    public EventoItem(int id, String nombre, String lugar, String fecha, String jueces) {
        this.id = id;
        this.nombre = nombre;
        this.lugar = lugar;
        this.fecha = fecha;
        this.jueces = jueces; // <--- ASIGNARLO
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLugar() { return lugar; }
    public String getFecha() { return fecha; }

    // Getter nuevo necesario para la tabla
    public String getJueces() { return jueces; }
}