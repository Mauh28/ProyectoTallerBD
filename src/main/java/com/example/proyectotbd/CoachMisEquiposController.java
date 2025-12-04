package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class CoachMisEquiposController {

    @FXML private TableView<EquipoCoachItem> tablaEquipos;
    @FXML private TableColumn<EquipoCoachItem, String> colNombre;
    @FXML private TableColumn<EquipoCoachItem, String> colInstitucion;
    @FXML private TableColumn<EquipoCoachItem, String> colCategoria;
    @FXML private TableColumn<EquipoCoachItem, String> colEvento;

    private CoachDAO dao = new CoachDAO();

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colInstitucion.setCellValueFactory(new PropertyValueFactory<>("institucion"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEvento.setCellValueFactory(new PropertyValueFactory<>("evento"));

        cargarDatos();
    }

    private void cargarDatos() {
        int usuarioId = UserSession.getInstance().getUserId();
        try {
            tablaEquipos.setItems(dao.obtenerMisEquipos(usuarioId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("coach_menu.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}