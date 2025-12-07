package com.example.proyectotbd;

public class UserSession {

    // Instancia única de la clase (Singleton)
    private static UserSession instance;

    // --- CAMPOS DE USUARIO Y ROL ---
    private int userId;
    private String username;
    private String nombreCompleto;
    private boolean coach;
    private boolean juez;

    // --- CAMPOS TEMPORALES DEL COACH (Registro/Edición de Equipo) ---
    private int equipoIdTemp;
    private boolean modoEdicion = false;
    private int tempEventoId;
    private int tempCategoriaId;
    private String tempCategoriaNombre;
    private String tempNombreEquipo;
    private String tempInstitucion;

    // --- CAMPOS TEMPORALES DEL JUEZ (Evaluación Activa) ---
    private int evaluacionIdTemp;
    private int idDisenoTemp;        // ¡Añadido! ID del registro de Diseño
    private int idProgramacionTemp;  // ¡Añadido! ID del registro de Programación
    private int idConstruccionTemp;  // ¡Añadido! ID del registro de Construcción

    // Constructor privado para el patrón Singleton
    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    // =================================================================
    //  MÉTODO DE LIMPIEZA
    // =================================================================

    /**
     * Limpia todos los datos de sesión al cerrar la aplicación o cambiar de usuario.
     */
    public void cleanUserSession() {
        userId = 0;
        username = null;
        nombreCompleto = null;
        coach = false;
        juez = false;

        equipoIdTemp = 0;
        modoEdicion = false;
        tempEventoId = 0;
        tempCategoriaId = 0;
        tempCategoriaNombre = null;
        tempNombreEquipo = null;
        tempInstitucion = null;

        evaluacionIdTemp = 0;
        idDisenoTemp = 0;         // Limpieza de campos de Juez
        idProgramacionTemp = 0;   // Limpieza de campos de Juez
        idConstruccionTemp = 0;   // Limpieza de campos de Juez
    }

    // =================================================================
    //  GETTERS Y SETTERS PRINCIPALES (Usuario, Roles, Edición Coach)
    // =================================================================

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public boolean isCoach() { return coach; }
    public void setCoach(boolean coach) { this.coach = coach; }

    public boolean isJuez() { return juez; }
    public void setJuez(boolean juez) { this.juez = juez; }

    public int getEquipoIdTemp() { return equipoIdTemp; }
    public void setEquipoIdTemp(int equipoIdTemp) { this.equipoIdTemp = equipoIdTemp; }

    public boolean isModoEdicion() { return modoEdicion; }
    public void setModoEdicion(boolean modoEdicion) { this.modoEdicion = modoEdicion; }

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

    // =================================================================
    //  GETTERS Y SETTERS DEL JUEZ (Evaluación Activa)
    // =================================================================

    public int getEvaluacionIdTemp() { return evaluacionIdTemp; }
    public void setEvaluacionIdTemp(int evaluacionIdTemp) { this.evaluacionIdTemp = evaluacionIdTemp; }

    // Nuevos métodos para los IDs de las áreas
    public int getIdDisenoTemp() { return idDisenoTemp; }
    public void setIdDisenoTemp(int idDisenoTemp) { this.idDisenoTemp = idDisenoTemp; }

    public int getIdProgramacionTemp() { return idProgramacionTemp; }
    public void setIdProgramacionTemp(int idProgramacionTemp) { this.idProgramacionTemp = idProgramacionTemp; }

    public int getIdConstruccionTemp() { return idConstruccionTemp; }
    public void setIdConstruccionTemp(int idConstruccionTemp) { this.idConstruccionTemp = idConstruccionTemp; }
}