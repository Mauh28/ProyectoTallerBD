package com.example.proyectotbd;

public class EventoItem {
    private int id;
    private String nombre;
    private String lugar;
    private String fecha;

    public EventoItem(int id, String nombre, String lugar, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.lugar = lugar;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLugar() { return lugar; }
    public String getFecha() { return fecha; }
}