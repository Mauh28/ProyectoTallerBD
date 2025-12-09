package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
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
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class OrganizadorCrearUsuarioController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtInstitucion;
    @FXML private TextField txtUsername;

    // --- CAMPOS DE CONTRASEÑA ---
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPasswordVisible; // Nuevo campo visible
    @FXML private Button btnVerPassword;        // Nuevo botón

    @FXML private CheckBox checkCoach;
    @FXML private CheckBox checkJuez;
    @FXML private Label lblMensaje;

    private boolean contrasenaVisible = false;

    // --- PATRONES DE VALIDACIÓN ---
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");
    private static final Pattern PATRON_USERNAME = Pattern.compile("^[a-zA-Z0-9._-]*$");
    private static final Pattern PATRON_PASSWORD_COMPLEJO = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$");

    @FXML
    public void initialize() {
        // Sincronizar el texto de ambos campos automáticamente
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        configurarValidaciones();
    }

    // --- LÓGICA PARA MOSTRAR/OCULTAR CONTRASEÑA ---
    @FXML
    public void handleTogglePassword(ActionEvent event) {
        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            // Mostrar texto, ocultar asteriscos
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);

            btnVerPassword.setText("🙈"); // Icono de ocultar
        } else {
            // Mostrar asteriscos, ocultar texto
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);

            btnVerPassword.setText("👁️"); // Icono de ver
        }
    }

    private void configurarValidaciones() {
        // 1. VALIDACIÓN NOMBRE
        txtNombre.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) {
                txtNombre.setText(oldV);
                return;
            }
            if (!PATRON_NOMBRE.matcher(newV).matches()) {
                txtNombre.setText(oldV);
                txtNombre.setStyle("-fx-border-color: red;");
            } else {
                txtNombre.setStyle("");
            }
        });

        // 2. VALIDACIÓN INSTITUCIÓN
        txtInstitucion.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) {
                txtInstitucion.setText(oldV);
                return;
            }
            if (!newV.isEmpty() && newV.matches("^[0-9]+$")) {
                txtInstitucion.setStyle("-fx-border-color: orange;");
            } else {
                txtInstitucion.setStyle("");
            }
        });

        // 3. VALIDACIÓN USERNAME
        txtUsername.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) {
                txtUsername.setText(oldV);
                return;
            }
            if (!PATRON_USERNAME.matcher(newV).matches()) {
                txtUsername.setText(oldV);
                txtUsername.setStyle("-fx-border-color: red;");
            } else {
                txtUsername.setStyle("");
            }
        });

        // 4. VALIDACIÓN PASSWORD (Se aplica al campo oculto, pero como están vinculados, afecta a ambos)
        txtPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) {
                txtPassword.setText(oldV); // Esto actualiza también txtPasswordVisible automáticamente
                return;
            }
            analizarSeguridadPassword(newV);
        });
    }

    private void analizarSeguridadPassword(String password) {
        if (password.isEmpty()) {
            lblMensaje.setVisible(false);
            resetearEstilosPassword();
            return;
        }

        boolean cumpleLongitud = password.length() >= 8;
        boolean cumpleMayuscula = password.matches(".*[A-Z].*");
        boolean cumpleNumero = password.matches(".*[0-9].*");
        boolean cumpleEspecial = password.matches(".*[^a-zA-Z0-9].*");

        if (cumpleLongitud && cumpleMayuscula && cumpleNumero && cumpleEspecial) {
            lblMensaje.setText("Contraseña Segura ✅");
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);
            aplicarEstiloPassword("-fx-border-color: #27ae60; -fx-border-width: 2px;");
        } else {
            StringBuilder faltantes = new StringBuilder("Faltan: ");
            if (!cumpleLongitud) faltantes.append("8 chars, ");
            if (!cumpleMayuscula) faltantes.append("Mayúscula, ");
            if (!cumpleNumero) faltantes.append("Número, ");
            if (!cumpleEspecial) faltantes.append("Símbolo, ");

            String msg = faltantes.substring(0, faltantes.length() - 2);
            lblMensaje.setText(msg);
            lblMensaje.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);
            aplicarEstiloPassword("-fx-border-color: #e67e22;");
        }
    }

    // Método auxiliar para aplicar estilo (borde rojo/verde) al campo que esté visible en ese momento
    private void aplicarEstiloPassword(String estilo) {
        // Aplicamos el estilo base + el borde
        String estiloBase = "-fx-background-color: #f4f6f8; -fx-border-radius: 5; ";
        txtPassword.setStyle(estiloBase + estilo);
        txtPasswordVisible.setStyle(estiloBase + estilo);
    }

    private void resetearEstilosPassword() {
        String estiloNormal = "-fx-background-color: #f4f6f8; -fx-border-color: #bdc3c7; -fx-border-radius: 5;";
        txtPassword.setStyle(estiloNormal);
        txtPasswordVisible.setStyle(estiloNormal);
    }

    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarUsuario(ActionEvent event) {
        // Obtenemos password de cualquiera de los dos (están sincronizados)
        String rawNombre = txtNombre.getText();
        String institucion = txtInstitucion.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        boolean esCoach = checkCoach.isSelected();
        boolean esJuez = checkJuez.isSelected();

        if (rawNombre.isEmpty() || institucion.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Error: Por favor llena todos los campos.", true);
            return;
        }

        if (!esCoach && !esJuez) {
            mostrarMensaje("Error: Debes seleccionar al menos un rol.", true);
            return;
        }

        String nombre = capitalizarTexto(rawNombre);
        if (!nombre.contains(" ")) {
            mostrarMensaje("Error: Ingresa nombre y apellido completo.", true);
            txtNombre.setStyle("-fx-border-color: red;");
            return;
        }

        if (institucion.matches("^[0-9]+$")) {
            mostrarMensaje("Error: La institución debe ser un nombre válido.", true);
            txtInstitucion.setStyle("-fx-border-color: red;");
            return;
        }

        // Validación final de contraseña
        if (password.length() < 8 || !PATRON_PASSWORD_COMPLEJO.matcher(password).matches()) {
            mostrarMensaje("La contraseña es insegura. Revisa los requisitos.", true);
            aplicarEstiloPassword("-fx-border-color: red;");
            analizarSeguridadPassword(password);
            return;
        }

        String sql = "{call SP_registrarUsuario(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, nombre);
            stmt.setString(4, institucion);
            stmt.setBoolean(5, esCoach);
            stmt.setBoolean(6, esJuez);

            stmt.execute();

            mostrarNotificacionExito("¡Usuario " + username + " registrado correctamente!");
            cambiarVista(event, "organizador_menu.fxml");

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensaje("Error BD: " + e.getMessage(), true);
        }
    }

    private String capitalizarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        String[] palabras = texto.trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1).toLowerCase());
                }
                resultado.append(" ");
            }
        }
        return resultado.toString().trim();
    }

    private void mostrarNotificacionExito(String mensaje) {
        try {
            Stage toastStage = new Stage();
            toastStage.initStyle(StageStyle.TRANSPARENT);
            toastStage.setAlwaysOnTop(true);
            Label label = new Label("✅ " + mensaje);
            label.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 20px; -fx-background-radius: 10px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
        if (!esError) {
            txtNombre.setStyle("");
            txtInstitucion.setStyle("");
            txtUsername.setStyle("");
            resetearEstilosPassword();
        }
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