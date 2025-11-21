/**
 * Небольшая библиотека, позволяющая стилизовать вывод программы.
 *
 * <br>Для окраски подойдет метод {@link Ansi#applyStyle(Object, Ansi)}.
 * <br>Все доступные стили раскиданы по категориям:
 * <ul>
 *     <li> {@link Ansi.Modes} - содержит все "режимы шрифта"
 *     <li> {@link Ansi.Colors} - содержит все цвета текста
 *     <ul>
 *         <li> {@link Ansi.Colors.Bright} - содержит все яркие цвета текста
 *     </ul>
 *     <li> {@link Ansi.BgColors} - содержит все цвета заднего фона
 *     <ul>
 *         <li> {@link Ansi.BgColors.Bright} - содержит все яркие цвета заднего фона
 *     </ul>
 * </ul>
 *
 * <br>Пример использования:<br>
 * <pre><code>
 *     println("amongus! " + Ansi.applyStyle("i'm sus", Ansi.Colors.RED) + " no u")
 * </code></pre><br>
 * Конечный результат:<br>
 * amongus! <span style="color:#ff0000;">i'm sus</span> no u
 * <br>
 * <br> Подробнее о стилях можно почитать <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">здесь</a>
 */

@SuppressWarnings("unused")
public class Ansi {
    private final String style;

    private Ansi(String style) {
        this.style = style;
    }

    /**
     * Создает новый экземпляр ANSI со стилем style.
     * <br> Подробнее о стилях можно почитать <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">здесь</a>
     */
    public static Ansi of(String style) {
        return new Ansi(style);
    }

    /**
     * Объединяет 2 стиля в один.
     */
    public Ansi and(Ansi otherStyle) {
        return new Ansi(this.style + ";" + otherStyle.style);
    }

    /**
     * Преобразует ANSI объект в функциональную строку.
     *
     * <br> При ее выводе в консоль будет применен соответствующий стиль.
     */
    @Override
    public String toString() {
        return "\u001B[" + style + "m";
    }

    /**
     * Стилизует указанный текст, по завершении которого стиль сбрасывается.
     */
    public static String applyStyle(Object string, Ansi style) {
        return style.toString() + string + Modes.RESET;
    }

    /**
     * Убирает последний написанный в консоли символ.
     * <br>
     * <br>Встроенная консоль Intelij IDEA не поддерживает эту функцию.
     */
    public static void clearChar() {
        System.out.print("\b\033[K");
    }

    /**
     * Очищает самую последнюю написанную строчку консоли.
     * <br>
     * <br>Встроенная консоль Intelij IDEA не поддерживает эту функцию.
     */
    public static void clearLine() {
        System.out.print("\33[2K\r");
    }

    public static class Modes {
        public static final Ansi RESET = new Ansi("0");
        public static final Ansi BOLD = new Ansi("1");
        public static final Ansi FAINT = new Ansi("2");
        public static final Ansi ITALIC = new Ansi("3");
        public static final Ansi UNDERLINE = new Ansi("4");
        public static final Ansi BLINKING = new Ansi("6");
        public static final Ansi INVERTED = new Ansi("7");
        public static final Ansi INVISIBLE_TEXT = new Ansi("8");
        public static final Ansi STRIKETHROUGH = new Ansi("9");

    }

    public static class Colors {

        public static final Ansi BLACK = new Ansi("30");
        public static final Ansi RED = new Ansi("31");
        public static final Ansi GREEN = new Ansi("32");
        public static final Ansi YELLOW = new Ansi("33");
        public static final Ansi BLUE = new Ansi("34");
        public static final Ansi MAGENTA = new Ansi("35");
        public static final Ansi CYAN = new Ansi("36");
        public static final Ansi WHITE = new Ansi("37");
        public static final Ansi RESET = new Ansi("39");

        public static class Bright {
            public static final Ansi BLACK = new Ansi("90");
            public static final Ansi RED = new Ansi("91");
            public static final Ansi GREEN = new Ansi("92");
            public static final Ansi YELLOW = new Ansi("93");
            public static final Ansi BLUE = new Ansi("94");
            public static final Ansi MAGENTA = new Ansi("95");
            public static final Ansi CYAN = new Ansi("96");
            public static final Ansi WHITE = new Ansi("97");
        }

        public static Ansi fromRgb(int r, int g, int b) {
            return new Ansi("38;2;" + r + ";" + g + ";" + b);
        }

        public static Ansi from8Bit(int index) {
            return new Ansi("38;5;" + index);
        }

    }

    public static class BgColors {
        public static final Ansi BLACK = new Ansi("40");
        public static final Ansi RED = new Ansi("41");
        public static final Ansi GREEN = new Ansi("42");
        public static final Ansi YELLOW = new Ansi("43");
        public static final Ansi BLUE = new Ansi("44");
        public static final Ansi MAGENTA = new Ansi("45");
        public static final Ansi CYAN = new Ansi("46");
        public static final Ansi WHITE = new Ansi("47");
        public static final Ansi RESET = new Ansi("49");

        public static class Bright {
            public static final Ansi BLACK = new Ansi("100");
            public static final Ansi RED = new Ansi("101");
            public static final Ansi GREEN = new Ansi("102");
            public static final Ansi YELLOW = new Ansi("103");
            public static final Ansi BLUE = new Ansi("104");
            public static final Ansi MAGENTA = new Ansi("105");
            public static final Ansi CYAN = new Ansi("106");
            public static final Ansi WHITE = new Ansi("107");
        }

        public static Ansi fromRgb(int r, int g, int b) {
            return new Ansi("48;2;" + r + ";" + g + ";" + b);
        }

        public static Ansi from8Bit(int index) {
            return new Ansi("48;5;" + index);
        }
    }
}