package server.elements;

import utils.extensions.CollectionExt;

import java.util.ArrayList;

public class ServerData {
    public static final int MAX_USERNAME_LENGTH = 32;
    @Deprecated
    private static final ArrayList<User> registeredUsers = new ArrayList<>();
    @Deprecated
    private static final ArrayList<Group> registeredGroups = new ArrayList<>();

    private static final ArrayList<Client> clients = new ArrayList<>();
    private static final ArrayList<Client> registeredClients = new ArrayList<>();

    public static ArrayList<Client> getClients() {
        return clients;
    }

    public static ArrayList<Client> getRegisteredClients() {
        return registeredClients;
    }

    public static Client findClient(String username) {
        return CollectionExt.findBy(
            ServerData.getRegisteredClients(),
            (it) -> it.user.getUserName().equals(username)
        );
    }

    public static Client findClient(int id) {
        return CollectionExt.findBy(
            ServerData.getRegisteredClients(),
            (it) -> it.user.getId() == id
        );
    }

    @Deprecated
    public static ArrayList<User> getRegisteredUsers() {
        return registeredUsers;
    }

    @Deprecated
    public static void addGroup(Group group) {
        registeredGroups.add(group);
    }
}
