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

public class JuezCategoriaController {

    @FXML private Button btnPrimaria;
    @FXML private Button btnSecundaria;
    @FXML private Button btnPreparatoria;

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresar al paso anterior: Lista de Eventos
        cambiarVista(event, "juez_evento.fxml");
    }

    @FXML
    public void handleSeleccionCategoria(ActionEvent event) {
        // Identificar qué botón se presionó (útil para filtrar la BD después)
        Button btnPresionado = (Button) event.getSource();
        String categoria = btnPresionado.getText();
        System.out.println("Categoría seleccionada: " + categoria);

        // Avanzar al siguiente paso: Lista de Equipos
        cambiarVista(event, "juez_equipo.fxml");
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

    public class SeleccionCategoriaController {

        @FXML private Button btnPrimaria;
        @FXML private Button btnSecundaria;
        @FXML private Button btnPreparatoria;
        @FXML private Button btnUniversidad; // <-- Agrega esta línea

        // ... el resto del código sigue igual ...
    }
}
