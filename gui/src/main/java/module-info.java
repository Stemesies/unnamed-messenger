module guifx {
    requires javafx.controls;
    requires javafx.fxml;
    requires clientmodule;
    requires utilsmodule;

    opens gui to javafx.fxml;
    exports gui;
}