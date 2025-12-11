package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class JuezEventoController {

    @FXML private VBox vboxListaEventos;

    private JuezDAO juezDao = new JuezDAO();

    @FXML
    public void initialize() {
        cargarEventos();
    }

    private void cargarEventos() {
        vboxListaEventos.getChildren().clear();

        int juezId = UserSession.getInstance().getUserId();

        try {
            ObservableList<EventoItem> eventos = juezDao.obtenerEventosDelJuez(juezId);

            if (eventos.isEmpty()) {
                Label vacio = new Label("No tienes eventos asignados.");
                vacio.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-padding: 20;");
                vboxListaEventos.getChildren().add(vacio);
                return;
            }

            for (EventoItem evento : eventos) {
                vboxListaEventos.getChildren().add(crearTarjetaEvento(evento));
            }

        } catch (SQLException e) {
            // e.printStackTrace();
            Label error = new Label("Error de conexión al cargar eventos: Asegúrate que la BD está activa.");
            error.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-padding: 20;");
            vboxListaEventos.getChildren().add(error);
        }
    }

    private HBox crearTarjetaEvento(EventoItem evento) {
        HBox tarjeta = new HBox();
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setSpacing(10);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");
        tarjeta.setEffect(new DropShadow(5, Color.color(0,0,0,0.1)));

        // 1. Nombre del Evento
        Label lblNombre = new Label(evento.getNombre());
        lblNombre.setPrefWidth(350.0);
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");

        // 2. Lugar
        Label lblLugar = new Label(evento.getLugar());
        lblLugar.setPrefWidth(200.0);
        lblLugar.setWrapText(true);
        lblLugar.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        // 3. Fecha
        Label lblFecha = new Label(evento.getFecha());
        lblFecha.setPrefWidth(120.0);
        lblFecha.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        // 4. HORA DE INICIO
        String horaInicioFormatted = evento.getHoraInicio().substring(0, 5);
        Label lblHoraInicio = new Label(horaInicioFormatted);
        lblHoraInicio.setPrefWidth(80.0);
        lblHoraInicio.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

        // 5. HORA DE FIN
        String horaFinFormatted = evento.getHoraFin().substring(0, 5);
        Label lblHoraFin = new Label(horaFinFormatted);
        lblHoraFin.setPrefWidth(80.0);
        lblHoraFin.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 6. Botón Seleccionar y Lógica de Deshabilitación
        Button btnSeleccionar = new Button();

        try {
            // Conversión de String a LocalDateTime
            LocalDate fechaEvento = LocalDate.parse(evento.getFecha());
            LocalTime horaInicio = LocalTime.parse(evento.getHoraInicio().substring(0, 5));
            LocalTime horaFin = LocalTime.parse(evento.getHoraFin().substring(0, 5)); // <--- Hora de Fin

            LocalDateTime horaInicioEvento = LocalDateTime.of(fechaEvento, horaInicio);
            LocalDateTime horaFinEvento = LocalDateTime.of(fechaEvento, horaFin); // <--- FECHA Y HORA DE FIN
            LocalDateTime ahora = LocalDateTime.now();

            boolean eventoNoIniciado = ahora.isBefore(horaInicioEvento);
            boolean eventoTerminado = ahora.isAfter(horaFinEvento); // <--- LÍMITE SUPERIOR

            if (eventoNoIniciado) {
                // Caso 1: Evento NO iniciado
                long minutosRestantes = ChronoUnit.MINUTES.between(ahora, horaInicioEvento);
                long horas = minutosRestantes / 60;
                long minutos = minutosRestantes % 60;

                btnSeleccionar.setText(String.format("INICIA EN %d H %d M", horas, minutos));
                btnSeleccionar.setDisable(true);
                btnSeleccionar.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

            } else if (eventoTerminado) {
                // Caso 2: Evento TERMINADO
                btnSeleccionar.setText("CERRADO");
                btnSeleccionar.setDisable(true);
                btnSeleccionar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");

            } else {
                // Caso 3: Evento ACTIVO (entre inicio y fin)
                btnSeleccionar.setText("SELECCIONAR");
                btnSeleccionar.setDisable(false);
                btnSeleccionar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 10 25;");
                btnSeleccionar.setOnAction(e -> handleSeleccionarEvento(evento));
            }

        } catch (Exception e) {
            // Manejo de error si la fecha o la hora tienen un formato incorrecto
            btnSeleccionar.setText("ERROR FECHA");
            btnSeleccionar.setDisable(true);
            btnSeleccionar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            //e.printStackTrace();
        }


        // Añadir todos los componentes
        tarjeta.getChildren().addAll(
                lblNombre,
                lblLugar,
                lblFecha,
                lblHoraInicio,
                lblHoraFin,
                spacer,
                btnSeleccionar
        );
        return tarjeta;
    }

    private void handleSeleccionarEvento(EventoItem evento) {
        System.out.println("Evento seleccionado: " + evento.getNombre());

        // Guardar el ID del evento en la sesión
        UserSession.getInstance().setTempEventoId(evento.getId());

        // Ir a la pantalla unificada de equipos
        cambiarVistaBoton(vboxListaEventos, "juez_equiposUnificado.fxml");
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "juez_menu.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        cambiarVista(event, "login.fxml");
    }

    // Metodo para ActionEvent (Botones del FXML)
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }

    // Metodo para eventos manuales (Botones generados dinámicamente)
    private void cambiarVistaBoton(Node nodo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }
}