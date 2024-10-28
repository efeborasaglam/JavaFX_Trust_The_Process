module ost {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.codec;
    requires java.sql;
    requires jbcrypt;

    opens ost to javafx.fxml;
    exports ost;
}