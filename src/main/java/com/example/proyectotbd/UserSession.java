package com.example.proyectotbd;

public class UserSession {
    private static UserSession instance;

    // --- CAMPOS DE USUARIO Y ROL ---
    private int userId;
    private String username;
    private String nombreCompleto;
    private boolean coach;
    private boolean juez;
    private String institucionUsuario;
    private int equipoIdTemp;
    private boolean modoEdicion = false;
    private int tempEventoId;
    private int tempCategoriaId;
    private String tempCategoriaNombre;
    private String tempNombreEquipo;
    private String tempInstitucion;

    // --- CAMPOS TEMPORALES DEL JUEZ (Evaluación Activa) ---
    private int evaluacionIdTemp;
    private int idDisenoTemp;
    private int idProgramacionTemp;
    private int idConstruccionTemp;

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


    public void cleanUserSession() {
        userId = 0;
        username = null;
        nombreCompleto = null;
        coach = false;
        juez = false;
        institucionUsuario = null;

        equipoIdTemp = 0;
        modoEdicion = false;
        tempEventoId = 0;
        tempCategoriaId = 0;
        tempCategoriaNombre = null;
        tempNombreEquipo = null;
        tempInstitucion = null;

        evaluacionIdTemp = 0;
        idDisenoTemp = 0;
        idProgramacionTemp = 0;
        idConstruccionTemp = 0;
    }

    // =================================================================
    //  GETTERS Y SETTERS PRINCIPALES (Perfil de Usuario Logueado)
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

    // Getter y Setter para la institución del perfil
    public String getInstitucionUsuario() { return institucionUsuario; }
    public void setInstitucionUsuario(String institucionUsuario) { this.institucionUsuario = institucionUsuario; }


    // =================================================================
    //  GETTERS Y SETTERS TEMPORALES
    // =================================================================

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

    // Getter y Setter para la institución temporal (usada durante la inscripción)
    public String getTempInstitucion() { return tempInstitucion; }
    public void setTempInstitucion(String tempInstitucion) { this.tempInstitucion = tempInstitucion; }

    // =================================================================
    //  GETTERS Y SETTERS DEL JUEZ
    // =================================================================

    public int getEvaluacionIdTemp() { return evaluacionIdTemp; }
    public void setEvaluacionIdTemp(int evaluacionIdTemp) { this.evaluacionIdTemp = evaluacionIdTemp; }

    public int getIdDisenoTemp() { return idDisenoTemp; }
    public void setIdDisenoTemp(int idDisenoTemp) { this.idDisenoTemp = idDisenoTemp; }

    public int getIdProgramacionTemp() { return idProgramacionTemp; }
    public void setIdProgramacionTemp(int idProgramacionTemp) { this.idProgramacionTemp = idProgramacionTemp; }

    public int getIdConstruccionTemp() { return idConstruccionTemp; }
    public void setIdConstruccionTemp(int idConstruccionTemp) { this.idConstruccionTemp = idConstruccionTemp; }
}