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

public class JuezReportesController {

    @FXML private TableView<ReporteJuezItem> tablaReportes;
    @FXML private TableColumn<ReporteJuezItem, String> colEquipo;
    @FXML private TableColumn<ReporteJuezItem, String> colCategoria;
    @FXML private TableColumn<ReporteJuezItem, String> colDiseno;
    @FXML private TableColumn<ReporteJuezItem, String> colProg;
    @FXML private TableColumn<ReporteJuezItem, String> colConst;
    @FXML private TableColumn<ReporteJuezItem, String> colTotal;

    @FXML private Label lblMensaje;

    private JuezDAO juezDao = new JuezDAO();

    @FXML
    public void initialize() {
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDiseno.setCellValueFactory(new PropertyValueFactory<>("diseno"));
        colProg.setCellValueFactory(new PropertyValueFactory<>("prog"));
        colConst.setCellValueFactory(new PropertyValueFactory<>("construc"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        cargarDatos();
    }

    private void cargarDatos() {
        int juezId = UserSession.getInstance().getUserId();
        try {
            ObservableList<ReporteJuezItem> historial = juezDao.obtenerHistorial(juezId);
            tablaReportes.setItems(historial);

            if (historial.isEmpty()) {
                // Si tienes un label de mensaje en el FXML:
                if (lblMensaje != null) lblMensaje.setText("Aún no has realizado ninguna evaluación.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "juez_menu.fxml");
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
}