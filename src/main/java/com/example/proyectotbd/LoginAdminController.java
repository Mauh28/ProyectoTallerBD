package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    @FXML private Label lblError;

    // --- CAMPOS DE CONTRASEÑA CORREGIDOS ---
    @FXML private PasswordField pfContrasena;         // Campo oculto (reemplaza txtPassword)
    @FXML private TextField txtContrasenaVisible;     // Campo de texto visible
    @FXML private Button btnVerContrasena;           // Botón de alternar

    // Bandera para rastrear el estado
    private boolean contrasenaVisible = false;

    @FXML
    public void initialize() {
        // 1. Si el usuario escribe en el PasswordField (oculto), actualiza el campo visible.
        pfContrasena.textProperty().addListener((obs, oldV, newV) -> {
            if (!contrasenaVisible) {
                txtContrasenaVisible.setText(newV);
            }
        });

        // 2. Si el usuario escribe en el TextField visible, actualiza el PasswordField oculto.
        txtContrasenaVisible.textProperty().addListener((obs, oldV, newV) -> {
            if (contrasenaVisible) {
                pfContrasena.setText(newV);
            }
        });
    }

    // =================================================================
    // GESTIÓN DE LA VISIBILIDAD DE CONTRASEÑA
    // =================================================================

    @FXML
    public void handleAlternarVisibilidad(ActionEvent event) {
        if (!contrasenaVisible) {
            // Actualmente Oculto (pfContrasena visible) -> Cambiar a Mostrar
            txtContrasenaVisible.setText(pfContrasena.getText());
            txtContrasenaVisible.setVisible(true);
            pfContrasena.setVisible(false);
            btnVerContrasena.setText("🔒"); // Ícono de candado
        } else {
            // Actualmente Visible (txtContrasenaVisible visible) -> Cambiar a Ocultar
            pfContrasena.setText(txtContrasenaVisible.getText());
            pfContrasena.setVisible(true);
            txtContrasenaVisible.setVisible(false);
            btnVerContrasena.setText("👁️"); // Ícono de ojo
        }

        // Invertir la bandera de estado
        contrasenaVisible = !contrasenaVisible;

        // Asegurar el foco
        if (contrasenaVisible) {
            txtContrasenaVisible.requestFocus();
        } else {
            pfContrasena.requestFocus();
        }
    }


    // =================================================================
    // LÓGICA DE INICIO DE SESIÓN
    // =================================================================

    @FXML
    public void handleAdminLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim(); // Quita espacios al inicio y final
        String password = pfContrasena.getText();

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
                    System.out.println("Login Admin Correcto. ID: " + adminId);
                    cambiarVista(event, "organizador_menu.fxml");
                } else {
                    lblError.setText("Credenciales de administrador inválidas.");
                    lblError.setVisible(true);
                }
            }
        } catch (SQLException e) {
            lblError.setText(e.getMessage());
            lblError.setVisible(true);
        }
    }

    @FXML
    public void handleVolver(ActionEvent event) {
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
            // e.printStackTrace();
            System.out.println("Error cargando vista: " + fxml);
        }
    }
}