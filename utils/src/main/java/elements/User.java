package elements;

public abstract class User {
    private int id; // int - ?
    private String userName;
    private String name;
    private String password;
    private int[] friends; // массив id'шников друзей
//    public default User getUser()
    public abstract void sendMessage(String text);
    public abstract void joinGroup();

}
