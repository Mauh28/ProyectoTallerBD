module com.example.proyectotbd {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    // AGREGA ESTAS LÍNEAS:
    requires java.sql;             // Para usar Connection, DriverManager, etc.
    requires mysql.connector.j;
    requires com.jfoenix;    // Para permitir el acceso al driver de MySQL

    opens com.example.proyectotbd to javafx.fxml;
    exports com.example.proyectotbd;
}