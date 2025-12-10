package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @FXML private TextField txtPasswordVisible;
    @FXML private Button btnVerPassword;

    @FXML private CheckBox checkCoach;
    @FXML private CheckBox checkJuez;
    @FXML private Label lblMensaje;

    // --- ComboBox para Institución ---
    @FXML private ComboBox<String> cmbInstitucion;

    private boolean contrasenaVisible = false;
    private OrganizadorDAO dao = new OrganizadorDAO();

    // --- PATRONES DE VALIDACIÓN ---
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");
    private static final Pattern PATRON_USERNAME = Pattern.compile("^[a-zA-Z0-9._-]*$");
    private static final Pattern PATRON_PASSWORD_COMPLEJO = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).{8,}$");
    private static final Pattern PATRON_INSTITUCION = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]*$");

    // --- NUEVO: Patrón para detectar caracteres repetidos excesivos (ej: "aaa") ---
    private static final Pattern PATRON_REPETICION = Pattern.compile("(.)\\1{2,}");


    @FXML
    public void initialize() {
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        // 1. Cargar Instituciones al ComboBox
        cargarInstituciones();

        configurarValidaciones();
        configurarRestriccionTeclasContrasena();
    }

    private void cargarInstituciones() {
        try {
            ObservableList<String> lista = dao.obtenerInstituciones();
            cmbInstitucion.setItems(lista);
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar instituciones: " + e.getMessage(), true);
        }
    }

    // =====================================================================
    // FUNCIÓN: Bloquear Barra Espaciadora y permitir Backspace
    // =====================================================================

    private void configurarRestriccionTeclasContrasena() {
        // Bloquear SPACE en campo oculto
        txtPassword.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume();
            }
        });

        // Bloquear SPACE en campo visible
        txtPasswordVisible.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (event.getCharacter().equals(" ")) {
                event.consume();
            }
        });

        // Bloqueo para evitar que se PEGUEN espacios iniciales/finales
        txtPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && (newV.startsWith(" ") || newV.endsWith(" "))) {
                txtPassword.setText(newV.trim());
            }
        });
    }

    // =====================================================================
    // LÓGICA DE VALIDACIÓN
    // =====================================================================

    @FXML
    public void handleTogglePassword(ActionEvent event) {
        contrasenaVisible = !contrasenaVisible;

        if (contrasenaVisible) {
            txtPasswordVisible.setVisible(true);
            txtPasswordVisible.setManaged(true);
            txtPassword.setVisible(false);
            txtPassword.setManaged(false);
            btnVerPassword.setText("🙈");
        } else {
            txtPasswordVisible.setVisible(false);
            txtPasswordVisible.setManaged(false);
            txtPassword.setVisible(true);
            txtPassword.setManaged(true);
            btnVerPassword.setText("👁️");
        }
    }

    private void configurarValidaciones() {
        // Nombre
        txtNombre.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) { txtNombre.setText(oldV); return; }
            if (!PATRON_NOMBRE.matcher(newV).matches()) {
                txtNombre.setText(oldV);
                txtNombre.setStyle("-fx-border-color: red;");
            } else {
                txtNombre.setStyle("");
            }
        });

        // --- VALIDACIÓN INSTITUCIÓN (Adaptada para ComboBox) ---
        cmbInstitucion.getEditor().textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) {
                cmbInstitucion.getEditor().setText(oldV);
                return;
            }
            if (!PATRON_INSTITUCION.matcher(newV).matches()) {
                cmbInstitucion.getEditor().setText(oldV);
                cmbInstitucion.setStyle("-fx-border-color: red;");
            } else {
                cmbInstitucion.setStyle("");
            }
        });

        // Username
        txtUsername.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) { txtUsername.setText(oldV); return; }
            if (!PATRON_USERNAME.matcher(newV).matches()) {
                txtUsername.setText(oldV);
                txtUsername.setStyle("-fx-border-color: red;");
            } else {
                txtUsername.setStyle("");
            }
        });

        // Password
        txtPassword.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) { txtPassword.setText(oldV); return; }
            if (newV.contains(" ")) {
                txtPassword.setText(newV.replaceAll(" ", ""));
                return;
            }
            analizarSeguridadPassword(newV);
        });
    }

    // --- NUEVO: MÉTODO DE VALIDACIÓN LÓGICA (COHERENCIA) ---
    private boolean esTextoLogico(String texto) {
        String limpio = texto.trim();

        // 1. Longitud mínima de 3 caracteres
        if (limpio.length() < 3) return false;

        // 2. Debe contener al menos una letra (no solo números "123")
        boolean tieneLetra = limpio.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
        if (!tieneLetra) return false;

        // 3. No tener 3 caracteres idénticos seguidos ("Juaaan")
        if (PATRON_REPETICION.matcher(limpio).find()) return false;

        return true;
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
        boolean cumpleEspecial = password.matches(".*[^a-zA-Z0-9\\s].*");

        if (cumpleLongitud && cumpleMayuscula && cumpleNumero && cumpleEspecial) {
            lblMensaje.setText("Contraseña Segura ✅");
            lblMensaje.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);
            aplicarEstiloPassword("-fx-border-color: #27ae60; -fx-border-width: 2px;");
        } else {
            StringBuilder faltantes = new StringBuilder("La contraseña necesita: ");
            if (!cumpleLongitud) faltantes.append("8 caracteres, ");
            if (!cumpleMayuscula) faltantes.append("1 Mayúscula, ");
            if (!cumpleNumero) faltantes.append("1 Número, ");
            if (!cumpleEspecial) faltantes.append("1 Símbolo, ");

            String msg = faltantes.substring(0, faltantes.length() - 2);
            lblMensaje.setText(msg);
            lblMensaje.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            lblMensaje.setVisible(true);
            aplicarEstiloPassword("-fx-border-color: #e67e22;");
        }
    }

    private void aplicarEstiloPassword(String estilo) {
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
        String rawNombre = txtNombre.getText();

        // Obtener texto del ComboBox
        String institucion = cmbInstitucion.getEditor().getText().trim();

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        boolean esCoach = checkCoach.isSelected();
        boolean esJuez = checkJuez.isSelected();

        // 1. Validaciones Básicas (Vacío)
        if (rawNombre.isEmpty() || institucion.isEmpty() || username.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Error: Por favor llena todos los campos.", true);
            return;
        }

        if (!esCoach && !esJuez) {
            mostrarMensaje("Error: Debes seleccionar al menos un rol.", true);
            return;
        }

        String nombre = capitalizarTexto(rawNombre);

        // 2. Validación de Nombre Completo
        if (!nombre.contains(" ")) {
            mostrarMensaje("Error: Ingresa nombre y apellido completo.", true);
            txtNombre.setStyle("-fx-border-color: red;");
            return;
        }

        // --- NUEVO: Validación de Coherencia en Nombre ---
        if (!esTextoLogico(rawNombre)) {
            mostrarMensaje("Error: El nombre parece inválido (letras repetidas o sin sentido).", true);
            txtNombre.setStyle("-fx-border-color: red;");
            return;
        }

        // 3. Validación de Contraseña
        if (password.length() < 8 || !PATRON_PASSWORD_COMPLEJO.matcher(password).matches()) {
            mostrarMensaje("La contraseña es insegura.", true);
            aplicarEstiloPassword("-fx-border-color: red;");
            analizarSeguridadPassword(password);
            return;
        }

        // 4. Validación de Caracteres en Institución
        if (!PATRON_INSTITUCION.matcher(institucion).matches()) {
            mostrarMensaje("Error: La institución contiene caracteres inválidos.", true);
            cmbInstitucion.setStyle("-fx-border-color: red;");
            return;
        }

        // --- NUEVO: Validación de Coherencia en Institución ---
        if (!esTextoLogico(institucion)) {
            mostrarMensaje("Error: La institución no es válida (muy corta, solo números o repetitiva).", true);
            cmbInstitucion.setStyle("-fx-border-color: red;");
            return;
        }

        // 5. Normalización
        String institucionFinal = capitalizarTexto(institucion);

        String sql = "{call SP_registrarUsuario(?, ?, ?, ?, ?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, nombre);
            stmt.setString(4, institucionFinal);
            stmt.setBoolean(5, esCoach);
            stmt.setBoolean(6, esJuez);

            stmt.execute();

            mostrarNotificacionExito("¡Usuario " + username + " registrado correctamente!");
            cambiarVista(event, "organizador_menu.fxml");

        } catch (SQLException e) {
            mostrarMensaje(e.getMessage(), true);
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
        } catch (Exception e) {  e.printStackTrace(); }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
        lblMensaje.setVisible(true);
        if (!esError) {
            txtNombre.setStyle("");
            if (cmbInstitucion != null) cmbInstitucion.setStyle("");
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
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Error navegando a: " + fxml);
            alert.show();
        }
    }
}