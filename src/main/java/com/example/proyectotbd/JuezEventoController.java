package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
            e.printStackTrace();
            // Mostrar un error más amigable para el usuario
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

        // 1. Nombre del Evento (Ajustado a 350.0 para hacer espacio a las horas)
        Label lblNombre = new Label(evento.getNombre());
        lblNombre.setPrefWidth(350.0);
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");

        // 2. Lugar (Ajustado a 200.0)
        Label lblLugar = new Label(evento.getLugar());
        lblLugar.setPrefWidth(200.0);
        lblLugar.setWrapText(true);
        lblLugar.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        // 3. Fecha (Ajustado a 120.0)
        Label lblFecha = new Label(evento.getFecha());
        lblFecha.setPrefWidth(120.0);
        lblFecha.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        // 4. HORA DE INICIO (NUEVO)
        // Tomamos solo HH:MM eliminando :SS (que se incluye por el .toString() de java.sql.Time)
        String horaInicioFormatted = evento.getHoraInicio().substring(0, 5);
        Label lblHoraInicio = new Label(horaInicioFormatted);
        lblHoraInicio.setPrefWidth(80.0);
        lblHoraInicio.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");

        // 5. HORA DE FIN (NUEVO)
        String horaFinFormatted = evento.getHoraFin().substring(0, 5);
        Label lblHoraFin = new Label(horaFinFormatted);
        lblHoraFin.setPrefWidth(80.0);
        lblHoraFin.setStyle("-fx-text-fill: #34495e; -fx-font-weight: bold;");


        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 6. Botón Seleccionar
        Button btnSeleccionar = new Button("SELECCIONAR");
        btnSeleccionar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 10 25;");

        // ACCIÓN: Guardar ID y Navegar
        btnSeleccionar.setOnAction(e -> handleSeleccionarEvento(evento));

        // Añadir todos los componentes en el orden de los encabezados (FXML)
        tarjeta.getChildren().addAll(
                lblNombre,
                lblLugar,
                lblFecha,
                lblHoraInicio, // <-- Nuevo
                lblHoraFin,    // <-- Nuevo
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

    // Método para ActionEvent (Botones del FXML)
    private void cambiarVista(ActionEvent event, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Método para eventos manuales (Botones generados dinámicamente)
    private void cambiarVistaBoton(Node nodo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}