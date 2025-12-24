package git.fsbteam.gui;

import elements.Client;
import elements.InputManager;
import elements.ServerConnectManager;
import elements.cli.ServerCommands;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ClientController implements Initializable {

    InputManager input = new InputManager();

    @FXML
    private Label titleText;

    @FXML
    private Label welcomeText;

    @FXML
    private TextField tf;

    @FXML
    private TextArea receivedMsg;

    @FXML
    private Button serverButton;

    public void setInput() {
        System.out.println("input: " + tf.getText());
        if (tf != null)
            this.input.setInput(tf.getText());
        else
            this.input.setInput(null);
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Добро пожаловать в мессенджер NeMax!");
    }

    @FXML
    public void showServerResult() {
        CompletableFuture.supplyAsync(() -> {
            Client.launch();
            return null;
        });
    }

    public static String getMsg() {
        return MSG.get();
    }

    public static void setMsg(String msg) {
        MSG.set(msg);
    }

    public static StringProperty msgProperty() {
        return MSG;
    }

    private static final StringProperty MSG = new SimpleStringProperty("");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerCommands.initGeneral();
//        System.out.println("text: " + tf.getText());
//        System.out.println("label: " + welcomeText.getText());

        // Если поле только для отображения (не может изменять значение MSG)
        receivedMsg.textProperty().bind(msgProperty());
        // Если через это поле можно менять значение MSG
//        tf.textProperty().bindBidirectional(MSGProperty());
        ServerConnectManager.addOutPutListener(ClientController::setMsg);
//        titleText.setText("NeMax");
    }

    private void finishInput() {
        input.processInput();
        tf.setText(null);
        setInput();
    }

    @FXML
    private Button sendBtn;

    @FXML
    public void onSend() {
        setInput();
        finishInput();
    }

    @FXML
    private Button register;

    @FXML
    private Button groupCreate;

    @FXML
    public void setRegister() {
        this.input.setInput("/register " + tf.getText());
        finishInput();
    }

    @FXML
    public void setGroup() {
        this.input.setInput("/groups create " + tf.getText());
        finishInput();
    }

    @FXML
    private Button openChatBtn;

    @FXML
    public void openChat() {
        this.input.setInput("/open " + tf.getText());
        finishInput();
    }
}

// module git.fsbteam.gui {
//    requires javafx.controls;
//    requires javafx.fxml;

//    requires org.kordamp.ikonli.javafx;

//    opens git.fsbteam.gui to javafx.fxml;
//    exports git.fsbteam.gui;
// }
