package server;

import server.elements.Client;
import server.elements.ClientStates;
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
                })
            ));
    }
}
