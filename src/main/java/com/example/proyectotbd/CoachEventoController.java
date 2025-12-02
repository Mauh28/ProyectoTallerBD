package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class CoachEventoController {

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresar al Menú del Coach
        cambiarVista(event, "coach_menu.fxml");
    }

    @FXML
    public void handleInscribir(ActionEvent event) {
        System.out.println("Evento seleccionado. Pasando a Selección de Categoría...");
        // SIGUIENTE PASO DEL DIAGRAMA:
        cambiarVista(event, "coach_categoria.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        cambiarVista(event, "login.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar: " + fxml);
        }
    }
}