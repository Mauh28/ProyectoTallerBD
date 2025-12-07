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

    // Reutilizamos el DAO que ya tiene el método para traer eventos con detalles (Nombre, Lugar, Fecha)
    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        cargarEventos();
    }

    private void cargarEventos() {
        vboxListaEventos.getChildren().clear();

        try {
            // Usamos el método que ya creamos para el Admin, pues nos sirve igual aquí
            // (Trae ID, Nombre, Lugar, Fecha)
            ObservableList<EventoItem> eventos = dao.obtenerTodosLosEventos();

            if (eventos.isEmpty()) {
                Label vacio = new Label("No hay eventos próximos disponibles.");
                vacio.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px; -fx-padding: 20;");
                vboxListaEventos.getChildren().add(vacio);
                return;
            }

            for (EventoItem evento : eventos) {
                vboxListaEventos.getChildren().add(crearTarjetaEvento(evento));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Label error = new Label("Error de conexión: " + e.getMessage());
            error.setStyle("-fx-text-fill: red;");
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
        lblNombre.setPrefWidth(400);
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #2c3e50;");

        // 2. Lugar
        Label lblLugar = new Label(evento.getLugar());
        lblLugar.setPrefWidth(250);
        lblLugar.setWrapText(true);
        lblLugar.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        // 3. Fecha
        Label lblFecha = new Label(evento.getFecha());
        lblFecha.setPrefWidth(150);
        lblFecha.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Botón Seleccionar
        Button btnSeleccionar = new Button("SELECCIONAR");
        btnSeleccionar.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 5; -fx-padding: 10 25;");

        // ACCIÓN: Guardar ID y Navegar
        btnSeleccionar.setOnAction(e -> handleSeleccionarEvento(evento));

        tarjeta.getChildren().addAll(lblNombre, lblLugar, lblFecha, spacer, btnSeleccionar);
        return tarjeta;
    }

    private void handleSeleccionarEvento(EventoItem evento) {
        System.out.println("Evento seleccionado: " + evento.getNombre());

        // 1. Guardar el ID del evento en la sesión para que la siguiente pantalla sepa cuál usar
        UserSession.getInstance().setTempEventoId(evento.getId());

        // 2. Ir a la pantalla unificada de equipos (ya no pasamos por categoría)
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