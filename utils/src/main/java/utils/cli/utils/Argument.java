package utils.cli.utils;

public class Argument {
    public String name;
    public boolean isOptional;
    public boolean isArray;

    public Argument(String name, boolean isOptional) {
        this.name = name;
        this.isOptional = isOptional;
        this.isArray = false;
    }

    public Argument(String name, boolean isOptional, boolean isArray) {
        this.name = name;
        this.isOptional = isOptional;
        this.isArray = isArray;
    }

    @Override
    public String toString() {
        if (isOptional)
            return "[" + name + "]";
        else
            return "<" + name + ">";
    }
}
