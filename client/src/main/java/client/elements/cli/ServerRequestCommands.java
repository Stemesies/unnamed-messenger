package client.elements.cli;

import client.elements.Client;
import client.elements.InputManager;
import client.elements.ServerConnectManager;
import utils.Ansi;
import utils.cli.CommandProcessor;

public class ServerRequestCommands {
    public static final CommandProcessor processor = new CommandProcessor();

    public static void init() {
        processor.register("request", (a) -> a
            .subcommand("type", (b) -> b
                .executes(() -> {
                    ServerConnectManager.socket.sendln("/response type " + Client.getType());
                })
            )
        );
        processor.register("ask", (a) -> a
            .subcommand("deletion", (b) -> b
                .requireArgument("groupname")
                .requireArgument("name")
                .executes((ctx) -> {
                    System.out.println(Ansi.applyStyle(
                        "Are you sure you want to delete \""
                            + ctx.getString("name")
                            + "\"? (Y/n)",
                        Ansi.Colors.RED
                    ));
                    InputManager.interceptNextLine((it) -> {
                        if (it.equalsIgnoreCase("y")) {
                            ServerConnectManager.socket.sendln(
                                "/confirm deletion " + ctx.getString("groupname")
                            );
                        } else {
                            System.out.println("Dismissed deletion.");
                        }
                    });
                })
            )
            .subcommand("exit_group", (b) -> b
                .requireArgument("groupname")
                .requireArgument("name")
                .executes((ctx) -> {
                    System.out.println(Ansi.applyStyle(
                        "Are you sure you want to exit group \""
                            + ctx.getString("name")
                            + "\"? (Y/n)",
                        Ansi.Colors.RED
                    ));
                    InputManager.interceptNextLine((it) -> {
                        if (it.equalsIgnoreCase("y")) {
                            ServerConnectManager.socket.sendln(
                                "/confirm exit_group " + ctx.getString("groupname")
                            );
                        } else {
                            System.out.println("Dismissed exit.");
                        }
                    });
                })
            )
        );
    }
}
