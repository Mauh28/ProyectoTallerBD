package com.example.proyectotbd;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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
    private OrganizadorDAO dao = new OrganizadorDAO(); // se mantiene pero no se utiliza en el codigo

    @FXML
    public void initialize() {
        mostrarEventoSeleccionado();
        recuperarDatosDeSesion();
        configurarValidacionNombre();
    }

    // Muestra el nombre del evento seleccionado, obtenido desde la sesión temporal.
    private void mostrarEventoSeleccionado() {
        int eventoId = UserSession.getInstance().getTempEventoId();

        if (eventoId != 0 && lblEventoSeleccionado != null) {
            lblEventoSeleccionado.setText("Inscribiendo al Evento ID: " + eventoId);
        } else {
            lblEventoSeleccionado.setText("Error: Evento no preseleccionado.");
        }
    }

    private void recuperarDatosDeSesion() {
        UserSession session = UserSession.getInstance();

        // 1. Restaurar Nombre de Equipo
        if (session.getTempNombreEquipo() != null) txtNombreEquipo.setText(session.getTempNombreEquipo());

        // 2. Restaurar Categoría (Visual y Lógica)
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

        // Mapeo ID (Asegúrate de que coincida con tu BD)
        switch (categoriaTexto) {
            case "Primaria": categoriaId = 1; break;
            case "Secundaria": categoriaId = 2; break;
            case "Preparatoria": categoriaId = 3; break;
            case "Profesional": categoriaId = 4; break;
            default: categoriaId = 0;
        }

        lblCategoriaSeleccionada.setText("Seleccionada: " + categoriaTexto);
        lblCategoriaSeleccionada.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-style: italic;");
    }

    @FXML
    public void handleContinuar(ActionEvent event) {
        // 1. Obtener datos de la vista y sesión
        int eventoId = UserSession.getInstance().getTempEventoId(); // (Ya debiste seleccionarlo antes)
        String nombre = txtNombreEquipo.getText().trim();

        // 2. Validaciones Visuales
        if (eventoId == 0) {
            mostrarMensaje("Error: Debe seleccionar un evento en la vista anterior.", true);
            return;
        }
        if (categoriaTexto == null) {
            mostrarMensaje("Selecciona una categoría.", true);
            return;
        }
        if (nombre.isEmpty()) {
            mostrarMensaje("Escribe el nombre del equipo.", true);
            return;
        }

        // Validación Regex (Al menos una palabra real)
        if (!nombre.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*")) {
            mostrarMensaje("El nombre debe contener al menos una palabra real (mínimo 2 letras seguidas).", true);
            txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            return;
        }

        // 3. VALIDACIÓN EN BD (SOLO LECTURA)
        // Usamos la FUNCIÓN 'FN_VerificarDisponibilidadEquipo' en lugar del Procedimiento.
        // Esta función retorna 1 si existe, 0 si está libre. NO INSERTA NADA.
        String sql = "{? = call FN_VerificarDisponibilidadEquipo(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Configurar parámetros
            stmt.registerOutParameter(1, Types.INTEGER); // El retorno de la función
            stmt.setString(2, nombre);      // El nombre que queremos checar
            stmt.setInt(3, categoriaId);    // La categoría

            stmt.execute();

            int existe = stmt.getInt(1); // 1 = Ocupado, 0 = Libre

            if (existe > 0) {
                mostrarMensaje("El nombre del equipo ya existe en esta categoría. Elige otro.", true);
                txtNombreEquipo.setStyle("-fx-border-color: #e74c3c;");
                return;
            }

            // 4. SI ESTÁ LIBRE: Guardamos en Sesión (Memoria) y avanzamos
            // NO insertamos en la BD todavía.
            UserSession session = UserSession.getInstance();

            session.setTempEventoId(eventoId);
            session.setTempCategoriaId(categoriaId);
            session.setTempCategoriaNombre(categoriaTexto);
            session.setTempNombreEquipo(nombre);

            // Asignamos la institución del perfil del usuario (Coach)
            session.setTempInstitucion(session.getInstitucionUsuario());

            System.out.println("Validación correcta. Nombre disponible en memoria. Pasando a integrantes...");

            // Cambiamos de pantalla
            cambiarVista(event, "coach_registroIntegrantes.fxml");

        } catch (SQLException e) {
            // e.printStackTrace();
            mostrarMensaje("Error de conexión al validar nombre: " + e.getMessage(), true);
        }
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

    private void configurarValidacionNombre() {
        txtNombreEquipo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 50) {
                txtNombreEquipo.setText(oldValue);
                return;
            }

            boolean contienePalabra = newValue.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ]{2,}.*");

            if (!newValue.isEmpty() && !contienePalabra) {
                txtNombreEquipo.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 5;");
            } else {
                txtNombreEquipo.setStyle("");
            }
        });
    }
}