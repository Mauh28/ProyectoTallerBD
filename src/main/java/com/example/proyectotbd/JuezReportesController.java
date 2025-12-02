package com.example.proyectotbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

public class JuezReportesController {

    @FXML private TableView<ReporteJuezItem> tablaReportes;
    @FXML private TableColumn<ReporteJuezItem, String> colEquipo;
    @FXML private TableColumn<ReporteJuezItem, String> colCategoria;
    @FXML private TableColumn<ReporteJuezItem, String> colDiseno;
    @FXML private TableColumn<ReporteJuezItem, String> colProg;
    @FXML private TableColumn<ReporteJuezItem, String> colConst;
    @FXML private TableColumn<ReporteJuezItem, String> colTotal;

    @FXML
    public void initialize() {
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDiseno.setCellValueFactory(new PropertyValueFactory<>("diseno"));
        colProg.setCellValueFactory(new PropertyValueFactory<>("prog"));
        colConst.setCellValueFactory(new PropertyValueFactory<>("construc"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // DATOS DE PRUEBA
        ObservableList<ReporteJuezItem> datos = FXCollections.observableArrayList(
                new ReporteJuezItem("Code Masters", "Secundaria", "10", "14", "15", "39"),
                new ReporteJuezItem("Alpha Team", "Primaria", "8", "10", "12", "30"),
                new ReporteJuezItem("Prepa Tec", "Preparatoria", "12", "15", "16", "43")
        );

        tablaReportes.setItems(datos);
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa al Menú del Juez
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

    // Clase auxiliar para la tabla
    public static class ReporteJuezItem {
        private final String equipo, categoria, diseno, prog, construc, total;

        public ReporteJuezItem(String e, String c, String d, String p, String co, String t) {
            this.equipo = e; this.categoria = c; this.diseno = d;
            this.prog = p; this.construc = co; this.total = t;
        }
        public String getEquipo() { return equipo; }
        public String getCategoria() { return categoria; }
        public String getDiseno() { return diseno; }
        public String getProg() { return prog; }
        public String getConstruc() { return construc; }
        public String getTotal() { return total; }
    }
}