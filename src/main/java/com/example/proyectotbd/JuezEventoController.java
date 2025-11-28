package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class JuezEventoController {

    @FXML
    public void handleSeleccionarEvento(ActionEvent event) {
        System.out.println("Evento seleccionado. Avanzando a Categorías...");
        // Avanzar al siguiente paso: Filtro de Categoría
        cambiarVista(event, "juez_categoria.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        System.out.println("Cerrando sesión...");
        // Regresar al Login (asegúrate de usar el nombre correcto de tu login)
        cambiarVista(event, "login.fxml");
    }

    // Método auxiliar para cargar vistas
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

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa al Menú Principal del Juez
        cambiarVista(event, "juez_menu.fxml");
    }
}
