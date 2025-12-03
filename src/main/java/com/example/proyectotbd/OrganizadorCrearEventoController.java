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
import java.time.LocalDate;

public class OrganizadorCrearEventoController {

    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblMensaje;

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarEvento(ActionEvent event) {
        // 1. Obtener datos de la interfaz
        String nombre = txtNombreEvento.getText();
        String lugar = txtLugar.getText();
        LocalDate fecha = dpFecha.getValue();

        // 2. Validación Básica (Campos vacíos)
        if (nombre.isEmpty() || lugar.isEmpty() || fecha == null) {
            mostrarMensaje("Error: Todos los campos son obligatorios.", true);
            return;
        }

        // 3. CONEXIÓN Y LLAMADA AL PROCEDIMIENTO ALMACENADO
        String sql = "{call SP_ValidarNombreEvento(?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Asignar parámetros
            stmt.setString(1, nombre);
            stmt.setString(2, lugar);
            stmt.setDate(3, java.sql.Date.valueOf(fecha)); // Conversión vital: LocalDate -> SQL Date

            // Ejecutar el procedimiento
            stmt.execute();

            // --- ÉXITO ---
            System.out.println("Evento creado en BD: " + nombre);

            // 1. Mostrar la notificación flotante
            mostrarNotificacionExito("¡Evento '" + nombre + "' creado exitosamente!");

            // 2. Regresar al menú principal
            cambiarVista(event, "organizador_menu.fxml");

        } catch (SQLException e) {
            // Aquí atrapamos los errores del SP (fecha pasada, nombre duplicado)
            e.printStackTrace();
            mostrarMensaje("Error BD: " + e.getMessage(), true);
        }
    }

    // --- MÉTODO PARA CREAR LA NOTIFICACIÓN FLOTANTE (TOAST) ---
    private void mostrarNotificacionExito(String mensaje) {
        try {
            // 1. Crear ventana transparente sin bordes
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            // 2. Crear etiqueta con estilo verde
            Label label = new Label("📅 " + mensaje);
            label.setStyle(
                    "-fx-background-color: #27ae60;" +      // Fondo Verde
                            "-fx-text-fill: white;" +               // Texto Blanco
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 20px;" +                  // Espaciado interno
                            "-fx-background-radius: 10px;" +        // Bordes redondeados
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);" // Sombra
            );

            // 3. Contenedor transparente
            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            // 4. Posicionar abajo a la derecha
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);

            // 5. Mostrar y cerrar automáticamente
            toastStage.show();
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para mensajes de error dentro del formulario
    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        if (esError) {
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
        } else {
            lblMensaje.setStyle("-fx-text-fill: #27ae60;"); // Verde
        }
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
            System.out.println("Error cargando vista: " + fxml);
        }
    }
}