package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CoachRegistroEquipoController {

    @FXML private TextField txtNombreEquipo;
    @FXML private TextField txtInstitucion;
    @FXML private Label lblMensaje;

    @FXML private ComboBox<OpcionCombo> cbEventos;
    @FXML private Label lblCategoriaSeleccionada;

    private String categoriaTexto = null;
    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        cargarEventos();
    }

    private void cargarEventos() {
        try {
            cbEventos.setItems(dao.obtenerEventosFuturos());
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar eventos: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        categoriaTexto = btn.getText();

        lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
    }

    @FXML
    public void handleContinuar(ActionEvent event) {
        OpcionCombo eventoSeleccionado = cbEventos.getValue();
        String nombre = txtNombreEquipo.getText();
        String institucion = txtInstitucion.getText();

        if (eventoSeleccionado == null) {
            mostrarMensaje("Selecciona un evento.", true); return;
        }
        if (categoriaTexto == null) {
            mostrarMensaje("Selecciona una categoría (haz clic en un botón).", true); return;
        }
        if (nombre.isEmpty() || institucion.isEmpty()) {
            mostrarMensaje("Llena el nombre del equipo e institución.", true); return;
        }

        int usuarioId = UserSession.getInstance().getUserId();

        try (Connection conn = ConexionDB.getConnection()) {

            // A. Registrar Equipo
            String sqlEquipo = "{call SP_NombreEquipoExiste(?, ?, ?, ?)}";
            int nuevoEquipoId = 0;

            try (CallableStatement stmt = conn.prepareCall(sqlEquipo)) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, categoriaTexto);
                stmt.setString(3, nombre);
                stmt.setString(4, institucion);

                boolean hasResults = stmt.execute();
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        if (rs.next()) {
                            nuevoEquipoId = rs.getInt("nuevo_equipo_id");
                        }
                    }
                }
            }

            if (nuevoEquipoId == 0) {
                mostrarMensaje("Error: No se obtuvo el ID del equipo.", true);
                return;
            }

            // B. Inscribir en Evento
            String sqlInscripcion = "{call SP_RegistrarEquipoEnEvento(?, ?)}";
            try (CallableStatement stmt = conn.prepareCall(sqlInscripcion)) {
                stmt.setInt(1, nuevoEquipoId);
                stmt.setInt(2, eventoSeleccionado.getId());
                stmt.execute();
            }

            // --- ÉXITO ---
            UserSession.getInstance().setEquipoIdTemp(nuevoEquipoId);

            // 1. Mostrar Pop-up
            mostrarNotificacionExito("¡Equipo '" + nombre + "' creado correctamente!");

            // 2. Cambiar vista
            cambiarVista(event, "coach_registroIntegrantes.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error BD: " + e.getMessage(), true);
        }
    }

    // --- MÉTODO POP-UP ---
    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label("✅ " + mensaje);
            label.setStyle(
                    "-fx-background-color: #27ae60;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 20px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);"
            );

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);

            toastStage.show();
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
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
}