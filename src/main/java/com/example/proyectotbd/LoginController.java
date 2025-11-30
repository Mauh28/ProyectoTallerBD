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

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();

        // SIMULACIÓN DE LOGIN (Sin Base de Datos)
        // Esto es solo para que puedas probar la navegación entre las vistas

        if (usuario.equalsIgnoreCase("juez")) {
            System.out.println("Simulando entrada de Juez...");
            cambiarVista(event, "juez_menu.fxml");

        } else if (usuario.equalsIgnoreCase("coach")) {
            lblError.setText("Vista de Coach pendiente.");
            lblError.setStyle("-fx-text-fill: green;");
            lblError.setVisible(true);

        } else if (usuario.equalsIgnoreCase("ambos")) {
            lblError.setText("Vista Mixta pendiente.");
            lblError.setStyle("-fx-text-fill: green;");
            lblError.setVisible(true);

        } else {
            lblError.setText("Para probar, usa el usuario: 'juez' o 'coach'");
            lblError.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
            lblError.setVisible(true);
        }
    }

    @FXML
    public void handleGoToAdminLogin(ActionEvent event) {
        cambiarVista(event, "login_admin.fxml");
    }

    @FXML
    public void handleGoToRegister(ActionEvent event) {
        cambiarVista(event, "register_selection.fxml");
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