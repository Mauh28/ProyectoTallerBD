package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class CoachRegistroEquipoController {

    @FXML private TextField txtNombreEquipo;
    @FXML private TextField txtInstitucion;
    @FXML private Label lblMensaje;

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa a seleccionar categoría
        cambiarVista(event, "coach_categoria.fxml");
    }

    @FXML
    public void handleContinuar(ActionEvent event) {
        String nombre = txtNombreEquipo.getText();
        String institucion = txtInstitucion.getText();

        // 1. Validación Visual Básica
        if (nombre.isEmpty() || institucion.isEmpty()) {
            lblMensaje.setText("Por favor, completa todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
            lblMensaje.setVisible(true);
            return;
        }

        // 2. Simulación de Validación de BD (Nombre duplicado)
        // Aquí iría la llamada al procedimiento almacenado/insert.
        System.out.println("Validando nombre del equipo: " + nombre);
        System.out.println("Equipo válido. Pasando a registrar integrantes...");

        // 3. Avanzar al paso final: Registrar Integrantes
        cambiarVista(event, "coach_registroIntegrantes.fxml");
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