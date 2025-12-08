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
    @FXML private TableColumn<EventoItem, String> colJueces;

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
            mostrarAlerta("Error de BD", "No se pudieron cargar los eventos: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleEliminar(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un evento para eliminar.", false);
            return;
        }

        // Confirmación de seguridad
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Eliminar evento '" + seleccionado.getNombre() + "'?");
        confirm.setContentText("Esto borrará también todos los equipos y evaluaciones asociados (Hard Delete).");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.eliminarEvento(seleccionado.getId());
                cargarEventos(); // Refrescar la tabla
                mostrarAlerta("Éxito", "Evento eliminado correctamente.", false);
            } catch (SQLException e) {
                mostrarAlerta("Error de BD", "No se pudo eliminar: " + e.getMessage(), true);
            }
        }
    }

    // =================================================================
    //  NUEVA FUNCIÓN: VER REPORTES DETALLADOS (Hub de Eventos)
    // =================================================================

    /**
     * Guarda el ID del evento seleccionado en sesión y navega a la vista
     * consolidada de reportes por pestañas (organizador_verEquipos.fxml).
     */
    @FXML
    public void handleVerReporteDetallado(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un evento para ver sus reportes detallados.", false);
            return;
        }

        // 1. Guardar el ID del evento seleccionado en la sesión
        UserSession.getInstance().setTempEventoId(seleccionado.getId());

        // Opcional: Si hubieras guardado el nombre del evento en UserSession,
        // lo podrías usar para mostrar el título en la siguiente vista.

        // 2. Navegar a la vista consolidada de reportes por pestañas
        cambiarVista(event, "organizador_verEquipos.fxml");
    }

    // =================================================================
    //  NAVEGACIÓN Y ALERTAS
    // =================================================================

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

    /**
     * Muestra una alerta informativa o de error.
     * @param titulo Título de la alerta.
     * @param msg Mensaje a mostrar.
     * @param esError True para Alert.AlertType.ERROR, False para Alert.AlertType.INFORMATION.
     */
    private void mostrarAlerta(String titulo, String msg, boolean esError) {
        Alert.AlertType type = esError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}