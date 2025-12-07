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
                boolean esCoach = rs.getBoolean("coach");
                boolean esJuez = rs.getBoolean("juez");

                // DEBUG: Ver en consola qué detectó
                System.out.println("Usuario: " + nombre + " | Coach: " + esCoach + " | Juez: " + esJuez);

                UserSession.getInstance().cleanUserSession();
                UserSession.getInstance().setUserId(id);
                UserSession.getInstance().setUsername(usuario);
                UserSession.getInstance().setNombreCompleto(nombre);
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
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

            // Obtener el Stage (ventana) actual
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Crear nueva escena SIN estilos extra
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}