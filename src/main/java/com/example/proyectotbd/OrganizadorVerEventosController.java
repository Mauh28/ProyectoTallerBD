package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class OrganizadorVerEventosController {

    @FXML private TableView<EventoItem> tablaEventos;
    @FXML private TableColumn<EventoItem, Integer> colId;
    @FXML private TableColumn<EventoItem, String> colNombre;
    @FXML private TableColumn<EventoItem, String> colLugar;
    @FXML private TableColumn<EventoItem, String> colFecha;
    @FXML private TableColumn<EventoItem, String> colJueces; // Nueva referencia

    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        // Enlazamos las columnas con los atributos de EventoItem
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colJueces.setCellValueFactory(new PropertyValueFactory<>("jueces"));
        cargarEventos();
    }

    private void cargarEventos() {
        try {
            tablaEventos.setItems(dao.obtenerTodosLosEventos());
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los eventos: " + e.getMessage());
        }
    }

    @FXML
    public void handleEliminar(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un evento para eliminar.");
            return;
        }

        // Confirmación de seguridad
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Eliminar evento '" + seleccionado.getNombre() + "'?");
        confirm.setContentText("Esto borrará también todos los equipos y evaluaciones asociados.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.eliminarEvento(seleccionado.getId());
                cargarEventos(); // Refrescar la tabla
                mostrarAlerta("Éxito", "Evento eliminado correctamente.");
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
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

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}