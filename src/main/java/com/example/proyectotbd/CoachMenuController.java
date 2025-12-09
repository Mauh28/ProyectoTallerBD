package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class CoachMenuController {

    @FXML private Button btnVerReportes;
    @FXML private Label lblMensajeEstado;
    @FXML private Button btnCambiarRol; // Botón para usuarios híbridos

    private CoachDAO coachDAO = new CoachDAO();

    @FXML
    public void initialize() {
        verificarEstadoEquipos();
        verificarDobleRol();
    }

    private void verificarEstadoEquipos() {
        int usuarioId = UserSession.getInstance().getUserId();
        try {
            boolean tieneEquipos = coachDAO.tieneEquiposRegistrados(usuarioId);

            if (tieneEquipos) {
                btnVerReportes.setDisable(false);
                lblMensajeEstado.setVisible(false);
            } else {
                btnVerReportes.setDisable(true);
                lblMensajeEstado.setText("No tienes equipos registrados, registra uno.");
                lblMensajeEstado.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                lblMensajeEstado.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void verificarDobleRol() {
        if (UserSession.getInstance().isJuez()) {
            btnCambiarRol.setVisible(true);
            btnCambiarRol.setManaged(true);
        }
    }

    /**
     * CORRECCIÓN: Ahora navega a la vista de listado de eventos (coach_verEventos.fxml)
     * para que el Coach pueda seleccionar el evento antes de registrar el equipo.
     */
    @FXML
    public void handleRegistrarEquipo(ActionEvent event) {
        cambiarVista(event, "coach_verEventos.fxml");
    }

    @FXML
    public void handleVerReportes(ActionEvent event) {
        cambiarVista(event, "coach_misEquipos.fxml");
    }

    @FXML
    public void handleCambiarAJuez(ActionEvent event) {
        System.out.println("Cambiando a rol de Juez...");
        cambiarVista(event, "juez_menu.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión...");
        cambiarVista(event, "login.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Obtener el Stage (ventana) actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crear nueva escena
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleVerResultados(ActionEvent event) {
        System.out.println("Navegando a Resultados de Evaluación...");
        cambiarVista(event, "coach_reportes.fxml");
    }
}