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
import java.time.LocalTime;
import java.util.regex.Pattern;

public class OrganizadorCrearEventoController {

    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;

    @FXML private Spinner<Integer> spnHoraInicio;
    @FXML private Spinner<Integer> spnMinutoInicio;
    @FXML private Spinner<Integer> spnHoraFin;
    @FXML private Spinner<Integer> spnMinutoFin;

    @FXML private Label lblMensaje;

    private OrganizadorDAO dao = new OrganizadorDAO();

    // --- 1. PATRONES DE VALIDACIÓN ROBUSTA ---
    // Regex mejorado: Permite letras, números, espacios y signos comunes en direcciones (., #, -)
    private static final Pattern PATRON_CARACTERES_VALIDOS = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ.,#\\-\\s]*$");

    // Regex para detectar repeticiones excesivas (ej: "aaa")
    private static final Pattern PATRON_REPETICION = Pattern.compile("(.)\\1{2,}");

    @FXML
    public void initialize() {
        // Configuración de Spinners (Horas)
        spnHoraInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spnMinutoInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spnHoraFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        spnMinutoFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // --- CAMBIO: Deshabilitar escritura manual en Spinners (Igual que en Editar) ---
        spnHoraInicio.setEditable(false);
        spnMinutoInicio.setEditable(false);
        spnHoraFin.setEditable(false);
        spnMinutoFin.setEditable(false);
        // -------------------------------------------------------------------------------

        // Bloquear escritura manual en el DatePicker
        dpFecha.setEditable(false);

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

        // --- VALIDACIONES DE TEXTO EN TIEMPO REAL ---
        configurarValidacionTexto(txtNombreEvento);
        configurarValidacionTexto(txtLugar);
    }

    // =================================================================
    // 2. MÉTODOS DE VALIDACIÓN LÓGICA (COHERENCIA)
    // =================================================================

    // --- NUEVA FUNCIÓN: LÓGICA DE SENTIDO COMÚN ---
    private boolean esTextoLogico(String texto) {
        String limpio = texto.trim();

        // Regla A: Longitud mínima de 3 caracteres reales
        if (limpio.length() < 3) return false;

        // Regla B: Debe contener al menos una letra (no solo números o símbolos "123 #")
        boolean tieneLetra = limpio.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
        if (!tieneLetra) return false;

        // Regla C: No tener 3 caracteres idénticos seguidos (Anti-spam "Hooolaaa")
        if (PATRON_REPETICION.matcher(limpio).find()) return false;

        return true;
    }

    private void configurarValidacionTexto(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {

            // 1. Limitar longitud a 50 caracteres
            if (newValue.length() > 50) {
                field.setText(oldValue);
                return;
            }

            // 2. Validar caracteres permitidos
            if (!PATRON_CARACTERES_VALIDOS.matcher(newValue).matches()) {
                field.setText(oldValue);
                return;
            }

            // 3. Feedback Visual: ¿Tiene sentido lo que escribió?
            if (!newValue.isEmpty() && !esTextoLogico(newValue)) {
                // Borde ROJO si es inválido (muy corto, solo números, repetitivo)
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
            mostrarMensaje("", false);
            dpFecha.setStyle("-fx-border-color: #27ae60; -fx-border-radius: 5;");
            return true;
        } else {
            mostrarMensaje("Error: La fecha debe ser al menos el día siguiente a hoy.", true);
            dpFecha.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            return false;
        }
    }

    // =================================================================
    // MÉTODOS DE ACCIÓN (GUARDAR)
    // =================================================================

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

        // 3. NUEVA VALIDACIÓN: Lógica del Nombre (Coherencia)
        if (!esTextoLogico(rawNombre)) {
            mostrarMensaje("El nombre del evento no es válido (muy corto, repetitivo o sin letras).", true);
            txtNombreEvento.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 4. NUEVA VALIDACIÓN: Lógica del Lugar (Coherencia)
        if (!esTextoLogico(rawLugar)) {
            mostrarMensaje("El lugar no es válido (muy corto, repetitivo o sin letras).", true);
            txtLugar.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 5. Validación de Horario (Lógica: Fin > Inicio y Duración >= 1h)
        if (!validarHorario(horaInicioInt, minutoInicioInt, horaFinInt, minutoFinInt)) {
            return;
        }

        // 6. Validar Fecha
        if (!validarFechaSeleccionada(fechaLocal)) {
            return;
        }

        // 7. NORMALIZACIÓN
        String nombre = capitalizarTexto(rawNombre);
        String lugar = capitalizarTexto(rawLugar);

        // 8. Preparar datos SQL
        Date sqlFecha = java.sql.Date.valueOf(fechaLocal);
        String horaInicioStr = String.format("%02d:%02d:00", horaInicioInt, minutoInicioInt);
        String horaFinStr = String.format("%02d:%02d:00", horaFinInt, minutoFinInt);
        Time sqlHoraInicio = Time.valueOf(horaInicioStr);
        Time sqlHoraFin = Time.valueOf(horaFinStr);

        // 9. Guardar en BD
        try {
            dao.crearEvento(nombre, lugar, sqlFecha, sqlHoraInicio, sqlHoraFin);

            System.out.println("Evento creado: " + nombre);
            mostrarNotificacionExito("¡Evento '" + nombre + "' creado exitosamente!");
            cambiarVista(event, "organizador_verEventos.fxml");

        } catch (SQLException e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private boolean validarHorario(Integer hInicio, Integer mInicio, Integer hFin, Integer mFin) {
        if (hInicio == null || mInicio == null || hFin == null || mFin == null) return false;

        LocalTime inicio = LocalTime.of(hInicio, mInicio);
        LocalTime fin = LocalTime.of(hFin, mFin);

        // 1. Validar que el fin sea después del inicio
        if (!fin.isAfter(inicio)) {
            mostrarMensaje("Error de Horario: La hora de fin debe ser posterior a la de inicio.", true);
            estilarErrorHorario(true);
            return false;
        }

        // 2. Validar duración mínima de 1 hora (60 minutos)
        long duracionMinutos = java.time.Duration.between(inicio, fin).toMinutes();
        if (duracionMinutos < 60) {
            mostrarMensaje("Error de Duración: El evento debe durar al menos 1 hora.", true);
            estilarErrorHorario(true);
            return false;
        }

        estilarErrorHorario(false);
        return true;
    }

    private void estilarErrorHorario(boolean error) {
        String estilo = error ? "-fx-border-color: #e74c3c; -fx-border-width: 2px;" : "";
        spnHoraInicio.setStyle(estilo);
        spnHoraFin.setStyle(estilo);
    }

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
        } catch (Exception e) {}
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
        if (!esError) {
            txtNombreEvento.setStyle("");
            txtLugar.setStyle("");
            estilarErrorHorario(false);
        }
    }

    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }
}