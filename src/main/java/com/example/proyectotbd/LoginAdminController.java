package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginAdminController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleAdminLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();

        // SIMULACIÓN SIN BASE DE DATOS
        // Si el usuario escribe "admin", entra.
        if (usuario.equals("admin")) {
            System.out.println("Login Admin Correcto (Simulado)");
            cambiarVista(event, "organizador_menu.fxml");
        } else {
            lblError.setText("Credenciales incorrectas. Prueba con usuario: 'admin'");
            lblError.setVisible(true);
        }
    }

    @FXML
    public void handleVolver(ActionEvent event) {
        // Regresa al login general (login.fxml)
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
            System.out.println("Error cargando vista: " + fxml);
        }
    }
}