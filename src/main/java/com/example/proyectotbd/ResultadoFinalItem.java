package com.example.proyectotbd;

// Esta clase sirve como modelo para la tabla que muestra la calificación
// promedio final de los equipos en un evento.

public class ResultadoFinalItem {
    private int equipoId;
    private String nombreEquipo;
    private String nombreCategoria;
    private String nombreCoach;
    private double puntajePromedio;

    public ResultadoFinalItem(int equipoId, String nombreEquipo, String nombreCategoria, String nombreCoach, double puntajePromedio) {
        this.equipoId = equipoId;
        this.nombreEquipo = nombreEquipo;
        this.nombreCategoria = nombreCategoria;
        this.nombreCoach = nombreCoach;
        this.puntajePromedio = puntajePromedio;
    }


    public int getEquipoId() {
        return equipoId;
    }

    public String getNombreEquipo() {
        return nombreEquipo;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public String getNombreCoach() {
        return nombreCoach;
    }

    public double getPuntajePromedio() {
        return puntajePromedio;
    }
}