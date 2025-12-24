package git.fsbteam.gui;

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
                FXMLLoader(classLoader.getResource("git/fsbteam/gui/client-template.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
