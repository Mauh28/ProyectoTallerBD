package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class CoachCategoriaController {

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa a la lista de eventos
        cambiarVista(event, "coach_evento.fxml");
    }

    @FXML
    public void handleSeleccionCategoria(ActionEvent event) {
        // Obtenemos qué botón se presionó para saber la categoría (Primaria, etc.)
        Button btn = (Button) event.getSource();
        String categoriaSeleccionada = btn.getText();

        System.out.println("Categoría seleccionada: " + categoriaSeleccionada);
        System.out.println("Pasando a registro de datos del equipo...");

        // SIGUIENTE PASO DEL DIAGRAMA: Registrar Datos del Equipo
        cambiarVista(event, "coach_registroEquipo.fxml");
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