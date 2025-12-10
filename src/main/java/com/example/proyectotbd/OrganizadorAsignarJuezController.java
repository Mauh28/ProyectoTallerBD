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

    // Botones inyectados para cambiar su estilo visualmente
    @FXML private Button btnPrimaria;
    @FXML private Button btnSecundaria;
    @FXML private Button btnPrepa;
    @FXML private Button btnProfesional;

    @FXML private Label lblCategoriaSeleccionada;
    @FXML private Label lblMensaje;

    private OrganizadorDAO organizadorDAO = new OrganizadorDAO();
    private String categoriaTexto = null;
    private int categoriaId = 0;

    @FXML
    public void initialize() {
        cargarDatosIniciales();

        // Listener: Si cambian el evento, reseteamos la categoría para evitar errores
        cbEventos.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                resetearSeleccionCategoria();
                cbJueces.setItems(FXCollections.observableArrayList()); // Limpiar lista de jueces
                cbJueces.getSelectionModel().clearSelection();
                lblMensaje.setVisible(false);
            }
        });
    }

    private void cargarDatosIniciales() {
        try {
            cbEventos.setItems(organizadorDAO.obtenerEventosFuturos());
            cbJueces.setItems(FXCollections.observableArrayList());
        } catch (SQLException e) {
            mostrarMensaje("Error al conectar con eventos: " + e.getMessage(), true);
        }
    }

    // =================================================================
    //  MANEJO DE CATEGORÍA (Con Validación Visual)
    // =================================================================

    @FXML
    public void handleCategoria(ActionEvent event) {
        // 1. Validar que primero se haya seleccionado un evento
        if (cbEventos.getValue() == null) {
            mostrarMensaje("Primero selecciona un evento.", true);
            return;
        }

        Button btn = (Button) event.getSource();
        categoriaTexto = btn.getText().toUpperCase();

        // Mapeo de IDs
        switch (categoriaTexto) {
            case "PRIMARIA": categoriaId = 1; break;
            case "SECUNDARIA": categoriaId = 2; break;
            case "PREPARATORIA": categoriaId = 3; break;
            case "PROFESIONAL": categoriaId = 4; break;
            case "UNIVERSIDAD": categoriaId = 4; break;
            default: categoriaId = 0; break;
        }

        // 2. Feedback Visual: Iluminar el botón seleccionado
        actualizarEstiloBotones(btn);

        lblCategoriaSeleccionada.setText("Seleccionada: " + btn.getText());
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Cargar jueces disponibles para esa combinación
        cargarJuecesFiltrados();
    }

    // Método auxiliar para pintar el botón activo y apagar los demás
    private void actualizarEstiloBotones(Button btnActivo) {
        // Estilo base (gris claro)
        String estiloBase = "-fx-base: #f4f6f8; -fx-cursor: hand;";
        // Estilo activo (azul)
        String estiloActivo = "-fx-base: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;";

        if(btnPrimaria != null) btnPrimaria.setStyle(estiloBase);
        if(btnSecundaria != null) btnSecundaria.setStyle(estiloBase);
        if(btnPrepa != null) btnPrepa.setStyle(estiloBase);
        if(btnProfesional != null) btnProfesional.setStyle(estiloBase);

        if(btnActivo != null) {
            btnActivo.setStyle(estiloActivo);
        }
    }

    private void resetearSeleccionCategoria() {
        categoriaId = 0;
        categoriaTexto = null;
        lblCategoriaSeleccionada.setText("Ninguna seleccionada");
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // Resetear botones visualmente
        actualizarEstiloBotones(null);
    }

    private void cargarJuecesFiltrados() {
        OpcionCombo eventoSeleccionado = cbEventos.getValue();

        if (eventoSeleccionado == null || categoriaId == 0) {
            return;
        }

        try {
            ObservableList<OpcionCombo> jueces = organizadorDAO.obtenerJuecesSinConflicto(categoriaId, eventoSeleccionado.getId());
            cbJueces.setItems(jueces);

            if (jueces.isEmpty()) {
                mostrarMensaje("No hay jueces disponibles para esta categoría en este evento.", true);
                cbJueces.setDisable(true);
            } else {
                lblMensaje.setVisible(false);
                cbJueces.setDisable(false);
                cbJueces.setPromptText("Selecciona un juez (" + jueces.size() + " disponibles)");
            }
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar jueces: " + e.getMessage(), true);
        }
    }

    @FXML
    public void handleGuardar(ActionEvent event) {
        OpcionCombo evento = cbEventos.getValue();
        OpcionCombo juez = cbJueces.getValue();

        // 1. Validaciones
        if (evento == null || categoriaId == 0 || juez == null) {
            mostrarMensaje("Error: Completa los 3 pasos (Evento, Categoría y Juez).", true);
            return;
        }

        // 2. Confirmación
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar Asignación");
        alerta.setHeaderText(null);
        alerta.setContentText("¿Asignar a " + juez + " en " + categoriaTexto + "?");
        if (alerta.showAndWait().get() != ButtonType.OK) return;

        try {
            // 3. Guardar en BD
            organizadorDAO.asignarJuez(juez.getId(), categoriaId, evento.getId());

            mostrarNotificacionExito("¡Juez " + juez + " asignado correctamente!");

            // --- MEJORA: FLUJO CONTINUO ---
            // Limpiamos solo el juez para permitir agregar otro inmediatamente
            cbJueces.getSelectionModel().clearSelection();
            cargarJuecesFiltrados(); // Recargar la lista (el juez asignado desaparecerá)

            lblMensaje.setText("Asignación guardada. Puedes agregar otro juez.");
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);

        } catch (SQLException e) {
            // Error controlado (sin printStackTrace)
            mostrarMensaje("Error: " + e.getMessage(), true);
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            Label label = new Label("⚖️ " + mensaje);
            label.setStyle("-fx-background-color: #27ae60;-fx-text-fill: white;-fx-font-weight: bold;-fx-font-size: 16px;-fx-padding: 20px;-fx-background-radius: 10px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
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
        } catch (Exception e) {}
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
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("No se pudo cargar la vista: " + fxml);
            error.show();
        }
    }
}