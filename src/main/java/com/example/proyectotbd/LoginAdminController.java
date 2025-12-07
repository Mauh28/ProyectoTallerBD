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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginAdminController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;


    @FXML
    public void handleAdminLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor, ingrese usuario y clave de acceso.");
            lblError.setVisible(true);
            return;
        }

        String sql = "{call SP_AutenticarAdmin(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int adminId = rs.getInt("admin_id");

                    // NOTA: Guardar datos mínimos en una sesión (si es necesario)
                    // UserSession.getInstance().setUserId(adminId);

                    System.out.println("Login Admin Correcto. ID: " + adminId);
                    cambiarVista(event, "organizador_menu.fxml");
                }
            }
        } catch (SQLException e) {
            // Capturamos el mensaje de error de 'SIGNAL SQLSTATE'
            e.printStackTrace();
            lblError.setText(e.getMessage());
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