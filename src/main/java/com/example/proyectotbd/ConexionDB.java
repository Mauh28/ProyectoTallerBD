package com.example.proyectotbd; // Ajusta a tu paquete real

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    // Configuración de la base de datos basada en tu script SQL
    private static final String URL = "jdbc:mysql://localhost:3306/concurso_robotica"; //
    private static final String USER = "root"; // Tu usuario de MySQL (usualmente root)
    private static final String PASSWORD = "12345"; // Tu contraseña de MySQL

    private static Connection connection = null;

    // metodo para hacer la conexion para base de datos
    public static Connection getConnection() {
        try {
            // Verificar si la conexión está cerrada o es nula para abrirla
            if (connection == null || connection.isClosed()) {
                // Cargar el driver (opcional en versiones nuevas de Java, pero recomendado)
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Establecer la conexión
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión exitosa a la base de datos 'concurso_robotica'");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver de MySQL.");
            // e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            // e.printStackTrace();
        }
        return connection;
    }

    // metodo para cerrar la conexion
    public static void cerrarConexion() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}