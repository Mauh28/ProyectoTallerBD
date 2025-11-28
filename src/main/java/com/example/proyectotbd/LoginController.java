package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    // Vinculamos los elementos del FXML
    @FXML private ComboBox<String> cbRol;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        // 1. Ver qué seleccionó el usuario
        String rolSeleccionado = cbRol.getValue();

        // Validación básica
        if (rolSeleccionado == null) {
            lblError.setText("Selecciona un perfil (Elige 'Juez').");
            lblError.setVisible(true);
            return;
        }

        // 2. Lógica simplificada solo para JUEZ
        if (rolSeleccionado.equals("Juez")) {
            System.out.println("Cargando menú de Juez...");
            cambiarVista(event, "juez_menu.fxml"); // ⚠️ Asegúrate que este archivo exista
        } else {
            // Si elige Organizador o Coach
            lblError.setText("Por ahora, solo la vista de JUEZ está conectada.");
            lblError.setVisible(true);
        }
    }

    // Método para cambiar la pantalla
    private void cambiarVista(ActionEvent event, String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Obtener la ventana actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Mostrar la nueva escena
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText("Error: No se encontró el archivo " + fxmlFile);
            lblError.setVisible(true);
        }
    }

    // Método vacío para el registro (para que no de error si pulsas el link)
    @FXML
    public void handleGoToRegister(ActionEvent event) {
        System.out.println("Ir al registro (pendiente)");
    }
}