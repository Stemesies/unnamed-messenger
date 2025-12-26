package server.elements;

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

    @Deprecated
    public static ArrayList<Group> getRegisteredGroups() {
        return registeredGroups;
    }

    @Deprecated
    public static ArrayList<User> getRegisteredUsers() {
        return registeredUsers;
    }

    @Deprecated
    public static void addUser(User user) {
        registeredUsers.add(user);
    }

    @Deprecated
    public static void addGroup(Group group) {
        registeredGroups.add(group);
    }
}
