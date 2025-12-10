package com.example.proyectotbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JuezDAO {
    /**
     * Obtiene la fecha y hora exacta de inicio de un evento por su ID.
     * Requiere que el SP_ObtenerFechaHoraInicio devuelva las columnas 'fecha' y 'hora_inicio'.
     */
    public LocalDateTime obtenerFechaHoraInicioEvento(int eventoId) throws SQLException {
        // SP_ObtenerFechaHoraInicio debe existir en la BD y devolver fecha/hora_inicio
        String sql = "{call SP_ObtenerFechaHoraInicio(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Extraer DATE y TIME de SQL
                    Date sqlDate = rs.getDate("fecha");
                    Time sqlTime = rs.getTime("hora_inicio");

                    // Convertir a LocalDateTime de Java
                    LocalDate localDate = sqlDate.toLocalDate();
                    LocalTime localTime = sqlTime.toLocalTime();

                    return LocalDateTime.of(localDate, localTime);
                }
            }
        }
        return null; // Devuelve nulo si no se encuentra el evento
    }

    // =================================================================
    // MÉTODOS EXISTENTES
    // =================================================================

    // Nuevo método que acepta Connection para ser parte de la transacción de guardado
    public EvaluacionIds iniciarEvaluacion(Connection conn, int equipoId, int eventoId, int juezId) throws SQLException {
        String sql = "{call SP_IniciarEvaluacionSegura(?, ?, ?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.setInt(2, eventoId);
            stmt.setInt(3, juezId);
            stmt.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new EvaluacionIds(
                            rs.getInt("nueva_evaluacion_id"),
                            rs.getInt("id_disenio"),
                            rs.getInt("id_programacion"),
                            rs.getInt("id_construccion")
                    );
                }
            }
        }
        throw new SQLException("No se pudieron generar los IDs de evaluación.");
    }

    // Clase auxiliar simple (similar a IdsAreas) para contener todos los IDs necesarios
    public static class EvaluacionIds {
        public int evaluacionId;
        public int idDiseno, idProg, idConst;

        public EvaluacionIds(int evalId, int d, int p, int c) {
            this.evaluacionId = evalId;
            this.idDiseno = d;
            this.idProg = p;
            this.idConst = c;
        }
    }

    public IdsAreas obtenerIdsAreas(Connection conn, int evaluacionId, int juezId) throws SQLException {
        String sql = "{call SP_ObtenerIdsAreas(?, ?)}";

        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, evaluacionId);
            stmt.setInt(2, juezId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new IdsAreas(
                            rs.getInt("id_disenio"),
                            rs.getInt("id_programacion"),
                            rs.getInt("id_construccion")
                    );
                }
            }
        }
        throw new SQLException("No se encontraron las áreas para esta evaluación.");
    }

    // --- MÉTODOS DE GUARDADO (Reciben la conexión para mantener la transacción) ---

    public void registrarBitacora(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeBitacora(?, ?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarMedioDigital(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeMedioDigital(?, ?, ?, ?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarInspeccionProg(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeProgramacionInspeccion(?, ?, ?, ?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarAutonomo(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeProgramacionAutonomo(?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarManipulado(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeProgramacionManipulado(?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarDemostracion(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeProgramacionDemostracion(?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarConstruccionInsp(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeConstruccionInspeccion(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    public void registrarLibreta(Connection conn, int id, boolean... p) throws SQLException {
        String sql = "{call SP_RegistrarPuntajeConstruccionLibreta(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        ejecutarUpdate(conn, sql, id, p);
    }

    // Helper privado para ejecutar los updates de booleanos
    private void ejecutarUpdate(Connection conn, String sql, int idArea, boolean... checks) throws SQLException {
        try (CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, idArea);
            for (int i = 0; i < checks.length; i++) {
                stmt.setBoolean(i + 2, checks[i]);
            }
            stmt.execute();
        }
    }

    // Clase interna para transportar los 3 IDs
    public static class IdsAreas {
        public int idDiseno, idProg, idConst;

        public IdsAreas(int d, int p, int c) {
            this.idDiseno = d;
            this.idProg = p;
            this.idConst = c;
        }
    }

    public ObservableList<EquipoItem> obtenerEquiposPorEventoYCategoria(int eventoId, String categoriaNombre, int juezId) throws SQLException {
        ObservableList<EquipoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarEquiposPorCategoria(?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);
            stmt.setString(2, categoriaNombre);
            stmt.setInt(3, juezId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EquipoItem(
                            rs.getInt("equipo_id"),
                            rs.getString("nombre_equipo"),
                            rs.getString("institucion_equipo"),
                            rs.getString("estado_evaluacion"),
                            rs.getInt("conteo_jueces")
                    ));
                }
            }
        }
        return lista;
    }

    public ObservableList<ReporteJuezItem> obtenerHistorial(int juezId) throws SQLException {
        ObservableList<ReporteJuezItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarHistorialJuez(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, juezId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteJuezItem(
                            rs.getString("nombre_equipo"),
                            rs.getString("categoria_nivel"),
                            String.valueOf(rs.getInt("pts_disenio")),
                            String.valueOf(rs.getInt("pts_programacion")),
                            String.valueOf(rs.getInt("pts_construccion")),
                            String.valueOf(rs.getInt("total_juez"))
                    ));
                }
            }
        }
        return lista;
    }

    // Método para listar SOLO los eventos asignados al juez logueado
    public ObservableList<EventoItem> obtenerEventosDelJuez(int juezId) throws SQLException {
        ObservableList<EventoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarEventosDelJuez(?)}"; // Nuevo SP

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, juezId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EventoItem(
                            rs.getInt("evento_id"),
                            rs.getString("nombre_evento"),
                            rs.getString("lugar"),
                            rs.getDate("fecha").toString(),
                            "Tú estás asignado", // lista_jueces
                            rs.getTime("hora_inicio").toString(), // Hora de inicio
                            rs.getTime("hora_fin").toString()     // Hora de fin
                    ));
                }
            }
        }
        return lista;
    }

    public List<String> obtenerCategoriasAsignadas(int juezId, int eventoId) throws SQLException {
        List<String> categorias = new ArrayList<>();

        String sql = "{call SP_ObtenerCategoriaAsignada(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, juezId);
            stmt.setInt(2, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    categorias.add(rs.getString("nivel"));
                }
            }
        }
        return categorias;
    }
}