module org.btet.btet {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.sql;
    requires java.desktop;


    opens org.btet.enums to javafx.fxml;
    exports org.btet.enums;
    opens org.btet.model to javafx.fxml;
    exports org.btet.model;
    opens org.btet.controller to javafx.fxml;
    exports org.btet.app;
    opens org.btet.app to javafx.fxml;
    exports org.btet.util;
    opens org.btet.util to javafx.fxml;
}