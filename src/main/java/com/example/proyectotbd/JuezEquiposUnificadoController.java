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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class JuezEquiposUnificadoController {

    @FXML private VBox vboxListaEquipos;
    @FXML private Label lblCategoriaActual;
    @FXML private HBox contenedorBotones;

    // Instancia exclusiva del DAO de Juez
    private JuezDAO juezDao = new JuezDAO();

    // Variable para guardar la hora de inicio del evento
    private LocalDateTime horaInicioEvento;

    @FXML
    public void initialize() {
        cargarDatosInteligentes();
        verificarYEstablecerHoraEvento();
    }

    private void verificarYEstablecerHoraEvento() {
        int eventoId = UserSession.getInstance().getTempEventoId();
        if (eventoId == 0) return;

        try {
            horaInicioEvento = juezDao.obtenerFechaHoraInicioEvento(eventoId);
            if (horaInicioEvento == null) {
                System.err.println("Advertencia: No se pudo obtener la hora de inicio del evento.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlertaError("Error BD al cargar horario.");
            // Si hay error, horaInicioEvento seguirá siendo null o la evaluación será bloqueada.
        }
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
                contenedorBotones.getChildren().clear();

                cargarEquipos(unicaCategoria);
            }
            // CASO B: Múltiples categorías (Generar Botones)
            else {
                lblCategoriaActual.setText("Selecciona una Categoría:");
                lblCategoriaActual.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold;");

                contenedorBotones.getChildren().clear();

                for (String cat : categorias) {
                    Button btn = new Button(cat);
                    btn.setStyle("-fx-background-color: white; -fx-text-fill: #2980b9; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);");
                    btn.setPrefWidth(150);

                    btn.setOnAction(e -> {
                        lblCategoriaActual.setText("Viendo: " + cat);
                        cargarEquipos(cat);

                        resetearEstilosBotones();
                        btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
                    });

                    contenedorBotones.getChildren().add(btn);
                }

                // Cargar la primera por defecto
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
        String categoria = btn.getText();

        lblCategoriaActual.setText("Viendo: " + categoria);

        cargarEquipos(categoria);
    }

    private void cargarEquipos(String categoria) {
        vboxListaEquipos.getChildren().clear();
        int eventoId = UserSession.getInstance().getTempEventoId();
        int juezId = UserSession.getInstance().getUserId();

        try {
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
        }
    }

    private HBox crearTarjetaEquipo(EquipoItem equipo) {
        HBox tarjeta = new HBox();
        tarjeta.setAlignment(Pos.CENTER_LEFT);
        tarjeta.setSpacing(15);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        tarjeta.setEffect(new DropShadow(5, Color.color(0,0,0,0.1)));

        // --- 1. Datos del Equipo ---
        VBox infoEquipo = new VBox(5);
        Label lblNombre = new Label(equipo.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        Label lblInst = new Label(equipo.getInstitucion());
        lblInst.setStyle("-fx-text-fill: #7f8c8d;");
        infoEquipo.getChildren().addAll(lblNombre, lblInst);
        infoEquipo.setPrefWidth(350);

        // --- 2. Indicador de Progreso Global ---
        Label lblProgreso = new Label("Jueces: " + equipo.getConteoJueces() + "/3");
        lblProgreso.setPrefWidth(120);

        if (equipo.getConteoJueces() >= 3) {
            lblProgreso.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            lblProgreso.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 14px;");
        }

        // --- 3. Estado Personal (Tu estado como juez) ---
        boolean yaEvaluado = "EVALUADO".equalsIgnoreCase(equipo.getEstado());
        Label lblEstado = new Label(yaEvaluado ? "TU ESTADO: LISTO" : "TU ESTADO: PENDIENTE");
        lblEstado.setPrefWidth(180);
        lblEstado.setStyle(yaEvaluado
                ? "-fx-text-fill: #7f8c8d; -fx-font-size: 11px;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- 4. Botón de Acción y Lógica de Deshabilitación por Hora ---
        Button btnAccion = new Button();
        boolean deshabilitadoPorTiempo = false;

        if (yaEvaluado) {
            btnAccion.setText("COMPLETADO");
            btnAccion.setDisable(true);
            btnAccion.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        } else {
            // Verificar si el evento aún no ha comenzado
            if (horaInicioEvento != null && LocalDateTime.now().isBefore(horaInicioEvento)) {
                deshabilitadoPorTiempo = true;

                long minutosRestantes = ChronoUnit.MINUTES.between(LocalDateTime.now(), horaInicioEvento);
                long horas = minutosRestantes / 60;
                long minutos = minutosRestantes % 60;

                btnAccion.setText(String.format("INICIA EN %d H %d M", horas, minutos));
                btnAccion.setDisable(true);
                btnAccion.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else {
                // Evaluación activa (tiempo cumplido)
                btnAccion.setText("EVALUAR");
                btnAccion.setDisable(false);
                btnAccion.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 5;");
                btnAccion.setOnAction(e -> irAEvaluar(equipo));
            }
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
        // 1. Guardar datos de contexto en Sesión
        UserSession session = UserSession.getInstance();
        session.setEquipoIdTemp(equipo.getId());
        session.setTempNombreEquipo(equipo.getNombre());

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
        } catch (IOException e) {
            // Muestra un error más claro en la consola sobre la fuente del problema
            System.err.println("\n*** ERROR CRÍTICO DE NAVEGACIÓN ***");
            System.err.println("Fallo al cargar la vista FXML: " + fxml);
            System.err.println("Causa más probable: 1) Nombre de archivo incorrecto; 2) Error de sintaxis en el FXML de destino.");
            System.err.println("***********************************\n");
        }
    }

    // Metodo auxiliar para cambiar vista desde un evento de código (no ActionEvent directo)
    private void cambiarVistaBoton(Node nodo, String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) nodo.getScene().getWindow();
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

    @FXML
    public void handleIrAlMenu(ActionEvent event) {
        cambiarVista(event, "juez_menu.fxml");
    }
}