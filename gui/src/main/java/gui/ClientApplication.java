package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        var classLoader = Thread.currentThread().getContextClassLoader();
        FXMLLoader fxmlLoader = new
                FXMLLoader(classLoader.getResource("gui/client-template.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 800);
        stage.setTitle("NeMax");
        stage.setScene(scene);
        stage.show();
    }
}
