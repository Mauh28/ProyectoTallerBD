package com.example.proyectotbd;

// Esta clase sirve como modelo para la tabla que lista qué jueces están asignados
// a qué categoría dentro de qué evento.

public class AsignacionItem {

    // Campos que corresponden a las columnas de la tabla del Administrador
    private String evento;
    private String categoria;
    private String jueces;

    /**
     * Constructor para inicializar una nueva AsignacionItem.
     * @param evento Nombre del evento.
     * @param categoria Nombre de la categoría (nivel).
     * @param jueces Lista concatenada de nombres de jueces asignados.
     */
    public AsignacionItem(String evento, String categoria, String jueces) {
        this.evento = evento;
        this.categoria = categoria;
        this.jueces = jueces;
    }

    // getters

    public String getEvento() {
        return evento;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getJueces() {
        return jueces;
    }

}