package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class CoachRegistroEquipoController {

    @FXML private TextField txtNombreEquipo;
    // @FXML private TextField txtInstitucion;
    @FXML private Label lblMensaje;

    @FXML private ComboBox<OpcionCombo> cbEventos;
    @FXML private Label lblCategoriaSeleccionada;

    private String categoriaTexto = null;
    private int categoriaId = 0;
    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        cargarEventos();
        recuperarDatosDeSesion(); // <--- NUEVO: Recuperar si volvemos atrás
        // Activar validación de nombre en tiempo real ---
        configurarValidacionNombre();
    }

    private void cargarEventos() {
        try {
            cbEventos.setItems(dao.obtenerEventosFuturos());
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar eventos: " + e.getMessage(), true);
        }
    }

    // Restaurar datos si el usuario regresó para corregir
    private void recuperarDatosDeSesion() {
        UserSession session = UserSession.getInstance();

        // 1. Restaurar Texto
        if (session.getTempNombreEquipo() != null) txtNombreEquipo.setText(session.getTempNombreEquipo());
        // if (session.getTempInstitucion() != null) txtInstitucion.setText(session.getTempInstitucion());

        // 2. Restaurar Categoría (Visual y Lógica)
        if (session.getTempCategoriaNombre() != null) {
            this.categoriaTexto = session.getTempCategoriaNombre();
            this.categoriaId = session.getTempCategoriaId();
            lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
            lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
        }

        // 3. Restaurar Evento (Un poco más complejo en ComboBox, buscamos por ID)
        if (session.getTempEventoId() != 0) {
            for (OpcionCombo item : cbEventos.getItems()) {
                if (item.getId() == session.getTempEventoId()) {
                    cbEventos.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    @FXML
    public void handleCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        categoriaTexto = btn.getText();

        // Mapeo ID (Asegúrate de que coincida con tu BD)
        switch (categoriaTexto) {
            case "Primaria": categoriaId = 1; break;
            case "Secundaria": categoriaId = 2; break;
            case "Preparatoria": categoriaId = 3; break;
            case "Profesional": categoriaId = 4; break;
            default: categoriaId = 0;
        }

        lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
    }

    @FXML
    public void handleContinuar(ActionEvent event) {
        OpcionCombo eventoSeleccionado = cbEventos.getValue();
        String nombre = txtNombreEquipo.getText().trim();

        // 1. Validaciones Visuales
        if (eventoSeleccionado == null) { mostrarMensaje("Selecciona un evento.", true); return; }
        if (categoriaTexto == null) { mostrarMensaje("Selecciona una categoría.", true); return; }
        if (nombre.isEmpty()) { mostrarMensaje("Escribe el nombre del equipo.", true); return; } // <--- CAMBIO


        // --- NUEVA VALIDACIÓN: BLOQUEAR SI NO HAY PALABRA REAL ---
        // Verificamos si cumple el patrón de tener al menos 2 letras consecutivas
        if (!nombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*")) {
            mostrarMensaje("El nombre debe contener al menos una palabra real (mínimo 2 letras seguidas).", true);
            txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            return;
        }

        // 2. VALIDACIÓN EN BD (Sin Insertar)
        // Usamos la nueva Función FN_VerificarDisponibilidadEquipo
        String sql = "{? = call FN_VerificarDisponibilidadEquipo(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, Types.INTEGER); // Valor de retorno
            stmt.setString(2, nombre);
            stmt.setInt(3, categoriaId);

            stmt.execute();

            int existe = stmt.getInt(1);

            if (existe > 0) {
                mostrarMensaje("El nombre del equipo ya existe en esta categoría.", true);
                return;
            }

            // 3. SI TODO ESTÁ BIEN: GUARDAR EN SESIÓN Y AVANZAR
            UserSession session = UserSession.getInstance();
            session.setTempEventoId(eventoSeleccionado.getId());
            session.setTempCategoriaId(categoriaId);
            session.setTempCategoriaNombre(categoriaTexto);
            session.setTempNombreEquipo(nombre);

            // --- CAMBIO IMPORTANTE ---
            // Tomamos la institución del perfil del usuario logueado
            session.setTempInstitucion(session.getInstitucionUsuario());
            // -------------------------
            System.out.println("Institución asignada automáticamente: " + session.getInstitucionUsuario());

            System.out.println("Datos guardados en memoria. Pasando a integrantes...");
            cambiarVista(event, "coach_registroIntegrantes.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error BD: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Limpiamos los temporales si cancela todo el proceso
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setTempInstitucion(null);
        // ... (opcional limpiar resto)

        cambiarVista(event, "coach_menu.fxml");
    }

    private void mostrarMensaje(String msg, boolean error) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void configurarValidacionNombre() {
        txtNombreEquipo.textProperty().addListener((observable, oldValue, newValue) -> {

            // 1. Validación de longitud (Protección BD)
            if (newValue.length() > 50) {
                txtNombreEquipo.setText(oldValue);
                return;
            }

            // 2. NUEVA VALIDACIÓN: ¿Contiene al menos una "palabra" (2 letras seguidas)?
            // Regex: busca cualquier cosa, luego 2+ letras juntas, luego cualquier cosa
            boolean contienePalabra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*");

            // Si hay texto pero NO contiene una palabra válida -> Borde Rojo
            if (!newValue.isEmpty() && !contienePalabra) {
                txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            } else {
                // Estilo normal (Limpio)
                txtNombreEquipo.setStyle("");
            }
        });
    }
}