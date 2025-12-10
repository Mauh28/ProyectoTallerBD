package com.example.proyectotbd;

public class EquipoItem {
    private int id;
    private String nombre;
    private String institucion;
    private String estado;
    private int conteoJueces;

    /**
     * Constructor para EquipoItem.
     * @param id ID del equipo.
     * @param nombre Nombre del equipo.
     * @param institucion Institución del equipo.
     * @param estado Estado personal del juez actual ("PENDIENTE" o "EVALUADO").
     * @param conteoJueces Estado global del equipo (0: Abierto, 1: Cerrado/Ya evaluado).
     */
    public EquipoItem(int id, String nombre, String institucion, String estado, int conteoJueces) {
        this.id = id;
        this.nombre = nombre;
        this.institucion = institucion;
        this.estado = estado;
        this.conteoJueces = conteoJueces;
    }

    // Getters necesarios para que el Controlador pueda leer los datos
    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getEstado() {
        return estado;
    }

    /**
     * Devuelve el estado global de evaluación del equipo (0 o 1).
     * Se usa para verificar si el equipo ya fue cerrado por CUALQUIER juez (Regla 1/1).
     */
    public int getConteoJueces() { return conteoJueces; }
}