package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class OrganizadorCrearUsuarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtInstitucion;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox checkCoach;
    @FXML private CheckBox checkJuez;
    @FXML private Label lblMensaje;

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa al Dashboard del Organizador si cancela
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarUsuario(ActionEvent event) {
        // 1. Validación visual básica
        if (txtNombre.getText().isEmpty() || txtUsername.getText().isEmpty() ||
                (!checkCoach.isSelected() && !checkJuez.isSelected())) {

            lblMensaje.setText("Error: Llena los datos y selecciona al menos un rol.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            lblMensaje.setVisible(true);
            return; // Se detiene aquí si hay error
        }

        // 2. Simulación de Guardado (Aquí irá tu Procedure después)
        System.out.println("--- NUEVO USUARIO GUARDADO ---");
        System.out.println("Usuario: " + txtUsername.getText());
        System.out.println("Roles: Coach=" + checkCoach.isSelected() + ", Juez=" + checkJuez.isSelected());

        // 3. CAMBIO DE VISTA: Regresar al menú automáticamente
        System.out.println("Redirigiendo al menú...");
        cambiarVista(event, "organizador_menu.fxml");
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
            System.out.println("Error al cargar la vista: " + fxml);
        }
    }
}