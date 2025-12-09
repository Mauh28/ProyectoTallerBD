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
import java.time.LocalDate;
import java.util.Optional;

public class OrganizadorVerEventosController {

    @FXML private TableView<EventoItem> tablaEventos;
    @FXML private TableColumn<EventoItem, Integer> colId;
    @FXML private TableColumn<EventoItem, String> colNombre;
    @FXML private TableColumn<EventoItem, String> colLugar;
    @FXML private TableColumn<EventoItem, String> colFecha;
    // --- Columnas añadidas para los nuevos campos de hora ---
    @FXML private TableColumn<EventoItem, String> colHoraInicio;
    @FXML private TableColumn<EventoItem, String> colHoraFin;
    // --------------------------------------------------------
    @FXML private TableColumn<EventoItem, String> colJueces;
    // @FXML private TableColumn<EventoItem, Void> colAcciones; // Si hubiéramos usado botones por fila, pero usamos un botón global.

    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        // Enlazamos las columnas con los atributos de EventoItem
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugar"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        // Mapeo de las nuevas columnas de hora
        colHoraInicio.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colHoraFin.setCellValueFactory(new PropertyValueFactory<>("horaFin"));

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

    // =================================================================
    //  NUEVA FUNCIÓN: EDITAR EVENTO
    // =================================================================

    @FXML
    public void handleEditarEvento(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un evento para editar.", false);
            return;
        }

        // 1. Validar la regla de negocio: No se puede editar si es hoy o pasó.
        // Convertimos la fecha de String (yyyy-MM-dd) a LocalDate para la comparación
        LocalDate fechaEvento = LocalDate.parse(seleccionado.getFecha());

        if (!fechaEvento.isAfter(LocalDate.now())) {
            mostrarAlerta("Edición Restringida",
                    "No puedes editar este evento porque ya expiró o está programado para hoy. Solo se pueden editar eventos futuros.",
                    true);
            return;
        }

        // 2. Navegar a la vista de edición, pasando el objeto EventoItem
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("organizador_editarEvento.fxml"));
            Parent root = loader.load();

            // Obtener el controlador de la vista de edición
            OrganizadorEditarEventoController editorController = loader.getController();

            // Pasar el evento seleccionado al controlador de edición
            editorController.setEventoItem(seleccionado);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo cargar la vista de edición.", true);
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
    //  REPORTE DETALLADO Y NAVEGACIÓN
    // =================================================================

    @FXML
    public void handleVerReporteDetallado(ActionEvent event) {
        EventoItem seleccionado = tablaEventos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un evento para ver sus reportes detallados.", false);
            return;
        }

        // 1. Guardar el ID del evento seleccionado en la sesión
        UserSession.getInstance().setTempEventoId(seleccionado.getId());

        // 2. Navegar a la vista consolidada de reportes por pestañas
        cambiarVista(event, "organizador_verEquipos.fxml");
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

    /**
     * Muestra una alerta informativa o de error.
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