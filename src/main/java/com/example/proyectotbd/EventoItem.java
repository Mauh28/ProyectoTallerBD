package com.example.proyectotbd;

public class EventoItem {
    private int id;
    private String nombre;
    private String lugar;
    private String fecha;
    private String jueces;
    private String horaInicio;
    private String horaFin;

    public EventoItem(int id, String nombre, String lugar, String fecha, String jueces, String horaInicio, String horaFin) {
        this.id = id;
        this.nombre = nombre;
        this.lugar = lugar;
        this.fecha = fecha;
        this.jueces = jueces;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getLugar() { return lugar; }
    public String getFecha() { return fecha; }
    public String getJueces() { return jueces; }

    // Nuevos Getters para las horas_
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }
}