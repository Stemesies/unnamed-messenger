package gui;

import client.elements.OutputManager;
import client.elements.cli.ServerCommands;
import client.elements.Client;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import utils.StringPrintWriter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML
    private Label titleText;

    @FXML
    private Label welcomeText;

    @FXML
    private TextField tf;

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
        setInput("/retry");
    }

    public static String getMsg() {
        return MSG.get();
    }

    public static void setMsg(String msg) {
//        System.out.println("Output: " + msg);
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
                receivedMsg.getEngine().loadContent("<html><body style="
                        + "\"background-color: rgb(17, 147, 187); "
                                + "font-style: italic; color: white; overflow-y: scroll;"
                                + "overflow-x: scroll;\">"
                                + "<pre>" + msgProperty().getValue() + "</pre>"
                        + "</body></html>");
                var height = (int) receivedMsg.getEngine()
                        .executeScript("document.body.scrollHeight; document.body.scrollWidth");
                receivedMsg.setPrefHeight(height);
            }
        });

        OutputManager.addOutPutListener(ClientController::setMsg);
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
