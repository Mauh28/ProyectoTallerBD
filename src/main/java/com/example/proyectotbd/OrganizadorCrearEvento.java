package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class OrganizadorCrearEvento {

    @FXML private TextField txtNombreEvento;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;
    @FXML private Label lblMensaje;

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa al Dashboard del Organizador (organizador_menu.fxml)
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarEvento(ActionEvent event) {
        // 1. Obtener datos
        String nombre = txtNombreEvento.getText();
        String lugar = txtLugar.getText();
        LocalDate fecha = dpFecha.getValue();

        // 2. Validación Básica
        if (nombre.isEmpty() || lugar.isEmpty() || fecha == null) {
            lblMensaje.setText("Error: Todos los campos son obligatorios.");
            lblMensaje.setStyle("-fx-text-fill: #e74c3c;"); // Rojo
            lblMensaje.setVisible(true);
            return;
        }

        // 3. Simulación de Guardado (Aquí conectarás el SP sp_crear_evento después)
        System.out.println("--- NUEVO EVENTO ---");
        System.out.println("Nombre: " + nombre);
        System.out.println("Lugar:  " + lugar);
        System.out.println("Fecha:  " + fecha);

        lblMensaje.setText("¡Evento creado exitosamente!");
        lblMensaje.setStyle("-fx-text-fill: #27ae60;"); // Verde
        lblMensaje.setVisible(true);

        // Opcional: Limpiar campos
        // txtNombreEvento.clear(); txtLugar.clear(); dpFecha.setValue(null);
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