package gui;

import client.elements.cli.ServerCommands;
import client.elements.Client;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import utils.StringPrintWriter;
import utils.elements.ClientTypes;
import client.elements.ServerConnectManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ClientController implements Initializable {

    @FXML
    private Label titleText;

    @FXML
    private Label welcomeText;

    @FXML
    private TextField tf;

//    @FXML
//    private TextArea receivedMsg;

    @FXML
    private WebView receivedMsg;

    @FXML
    private Button serverButton;

    public void setInput(String input) {

        if (tf != null) {
            System.out.println("input: " + input);
            Client.input.processInput(input);
            tf.setText(null);
        }

    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Добро пожаловать в мессенджер NeMax!");
    }

    @FXML
    public void showServerResult() {
        CompletableFuture.supplyAsync(() -> {
            Client.launch(ClientTypes.GUI);
            return null;
        });
    }

    public static String getMsg() {
        return MSG.get();
    }

    public static void setMsg(String msg) {
        System.out.println("Output: " + msg);
        output.println(msg + "<br>");
        MSG.set(output.toString());
    }

    public static StringProperty msgProperty() {
        return MSG;
    }

    private static final StringProperty MSG = new SimpleStringProperty("");
    private static final StringPrintWriter output = new StringPrintWriter();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerCommands.initGeneral();

        receivedMsg.getEngine().loadContent("some content");

        receivedMsg.getEngine().getLoadWorker().stateProperty().addListener((
                obs, oldVal, newVal) -> {
            if (newVal == Worker.State.SUCCEEDED) {
                receivedMsg.getEngine().loadContent(
                        "<html><body style=\"background-color: rgb(17, 147, 187); "
                                + "font-family: Segoe UI; text-fill: rgb(142, 237, 137)\">"
                                + msgProperty().getValue() + "</body></html>");
            }
        });
//        receivedMsg.accessibleTextProperty().bindBidirectional(msgProperty());

        // Если поле только для отображения (не может изменять значение MSG)
//        receivedMsg.textProperty().bind(msgProperty());
        // Если через это поле можно менять значение MSG
//        tf.textProperty().bindBidirectional(MSGProperty());
        ServerConnectManager.addOutPutListener(ClientController::setMsg);
    }

    @FXML
    private Button sendBtn;

    @FXML
    public void onSend() {
        setInput(tf.getText());
    }

    @FXML
    private Button register;

    @FXML
    private Button groupCreate;

    @FXML
    private Button openChatBtn;

    @FXML
    private Button inviteBtn;

    @FXML
    private Button helpBtn;

    @FXML
    private Button logInBtn;

    @FXML
    public void setRegister() {
        setInput("/register " + tf.getText());
    }

    @FXML
    public void setGroup() {
        setInput("/groups create " + tf.getText());
    }

    @FXML
    public void openChat() {
        setInput("/open " + tf.getText());
    }

    @FXML
    public void inviteToGroup() {
        setInput("/groups invite " + tf.getText());
    }

    @FXML
    public void showHelp() {
        var command = "/help ";
        var text = tf.getText();
        if (text != null)
            setInput(command + text);
        else
            setInput(command);
    }

    @FXML
    public void onLogIn() {
        setInput("/login " + tf.getText());
    }
}
