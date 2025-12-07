package com.example.proyectotbd;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class OrganizadorAsignarJuezController {

    @FXML private ComboBox<OpcionCombo> cbEventos;
    @FXML private ComboBox<OpcionCombo> cbJueces;
    @FXML private Label lblCategoriaSeleccionada;
    @FXML private Label lblMensaje;

    // Instancia del DAO
    private OrganizadorDAO organizadorDAO = new OrganizadorDAO();

    private String categoriaTexto = null;
    private int categoriaId = 0;

    @FXML
    public void initialize() {
        // Solo cargamos eventos; los jueces se cargan DESPUÉS de la selección del evento/categoría.
        cargarDatosIniciales();
    }

    private void cargarDatosIniciales() {
        try {
            cbEventos.setItems(organizadorDAO.obtenerEventosFuturos());
            // Se inicializa la lista de jueces vacía
            cbJueces.setItems(FXCollections.observableArrayList());
        } catch (SQLException e) {
            mostrarMensaje("Error al conectar con eventos: " + e.getMessage(), true);
        }

        // Agregar un listener al ComboBox de Eventos para disparar la carga de jueces
        cbEventos.setOnAction(e -> cargarJuecesFiltrados());
    }

    // =================================================================
    //  MANEJO DE CATEGORÍA
    // =================================================================

    @FXML
    public void handleCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        categoriaTexto = btn.getText().toUpperCase();

        switch (categoriaTexto) {
            case "PRIMARIA": categoriaId = 1; break;
            case "SECUNDARIA": categoriaId = 2; break;
            case "PREPARATORIA": categoriaId = 3; break;
            case "PROFESIONAL": categoriaId = 4; break;
            case "UNIVERSIDAD": categoriaId = 4; break;
            default: categoriaId = 0; break;
        }

        lblCategoriaSeleccionada.setText("Seleccionada: " + btn.getText());
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");

        // Llamar a cargar jueces cada vez que se selecciona una categoría
        cargarJuecesFiltrados();
    }

    // =================================================================
    //  CARGA DINÁMICA DE JUECES (NUEVO MÉTODO)
    // =================================================================

    private void cargarJuecesFiltrados() {
        OpcionCombo eventoSeleccionado = cbEventos.getValue();

        // 1. Validar que ambos campos estén seleccionados
        if (eventoSeleccionado == null || categoriaId == 0) {
            cbJueces.setItems(FXCollections.observableArrayList()); // Limpiar lista
            mostrarMensaje("Selecciona un Evento y una Categoría para ver los jueces disponibles.", false);
            return;
        }

        int eventoId = eventoSeleccionado.getId();

        try {
            // 2. Llamada al DAO con los filtros (categoriaId y eventoId)
            ObservableList<OpcionCombo> jueces = organizadorDAO.obtenerJuecesSinConflicto(categoriaId, eventoId);
            cbJueces.setItems(jueces);

            if (jueces.isEmpty()) {
                mostrarMensaje("No hay jueces disponibles sin conflicto de interés en esta categoría/evento.", true);
            } else {
                lblMensaje.setVisible(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error al cargar jueces: " + e.getMessage(), true);
        }
    }


    @FXML
    public void handleGuardar(ActionEvent event) {
        OpcionCombo evento = cbEventos.getValue();
        OpcionCombo juez = cbJueces.getValue();

        // 1. Validaciones de UI
        if (evento == null || categoriaId == 0 || juez == null) {
            mostrarMensaje("Error: Completa los 3 pasos (Evento, Categoría y Juez).", true);
            return;
        }

        try {
            // 2. Llamada al DAO (Procedimiento Almacenado)
            organizadorDAO.asignarJuez(juez.getId(), categoriaId, evento.getId());

            // --- ÉXITO ---
            System.out.println("Juez asignado correctamente.");

            // Mostrar Pop-up
            mostrarNotificacionExito("¡Juez " + juez + " asignado correctamente!");

            // Regresar al menú principal
            cambiarVista(event, "organizador_menu.fxml");

        } catch (SQLException e) {
            // Errores de negocio (cupo lleno, conflicto de interés, faltan equipos)
            e.printStackTrace();
            mostrarMensaje("Error: " + e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES (Sin cambios) ---
    private void mostrarNotificacionExito(String mensaje) {
        try {
            // ... (código del Toast Pop-up) ...
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);

            Label label = new Label("⚖️ " + mensaje);
            label.setStyle(
                    "-fx-background-color: #27ae60;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 16px;" +
                            "-fx-padding: 20px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);"
            );

            StackPane root = new StackPane(label);
            root.setStyle("-fx-background-color: transparent;");
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            toastStage.setScene(scene);

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            toastStage.setX(screenBounds.getMaxX() - 450);
            toastStage.setY(screenBounds.getMaxY() - 100);

            toastStage.show();
            PauseTransition delay = new PauseTransition(Duration.seconds(3));
            delay.setOnFinished(e -> toastStage.close());
            delay.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    private void mostrarMensaje(String msg, boolean error) {
        lblMensaje.setText(msg);
        lblMensaje.setStyle(error ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
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