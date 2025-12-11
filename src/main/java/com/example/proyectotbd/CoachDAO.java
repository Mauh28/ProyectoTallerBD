package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CoachDAO {

    // 1. Verifica si existen equipos (Devuelve true/false)
    public boolean tieneEquiposRegistrados(int usuarioId) throws SQLException {
        String sql = "{call SP_VerificarEquiposDelCoach(?, ?)}";
        boolean tieneEquipos = false;

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.registerOutParameter(2, Types.BOOLEAN); // Parámetro de SALIDA

            stmt.execute();

            tieneEquipos = stmt.getBoolean(2);
        }
        return tieneEquipos;
    }

    // 2. Obtiene la lista para la tabla "Mis Equipos"
    public ObservableList<EquipoCoachItem> obtenerMisEquipos(int usuarioId) throws SQLException {
        ObservableList<EquipoCoachItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarMisEquiposCoach(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EquipoCoachItem(
                            rs.getInt("equipo_id"),
                            rs.getString("nombre_equipo"),
                            rs.getString("institucion_equipo"),
                            rs.getString("categoria_nivel"),
                            rs.getString("nombre_evento"),
                            rs.getString("integrantes")
                    ));
                }
            }
        }
        return lista;
    }

    // 3. Método para limpiar participantes (útil al editar)
    public void limpiarParticipantes(int equipoId) throws SQLException {
        String sql = "{call SP_EliminarParticipantesPorEquipo(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.execute();
        }
    }

    // 4. Método para eliminar equipo
    public void eliminarEquipo(int equipoId) throws SQLException {
        String sql = "{call SP_EliminarEquipoCoach(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.execute();
        }
    }

    // 5. Método que trae participantes para editarlos
    public ObservableList<String> obtenerParticipantes(int equipoId) throws SQLException {
        ObservableList<String> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarParticipantesPorEquipo(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Formato: Nombre | Fecha | Sexo
                    String p = rs.getString("nombre_participante") + " | " +
                            rs.getDate("fecha_nac") + " | " +
                            rs.getString("sexo");
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // 6. Método que obtiene el reporte de evaluación
    public ObservableList<ReporteCoachItem> obtenerReporteEvaluaciones(int coachId) throws SQLException {
        ObservableList<ReporteCoachItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Coach_ObtenerReporteEvaluaciones(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, coachId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ReporteCoachItem(
                            rs.getString("nombre_equipo"),
                            rs.getString("nombre_categoria"),
                            rs.getString("nombre_evento"),
                            rs.getString("evaluado"), // <--- Nueva columna EVALUADO
                            rs.getString("pts_disenio"),
                            rs.getString("pts_programacion"),
                            rs.getString("pts_construccion"),
                            rs.getString("total_promedio")
                    ));
                }
            }
        }
        return lista;
    } // <--- ¡AQUÍ PROBABLEMENTE FALTABA TU LLAVE EN EL CÓDIGO ANTERIOR!

    // 7. Obtiene la lista de eventos futuros donde el coach NO es juez
    public ObservableList<EventoItem> obtenerEventosSinConflicto(int coachId) throws SQLException {
        ObservableList<EventoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Coach_ListarEventosSinConflicto(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, coachId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EventoItem(
                            rs.getInt("evento_id"),
                            rs.getString("nombre_evento"),
                            rs.getString("lugar"),
                            rs.getDate("fecha").toString(),
                            "N/A", // Jueces no aplica aquí
                            rs.getTime("hora_inicio").toString(),
                            rs.getTime("hora_fin").toString()
                    ));
                }
            }
        }
        return lista;
    }
}