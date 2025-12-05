import elements.Client;
import elements.Group;
import elements.User;

public class ServerCommands {

    public static class ServerContextData {
        public Client client;
        public User user;
        public Group group;

        public ServerContextData(Client client, User user, Group group) {
            this.client = client;
            this.user = user;
            this.group = group;
        }
    }

}
