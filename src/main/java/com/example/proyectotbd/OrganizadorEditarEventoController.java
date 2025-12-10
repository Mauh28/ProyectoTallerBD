package com.example.proyectotbd;

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
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class OrganizadorEditarEventoController {

    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;

    @FXML private Spinner<Integer> spnHoraInicio;
    @FXML private Spinner<Integer> spnMinutoInicio;
    @FXML private Spinner<Integer> spnHoraFin;
    @FXML private Spinner<Integer> spnMinutoFin;

    @FXML private Label lblMensaje;
    @FXML private Label lblEventoId;

    private OrganizadorDAO dao = new OrganizadorDAO();
    private EventoItem eventoActual;

    @FXML
    public void initialize() {
        // Inicializar Spinners
        spnHoraInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spnMinutoInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spnHoraFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        spnMinutoFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // --- CAMBIO: Deshabilitar escritura manual en Spinners ---
        spnHoraInicio.setEditable(false);
        spnMinutoInicio.setEditable(false);
        spnHoraFin.setEditable(false);
        spnMinutoFin.setEditable(false);
        // ---------------------------------------------------------

        // --- CAMBIO: Deshabilitar escritura manual en DatePicker ---
        dpFecha.setEditable(false);
        // -----------------------------------------------------------

        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        // Listener para la fecha
        dpFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarFechaSeleccionada(newValue);
        });

        // --- CAMBIO: Validaciones de Texto en Tiempo Real ---
        configurarValidacionTexto(txtNombreEvento);
        configurarValidacionTexto(txtLugar);
    }

    public void setEventoItem(EventoItem evento) {
        this.eventoActual = evento;
        if (evento != null) {
            lblEventoId.setText("ID: " + evento.getId() + " - " + evento.getNombre());
            cargarDatosEnFormulario(evento);
        }
    }

    private void cargarDatosEnFormulario(EventoItem evento) {
        txtNombreEvento.setText(evento.getNombre());
        txtLugar.setText(evento.getLugar());

        try {
            dpFecha.setValue(LocalDate.parse(evento.getFecha()));
        } catch (DateTimeParseException e) {
            // Silencio en consola
        }

        try {
            LocalTime inicio = LocalTime.parse(evento.getHoraInicio());
            LocalTime fin = LocalTime.parse(evento.getHoraFin());

            spnHoraInicio.getValueFactory().setValue(inicio.getHour());
            spnMinutoInicio.getValueFactory().setValue(inicio.getMinute());
            spnHoraFin.getValueFactory().setValue(fin.getHour());
            spnMinutoFin.getValueFactory().setValue(fin.getMinute());
        } catch (DateTimeParseException e) {
            // Silencio en consola
        }
    }

    // =================================================================
    // MÉTODOS DE VALIDACIÓN
    // =================================================================

    private void configurarValidacionTexto(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                field.setText(oldValue);
                return;
            }
            boolean tieneLetra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
            if (!newValue.isEmpty() && !tieneLetra) {
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            } else {
                field.setStyle("");
            }
        });
    }

    private boolean validarFechaSeleccionada(LocalDate fecha) {
        if (fecha == null) { dpFecha.setStyle(""); return false; }
        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("", false);
            dpFecha.setStyle("-fx-border-color: #27ae60; -fx-border-radius: 5;");
            return true;
        } else {
            mostrarMensaje("Error: La fecha debe ser futura.", true);
            dpFecha.setStyle("-fx-border-color: #e74c3c; -fx-border-radius: 5;");
            return false;
        }
    }

    // --- CAMBIO: Validación Lógica de Horario ---
    private boolean validarHorario(Integer hInicio, Integer mInicio, Integer hFin, Integer mFin) {
        if (hInicio == null || mInicio == null || hFin == null || mFin == null) return false;

        LocalTime inicio = LocalTime.of(hInicio, mInicio);
        LocalTime fin = LocalTime.of(hFin, mFin);

        if (!fin.isAfter(inicio)) {
            mostrarMensaje("Error de Horario: La hora de fin debe ser posterior a la de inicio.", true);
            estilarErrorHorario(true);
            return false;
        }

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

    // =================================================================
    // MÉTODOS DE ACCIÓN
    // =================================================================

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_verEventos.fxml");
    }

    @FXML
    public void handleGuardarEdicion(ActionEvent event) {
        if (eventoActual == null) {
            mostrarMensaje("Error interno: No se cargó el evento.", true);
            return;
        }

        String rawNombre = txtNombreEvento.getText();
        String rawLugar = txtLugar.getText();
        LocalDate fechaLocal = dpFecha.getValue();
        Integer hInicio = spnHoraInicio.getValue();
        Integer mInicio = spnMinutoInicio.getValue();
        Integer hFin = spnHoraFin.getValue();
        Integer mFin = spnMinutoFin.getValue();

        // 1. Validaciones Básicas
        if (rawNombre.trim().isEmpty() || rawLugar.trim().isEmpty() || fechaLocal == null) {
            mostrarMensaje("Error: Todos los campos son obligatorios.", true);
            return;
        }

        // 2. Validaciones de Contenido
        if (!rawNombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*")) {
            mostrarMensaje("El nombre debe contener texto descriptivo.", true);
            txtNombreEvento.setStyle("-fx-border-color: red;");
            return;
        }
        if (!rawLugar.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*")) {
            mostrarMensaje("El lugar debe ser válido.", true);
            txtLugar.setStyle("-fx-border-color: red;");
            return;
        }

        // 3. Validaciones Lógicas
        if (!validarFechaSeleccionada(fechaLocal)) return;
        if (!validarHorario(hInicio, mInicio, hFin, mFin)) return;

        // 4. Normalización
        String nombre = capitalizarTexto(rawNombre);
        String lugar = capitalizarTexto(rawLugar);

        // 5. Preparar SQL
        Date sqlFecha = java.sql.Date.valueOf(fechaLocal);
        Time sqlHoraInicio = Time.valueOf(String.format("%02d:%02d:00", hInicio, mInicio));
        Time sqlHoraFin = Time.valueOf(String.format("%02d:%02d:00", hFin, mFin));

        // 6. Guardar
        try {
            dao.editarEvento(eventoActual.getId(), nombre, lugar, sqlFecha, sqlHoraInicio, sqlHoraFin);
            mostrarNotificacionExito("¡Evento '" + nombre + "' actualizado!");
            cambiarVista(event, "organizador_verEventos.fxml");
        } catch (SQLException e) {
            // Sin printStackTrace
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) {
            if (!p.isEmpty()) {
                sb.append(Character.toUpperCase(p.charAt(0)));
                if (p.length() > 1) sb.append(p.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            Label label = new Label("✅ " + mensaje);
            label.setStyle("-fx-background-color: #27ae60;-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 16px;-fx-padding: 20px;-fx-background-radius: 10px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
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
            spnHoraInicio.setStyle("");
            spnHoraFin.setStyle("");
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
            alert.setTitle("Error de Navegación");
            alert.setContentText("No se pudo cargar la vista: " + fxml);
            alert.show();
        }
    }
}