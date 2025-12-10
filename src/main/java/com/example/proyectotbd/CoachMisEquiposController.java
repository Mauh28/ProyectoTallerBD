package com.example.proyectotbd;

import javafx.collections.ObservableList;
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

public class CoachMisEquiposController {

    @FXML private TableView<EquipoCoachItem> tablaEquipos;
    @FXML private TableColumn<EquipoCoachItem, String> colNombre;
    @FXML private TableColumn<EquipoCoachItem, String> colInstitucion;
    @FXML private TableColumn<EquipoCoachItem, String> colCategoria;
    @FXML private TableColumn<EquipoCoachItem, String> colEvento;
    @FXML private TableColumn<EquipoCoachItem, String> colIntegrantes; // Nueva referencia
    private CoachDAO dao = new CoachDAO();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInstitucion.setCellValueFactory(new PropertyValueFactory<>("institucion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEvento.setCellValueFactory(new PropertyValueFactory<>("evento"));
        colIntegrantes.setCellValueFactory(new PropertyValueFactory<>("integrantes"));

        cargarDatos();
    }

    private void cargarDatos() {
        int usuarioId = UserSession.getInstance().getUserId();
        System.out.println("--> Buscando equipos para el Coach con ID: " + usuarioId);
        try {
            tablaEquipos.setItems(dao.obtenerMisEquipos(usuarioId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- ACCIÓN EDITAR ---
    @FXML
    public void handleEditar(ActionEvent event) {
        EquipoCoachItem seleccionado = tablaEquipos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selecciona un equipo para editar.", true);
            return;
        }

        // Configurar Sesión para Edición
        UserSession session = UserSession.getInstance();

        // Aquí se guarda el ID. Si getId() es 0, fallará en la siguiente pantalla.
        session.setEquipoIdTemp(seleccionado.getId());
        session.setTempNombreEquipo(seleccionado.getNombre());
        session.setModoEdicion(true); // Activar bandera de edición

        System.out.println("Editando equipo ID: " + seleccionado.getId());

        // Redirigir a la pantalla de integrantes (donde se cargará la lista)
        cambiarVista(event, "coach_registroIntegrantes.fxml");
    }

    // --- ACCIÓN BORRAR ---
    @FXML
    public void handleBorrar(ActionEvent event) {
        EquipoCoachItem seleccionado = tablaEquipos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selecciona un equipo para eliminar.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Equipo");
        confirm.setHeaderText("¿Borrar equipo '" + seleccionado.getNombre() + "'?");
        confirm.setContentText("Se eliminarán también sus integrantes y registros.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.eliminarEquipo(seleccionado.getId());
                cargarDatos(); // Refrescar tabla
                mostrarAlerta("Equipo eliminado correctamente.", false);
            } catch (SQLException e) {
                mostrarAlerta("Error al eliminar: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_menu.fxml");
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

    private void mostrarAlerta(String msg, boolean error) {
        Alert.AlertType type = error ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(error ? "Error" : "Éxito");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}