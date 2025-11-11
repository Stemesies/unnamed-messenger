package elements;

public abstract class Group {
    private int id;
    private String groupName;
    private String name;
    private GroupTypes type;
    private int[] members;
    private int[] admins;
    private int owner;
    private int[] messages; // id-s сообщений группы

}
