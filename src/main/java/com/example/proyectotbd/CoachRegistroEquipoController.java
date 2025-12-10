package com.example.proyectotbd;

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
import java.sql.SQLException;
import java.sql.Types;

public class CoachRegistroEquipoController {

    @FXML private TextField txtNombreEquipo;
    @FXML private Label lblMensaje;

    @FXML private Label lblEventoSeleccionado;
    @FXML private Label lblCategoriaSeleccionada;

    private String categoriaTexto = null;
    private int categoriaId = 0;
    private OrganizadorDAO dao = new OrganizadorDAO(); // Se mantiene por si se usa en el futuro, aunque no se usa en el código visible.

    @FXML
    public void initialize() {
        mostrarEventoSeleccionado();
        recuperarDatosDeSesion();
        configurarValidacionNombre();
    }

    /**
     * Muestra el nombre del evento seleccionado, obtenido desde la sesión temporal.
     */
    private void mostrarEventoSeleccionado() {
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (eventoId != 0 && lblEventoSeleccionado != null) {
            lblEventoSeleccionado.setText("Inscribiendo al Evento ID: " + eventoId);
        } else {
            lblEventoSeleccionado.setText("Error: Evento no preseleccionado.");
        }
    }

    private void recuperarDatosDeSesion() {
        UserSession session = UserSession.getInstance();

        // 1. Restaurar Nombre de Equipo
        if (session.getTempNombreEquipo() != null) txtNombreEquipo.setText(session.getTempNombreEquipo());

        // 2. Restaurar Categoría (Visual y Lógica)
        if (session.getTempCategoriaNombre() != null) {
            this.categoriaTexto = session.getTempCategoriaNombre();
            this.categoriaId = session.getTempCategoriaId();
            lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
            lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
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
        UserSession session = UserSession.getInstance();

        int usuarioId = session.getUserId();

        int eventoId = session.getTempEventoId();
        String nombre = txtNombreEquipo.getText().trim();

        if (eventoId == 0) {
            mostrarMensaje("Error: Debe seleccionar un evento en la vista anterior.", true);
            return;
        }
        if (categoriaTexto == null) { mostrarMensaje("Selecciona una categoría.", true); return; }
        if (nombre.isEmpty()) { mostrarMensaje("Escribe el nombre del equipo.", true); return; }

        if (!nombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*")) {
            mostrarMensaje("El nombre debe contener al menos una palabra real (mínimo 2 letras seguidas).", true);
            txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            return;
        }

        String sql = "{call SP_NombreEquipoExiste(?, ?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, usuarioId);
            stmt.setString(2, categoriaTexto);
            stmt.setString(3, nombre);
            stmt.setInt(4, eventoId);

            stmt.registerOutParameter(5, Types.INTEGER); // p_nuevo_equipo_id OUT

            stmt.execute();

            int nuevoEquipoId = stmt.getInt(5); // Obtener el ID del equipo recién creado

            session.setEquipoIdTemp(nuevoEquipoId);

            session.setTempEventoId(eventoId);
            session.setTempCategoriaId(categoriaId);
            session.setTempCategoriaNombre(categoriaTexto);
            session.setTempNombreEquipo(nombre);
            session.setTempInstitucion(session.getInstitucionUsuario());

            mostrarMensaje("Equipo registrado exitosamente. ID: " + nuevoEquipoId, false);
            txtNombreEquipo.setStyle("");

            System.out.println("Datos guardados en memoria. Pasando a integrantes...");

            cambiarVista(event, "coach_registroIntegrantes.fxml");

        } catch (SQLException e) {
            String errorMessage = e.getMessage();

            if (errorMessage != null && errorMessage.contains("Error:")) {
                mostrarMensaje(errorMessage.substring(errorMessage.indexOf("Error:")), true);
            } else {
                mostrarMensaje(errorMessage, true);
            }

            txtNombreEquipo.setStyle("");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_verEventos.fxml");
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setTempCategoriaNombre(null);
        UserSession.getInstance().setTempCategoriaId(0);
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
            if (newValue.length() > 50) {
                txtNombreEquipo.setText(oldValue);
                return;
            }

            boolean contienePalabra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*");

            if (!newValue.isEmpty() && !contienePalabra) {
                txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            } else {
                txtNombreEquipo.setStyle("");
            }
        });
    }
}