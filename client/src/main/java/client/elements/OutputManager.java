package client.elements;

import utils.Ansi;
import utils.elements.ClientTypes;
import utils.kt.Apply;

import java.util.ArrayList;
import java.util.List;

public class OutputManager {

    private static final List<Apply<String>> outputListeners = new ArrayList<>();

    public static void addOutPutListener(Apply<String> listener) {
        outputListeners.add(listener);
    }

    public static void print(String message) {
        var msg = message.replace("\n", "<br>");
        if (Client.getType() == ClientTypes.GUI) {
            System.out.println(msg);
            outputListeners.forEach(it -> it.run(msg));
//            System.out.println("printing to GUI");
        } else {
            outputListeners.forEach(it -> it.run(message));
//            System.out.println("printing to CONSOLE");
        }
    }

    public static void println(String message) {
        print(message + '\n');
    }

    public static void stylePrint(String message, Ansi style) {
        if (Client.getType() == ClientTypes.CONSOLE) {
            print(Ansi.applyStyle(message, style));
        } else {
            print(Ansi.applyHtml(message, style));
        }
    }

    public static void stylePrintln(String message, Ansi style) {
        stylePrint(message + '\n', style);
    }
}
