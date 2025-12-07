package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class OrganizadorDAO {

    // 1. Obtener Eventos llamando al SP
    public ObservableList<OpcionCombo> obtenerEventosFuturos() throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();

        // Llamada al procedimiento almacenado
        String sql = "{call SP_ListarEventosFuturos()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Java recibe los datos sin saber de qué tabla vinieron
                lista.add(new OpcionCombo(rs.getInt("evento_id"), rs.getString("nombre_evento")));
            }
        }
        return lista;
    }

    // 2. Obtener Jueces llamando al SP
// OrganizadorDAO.java

    public ObservableList<OpcionCombo> obtenerJuecesSinConflicto(int categoriaId, int eventoId) throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();

        // Llama al nuevo procedimiento que aplica el filtro de Conflicto de Interés
        String sql = "{call SP_ListarJuecesSinConflicto(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, categoriaId);
            stmt.setInt(2, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OpcionCombo(rs.getInt("usuario_id"), rs.getString("nombre")));
                }
            }
        }
        return lista;
    }

    // 3. Asignar Juez (Llama a tu SP de lógica de negocio)
    public void asignarJuez(int juezId, int categoriaId, int eventoId) throws SQLException {
        String sql = "{call SP_AsignarJuezACategoriaSegura(?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, juezId);
            stmt.setInt(2, categoriaId);
            stmt.setInt(3, eventoId);

            stmt.execute();
        }
    }

    // Método para obtener categorías dinámicamente
    public ObservableList<OpcionCombo> obtenerCategorias() throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarCategorias()}"; // Llamada al SP

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Aquí guardamos el ID real de la BD y el nombre
                lista.add(new OpcionCombo(rs.getInt("categoria_id"), rs.getString("nivel")));
            }
        }
        return lista;
    }

    // --- GESTIÓN DE USUARIOS ---
    public ObservableList<UsuarioItem> obtenerTodosLosUsuarios() throws SQLException {
        ObservableList<UsuarioItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Admin_ListarUsuarios()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new UsuarioItem(
                        rs.getInt("usuario_id"),
                        rs.getString("username"),
                        rs.getString("nombre"),
                        rs.getString("institucion"),
                        rs.getString("rol")
                ));
            }
        }
        return lista;
    }

    public void eliminarUsuario(int id) throws SQLException {
        String sql = "{call SP_Admin_EliminarUsuario(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, id);
            stmt.execute();
        }
    }

    // --- GESTIÓN DE EVENTOS ---
    public ObservableList<EventoItem> obtenerTodosLosEventos() throws SQLException {
        ObservableList<EventoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Admin_ListarEventos()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new EventoItem(
                        rs.getInt("evento_id"),
                        rs.getString("nombre_evento"),
                        rs.getString("lugar"),
                        rs.getDate("fecha").toString(),
                        rs.getString("lista_jueces") // <--- NUEVA COLUMNA
                ));
            }
        }
        return lista;
    }

    public void eliminarEvento(int id) throws SQLException {
        String sql = "{call SP_Admin_EliminarEvento(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, id);
            stmt.execute();
        }
    }
}