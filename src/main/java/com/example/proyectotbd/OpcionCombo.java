package com.example.proyectotbd;

public class OpcionCombo {
    private final int id;
    private final String nombre;

    public OpcionCombo(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }

    @Override
    public String toString() {
        return nombre; // Esto es lo que se ve en la lista
    }
}