package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class JuezDAO {

    // --- MÉTODOS EXISTENTES ---
    public int iniciarEvaluacion(int equipoId, int eventoId, int juezId) throws SQLException {
        // ... (Tu código existente) ...
        // Nota: Asegúrate de que este método devuelva el ID o lo maneje como vimos antes
        String sql = "{call SP_IniciarEvaluacionSegura(?, ?, ?, ?)}";
        int nuevaEvaluacionId = 0;
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, equipoId);
            stmt.setInt(2, eventoId);
            stmt.setInt(3, juezId);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            if(stmt.execute()) {
                try(ResultSet rs = stmt.getResultSet()){
                    if(rs.next()) nuevaEvaluacionId = rs.getInt("nueva_evaluacion_id");
                }
            }
        }
        return nuevaEvaluacionId;
    }

    // --- MÉTODO CORREGIDO ---
    // Ahora recibe la Connection 'conn' como parámetro
    public IdsAreas obtenerIdsAreas(Connection conn, int evaluacionId, int juezId) throws SQLException {
        String sql = "{call SP_ObtenerIdsAreas(?, ?)}"; // Asegúrate que el nombre coincida con tu SQL

        // OJO: NO usamos try(Connection...) aquí, porque eso la cerraría.
        // Solo usamos try para el CallableStatement.
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
        public IdsAreas(int d, int p, int c) { this.idDiseno=d; this.idProg=p; this.idConst=c; }
    }

    public ObservableList<EquipoItem> obtenerEquiposPorEventoYCategoria(int eventoId, String categoriaNombre) throws SQLException {
        ObservableList<EquipoItem> lista = FXCollections.observableArrayList();

        // Llamada al procedimiento que acabamos de crear
        String sql = "{call SP_ListarEquiposPorCategoria(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);
            stmt.setString(2, categoriaNombre);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new EquipoItem(
                            rs.getInt("equipo_id"),
                            rs.getString("nombre_equipo"),
                            rs.getString("institucion_equipo"),
                            rs.getString("estado_evaluacion") // Retorna "EVALUADO" o "PENDIENTE"
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
}