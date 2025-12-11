package com.example.proyectotbd;

import com.example.proyectotbd.ConexionDB;
import javafx.animation.PauseTransition;
import javafx.application.Platform; // Importante para actualizar la UI desde un hilo
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
import java.sql.Types;
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

    // --- NUEVO: Temporizadores para validación en tiempo real (Debounce) ---
    private PauseTransition pauseNombre = new PauseTransition(Duration.millis(500));
    private PauseTransition pauseUsername = new PauseTransition(Duration.millis(500));
    // -----------------------------------------------------------------------

    // --- PATRONES DE VALIDACIÓN ---
    private static final Pattern PATRON_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$");
    private static final Pattern PATRON_USERNAME = Pattern.compile("^[a-zA-Z0-9._-]*$");
    private static final Pattern PATRON_PASSWORD_COMPLEJO = Pattern.compile("^(?=.*[0-9])(?=.*[A-Z])(?=.*[^a-zA-Z0-9\\s]).{8,}$");
    private static final Pattern PATRON_INSTITUCION = Pattern.compile("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s]*$");
    private static final Pattern PATRON_REPETICION = Pattern.compile("(.)\\1{2,}");


    @FXML
    public void initialize() {
        txtPasswordVisible.textProperty().bindBidirectional(txtPassword.textProperty());

        cargarInstituciones();
        configurarValidaciones();
        configurarRestriccionTeclasContrasena();

        // NUEVO: Configurar la lógica de validación asíncrona
        configurarValidacionEnTiempoReal();
    }

    // Método auxiliar para la lógica asíncrona (Thread + Platform.runLater)
    private void configurarValidacionEnTiempoReal() {
        // 1. Configuración de la validación de NOMBRE/USERNAME al soltar la tecla
        pauseNombre.setOnFinished(e -> {
            String nombre = txtNombre.getText().trim();
            String username = txtUsername.getText().trim();
            if (!nombre.isEmpty() && nombre.contains(" ") && esTextoLogico(nombre)) {
                verificarConflictoEnTiempoReal(nombre, username, txtNombre, "Nombre");
            }
        });

        // 2. Configuración de la validación de USERNAME al soltar la tecla
        pauseUsername.setOnFinished(e -> {
            String nombre = txtNombre.getText().trim();
            String username = txtUsername.getText().trim();
            if (!username.isEmpty()) {
                verificarConflictoEnTiempoReal(nombre, username, txtUsername, "Username");
            }
        });
    }


    // Lógica que corre en un hilo de fondo para consultar la BD
    private void verificarConflictoEnTiempoReal(String nombre, String username, TextField targetField, String fieldName) {
        String nombreCapitalizado = capitalizarTexto(nombre);

        // Validar que el nombre tenga al menos dos partes antes de consultar
        if (fieldName.equals("Nombre") && !nombre.contains(" ")) return;

        // Ejecutar la consulta en un hilo de fondo para no congelar la UI
        new Thread(() -> {
            try {
                // Llama a la misma función que el botón de guardar
                int conflicto = verificarConflictoGlobal(nombreCapitalizado, username);

                Platform.runLater(() -> {
                    if (conflicto == 1) {
                        targetField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;"); // Rojo: Conflicto
                        lblMensaje.setText("Error: El " + fieldName + " ya está en uso por un Participante.");
                        lblMensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        lblMensaje.setVisible(true);
                    } else {
                        // Si pasa el filtro de formato y el filtro de conflicto: OK
                        targetField.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2px;"); // Verde: Disponible
                        lblMensaje.setVisible(false);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    mostrarMensaje("Error de BD al validar en tiempo real: " + e.getMessage(), true);
                    targetField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px;");
                });
            }
        }).start();
    }
    // -----------------------------------------------------------------------


    private void cargarInstituciones() {
        try {
            ObservableList<String> lista = dao.obtenerInstituciones();
            cmbInstitucion.setItems(lista);
        } catch (SQLException e) {
            mostrarMensaje("Error al cargar instituciones: " + e.getMessage(), true);
        }
    }

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
    // LÓGICA DE VALIDACIÓN (Contiene llamadas a PauseTransition)
    // =====================================================================

    private void configurarValidaciones() {
        // Nombre
        txtNombre.textProperty().addListener((obs, oldV, newV) -> {
            if (newV.length() > 50) { txtNombre.setText(oldV); return; }
            if (!PATRON_NOMBRE.matcher(newV).matches()) {
                txtNombre.setText(oldV);
                txtNombre.setStyle("-fx-border-color: red;");
            } else {
                txtNombre.setStyle("");
                pauseNombre.playFromStart(); // <-- NUEVO: Inicia chequeo en tiempo real
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
                pauseUsername.playFromStart(); // <-- NUEVO: Inicia chequeo en tiempo real
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

    private boolean esTextoLogico(String texto) {
        String limpio = texto.trim();

        if (limpio.length() < 3) return false;

        boolean tieneLetra = limpio.matches(".*[a-zA-ZáéíóúÁÉÍÓÚñÑ].*");
        if (!tieneLetra) return false;

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


    // =====================================================================
    // VALIDACIÓN CRUZADA EN BD (Función Requerida: FN_VerificarConflictoGlobalUsuario)
    // =====================================================================

    /**
     * Llama a la función de la BD para verificar si el nombre del nuevo usuario (o username)
     * ya existe como nombre de un Participante, o viceversa.
     * * @param nombre Nombre completo (capitalizado) del nuevo usuario.
     * @param username Nombre de usuario del nuevo usuario.
     * @return 1 si hay conflicto, 0 si no.
     * @throws SQLException Si ocurre un error de BD.
     */
    private int verificarConflictoGlobal(String nombre, String username) throws SQLException {
        // La función FN_VerificarConflictoGlobalUsuario debe existir en la BD
        String sql = "{? = call FN_VerificarConflictoGlobalUsuario(?, ?)}";

        try (Connection conn = ConexionDB.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, nombre);
            stmt.setString(3, username);
            stmt.execute();

            return stmt.getInt(1); // 1 = Conflicto, 0 = OK
        } catch (SQLException e) {
            throw new SQLException("Fallo la validación global de nombres. Asegúrate de ejecutar FN_VerificarConflictoGlobalUsuario en la BD.", e);
        }
    }


    // =====================================================================
    // LÓGICA DE ACCIÓN (Botón Guardar)
    // =====================================================================


    @FXML
    public void handleRegresar(ActionEvent event) {
        cambiarVista(event, "organizador_menu.fxml");
    }

    @FXML
    public void handleGuardarUsuario(ActionEvent event) {
        String rawNombre = txtNombre.getText();
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

        // 3. Validación de Coherencia
        if (!esTextoLogico(rawNombre)) {
            mostrarMensaje("Error: El nombre parece inválido (letras repetidas o sin sentido).", true);
            txtNombre.setStyle("-fx-border-color: red;");
            return;
        }

        // 4. Validación de Contraseña
        if (password.length() < 8 || !PATRON_PASSWORD_COMPLEJO.matcher(password).matches()) {
            mostrarMensaje("La contraseña es insegura.", true);
            aplicarEstiloPassword("-fx-border-color: red;");
            analizarSeguridadPassword(password);
            return;
        }

        // 5. Validación de Institución
        if (!PATRON_INSTITUCION.matcher(institucion).matches() || !esTextoLogico(institucion)) {
            mostrarMensaje("Error: La institución es inválida.", true);
            cmbInstitucion.setStyle("-fx-border-color: red;");
            return;
        }

        String institucionFinal = capitalizarTexto(institucion);


        // ======================================================================
        // 6. VALIDACIÓN DE CONFLICTO DE INTERES CRUZADO (FINAL CHECK)
        // ======================================================================
        try {
            int conflicto = verificarConflictoGlobal(nombre, username);
            if (conflicto == 1) {
                mostrarMensaje("Error de Conflicto: El nombre o usuario ya existe como Participante en un equipo. No se permite la duplicación de nombres.", true);
                txtNombre.setStyle("-fx-border-color: red;");
                txtUsername.setStyle("-fx-border-color: red;");
                return;
            }
        } catch (SQLException e) {
            mostrarMensaje(e.getMessage(), true);
            return;
        }

        // ======================================================================
        // 7. REGISTRO FINAL
        // ======================================================================

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