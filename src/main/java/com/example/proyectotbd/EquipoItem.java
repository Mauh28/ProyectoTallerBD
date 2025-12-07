package com.example.proyectotbd;

public class EquipoItem {
    private int id;
    private String nombre;
    private String institucion;
    private String estado; // "PENDIENTE" o "EVALUADO"

    // Constructor que usa el DAO para llenar los datos
    public EquipoItem(int id, String nombre, String institucion, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.institucion = institucion;
        this.estado = estado;
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
}