package com.example.proyectotbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class CoachRegistroIntegrantesController {

    @FXML private TextField txtNombre;
    @FXML private DatePicker dpNacimiento;
    @FXML private ComboBox<String> cbSexo;
    @FXML private Label lblError;

    @FXML private ListView<String> listaParticipantes;
    @FXML private Label lblContador;

    private ObservableList<String> participantes = FXCollections.observableArrayList();

    // CAMBIO: Límite ajustado a 3
    private final int MAX_PARTICIPANTES = 3;

    @FXML
    public void initialize() {
        // Enlazar la lista visual con los datos
        listaParticipantes.setItems(participantes);
        actualizarContador();
    }

    @FXML
    public void handleAgregar(ActionEvent event) {
        // 1. Validar límite (Ahora es 3)
        if (participantes.size() >= MAX_PARTICIPANTES) {
            lblError.setText("Error: Has alcanzado el límite de " + MAX_PARTICIPANTES + " integrantes.");
            lblError.setVisible(true);
            return;
        }

        // 2. Obtener datos
        String nombre = txtNombre.getText();
        LocalDate nacimiento = dpNacimiento.getValue();
        String sexo = cbSexo.getValue();

        // 3. Validar campos vacíos
        if (nombre.isEmpty() || nacimiento == null || sexo == null) {
            lblError.setText("Por favor llena todos los campos.");
            lblError.setVisible(true);
            return;
        }

        // 4. Agregar a la lista
        String registro = nombre + " | " + nacimiento.toString() + " | " + sexo;
        participantes.add(registro);

        // Actualizar UI
        actualizarContador();
        lblError.setVisible(false);

        // Limpiar campos
        txtNombre.clear();
        dpNacimiento.setValue(null);
        cbSexo.getSelectionModel().clearSelection();
    }

    private void actualizarContador() {
        lblContador.setText(participantes.size() + " / " + MAX_PARTICIPANTES);
    }

    @FXML
    public void handleFinalizar(ActionEvent event) {
        if (participantes.isEmpty()) {
            lblError.setText("Debes registrar al menos un integrante.");
            lblError.setVisible(true);
            return;
        }

        System.out.println("Guardando equipo con " + participantes.size() + " integrantes...");

        // Lógica de BD aquí...

        System.out.println("¡Inscripción exitosa! Volviendo al inicio.");
        cambiarVista(event, "coach_menu.fxml");
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_registroEquipo.fxml");
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
            System.out.println("Error al cargar: " + fxml);
        }
    }
}