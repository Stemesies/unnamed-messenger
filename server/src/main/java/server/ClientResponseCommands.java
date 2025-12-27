package server;

import server.elements.Client;
import server.elements.ClientStates;
import server.elements.Group;
import utils.cli.CustomCommandProcessor;
import utils.elements.ClientTypes;

public class ClientResponseCommands {
    public static class ClientContextData {
        public Client client;

        public ClientContextData(Client client) {
            this.client = client;
        }
    }

    public static final CustomCommandProcessor<ClientContextData> processor
        = new CustomCommandProcessor<>();

    public static void init() {
        processor.register("response", (a) -> a
            .subcommand("type", (b) -> b
                .requireArgument("type")
                .executes((ctx) -> {
                    ctx.data.client.state = ClientStates.Fine;
                    ctx.data.client.type = ClientTypes.valueOf(ctx.getString("type"));
                    System.out.println(
                        "Received type from client "
                            + ctx.data.client + ": " + ctx.getString("type")
                    );
                })
            ));
        processor.register("confirm", (a) -> a
            .subcommand("deletion", (b) -> b
                .requireArgument("groupname")
                .executes((ctx) -> {
                    Group.deleteGroup(
                        ctx.out,
                        ctx.getString("groupname")
                    );
                })
            )
            .subcommand("exit_group", (b) -> b
                .requireArgument("groupname")
                .executes((ctx) -> {
                    ctx.data.client.group = null;
                    var success = Group.removeMember(
                        ctx.out,
                        ctx.getString("groupname"),
                        ctx.data.client.user.getUserName()
                    );
                    if (success) {
                        ctx.out.println(
                            "You successfully left the group."
                        );
                    } else {
                        ctx.out.println(
                            "Something went wrong!"
                        );
                    }
                })
            )
        );
    }
}
