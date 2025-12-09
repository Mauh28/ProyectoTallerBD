package com.example.proyectotbd;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class JuezEquiposUnificadoController {

    @FXML private VBox vboxListaEquipos;
    @FXML private Label lblCategoriaActual;
    @FXML private HBox contenedorBotones; // <--- Referencia al nuevo HBox del FXML

    // Instancia exclusiva del DAO de Juez
    private JuezDAO juezDao = new JuezDAO();

    @FXML
    public void initialize() {
        // --- CAMBIO: Carga automática al iniciar ---
        cargarDatosInteligentes();
    }

    private void cargarDatosInteligentes() {
        int juezId = UserSession.getInstance().getUserId();
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (eventoId == 0) return;

        try {
            // 1. Obtenemos la LISTA de categorías
            List<String> categorias = juezDao.obtenerCategoriasAsignadas(juezId, eventoId);

            if (categorias.isEmpty()) {
                lblCategoriaActual.setText("Sin asignación para este evento.");
                lblCategoriaActual.setStyle("-fx-text-fill: #e74c3c;");
                return;
            }

            // CASO A: Solo una categoría (Comportamiento Automático)
            if (categorias.size() == 1) {
                String unicaCategoria = categorias.get(0);
                lblCategoriaActual.setText("Categoría Asignada: " + unicaCategoria);
                lblCategoriaActual.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                contenedorBotones.getChildren().clear(); // Asegurar que esté limpio

                cargarEquipos(unicaCategoria);
            }
            // CASO B: Múltiples categorías (Generar Botones)
            else {
                lblCategoriaActual.setText("Selecciona una Categoría:");
                lblCategoriaActual.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                contenedorBotones.getChildren().clear(); // Limpiar por si acaso

                // Crear un botón por cada categoría encontrada
                for (String cat : categorias) {
                    Button btn = new Button(cat);
                    // Estilo similar a tus botones anteriores
                    btn.setStyle("-fx-background-color: white; -fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                    btn.setPrefWidth(150);

                    // Acción: Al hacer clic, carga esa categoría específica
                    btn.setOnAction(e -> {
                        lblCategoriaActual.setText("Viendo: " + cat);
                        cargarEquipos(cat);

                        // Opcional: Resaltar el botón seleccionado visualmente
                        resetearEstilosBotones();
                        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
                    });

                    contenedorBotones.getChildren().add(btn);
                }

                // Opcional: Cargar la primera por defecto para no dejar la lista vacía
                cargarEquipos(categorias.get(0));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblCategoriaActual.setText("Error BD: " + e.getMessage());
        }
    }

    // Método auxiliar para limpiar estilos visuales de los botones dinámicos
    private void resetearEstilosBotones() {
        for (Node node : contenedorBotones.getChildren()) {
            if (node instanceof Button) {
                node.setStyle("-fx-background-color: white; -fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
            }
        }
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "juez_evento.fxml");
    }

    @FXML
    public void handleFiltroCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String categoria = btn.getText(); // "PRIMARIA", "SECUNDARIA", etc.

        lblCategoriaActual.setText("Viendo: " + categoria);

        // Cargar datos reales
        cargarEquipos(categoria);
    }

    private void cargarEquipos(String categoria) {
        vboxListaEquipos.getChildren().clear();
        int eventoId = UserSession.getInstance().getTempEventoId();

        // Obtenemos el ID del juez actual
        int juezId = UserSession.getInstance().getUserId();

        try {
            // Llamamos al DAO pasando el juezId
            ObservableList<EquipoItem> equipos = juezDao.obtenerEquiposPorEventoYCategoria(eventoId, categoria, juezId);

            if (equipos.isEmpty()) {
                Label vacio = new Label("No hay equipos registrados en la categoría " + categoria + ".");
                vacio.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 20;");
                vboxListaEquipos.getChildren().add(vacio);
                return;
            }

            for (EquipoItem equipo : equipos) {
                vboxListaEquipos.getChildren().add(crearTarjetaEquipo(equipo));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Manejo de error visual...
        }
    }

    private HBox crearTarjetaEquipo(EquipoItem equipo) {
        HBox tarjeta = new HBox();
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setSpacing(15); // Un poco más de espacio
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tarjeta.setEffect(new DropShadow(5, Color.color(0,0,0,0.1)));

        // 1. Datos del Equipo
        VBox infoEquipo = new VBox(5);
        Label lblNombre = new Label(equipo.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        Label lblInst = new Label(equipo.getInstitucion());
        lblInst.setStyle("-fx-text-fill: #7f8c8d;");
        infoEquipo.getChildren().addAll(lblNombre, lblInst);
        infoEquipo.setPrefWidth(350);

        // 2. NUEVO: Indicador de Progreso Global
        // Muestra cuántos jueces han evaluado en total
        Label lblProgreso = new Label("Jueces: " + equipo.getConteoJueces() + "/3");
        lblProgreso.setPrefWidth(120);
        // Cambia de color si ya está completo (3/3)
        if (equipo.getConteoJueces() >= 3) {
            lblProgreso.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            lblProgreso.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        // 3. Estado Personal (Tu estado como juez)
        boolean yaEvaluado = "EVALUADO".equalsIgnoreCase(equipo.getEstado());
        Label lblEstado = new Label(yaEvaluado ? "TU ESTADO: LISTO" : "TU ESTADO: PENDIENTE");
        lblEstado.setPrefWidth(180);
        lblEstado.setStyle(yaEvaluado
                ? "-fx-text-fill: #7f8c8d; -fx-font-size: 11px;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Botón de Acción
        Button btnAccion = new Button();
        if (yaEvaluado) {
            btnAccion.setText("COMPLETADO");
            btnAccion.setDisable(true);
            btnAccion.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else {
            // Si tú no has evaluado, el botón está activo (incluso si otros jueces ya evaluaron)
            btnAccion.setText("EVALUAR");
            btnAccion.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
            btnAccion.setOnAction(e -> irAEvaluar(equipo));
        }

        // Añadir todo al HBox
        tarjeta.getChildren().addAll(infoEquipo, lblProgreso, lblEstado, spacer, btnAccion);
        return tarjeta;
    }

    // Método auxiliar para mostrar alertas de error
    private void mostrarAlertaError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error en la Evaluación");
        alert.setHeaderText("Fallo en la operación de inicio");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void irAEvaluar(EquipoItem equipo) {
        // 1. Guardar datos de contexto en Sesión (NO insertamos nada en BD todavía)
        UserSession session = UserSession.getInstance();
        session.setEquipoIdTemp(equipo.getId()); // ID del equipo a evaluar
        session.setTempNombreEquipo(equipo.getNombre()); // Nombre para el título

        // Limpiamos IDs viejos por seguridad
        session.setEvaluacionIdTemp(0);
        session.setIdDisenoTemp(0);
        session.setIdProgramacionTemp(0);
        session.setIdConstruccionTemp(0);

        // 2. Navegar directamente al formulario vacío
        cambiarVistaBoton(vboxListaEquipos, "juez_evaluacion.fxml");
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

    // Método auxiliar para cambiar vista desde un evento de código (no ActionEvent directo)
    private void cambiarVistaBoton(Node nodo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) nodo.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleIrAlMenu(ActionEvent event) {
        cambiarVista(event, "juez_menu.fxml");
    }
}