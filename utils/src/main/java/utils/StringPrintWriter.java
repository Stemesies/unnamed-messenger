package utils;

public class StringPrintWriter {
    StringBuilder str = new StringBuilder();

    @Override
    public String toString() {
        return str.toString();
    }

    public void print(Object obj) {
        str.append(obj);
    }
    
    public void println(Object obj) {
        str.append(obj);
        str.append('\n');
    }
    
    public void println() {
        str.append('\n');
    }
    
    public void printf(String format, Object... args) {
        str.append(String.format(format, args));
    }
    
    public void printlnf(String format, Object... args) {
        str.append(String.format(format, args));
        str.append('\n');
    }

    public void clear() {
        str = new StringBuilder();
    }

    public boolean isEmpty() {
        return str.isEmpty();
    }
}
