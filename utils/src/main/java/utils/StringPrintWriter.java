package utils;

/**
 * Класс предназначен для упрощения работы со строкой,
 * представляя ее редактирование в привычном формате записи,
 * который используется, например, в {@link System#out}.
 * <br>Проще говоря, обертка {@link StringBuilder},
 * использующая методы {@link System#out}
 *
 * <pre><code>
 *     sb.append("totalAmount: ");
 *     sb.append(totalAmount);
 *     sb.append("\n");
 *     sb.append("also: %s\n".formatted(also));
 *     ==== Эквивалентно ====
 *     spw.print("totalAmount: ");
 *     spw.println(totalAmount);
 *     sb.printlnf("also: %s", also);
 * </code></pre>
 */
public class StringPrintWriter {
    StringBuilder str = new StringBuilder();

    @Override
    public String toString() {
        return str.toString();
    }

    public void print(Object... obj) {
        str.append(obj[0]);
        for (int i = 1; i < obj.length; i++) {
            str.append(' ');
            str.append(obj[i]);
        }
    }

    public void println(Object... obj) {
        print(obj);
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
