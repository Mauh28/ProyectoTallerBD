package com.example.proyectotbd;

public class UserSession {

    // Instancia única de la clase (Singleton)
    private static UserSession instance;

    // Datos que queremos recordar del usuario logueado
    private int userId;
    private String username;
    private String nombreCompleto;
    private int equipoIdTemp;

    // Constructor privado para que nadie pueda hacer "new UserSession()"
    private UserSession() {}

    // Método estático para obtener la única instancia disponible
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // Método para borrar datos al Cerrar Sesión
    public void cleanUserSession() {
        userId = 0;
        username = null;
        nombreCompleto = null;
    }

    // --- GETTERS Y SETTERS ---

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getEquipoIdTemp() {
        return equipoIdTemp;
    }

    public void setEquipoIdTemp(int equipoIdTemp) {
        this.equipoIdTemp = equipoIdTemp;
    }
}