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

public class OrganizadorVerUsuariosController {

    @FXML private TableView<UsuarioItem> tablaUsuarios;
    @FXML private TableColumn<UsuarioItem, Integer> colId;
    @FXML private TableColumn<UsuarioItem, String> colNombre;
    @FXML private TableColumn<UsuarioItem, String> colUser;
    @FXML private TableColumn<UsuarioItem, String> colInst;
    @FXML private TableColumn<UsuarioItem, String> colRol;

    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colInst.setCellValueFactory(new PropertyValueFactory<>("institucion"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        cargarUsuarios();
    }

    private void cargarUsuarios() {
        try {
            tablaUsuarios.setItems(dao.obtenerTodosLosUsuarios());
        } catch (SQLException e) {
            mostrarAlerta("Error", "No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }

    @FXML
    public void handleEliminar(ActionEvent event) {
        UsuarioItem seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Atención", "Selecciona un usuario para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Estás seguro de eliminar a " + seleccionado.getNombre() + "?");
        confirm.setContentText("Esta acción borrará también sus equipos y evaluaciones asociadas.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                dao.eliminarUsuario(seleccionado.getId());
                cargarUsuarios(); // Refrescar tabla
                mostrarAlerta("Éxito", "Usuario eliminado correctamente.");
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    private void cambiarVista(ActionEvent event, String fxml) { /* Código estándar de cambio de vista */
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            // Muestra un error más claro en la consola sobre la fuente del problema
            System.err.println("\n*** ERROR CRÍTICO DE NAVEGACIÓN ***");
            System.err.println("Fallo al cargar la vista FXML: " + fxml);
            System.err.println("Causa más probable: 1) Nombre de archivo incorrecto; 2) Error de sintaxis en el FXML de destino.");
            System.err.println("***********************************\n");
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}