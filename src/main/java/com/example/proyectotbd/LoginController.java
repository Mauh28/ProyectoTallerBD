package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    // Eliminamos @FXML private ComboBox<String> cbRol; ya no existe
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String pass = txtPassword.getText();

        if (usuario.isEmpty() || pass.isEmpty()) {
            lblError.setText("Por favor ingresa usuario y contraseña.");
            lblError.setVisible(true);
            return;
        }

        // --- CONEXIÓN A BASE DE DATOS (TABLA USUARIO) ---
        String sql = "SELECT * FROM usuario WHERE username = ? AND password = ? AND activo = TRUE";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            pstmt.setString(2, pass);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Usuario encontrado, verificamos sus roles en la BD
                boolean esCoach = rs.getBoolean("coach");
                boolean esJuez = rs.getBoolean("juez");

                // LOGICA DE DIRECCIONAMIENTO AUTOMÁTICO
                if (esCoach && esJuez) {
                    System.out.println("Usuario Híbrido: Coach y Juez");
                    // cambiarVista(event, "dashboard_mixto.fxml"); // Aún no creada
                    lblError.setText("Login exitoso (Vista Mixta pendiente).");
                    lblError.setStyle("-fx-text-fill: green;");
                    lblError.setVisible(true);

                } else if (esJuez) {
                    System.out.println("Login correcto: Juez");
                    cambiarVista(event, "juez_menu.fxml");

                } else if (esCoach) {
                    System.out.println("Login correcto: Coach");
                    // cambiarVista(event, "coach_menu.fxml"); // Aún no creada
                    lblError.setText("Login exitoso (Vista Coach pendiente).");
                    lblError.setStyle("-fx-text-fill: green;");
                    lblError.setVisible(true);

                } else {
                    // El usuario existe pero ambos son false (raro, pero posible)
                    lblError.setText("Tu usuario no tiene roles asignados.");
                    lblError.setVisible(true);
                }

            } else {
                lblError.setText("Usuario o contraseña incorrectos.");
                lblError.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
                lblError.setVisible(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblError.setText("Error de conexión a la BD.");
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
        }
    }
}