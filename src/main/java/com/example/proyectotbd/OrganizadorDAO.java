package com.example.proyectotbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrganizadorDAO {

    // -----------------------------------------------------------------
    // MÉTODOS DE CONSULTA PARA COMBO BOXES Y FILTROS
    // -----------------------------------------------------------------

    /**
     * Obtiene una lista de eventos cuya fecha es hoy o futura.
     */
    public ObservableList<OpcionCombo> obtenerEventosFuturos() throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarEventosFuturos()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new OpcionCombo(rs.getInt("evento_id"), rs.getString("nombre_evento")));
            }
        }
        return lista;
    }

    /**
     * Obtiene una lista de todas las categorías (niveles) definidas en el sistema.
     */
    public ObservableList<OpcionCombo> obtenerCategorias() throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();
        String sql = "{call SP_ListarCategorias()}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new OpcionCombo(rs.getInt("categoria_id"), rs.getString("nivel")));
            }
        }
        return lista;
    }

    // -----------------------------------------------------------------
    // MÉTODO DE ASIGNACIÓN DE JUECES (CON FILTRO DE CONFLICTO)
    // -----------------------------------------------------------------

    /**
     * Obtiene una lista de jueces activos que NO tienen conflicto de interés.
     */
    public ObservableList<OpcionCombo> obtenerJuecesSinConflicto(int categoriaId, int eventoId) throws SQLException {
        ObservableList<OpcionCombo> lista = FXCollections.observableArrayList();
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

    /**
     * Asigna un juez a una categoría y evento.
     */
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

    // -----------------------------------------------------------------
    // MÉTODOS DE REPORTE DE EQUIPOS Y ASIGNACIONES (ADMIN)
    // -----------------------------------------------------------------

    /**
     * Obtiene la lista de equipos inscritos para un evento y categoría específicos.
     */
    public ObservableList<EquipoItem> obtenerEquiposAdmin(int eventoId, String nombreCategoria) throws SQLException {
        ObservableList<EquipoItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Admin_ListarEquiposPorEventoYCategoria(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);
            stmt.setString(2, nombreCategoria);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // CORRECCIÓN CLAVE: Se añade el quinto argumento (0)
                    // para el nuevo campo 'conteoJueces' requerido por EquipoItem.
                    lista.add(new EquipoItem(
                            rs.getInt("equipo_id"),
                            rs.getString("nombre_equipo"),
                            rs.getString("institucion_equipo"),
                            rs.getString("estado_inscripcion"),
                            0 // <-- Conteo inicializado a 0
                    ));
                }
            }
        }
        return lista;
    }

    /**
     * Obtiene un reporte detallado de las asignaciones Juez-Categoría-Evento para UN evento específico.
     * @param eventoId ID del evento seleccionado por el Administrador.
     */
    public ObservableList<AsignacionItem> obtenerReporteAsignaciones(int eventoId) throws SQLException {
        ObservableList<AsignacionItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Admin_ListarAsignacionesJueces(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AsignacionItem(
                            rs.getString("nombre_evento"),
                            rs.getString("nombre_categoria"),
                            rs.getString("lista_jueces_categoria")
                    ));
                }
            }
        }
        return lista;
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    /**
     * Calcula y obtiene los resultados finales (puntajes promedio) de todos los equipos
     * inscritos en un evento.
     * @param eventoId ID del evento.
     */
    public ObservableList<ResultadoFinalItem> obtenerResultadosFinales(int eventoId) throws SQLException {
        ObservableList<ResultadoFinalItem> lista = FXCollections.observableArrayList();
        String sql = "{call SP_Admin_ObtenerResultadosFinales(?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, eventoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResultadoFinalItem(
                            rs.getInt("equipo_id"),
                            rs.getString("nombre_equipo"),
                            rs.getString("nombre_categoria"),
                            rs.getString("nombre_coach"),
                            rs.getDouble("puntaje_total_promedio")
                    ));
                }
            }
        }
        return lista;
    }
    // ---------------------------------

    // -----------------------------------------------------------------
    // MÉTODOS DE GESTIÓN (ADMIN CRUD)
    // -----------------------------------------------------------------

    /**
     * Lista todos los usuarios (Coach y/o Juez) para la vista del Administrador.
     */
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

    /**
     * Elimina un usuario por su ID.
     */
    public void eliminarUsuario(int id) throws SQLException {
        String sql = "{call SP_Admin_EliminarUsuario(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, id);
            stmt.execute();
        }
    }

    /**
     * Lista todos los eventos registrados con sus jueces asignados.
     */
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
                        rs.getString("lista_jueces")
                ));
            }
        }
        return lista;
    }

    /**
     * Elimina un evento por su ID (Hard Delete).
     */
    public void eliminarEvento(int id) throws SQLException {
        String sql = "{call SP_Admin_EliminarEvento(?)}";
        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.setInt(1, id);
            stmt.execute();
        }
    }
}