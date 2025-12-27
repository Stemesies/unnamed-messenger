package utils;

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

@SuppressWarnings({"unused", "LineLength"})
public class Ansi {
    private final String style;
    private final String openSpan;
    private final String closeSpan;

    private Ansi(String style, String open, String close) {
        this.style = style;
        this.openSpan = open;
        this.closeSpan = close;
    }

    /**
     * Создает новый экземпляр ANSI со стилем style.
     * <br> Подробнее о стилях можно почитать <a href="https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797">здесь</a>
     */
    public static Ansi of(String style, String openSpan, String closeSpan) {
        return new Ansi(style, openSpan, closeSpan);
    }

    /**
     * Объединяет 2 стиля в один.
     */
    public Ansi and(Ansi otherStyle) {
        return new Ansi(this.style + ";" + otherStyle.style,
                this.openSpan + otherStyle.openSpan, this.closeSpan + otherStyle.closeSpan);
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

    public String start() {
        return this.toString();
    }

    public String end() {
        return Modes.RESET.toString();
    }

    public String startHtml() {
        return openSpan;
    }

    public String closeHtml() {
        return closeSpan;
    }

    /**
     * Стилизует указанный текст, по завершении которого стиль сбрасывается.
     */
    public String apply(Object string) {
        return this.toString() + string + Modes.RESET;
    }

    /**
     * Стилизует указанный текст, по завершении которого стиль сбрасывается.
     */
    public static String applyStyle(Object string, Ansi style) {
        return style.toString() + string + Modes.RESET;
    }

    /**
     * Стилизует указанный текст, по завершении которого стиль сбрасывается.
     */
    public static String applyStyle(Object string, Ansi... styles) {
        StringBuilder result = new StringBuilder();
        for (Ansi style : styles) {
            result.append(style);
        }
        result.append(string);
        result.append(Modes.RESET);
        return result.toString();
    }

    /**
     * Форматирует вывод для UI.
     */
    public static String applyHtml(Object msg, Ansi style) {
        return style.openSpan + msg + style.closeSpan;
    }

    /**
     * Выбираем, что применить.
     */
    public static String applyChoose(Object msg, Ansi style, boolean isHtml) {
        if (isHtml) {
            return applyHtml(msg, style);
        }
        return applyStyle(msg, style);
    }

    /**
     * Убирает последний написанный в консоли символ.
     * <br>
     * <br>Встроенная консоль Intellij IDEA не поддерживает эту функцию.
     */
    public static void clearChar() {
        System.out.print("\b\033[K");
    }

    /**
     * Очищает самую последнюю написанную строчку консоли.
     * <br>
     * <br>Встроенная консоль Intellij IDEA не поддерживает эту функцию.
     */
    public static void clearLine() {
        System.out.print("\033[1A\33[2K\r");
        System.out.flush();
    }

    public static class Modes {
        public static final Ansi RESET = new Ansi("0", "", "");
        public static final Ansi BOLD = new Ansi("1", "<b>", "</b>");
        public static final Ansi FAINT = new Ansi("2", "", "");
        public static final Ansi ITALIC = new Ansi("3", "<i>", "</i>");
        public static final Ansi UNDERLINE = new Ansi("4", "<u>", "</u>");
        public static final Ansi BLINKING = new Ansi("6", "", "");
        public static final Ansi INVERTED = new Ansi("7", "", "");
        public static final Ansi INVISIBLE_TEXT = new Ansi("8", "", "");
        public static final Ansi STRIKETHROUGH = new Ansi("9", "<s>", "</s>");

    }

    public static class Colors {

        public static final Ansi BLACK = new Ansi("30", "<span style=\"color: #000000\">", "</span>");
        public static final Ansi RED = new Ansi("31", "<span style=\"color: #dd1212\">", "</span>");
        public static final Ansi GREEN = new Ansi("32", "<span style=\"color: #004a02\">", "</span>");
        public static final Ansi YELLOW = new Ansi("33", "<span style=\"color: #f1ee35\">", "</span>");
        public static final Ansi BLUE = new Ansi("34", "<span style=\"color: #265dc2\">", "</span>");
        public static final Ansi MAGENTA = new Ansi("35", "<span style=\"color: #c226b8\">", "</span>");
        public static final Ansi CYAN = new Ansi("36", "<span style=\"color: #26b0c2\">", "</span>");
        public static final Ansi WHITE = new Ansi("37", "<span style=\"color: #ffffff\">", "</span>");

        public static class Bright {
            public static final Ansi BLACK = new Ansi("90", "<span style=\"color: #000000\">", "</span>");
            public static final Ansi RED = new Ansi("91", "<span style=\"color: #dd1212\">", "</span>");
            public static final Ansi GREEN = new Ansi("92", "<span style=\"color: #007500\">", "</span>");
            public static final Ansi YELLOW = new Ansi("93", "<span style=\"color: #f1ee35\">", "</span>");
            public static final Ansi BLUE = new Ansi("94", "<span style=\"color: #265dc2\">", "</span>");
            public static final Ansi MAGENTA = new Ansi("95", "<span style=\"color: #c226b8\">", "</span>");
            public static final Ansi CYAN = new Ansi("96", "<span style=\"color: #26b0c2\">", "</span>");
            public static final Ansi WHITE = new Ansi("97", "<span style=\"color: #ffffff\">", "</span>");
        }

        public static Ansi fromRgb(int r, int g, int b) {
            return new Ansi("38;2;" + r + ";" + g + ";" + b,
                    "<span style=\"color: rgb(" + r + ", " + g + ", " + b + ");\">", "</span>");
        }

        public static Ansi from8Bit(int index) {
            return new Ansi("38;5;" + index, "", "");
        }

    }

    public static class BgColors {
        public static final Ansi BLACK = new Ansi("40", "<span style=\"background-color: #000000\">", "</span>");
        public static final Ansi RED = new Ansi("41", "<span style=\"background-color: #dd1212\">", "</span>");
        public static final Ansi GREEN = new Ansi("42", "<span style=\"background-color: #007500\">", "</span>");
        public static final Ansi YELLOW = new Ansi("43", "<span style=\"background-color: #f1ee35\">", "</span>");
        public static final Ansi BLUE = new Ansi("44", "<span style=\"background-color: #265dc2\">", "</span>");
        public static final Ansi MAGENTA = new Ansi("45", "<span style=\"background-color: #c226b8\">", "</span>");
        public static final Ansi CYAN = new Ansi("46", "<span style=\"background-color: #26b0c2\">", "</span>");
        public static final Ansi WHITE = new Ansi("47", "<span style=\"background-color: #ffffff\">", "</span>");

        public static class Bright {
            public static final Ansi BLACK = new Ansi("100", "<span style=\"background-color: #000000\">", "</span>");
            public static final Ansi RED = new Ansi("101", "<span style=\"background-color: #dd1212\">", "</span>");
            public static final Ansi GREEN = new Ansi("102", "<span style=\"background-color: #007500\">", "</span>");
            public static final Ansi YELLOW = new Ansi("103", "<span style=\"background-color: #f1ee35\">", "</span>");
            public static final Ansi BLUE = new Ansi("104", "<span style=\"background-color: #265dc2\">", "</span>");
            public static final Ansi MAGENTA = new Ansi("105", "<span style=\"background-color: #c226b8\">", "</span>");
            public static final Ansi CYAN = new Ansi("106", "<span style=\"background-color: #26b0c2\">", "</span>");
            public static final Ansi WHITE = new Ansi("107", "<span style=\"background-color: #ffffff\">", "</span>");
        }

        public static Ansi fromRgb(int r, int g, int b) {
            return new Ansi("48;2;" + r + ";" + g + ";" + b,
                    "<span style=\"background-color: rgb(" + r + ", " + g + ", " + b + ");\">", "</span>");
        }

        public static Ansi from8Bit(int index) {
            return new Ansi("48;5;" + index, "", "");
        }
    }
}