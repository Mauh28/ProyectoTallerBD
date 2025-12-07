package com.example.proyectotbd;

public class UserSession {

    // Instancia única de la clase (Singleton)
    private static UserSession instance;

    // Datos que queremos recordar del usuario logueado
    private int userId;
    private String username;
    private String nombreCompleto;
    private int equipoIdTemp;
    // NUEVA VARIABLE
    private boolean modoEdicion = false;
    private int evaluacionIdTemp; // <--- AGREGAR ESTA VARIABLE

    // VARIABLES TEMPORALES PARA EL REGISTRO TRANSACCIONAL
    private int tempEventoId;
    private int tempCategoriaId;
    private String tempCategoriaNombre;
    private String tempNombreEquipo;
    private String tempInstitucion;

    // --- NUEVAS VARIABLES DE ROL ---
    private boolean coach;
    private boolean juez;

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
        tempEventoId = 0;
        tempCategoriaId = 0;
        tempCategoriaNombre = null;
        tempNombreEquipo = null;
        tempInstitucion = null;
        modoEdicion = false;
        evaluacionIdTemp = 0; // <--- AGREGAR ESTO PARA LIMPIAR
    }

    // --- MÉTODOS PARA EVALUACIÓN ---
    public int getEvaluacionIdTemp() {
        return evaluacionIdTemp;
    }

    public void setEvaluacionIdTemp(int evaluacionIdTemp) {
        this.evaluacionIdTemp = evaluacionIdTemp;
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

    // --- GETTERS Y SETTERS NUEVOS ---
    public int getTempEventoId() { return tempEventoId; }
    public void setTempEventoId(int tempEventoId) { this.tempEventoId = tempEventoId; }

    public int getTempCategoriaId() { return tempCategoriaId; }
    public void setTempCategoriaId(int tempCategoriaId) { this.tempCategoriaId = tempCategoriaId; }

    public String getTempCategoriaNombre() { return tempCategoriaNombre; }
    public void setTempCategoriaNombre(String tempCategoriaNombre) { this.tempCategoriaNombre = tempCategoriaNombre; }

    public String getTempNombreEquipo() { return tempNombreEquipo; }
    public void setTempNombreEquipo(String tempNombreEquipo) { this.tempNombreEquipo = tempNombreEquipo; }

    public String getTempInstitucion() { return tempInstitucion; }
    public void setTempInstitucion(String tempInstitucion) { this.tempInstitucion = tempInstitucion; }

    // --- NUEVOS GETTERS/SETTERS DE ROL ---
    public boolean isCoach() { return coach; }
    public void setCoach(boolean coach) { this.coach = coach; }

    public boolean isJuez() { return juez; }
    public void setJuez(boolean juez) { this.juez = juez; }

    public boolean isModoEdicion() { return modoEdicion; }
    public void setModoEdicion(boolean modoEdicion) { this.modoEdicion = modoEdicion; }
}