package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class OrganizadorCrearUsuarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtInstitucion;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox checkCoach;
    @FXML private CheckBox checkJuez;
    @FXML private Label lblMensaje;

    @FXML
    public void initialize() {
        limitarLongitud(txtNombre, 50);
        limitarLongitud(txtInstitucion, 50);
        limitarLongitud(txtUsername, 50);
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarUsuario(ActionEvent event) {
        // 1. Obtener valores
        String nombre = txtNombre.getText();
        String institucion = txtInstitucion.getText();
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        boolean esCoach = checkCoach.isSelected();
        boolean esJuez = checkJuez.isSelected();

        // 2. Validación visual
        if (nombre.isEmpty() || institucion.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Error: Por favor llena todos los campos.", true);
            return;
        }

        if (!esCoach && !esJuez) {
            mostrarMensaje("Error: Debes seleccionar al menos un rol.", true);
            return;
        }

        // 3. CONEXIÓN Y GUARDADO
        String sql = "{call SP_registrarUsuario(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, nombre);
            stmt.setString(4, institucion);
            stmt.setBoolean(5, esCoach);
            stmt.setBoolean(6, esJuez);

            stmt.execute();

            // --- AQUÍ OCURRE LA MAGIA ---
            // 1. Mostramos la notificación flotante abajo a la derecha
            mostrarNotificacionExito("¡Usuario " + username + " registrado correctamente!");

            // 2. Redirigimos al menú inmediatamente (la notificación se queda flotando unos segundos)
            cambiarVista(event, "organizador_menu.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error BD: " + e.getMessage(), true);
        }
    }

    // --- MÉTODO PARA CREAR LA NOTIFICACIÓN FLOTANTE (TOAST) ---
    private void mostrarNotificacionExito(String mensaje) {
        try {
            // 1. Crear una ventana nueva transparente
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT); // Sin bordes ni botones de cerrar
            toastStage.setAlwaysOnTop(true); // Siempre visible encima

            // 2. Crear la etiqueta con estilo bonito (Verde)
            Label label = new Label("✅ " + mensaje);
            label.setStyle(
                    "-fx-background-color: #27ae60;" +      // Fondo Verde
                            "-fx-text-fill: white;" +               // Texto Blanco
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 20px;" +                  // Espaciado interno
                            "-fx-background-radius: 10px;" +        // Bordes redondeados
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);" // Sombra
            );

            // 3. Ponerla en un contenedor transparente
            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // 4. Calcular la posición (Esquina Inferior Derecha)
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            // Restamos el ancho aproximado del mensaje y un margen
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);

            // 5. Mostrar
            toastStage.show();

            // 6. Temporizador para que desaparezca sola a los 3 segundos
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();

        } catch (Exception e) {
            e.printStackTrace(); // Por si falla, que no rompa el flujo principal
        }
    }

    // Método auxiliar para mensajes de error dentro del formulario (no flotantes)
    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
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

    private void limitarLongitud(TextField tf, int maxLength) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > maxLength) {
                tf.setText(oldValue); // Rechaza el cambio si excede el límite
            }
        });
    }
}