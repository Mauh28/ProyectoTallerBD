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
import java.util.regex.Pattern; // Importación necesaria

public class OrganizadorCrearEventoController {

    @FXML
    private TextField txtNombreEvento;
    @FXML
    private TextField txtLugar;
    @FXML
    private DatePicker dpFecha;

    // --- CAMPOS SPINNER ---
    @FXML
    private Spinner<Integer> spnHoraInicio;
    @FXML
    private Spinner<Integer> spnMinutoInicio;
    @FXML
    private Spinner<Integer> spnHoraFin;
    @FXML
    private Spinner<Integer> spnMinutoFin;

    @FXML
    private Label lblMensaje;

    // Regex mejorado: Permite letras, números, espacios y signos comunes en direcciones (., #, -)
    private static final Pattern PATRON_CARACTERES_VALIDOS = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ.,#\\-\\s]*$");

    // Regex para detectar repeticiones excesivas (ej: "aaa")
    private static final Pattern PATRON_REPETICION = Pattern.compile("(.)\\1{2,}");

    private OrganizadorDAO dao = new OrganizadorDAO();

    private static final Pattern PATRON_LETRAS_NUMEROS_ESPACIOS = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]*$");


    @FXML
    public void initialize() {
        // Configuración de Spinners (Horas)
        spnHoraInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spnMinutoInicio.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spnHoraFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 17));
        spnMinutoFin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

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
        // Validación en tiempo real con la nueva lógica
        configurarValidacionTexto(txtNombreEvento);
        configurarValidacionTexto(txtLugar);

        // --- VALIDACIONES DE TEXTO ---
        configurarValidacionTexto(txtNombreEvento);
        configurarValidacionTexto(txtLugar);
    }


    // --- MÉTODO DE VALIDACIÓN EN TIEMPO REAL MEJORADO ---
    private void configurarValidacionTexto(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {

            // 1. Limitar longitud
            if (newValue.length() > 50) {
                field.setText(oldValue);
                return;
            }

            // 2. Validar caracteres permitidos (Si escribe un símbolo raro, lo borra)
            if (!PATRON_CARACTERES_VALIDOS.matcher(newValue).matches()) {
                field.setText(oldValue);
                return;
            }

            // 3. Feedback Visual: ¿Tiene sentido lo que escribió?
            // Si está vacío o NO es lógico, borde rojo. Si es lógico, borde normal.
            if (!newValue.isEmpty() && !esTextoLogico(newValue)) {
                field.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;"); // Rojo
            } else {
                field.setStyle(""); // Limpio
            }
        });
    }

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

        // 3. NUEVA VALIDACIÓN: Lógica del Nombre
        if (!esTextoLogico(rawNombre)) {
            mostrarMensaje("El nombre del evento no parece válido (muy corto, repetitivo o solo números).", true);
            txtNombreEvento.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 4. NUEVA VALIDACIÓN: Lógica del Lugar
        if (!esTextoLogico(rawLugar)) {
            mostrarMensaje("El lugar no parece válido (muy corto, repetitivo o solo números).", true);
            txtLugar.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
            return;
        }

        // 5. Validación de Horario (Lógica de negocio: Fin > Inicio y Duración >= 1h)
        if (!validarHorario(horaInicioInt, minutoInicioInt, horaFinInt, minutoFinInt)) {
            return; // El metodo validarHorario ya muestra el mensaje y pinta los bordes
        }

        // 6. Validar Fecha (Futura)
        if (!validarFechaSeleccionada(fechaLocal)) {
            return; // El metodo validarFechaSeleccionada ya muestra el mensaje
        }

        // 7. NORMALIZACIÓN (Capitalizar textos para que se vean bien en reportes)
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
            // Manejo de errores de BD (ej: Nombre duplicado, cruce de horarios detectado por el SP)
            mostrarMensaje(e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show(); }
    }

    // Validar que la hora fin sea al menos 1 hora después del inicio
    private boolean validarHorario(Integer hInicio, Integer mInicio, Integer hFin, Integer mFin) {
        if (hInicio == null || mInicio == null || hFin == null || mFin == null) {
            return false;
        }

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

        // Si pasa, limpiamos estilos
        estilarErrorHorario(false);
        return true;
    }

    // Metodo auxiliar para pintar los spinners de rojo/verde
    private void estilarErrorHorario(boolean error) {
        String estiloError = "-fx-border-color: #e74c3c; -fx-border-width: 2px;";
        String estiloNormal = "";

        if (error) {
            spnHoraInicio.setStyle(estiloError);
            spnHoraFin.setStyle(estiloError);
        } else {
            spnHoraInicio.setStyle(estiloNormal);
            spnHoraFin.setStyle(estiloNormal);
        }
    }
}