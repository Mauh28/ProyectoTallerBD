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
                            rs.getString("nombre_equipo"),
                            rs.getString("institucion_equipo"),
                            rs.getString("categoria_nivel"),
                            rs.getString("nombre_evento")
                    ));
                }
            }
        }
        return lista;
    }
}