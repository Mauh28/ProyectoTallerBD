package com.example.proyectotbd;

// Esta clase sirve como modelo para la tabla que muestra la calificación
// promedio final de los equipos en un evento.

public class ResultadoFinalItem {

    // Campos que corresponden a las columnas del reporte final
    private int equipoId;
    private String nombreEquipo;
    private String nombreCategoria;
    private String nombreCoach;
    private double puntajePromedio;

    /**
     * Constructor para inicializar un nuevo ResultadoFinalItem.
     * * @param equipoId ID del equipo.
     * @param nombreEquipo Nombre del equipo.
     * @param nombreCategoria Nivel de la categoría.
     * @param nombreCoach Nombre del Coach del equipo.
     * @param puntajePromedio Promedio de la calificación total de las rúbricas (Diseño + Programación + Construcción).
     */
    public ResultadoFinalItem(int equipoId, String nombreEquipo, String nombreCategoria, String nombreCoach, double puntajePromedio) {
        this.equipoId = equipoId;
        this.nombreEquipo = nombreEquipo;
        this.nombreCategoria = nombreCategoria;
        this.nombreCoach = nombreCoach;
        this.puntajePromedio = puntajePromedio;
    }

    // =================================================================
    // GETTERS (OBLIGATORIOS PARA TableView y PropertyValueFactory)
    // =================================================================

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
        // Formato: Usamos un String para devolver el puntaje con un decimal, si es necesario,
        // aunque el PropertyValueFactory buscará el getter del tipo primitivo.
        return puntajePromedio;
    }
}