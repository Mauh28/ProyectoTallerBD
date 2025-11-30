package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class OrganizadorAsignarCategoriaController {

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_selecEvento.fxml");
    }

    @FXML
    public void handleCategoria(ActionEvent event) {
        System.out.println("Categoría seleccionada. (Validación de equipos pendiente).");
        // SIGUIENTE PASO: Asignar Juez
        cambiarVista(event, "organizador_asignarJuez.fxml");
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