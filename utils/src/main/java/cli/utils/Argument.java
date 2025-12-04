package cli.utils;

public class Argument {
    public String name;
    public boolean isOptional;

    public Argument(String name, boolean isOptional) {
        this.name = name;
        this.isOptional = isOptional;
    }

    @Override
    public String toString() {
        if (isOptional)
            return "[" + name + "]";
        else
            return "<" + name + ">";
    }
}