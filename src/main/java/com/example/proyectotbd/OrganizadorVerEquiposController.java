package com.example.proyectotbd;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class OrganizadorVerEquiposController {

    @FXML private TabPane tabPaneCategorias;
    @FXML private Label lblEventoNombre;

    private OrganizadorDAO dao = new OrganizadorDAO();

    @FXML
    public void initialize() {
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (eventoId == 0) {
            tabPaneCategorias.getTabs().add(new Tab("Error", new Label("Error: No se seleccionó ningún evento.")));
            lblEventoNombre.setText("N/A");
            return;
        }

        // 1. Obtener y mostrar el nombre del evento
        String nombreEvento = obtenerNombreEvento(eventoId);
        lblEventoNombre.setText("Evento: " + nombreEvento);

        // 2. Cargar pestañas dinámicas de Equipos por Categoría
        cargarPestanasEquipos(eventoId);

        // 3. Cargar pestañas estáticas de Reportes Consolidados
        cargarPestanasReportesEstaticos(eventoId);
    }


    private String obtenerNombreEvento(int eventoId) {
        return "ID " + eventoId;
    }

    private void cargarPestanasEquipos(int eventoId) {
        // Limpiamos solo para la carga inicial
        tabPaneCategorias.getTabs().clear();

        try {
            ObservableList<OpcionCombo> categorias = dao.obtenerCategorias();

            if (categorias.isEmpty()) {
                tabPaneCategorias.getTabs().add(new Tab("Aviso", new Label("No hay categorías definidas.")));
                return;
            }

            for (OpcionCombo categoria : categorias) {
                String nombreCategoria = categoria.toString();

                ObservableList<EquipoItem> equipos = dao.obtenerEquiposAdmin(eventoId, nombreCategoria);

                TableView<EquipoItem> tabla = crearTablaEquipos(nombreCategoria, equipos);

                // La pestaña de equipos se crea con un BorderPane contenedor
                Tab tab = new Tab("Equipos: " + nombreCategoria, new BorderPane(tabla));
                tabPaneCategorias.getTabs().add(tab);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            tabPaneCategorias.getTabs().add(new Tab("Error", new Label("Error al cargar equipos: " + e.getMessage())));
        }
    }

    private void cargarPestanasReportesEstaticos(int eventoId) {
        // --- 1. Pestaña de Asignaciones de Jueces ---
        try {
            ObservableList<AsignacionItem> asignaciones = dao.obtenerReporteAsignaciones(eventoId);
            TableView<AsignacionItem> tablaAsignaciones = crearTablaAsignaciones(asignaciones);

            Tab tabJueces = new Tab("Asignaciones de Jueces", new BorderPane(tablaAsignaciones));
            tabPaneCategorias.getTabs().add(tabJueces);
        } catch (SQLException e) {
            e.printStackTrace();
            tabPaneCategorias.getTabs().add(new Tab("Asignaciones", new Label("Error al cargar asignaciones: " + e.getMessage())));
        }

        // --- 2. Pestaña de Resultados Finales ---
        try {
            ObservableList<ResultadoFinalItem> resultados = dao.obtenerResultadosFinales(eventoId);
            TableView<ResultadoFinalItem> tablaResultados = crearTablaResultados(resultados);

            Tab tabResultados = new Tab("Resultados Finales", new BorderPane(tablaResultados));
            tabPaneCategorias.getTabs().add(tabResultados);
        } catch (SQLException e) {
            e.printStackTrace();
            tabPaneCategorias.getTabs().add(new Tab("Resultados", new Label("Error al cargar resultados finales: " + e.getMessage())));
        }
    }


    private TableView<EquipoItem> crearTablaEquipos(String categoriaNombre, ObservableList<EquipoItem> datos) {
        TableView<EquipoItem> tabla = new TableView<>(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EquipoItem, String> colNombre = new TableColumn<>("EQUIPO");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<EquipoItem, String> colInst = new TableColumn<>("INSTITUCIÓN");
        colInst.setCellValueFactory(new PropertyValueFactory<>("institucion"));

        TableColumn<EquipoItem, String> colEstado = new TableColumn<>("ESTADO");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tabla.getColumns().addAll(colNombre, colInst, colEstado);

        if (datos.isEmpty()) {
            tabla.setPlaceholder(new Label("No hay equipos inscritos en la categoría: " + categoriaNombre));
        }
        return tabla;
    }

    private TableView<AsignacionItem> crearTablaAsignaciones(ObservableList<AsignacionItem> datos) {
        TableView<AsignacionItem> tabla = new TableView<>(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<AsignacionItem, String> colEvento = new TableColumn<>("EVENTO");
        colEvento.setCellValueFactory(new PropertyValueFactory<>("evento"));

        TableColumn<AsignacionItem, String> colCategoria = new TableColumn<>("CATEGORÍA");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<AsignacionItem, String> colJueces = new TableColumn<>("JUECES ASIGNADOS");
        colJueces.setCellValueFactory(new PropertyValueFactory<>("jueces"));

        tabla.getColumns().addAll(colEvento, colCategoria, colJueces);

        if (datos.isEmpty()) {
            tabla.setPlaceholder(new Label("No se encontraron asignaciones de jueces para este evento."));
        }

        return tabla;
    }


    private TableView<ResultadoFinalItem> crearTablaResultados(ObservableList<ResultadoFinalItem> datos) {
        TableView<ResultadoFinalItem> tabla = new TableView<>(datos);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ResultadoFinalItem, String> colEquipo = new TableColumn<>("EQUIPO");
        colEquipo.setCellValueFactory(new PropertyValueFactory<>("nombreEquipo"));

        TableColumn<ResultadoFinalItem, String> colCoach = new TableColumn<>("COACH");
        colCoach.setCellValueFactory(new PropertyValueFactory<>("nombreCoach"));

        TableColumn<ResultadoFinalItem, String> colCategoria = new TableColumn<>("CATEGORÍA");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));

        // Formato para mostrar el puntaje
        TableColumn<ResultadoFinalItem, Double> colPuntaje = new TableColumn<>("PUNTAJE PROMEDIO");
        colPuntaje.setCellValueFactory(new PropertyValueFactory<>("puntajePromedio"));

        // Opcional: Aplicar un CellFactory para limitar los decimales en el display
        colPuntaje.setCellFactory(tc -> new TableCell<ResultadoFinalItem, Double>() {
            @Override
            protected void updateItem(Double puntaje, boolean empty) {
                super.updateItem(puntaje, empty);
                if (empty || puntaje == null) {
                    setText(null);
                } else {
                    // Muestra el puntaje con dos decimales
                    setText(String.format("%.2f", puntaje));
                }
            }
        });

        tabla.getColumns().addAll(colEquipo, colCoach, colCategoria, colPuntaje);

        if (datos.isEmpty()) {
            tabla.setPlaceholder(new Label("Ningún equipo ha sido evaluado aún en este evento."));
        }

        return tabla;
    }


    // =================================================================
    //  NAVEGACIÓN
    // =================================================================

    @FXML
    public void handleRegresar(ActionEvent event) {
        // Vuelve al Directorio de Eventos (el Hub de Eventos)
        cambiarVista(event, "organizador_verEventos.fxml");
    }

    // Método auxiliar para navegación
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