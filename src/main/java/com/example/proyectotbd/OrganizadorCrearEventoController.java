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
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;

public class OrganizadorCrearEventoController {

    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;

    // --- CAMPOS SPINNER ---
    @FXML private Spinner<Integer> spnHoraInicio;
    @FXML private Spinner<Integer> spnMinutoInicio;
    @FXML private Spinner<Integer> spnHoraFin;
    @FXML private Spinner<Integer> spnMinutoFin;

    @FXML private Label lblMensaje;

    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        // Configuración de Spinners (Horas)
        spnHoraInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spnMinutoInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spnHoraFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        spnMinutoFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Validación de Fecha (No pasado, mínimo mañana)
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        dpFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarFechaSeleccionada(newValue);
        });

        // --- NUEVAS VALIDACIONES DE TEXTO ---
        configurarValidacionTexto(txtNombreEvento);
        configurarValidacionTexto(txtLugar);
    }

    // --- MÉTODO DE VALIDACIÓN EN TIEMPO REAL (GENÉRICO) ---
    private void configurarValidacionTexto(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            // 1. Limitar longitud a 50 caracteres (Restricción de BD)
            if (newValue.length() > 50) {
                field.setText(oldValue);
                return;
            }

            // 2. Validar contenido: Debe tener al menos una letra (no solo números/símbolos)
            boolean tieneLetra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");

            if (!newValue.isEmpty() && !tieneLetra) {
                // Borde ROJO si es inválido
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            } else {
                // Estilo limpio
                field.setStyle("");
            }
        });
    }

    private boolean validarFechaSeleccionada(LocalDate fecha) {
        if (fecha == null) {
            dpFecha.setStyle("");
            return false;
        }
        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("", false); // Limpiar mensaje
            dpFecha.setStyle("-fx-border-color: #27ae60; -fx-border-radius: 5;"); // Verde
            return true;
        } else {
            mostrarMensaje("Error: La fecha debe ser al menos el día siguiente a hoy.", true);
            dpFecha.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;"); // Rojo
            return false;
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarEvento(ActionEvent event) {
        // 1. Obtener datos CRUDOS
        String rawNombre = txtNombreEvento.getText();
        String rawLugar = txtLugar.getText();
        LocalDate fechaLocal = dpFecha.getValue();

        Integer horaInicioInt = spnHoraInicio.getValue();
        Integer minutoInicioInt = spnMinutoInicio.getValue();
        Integer horaFinInt = spnHoraFin.getValue();
        Integer minutoFinInt = spnMinutoFin.getValue();

        // 2. Validaciones Básicas (Vacíos)
        if (rawNombre.trim().isEmpty() || rawLugar.trim().isEmpty() || fechaLocal == null) {
            mostrarMensaje("Error: Todos los campos son obligatorios.", true);
            return;
        }

        // 3. Validaciones de Contenido (Nombre y Lugar)
        // Deben tener al menos una letra
        if (!rawNombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*")) {
            mostrarMensaje("El nombre del evento debe contener texto descriptivo (no solo números).", true);
            txtNombreEvento.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }
        if (!rawLugar.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*")) {
            mostrarMensaje("El lugar debe ser un nombre válido (no solo números).", true);
            txtLugar.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 4. Validar Fecha
        if (!validarFechaSeleccionada(fechaLocal)) {
            return;
        }

        // 5. NORMALIZACIÓN (Capitalizar textos para que se vean bien en reportes)
        String nombre = capitalizarTexto(rawNombre);
        String lugar = capitalizarTexto(rawLugar);

        // 6. Preparar datos SQL
        Date sqlFecha = java.sql.Date.valueOf(fechaLocal);
        String horaInicioStr = String.format("%02d:%02d:00", horaInicioInt, minutoInicioInt);
        String horaFinStr = String.format("%02d:%02d:00", horaFinInt, minutoFinInt);
        Time sqlHoraInicio = Time.valueOf(horaInicioStr);
        Time sqlHoraFin = Time.valueOf(horaFinStr);

        // 7. Guardar en BD
        try {
            dao.crearEvento(nombre, lugar, sqlFecha, sqlHoraInicio, sqlHoraFin);

            System.out.println("Evento creado: " + nombre);
            mostrarNotificacionExito("¡Evento '" + nombre + "' creado exitosamente!");
            cambiarVista(event, "organizador_verEventos.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    // Método para convertir "torneo nacional" -> "Torneo Nacional"
    private String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1).toLowerCase());
                }
                resultado.append(" ");
            }
        }
        return resultado.toString().trim();
    }

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            Label label = new Label("📅 " + mensaje);
            label.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);
            toastStage.show();
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
        // Limpiar bordes rojos si el mensaje es informativo o al reintentar
        if (!esError) {
            txtNombreEvento.setStyle("");
            txtLugar.setStyle("");
        }
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}