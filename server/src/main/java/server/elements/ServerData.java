package server.elements;

import java.util.ArrayList;

public class ServerData {
    public static final int MAX_USERNAME_LENGTH = 32;
    private static final ArrayList<User> registeredUsers = new ArrayList<>();
    private static final ArrayList<Group> registeredGroups = new ArrayList<>();
    private static final ArrayList<Client> clients = new ArrayList<>();

    public static ArrayList<Client> getClients() {
        return clients;
    }

    public static ArrayList<Group> getRegisteredGroups() {
        return registeredGroups;
    }

    public static ArrayList<User> getRegisteredUsers() {
        return registeredUsers;
    }

    public static void addUser(User user) {
        registeredUsers.add(user);
    }

    public static void addGroup(Group group) {
        registeredGroups.add(group);
    }
}
