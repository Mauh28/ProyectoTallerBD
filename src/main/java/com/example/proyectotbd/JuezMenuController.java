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

public class JuezMenuController {

    @FXML private Button btnEvaluar;
    @FXML private Button btnReportes;
    @FXML private Button btnCambiarRol;

    // --- NUEVO CAMPO FXML ---
    @FXML private Label lblBienvenida; // Label que mostrará "Hola, [Nombre Completo] (Juez)"
    // --------------------------

    @FXML
    public void initialize() {
        // 1. Cargar el nombre y el rol del usuario logueado
        cargarNombreUsuario();

        // 2. Verificar si tiene doble rol (Coach)
        verificarDobleRol();
    }

    /**
     * Carga el nombre del usuario desde la sesión y lo muestra en la barra superior.
     */
    private void cargarNombreUsuario() {
        String nombre = UserSession.getInstance().getNombreCompleto();
        String rol;

        // Determinar el rol principal a mostrar (si es Juez, será Juez)
        if (UserSession.getInstance().isJuez()) {
            rol = "Juez";
        } else if (UserSession.getInstance().isCoach()) {
            rol = "Coach";
        } else {
            rol = "Usuario";
        }

        if (lblBienvenida != null && nombre != null) {
            // Establece el texto: "Hola, [Nombre Completo] ([Rol])"
            lblBienvenida.setText("Hola, " + nombre);
        } else if (lblBienvenida != null) {
            // Fallback si la sesión no tiene nombre
            lblBienvenida.setText("Hola, Juez");
        }
    }

    private void verificarDobleRol() {
        if (UserSession.getInstance().isCoach()) {
            btnCambiarRol.setVisible(true);
            btnCambiarRol.setManaged(true);
        }
    }

    @FXML
    public void handleIrAEvaluar(ActionEvent event) {
        System.out.println("Ir a Seleccionar Evento...");
        // Asegúrate de que esta vista (juez_evento.fxml) usa el controlador JuezEventoController que corregimos
        cambiarVista(event, "juez_evento.fxml");
    }

    @FXML
    public void handleIrAReportes(ActionEvent event) {
        System.out.println("Ir a Reportes...");
        cambiarVista(event, "juez_reportes.fxml");
    }

    @FXML
    public void handleCambiarACoach(ActionEvent event) {
        System.out.println("Cambiando a rol de Coach...");
        cambiarVista(event, "coach_menu.fxml");
    }

    @FXML
    public void handleCerrarSesion(ActionEvent event) {
        System.out.println("Cerrando sesión...");
        // Limpiar sesión antes de ir al login
        UserSession.getInstance().cleanUserSession();
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
}