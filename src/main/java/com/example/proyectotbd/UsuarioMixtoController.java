package com.example.proyectotbd;
import javafx.scene.control.Alert; // <--- ESTA ES LA QUE TE FALTA
import javafx.scene.control.ButtonType;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class UsuarioMixtoController {

    @FXML
    public void handleEntrarComoCoach(ActionEvent event) {
        System.out.println("Accediendo como COACH...");
        cambiarVista(event, "coach_menu.fxml");
    }

    @FXML
    public void handleEntrarComoJuez(ActionEvent event) {
        System.out.println("Accediendo como JUEZ...");
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

            // Crear nueva escena SIN estilos extra
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}