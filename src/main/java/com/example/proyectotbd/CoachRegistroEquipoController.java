package com.example.proyectotbd;

import javafx.animation.PauseTransition; // Importante
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration; // Importante

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class CoachRegistroEquipoController {

    @FXML private TextField txtNombreEquipo;
    @FXML private Label lblMensaje;

    @FXML private Label lblEventoSeleccionado;
    @FXML private Label lblCategoriaSeleccionada;

    private String categoriaTexto = null;
    private int categoriaId = 0;

    // --- NUEVO: Temporizador para no saturar la BD mientras escribes ---
    private PauseTransition pause = new PauseTransition(Duration.millis(500));

    @FXML
    public void initialize() {
        mostrarEventoSeleccionado();
        recuperarDatosDeSesion();
        configurarValidacionNombre(); // Aquí está la magia nueva
    }

    private void mostrarEventoSeleccionado() {
        // CAMBIO CLAVE: Leer el nombre del evento de la sesión.
        String nombreEvento = UserSession.getInstance().getTempNombreEvento();
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (nombreEvento != null && eventoId != 0 && lblEventoSeleccionado != null) {
            // Muestra el nombre, y opcionalmente el ID entre paréntesis.
            lblEventoSeleccionado.setText("Inscribiendo a: " + nombreEvento);
        } else {
            lblEventoSeleccionado.setText("Error: Evento no preseleccionado.");
        }
    }

    private void recuperarDatosDeSesion() {
        UserSession session = UserSession.getInstance();
        if (session.getTempNombreEquipo() != null) txtNombreEquipo.setText(session.getTempNombreEquipo());
        if (session.getTempCategoriaNombre() != null) {
            this.categoriaTexto = session.getTempCategoriaNombre();
            this.categoriaId = session.getTempCategoriaId();
            lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
            lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
        }
    }

    @FXML
    public void handleCategoria(ActionEvent event) {
        Button btn = (Button) event.getSource();
        categoriaTexto = btn.getText();

        switch (categoriaTexto) {
            case "Primaria": categoriaId = 1; break;
            case "Secundaria": categoriaId = 2; break;
            case "Preparatoria": categoriaId = 3; break;
            case "Profesional": categoriaId = 4; break;
            default: categoriaId = 0;
        }

        lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");

        // Si cambia de categoría, revalidamos el nombre actual porque podría estar libre en esta nueva categoría
        if (!txtNombreEquipo.getText().isEmpty()) {
            verificarDisponibilidadEnBD(txtNombreEquipo.getText().trim());
        }
    }

    // =================================================================
    //  VALIDACIÓN EN TIEMPO REAL (Listener + DB Check)
    // =================================================================

    private void configurarValidacionNombre() {
        // Configurar qué hace el temporizador cuando termina el tiempo
        pause.setOnFinished(event -> {
            String nombre = txtNombreEquipo.getText().trim();
            if (!nombre.isEmpty() && categoriaId != 0) {
                verificarDisponibilidadEnBD(nombre);
            }
        });

        txtNombreEquipo.textProperty().addListener((observable, oldValue, newValue) -> {
            // 1. Validaciones básicas de formato (Frontend)
            if (newValue.length() > 50) {
                txtNombreEquipo.setText(oldValue);
                return;
            }

            // Reiniciamos estilos si borran todo
            if (newValue.isEmpty()) {
                txtNombreEquipo.setStyle("");
                lblMensaje.setVisible(false);
                return;
            }

            // Validar regex localmente
            boolean contienePalabra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*");
            if (!contienePalabra) {
                txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
                // No consultamos a la BD si el formato ya está mal
                return;
            } else {
                // Formato OK, quitamos borde rojo momentáneamente
                txtNombreEquipo.setStyle("");
            }

            // 2. Disparar el temporizador para consultar a la BD
            // Cada vez que escribes, reinicia la cuenta regresiva.
            pause.playFromStart();
        });
    }

    // Método que va a la Base de Datos (Solo Lectura)
    private void verificarDisponibilidadEnBD(String nombreEquipo) {
        if (categoriaId == 0) return; // No podemos verificar sin categoría

        String sql = "{? = call FN_VerificarDisponibilidadEquipo(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, nombreEquipo);
            stmt.setInt(3, categoriaId);

            stmt.execute();

            int existe = stmt.getInt(1); // 1 = Existe, 0 = Libre

            // Actualizamos la UI en el hilo principal de JavaFX
            Platform.runLater(() -> {
                if (existe > 0) {
                    // EL NOMBRE YA EXISTE
                    txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
                    lblMensaje.setText("⚠️ Este nombre ya está registrado en la categoría seleccionada.");
                    lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    lblMensaje.setVisible(true);
                } else {
                    // EL NOMBRE ESTÁ LIBRE
                    txtNombreEquipo.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px; -fx-border-radius: 5;");
                    lblMensaje.setText("✅ Nombre disponible.");
                    lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    lblMensaje.setVisible(true);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace(); // Error técnico (consola)
        }
    }

    // =================================================================

    @FXML
    public void handleContinuar(ActionEvent event) {
        // Validaciones Finales antes de cambiar de pantalla
        int eventoId = UserSession.getInstance().getTempEventoId();
        String nombre = txtNombreEquipo.getText().trim();

        if (eventoId == 0) { mostrarMensaje("Error: Evento no seleccionado.", true); return; }
        if (categoriaTexto == null) { mostrarMensaje("Selecciona una categoría.", true); return; }
        if (nombre.isEmpty()) { mostrarMensaje("Escribe el nombre del equipo.", true); return; }

        // Si el borde sigue rojo (por duplicado o regex), no dejamos pasar
        if (txtNombreEquipo.getStyle().contains("#e74c3c")) {
            mostrarMensaje("Corrige el nombre del equipo antes de continuar.", true);
            return;
        }

        // --- SI TODO ESTÁ BIEN: Guardamos en Sesión (Memoria) ---
        UserSession session = UserSession.getInstance();
        session.setTempEventoId(eventoId);
        session.setTempCategoriaId(categoriaId);
        session.setTempCategoriaNombre(categoriaTexto);
        session.setTempNombreEquipo(nombre);
        session.setTempInstitucion(session.getInstitucionUsuario());

        System.out.println("Validación correcta. Avanzando...");
        cambiarVista(event, "coach_registroIntegrantes.fxml");
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "coach_verEventos.fxml");
        UserSession.getInstance().setTempNombreEquipo(null);
        UserSession.getInstance().setTempCategoriaNombre(null);
        UserSession.getInstance().setTempCategoriaId(0);
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