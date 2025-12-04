package com.example.proyectotbd;

public class UsuarioItem {
    private int id;
    private String username;
    private String nombre;
    private String institucion;
    private String rol;

    public UsuarioItem(int id, String username, String nombre, String institucion, String rol) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.institucion = institucion;
        this.rol = rol;
    }

    // Getters necesarios para PropertyValueFactory
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNombre() { return nombre; }
    public String getInstitucion() { return institucion; }
    public String getRol() { return rol; }
}