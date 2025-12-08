package com.example.proyectotbd;

public class EquipoItem {
    private int id;
    private String nombre;
    private String institucion;
    private String estado;      // "PENDIENTE" o "EVALUADO"
    private int conteoJueces;   // <--- NUEVO CAMPO

    // Constructor Actualizado
    public EquipoItem(int id, String nombre, String institucion, String estado, int conteoJueces) {
        this.id = id;
        this.nombre = nombre;
        this.institucion = institucion;
        this.estado = estado;
        this.conteoJueces = conteoJueces; // <--- Asignar
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

    public int getConteoJueces() { return conteoJueces; } // <--- Nuevo Getter
}