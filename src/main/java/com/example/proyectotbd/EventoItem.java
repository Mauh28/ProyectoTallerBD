package com.example.proyectotbd;

public class EventoItem {
    private int id;
    private String nombre;
    private String lugar;
    private String fecha;
    private String jueces;
    private String horaInicio; // Campo nuevo y necesario para la edición
    private String horaFin;    // Campo nuevo y necesario para la edición

    /**
     * Constructor completo para EventoItem, incluyendo las horas.
     * @param id ID del evento.
     * @param nombre Nombre del evento.
     * @param lugar Lugar/Sede.
     * @param fecha Fecha (en formato String yyyy-MM-dd).
     * @param jueces Cadena con la lista de jueces asignados (para la tabla principal).
     * @param horaInicio Hora de inicio (en formato String HH:MM:SS).
     * @param horaFin Hora de fin (en formato String HH:MM:SS).
     */
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

    // Nuevos Getters para las horas
    public String getHoraInicio() { return horaInicio; }
    public String getHoraFin() { return horaFin; }

    // NOTA: Para el control de la tabla, los getters deben seguir las convenciones de JavaFX.
}