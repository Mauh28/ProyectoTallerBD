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
import java.time.format.DateTimeParseException; // Para manejar errores de formato de hora

public class OrganizadorEditarEventoController {

    // Campos FXML de la vista
    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;
    @FXML private Spinner<Integer> spnHoraInicio;
    @FXML private Spinner<Integer> spnMinutoInicio;
    @FXML private Spinner<Integer> spnHoraFin;
    @FXML private Spinner<Integer> spnMinutoFin;
    @FXML private Label lblMensaje;
    @FXML private Label lblEventoId; // Para mostrar el ID y confirmación

    private OrganizadorDAO dao = new OrganizadorDAO();
    private EventoItem eventoActual; // Objeto que guarda los datos originales del evento

    @FXML
    public void initialize() {
        // Inicializar Spinners con rangos válidos (0-23 horas, 0-59 minutos)
        spnHoraInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spnMinutoInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spnHoraFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        spnMinutoFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // --- CAMBIO: Bloquear escritura manual en el DatePicker ---
        dpFecha.setEditable(false); // El usuario solo podrá usar el calendario emergente
        // ----------------------------------------------------------

        // Establecer el día mínimo como mañana (prevención frontend)
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // No permitir seleccionar hoy o días pasados
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });

        // Listener para la validación de la fecha en tiempo real (mismo día o pasado)
        dpFecha.valueProperty().addListener((observable, oldValue, newValue) -> {
            validarFechaSeleccionada(newValue);
        });
    }

    /**
     * Método llamado por el controlador anterior (p. ej., OrganizadorVerEventosController)
     * para pasar los datos del evento a editar.
     */
    public void setEventoItem(EventoItem evento) {
        this.eventoActual = evento;

        if (evento != null) {
            lblEventoId.setText("ID: " + evento.getId() + " - " + evento.getNombre());
            cargarDatosEnFormulario(evento);
        }
    }

    /**
     * Llena los campos de la vista con los datos del evento actual.
     */
    private void cargarDatosEnFormulario(EventoItem evento) {
        txtNombreEvento.setText(evento.getNombre());
        txtLugar.setText(evento.getLugar());

        // FECHA: Convertir String (yyyy-MM-dd) a LocalDate
        try {
            dpFecha.setValue(LocalDate.parse(evento.getFecha()));
        } catch (DateTimeParseException e) {
            System.err.println("Error al parsear la fecha: " + evento.getFecha());
        }

        // HORAS: Convertir String (HH:MM:SS) a LocalTime y luego actualizar Spinners
        try {
            LocalTime inicio = LocalTime.parse(evento.getHoraInicio());
            LocalTime fin = LocalTime.parse(evento.getHoraFin());

            spnHoraInicio.getValueFactory().setValue(inicio.getHour());
            spnMinutoInicio.getValueFactory().setValue(inicio.getMinute());
            spnHoraFin.getValueFactory().setValue(fin.getHour());
            spnMinutoFin.getValueFactory().setValue(fin.getMinute());

        } catch (DateTimeParseException e) {
            System.err.println("Error al parsear la hora: " + e.getMessage());
        }
    }

    // =================================================================
    // MÉTODOS DE VALIDACIÓN FRONTEND (Reutilizados de CrearEvento)
    // =================================================================

    private boolean validarFechaSeleccionada(LocalDate fecha) {
        if (fecha == null) {
            dpFecha.setStyle("-fx-font-size: 14px; -fx-background-color: #f4f6f8; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
            return false;
        }

        // Regla: La fecha debe ser estrictamente posterior al día actual
        if (fecha.isAfter(LocalDate.now())) {
            mostrarMensaje("", false);
            dpFecha.setStyle("-fx-font-size: 14px; -fx-background-color: #f4f6f8; -fx-border-color: #27ae60; -fx-border-radius: 5;");
            return true;
        } else {
            mostrarMensaje("Error: La fecha debe ser posterior al día de hoy. No se permite editar eventos activos o expirados.", true);
            dpFecha.setStyle("-fx-font-size: 14px; -fx-background-color: #f4f6f8; -fx-border-color: #e74c3c; -fx-border-radius: 5;");
            return false;
        }
    }


    // =================================================================
    // MÉTODOS DE ACCIÓN
    // =================================================================

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Asumo que se regresa a la vista de lista de eventos
        cambiarVista(event, "organizador_verEventos.fxml");
    }

    @FXML
    public void handleGuardarEdicion(ActionEvent event) {
        if (eventoActual == null) {
            mostrarMensaje("Error interno: No se ha cargado el evento a editar.", true);
            return;
        }

        // 1. Obtener datos
        String nombre = txtNombreEvento.getText();
        String lugar = txtLugar.getText();
        LocalDate fechaLocal = dpFecha.getValue();

        Integer horaInicioInt = spnHoraInicio.getValue();
        Integer minutoInicioInt = spnMinutoInicio.getValue();
        Integer horaFinInt = spnHoraFin.getValue();
        Integer minutoFinInt = spnMinutoFin.getValue();

        // 2. Validación Básica Frontend
        if (nombre.isEmpty() || lugar.isEmpty() || fechaLocal == null ||
                horaInicioInt == null || minutoInicioInt == null ||
                horaFinInt == null || minutoFinInt == null) {

            mostrarMensaje("Error: Todos los campos son obligatorios.", true);
            return;
        }

        // 3. Revalidación de la fecha
        if (!validarFechaSeleccionada(fechaLocal)) {
            return;
        }

        // 4. Conversión a SQL Types
        Date sqlFecha = java.sql.Date.valueOf(fechaLocal);

        String horaInicioStr = String.format("%02d:%02d:00", horaInicioInt, minutoInicioInt);
        String horaFinStr = String.format("%02d:%02d:00", horaFinInt, minutoFinInt);

        Time sqlHoraInicio = Time.valueOf(horaInicioStr);
        Time sqlHoraFin = Time.valueOf(horaFinStr);


        // 5. LLAMADA AL DAO (Editar)
        try {
            dao.editarEvento(
                    eventoActual.getId(), // Usamos el ID original
                    nombre,
                    lugar,
                    sqlFecha,
                    sqlHoraInicio,
                    sqlHoraFin
            );

            // --- ÉXITO ---
            System.out.println("Evento editado en BD: " + nombre);
            mostrarNotificacionExito("¡Evento '" + nombre + "' actualizado exitosamente!");
            cambiarVista(event, "organizador_verEventos.fxml");

        } catch (SQLException e) {
            // Atrapamos errores del SP (ej: nombre duplicado, duración, horario inválido)
            e.printStackTrace();
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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