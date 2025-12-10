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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    // Campos de usuario y mensaje de error
    @FXML private TextField txtUsuario;
    @FXML private Label lblError;

    // Campos para la funcionalidad de 'ojo'
    @FXML private PasswordField pfContrasena;
    @FXML private TextField txtContrasenaVisible;
    @FXML private Button btnVerContrasena;

    private boolean contrasenaVisible = false;

    @FXML
    public void initialize() {
        // Sincronización de Contenido
        pfContrasena.textProperty().addListener((obs, oldV, newV) -> {
            if (!contrasenaVisible) {
                txtContrasenaVisible.setText(newV);
            }
        });

        txtContrasenaVisible.textProperty().addListener((obs, oldV, newV) -> {
            if (contrasenaVisible) {
                pfContrasena.setText(newV);
            }
        });

        // Inicializar el botón con el ícono correcto (el ojo, para ver)
        btnVerContrasena.setText("👁️");
    }


    @FXML
    public void handleAlternarVisibilidad(ActionEvent event) {
        if (!contrasenaVisible) {
            // Actualmente Oculto (pfContrasena visible) -> Cambiar a Mostrar
            txtContrasenaVisible.setText(pfContrasena.getText());
            txtContrasenaVisible.setVisible(true);
            pfContrasena.setVisible(false);
            btnVerContrasena.setText("🔒"); // <-- Solo el carácter Unicode
        } else {
            // Actualmente Visible (txtContrasenaVisible visible) -> Cambiar a Ocultar
            pfContrasena.setText(txtContrasenaVisible.getText());
            pfContrasena.setVisible(true);
            txtContrasenaVisible.setVisible(false);
            btnVerContrasena.setText("👁️"); // <-- Solo el carácter Unicode
        }

        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            txtContrasenaVisible.requestFocus();
        } else {
            pfContrasena.requestFocus();
        }
    }


    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String pass = pfContrasena.getText();

        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Por favor ingresa usuario y contraseña.", true);
            return;
        }

        String sql = "{call SP_AutenticarUsuario(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("usuario_id");
                String nombre = rs.getString("nombre");
                String institucion = rs.getString("institucion");
                boolean esCoach = rs.getBoolean("coach");
                boolean esJuez = rs.getBoolean("juez");

                System.out.println("Usuario: " + nombre + " | Coach: " + esCoach + " | Juez: " + esJuez);

                // --- GESTIÓN DE SESIÓN ---
                UserSession.getInstance().cleanUserSession();
                UserSession.getInstance().setUserId(id);
                UserSession.getInstance().setUsername(usuario);
                UserSession.getInstance().setNombreCompleto(nombre);
                UserSession.getInstance().setInstitucionUsuario(institucion);
                UserSession.getInstance().setCoach(esCoach);
                UserSession.getInstance().setJuez(esJuez);

                // LÓGICA DE REDIRECCIÓN
                if (esCoach && esJuez) {
                    cambiarVista(event, "usuario_mixto.fxml");
                } else if (esJuez) {
                    cambiarVista(event, "juez_menu.fxml");
                } else if (esCoach) {
                    cambiarVista(event, "coach_menu.fxml");
                } else {
                    mostrarMensaje("Tu usuario no tiene roles asignados.", true);
                }
            } else {
                mostrarMensaje("Credenciales inválidas. Verifica tu usuario y contraseña.", true);
            }

        } catch (SQLException e) {
            // e.printStackTrace();
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    public void handleGoToAdminLogin(ActionEvent event) {
        cambiarVista(event, "login_admin.fxml");
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblError.setText(mensaje);
        lblError.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblError.setVisible(true);
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }
}