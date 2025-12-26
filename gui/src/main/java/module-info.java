module guifx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires clientmodule;
    requires utilsmodule;
    requires java.desktop;

    opens gui to javafx.fxml;
    exports gui;
}
