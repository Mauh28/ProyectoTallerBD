package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CoachDAO {

    // Verifica si existen equipos (Devuelve true/false)
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

    // Obtiene la lista para la tabla
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

    // metodo para llamar participantes (necesario para editar) - esta obsoleto me parece
    public void limpiarParticipantes(int equipoId) throws SQLException {
        String sql = "{call SP_EliminarParticipantesPorEquipo(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.execute();
        }
    }

    // metodo para eliminar equipo - eliminar
    public void eliminarEquipo(int equipoId) throws SQLException {
        String sql = "{call SP_EliminarEquipoCoach(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.execute();
        }
    }

    // metodo que trae participantes para cuando vamos a editarlos
    public ObservableList<String> obtenerParticipantes(int equipoId) throws SQLException {
        ObservableList<String> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarParticipantesPorEquipo(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Formato compatible con tu lista visual: Nombre | Fecha | Sexo
                    String p = rs.getString("nombre_participante") + " | " +
                            rs.getDate("fecha_nac") + " | " +
                            rs.getString("sexo");
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    // metodo que obtiene el reporte de evaluacion
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
                            rs.getString("pts_disenio"),
                            rs.getString("pts_programacion"),
                            rs.getString("pts_construccion"),
                            rs.getString("total_promedio")
                    ));
                }
            }
        }
        return lista;
    }

    /**
     * Obtiene la lista de eventos donde el coach NO es juez y que son futuros.
     */
    public ObservableList<EventoItem> obtenerEventosSinConflicto(int coachId) throws SQLException {
        ObservableList<EventoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Coach_ListarEventosSinConflicto(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, coachId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Debe leer 6 columnas (id, nombre, lugar, fecha, horaInicio, horaFin)
                    lista.add(new EventoItem(
                            rs.getInt("evento_id"),
                            rs.getString("nombre_evento"),
                            rs.getString("lugar"),
                            rs.getDate("fecha").toString(),
                            "N/A", // Jueces no aplica para esta vista
                            rs.getTime("hora_inicio").toString(),
                            rs.getTime("hora_fin").toString()
                    ));
                }
            }
        }
        return lista;
    }

}