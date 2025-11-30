package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.io.IOException;

public class OrganizadorAsignarJuezController {

    @FXML private ComboBox<String> cbJueces;

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_asignarCategoria.fxml");
    }

    @FXML
    public void handleGuardar(ActionEvent event) {
        System.out.println("Juez asignado correctamente.");
        System.out.println("Regresando al menú principal...");

        // FINAL DEL FLUJO: Volver al Dashboard del Organizador
        cambiarVista(event, "organizador_menu.fxml");
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