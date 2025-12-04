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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    @FXML
    public void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText();
        String pass = txtPassword.getText();

        // 1. Validación básica de campos vacíos
        if (usuario.isEmpty() || pass.isEmpty()) {
            mostrarMensaje("Por favor ingresa usuario y contraseña.", true);
            return;
        }

        // 2. LLAMADA AL PROCEDIMIENTO ALMACENADO
        // El SP valida password y estado activo internamente.
        String sql = "{call SP_AutenticarUsuario(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, pass);

            // Ejecutamos. Si las credenciales son malas, el SP lanza un error (SQLException)
            // Si son buenas, nos devuelve los datos del usuario.
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("usuario_id");
                String nombre = rs.getString("nombre");
                boolean esCoach = rs.getBoolean("coach");
                boolean esJuez = rs.getBoolean("juez");

                // B. GUARDAR EN SESIÓN (ACTUALIZADO)
                UserSession.getInstance().cleanUserSession();
                UserSession.getInstance().setUserId(id);
                UserSession.getInstance().setUsername(usuario);
                UserSession.getInstance().setNombreCompleto(nombre);

                // Guardamos los permisos
                UserSession.getInstance().setCoach(esCoach);
                UserSession.getInstance().setJuez(esJuez);

                System.out.println("Login Exitoso: " + nombre);

                // C. REDIRECCIONAR SEGÚN ROL
                if (esCoach && esJuez) {
                    // Usuario Híbrido
                    cambiarVista(event, "usuario_mixto.fxml");
                } else if (esJuez) {
                    cambiarVista(event, "juez_menu.fxml");
                } else if (esCoach) {
                    cambiarVista(event, "coach_menu.fxml");
                } else {
                    mostrarMensaje("Tu usuario no tiene roles asignados.", true);
                }
            }

        } catch (SQLException e) {
            // Aquí capturamos los mensajes que pusiste en el SP:
            // "Error de inicio de sesión: Nombre de usuario o contraseña incorrectos."
            // "Error de inicio de sesión: La cuenta está inactiva..."
            e.printStackTrace();
            mostrarMensaje(e.getMessage(), true);
        }
    }

    @FXML
    public void handleGoToAdminLogin(ActionEvent event) {
        cambiarVista(event, "login_admin.fxml");
    }

    // Este método ya no se usa en la vista actual, pero lo dejamos por si acaso
    @FXML
    public void handleGoToRegister(ActionEvent event) {
        cambiarVista(event, "register_selection.fxml");
    }

    // Método auxiliar para mensajes de error
    private void mostrarMensaje(String mensaje, boolean esError) {
        lblError.setText(mensaje);
        if (esError) {
            lblError.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
        } else {
            lblError.setStyle("-fx-text-fill: #27ae60;"); // Verde
        }
        lblError.setVisible(true);
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