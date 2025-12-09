package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class CoachReportesController {

    // Cambiamos la referencia a la nueva clase ReporteCoachItem
    @FXML private TableView<ReporteCoachItem> tablaReportes;
    @FXML private TableColumn<ReporteCoachItem, String> colEquipo;
    @FXML private TableColumn<ReporteCoachItem, String> colCategoria;
    @FXML private TableColumn<ReporteCoachItem, String> colEvento;
    @FXML private TableColumn<ReporteCoachItem, String> colDiseno;
    @FXML private TableColumn<ReporteCoachItem, String> colProg;
    @FXML private TableColumn<ReporteCoachItem, String> colConst;
    @FXML private TableColumn<ReporteCoachItem, String> colTotal;

    private CoachDAO coachDAO = new CoachDAO();

    @FXML
    public void initialize() {
        // Configuramos las columnas usando los getters de ReporteCoachItem
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEvento.setCellValueFactory(new PropertyValueFactory<>("evento"));
        colDiseno.setCellValueFactory(new PropertyValueFactory<>("ptsDiseno"));
        colProg.setCellValueFactory(new PropertyValueFactory<>("ptsProg"));
        colConst.setCellValueFactory(new PropertyValueFactory<>("ptsConst"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        cargarDatosReales();
    }

    private void cargarDatosReales() {
        int coachId = UserSession.getInstance().getUserId();
        try {
            // El DAO ahora devuelve la lista correcta
            ObservableList<ReporteCoachItem> datos = coachDAO.obtenerReporteEvaluaciones(coachId);
            tablaReportes.setItems(datos);

            if (datos.isEmpty()) {
                tablaReportes.setPlaceholder(new Label("No hay evaluaciones registradas aún."));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            tablaReportes.setPlaceholder(new Label("Error de conexión: " + e.getMessage()));
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
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // NOTA: He eliminado la clase interna "ReporteItem" porque ahora usas "ReporteCoachItem.java"
}