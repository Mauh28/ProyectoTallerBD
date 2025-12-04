package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public class JuezMenuController {

    @FXML private Button btnEvaluar;
    @FXML private Button btnReportes;
    @FXML private Button btnCambiarRol; // Nuevo botón

    @FXML
    public void initialize() {
        // Verificar si tiene doble rol
        if (UserSession.getInstance().isCoach()) {
            btnCambiarRol.setVisible(true);
            btnCambiarRol.setManaged(true);
        }
    }

    @FXML
    public void handleIrAEvaluar(ActionEvent event) {
        System.out.println("Ir a Seleccionar Evento...");
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
        cambiarVista(event, "login.fxml"); // Usamos el método estándar
    }



    // Método auxiliar para navegación
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método especial para cuando el evento viene de un MenuItem
    private void cambiarVistaDesdeBoton(Button botonReferencia, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) botonReferencia.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
