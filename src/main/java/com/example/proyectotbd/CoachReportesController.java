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

public class CoachReportesController {

    @FXML private TableView<ReporteItem> tablaReportes;
    @FXML private TableColumn<ReporteItem, String> colEquipo;
    @FXML private TableColumn<ReporteItem, String> colCategoria;
    @FXML private TableColumn<ReporteItem, String> colEvento;
    @FXML private TableColumn<ReporteItem, String> colDiseno;
    @FXML private TableColumn<ReporteItem, String> colProg;
    @FXML private TableColumn<ReporteItem, String> colConst;
    @FXML private TableColumn<ReporteItem, String> colTotal;

    @FXML
    public void initialize() {
        // Configurar las columnas para que lean los datos de la clase ReporteItem
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("equipo"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colEvento.setCellValueFactory(new PropertyValueFactory<>("evento"));
        colDiseno.setCellValueFactory(new PropertyValueFactory<>("ptsDiseno"));
        colProg.setCellValueFactory(new PropertyValueFactory<>("ptsProg"));
        colConst.setCellValueFactory(new PropertyValueFactory<>("ptsConst"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // DATOS DE PRUEBA (CASCARÓN) - Solo para visualizar
        ObservableList<ReporteItem> datos = FXCollections.observableArrayList(
                new ReporteItem("RobotiX Alpha", "Secundaria", "Torneo Nacional 2025", "10", "12", "15", "37"),
                new ReporteItem("Cyber VEX", "Preparatoria", "Regional Norte", "8", "14", "14", "36"),
                new ReporteItem("Mecánicos Jr.", "Primaria", "Feria Escolar", "Pend.", "Pend.", "Pend.", "0")
        );

        tablaReportes.setItems(datos);
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Regresa al Dashboard del Coach
        cambiarVista(event, "coach_menu.fxml");
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

    // Clase auxiliar simple para representar una fila en la tabla
    public static class ReporteItem {
        private final String equipo, categoria, evento, ptsDiseno, ptsProg, ptsConst, total;

        public ReporteItem(String equipo, String categoria, String evento, String ptsDiseno, String ptsProg, String ptsConst, String total) {
            this.equipo = equipo;
            this.categoria = categoria;
            this.evento = evento;
            this.ptsDiseno = ptsDiseno;
            this.ptsProg = ptsProg;
            this.ptsConst = ptsConst;
            this.total = total;
        }

        public String getEquipo() { return equipo; }
        public String getCategoria() { return categoria; }
        public String getEvento() { return evento; }
        public String getPtsDiseno() { return ptsDiseno; }
        public String getPtsProg() { return ptsProg; }
        public String getPtsConst() { return ptsConst; }
        public String getTotal() { return total; }
    }
}