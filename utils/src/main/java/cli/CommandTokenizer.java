package cli;

import java.util.List;
import java.util.Objects;

public class CommandTokenizer {

    /**
     * Убирает экраны.
     * <br>Мне сказать больше нечего.
     */
    private static String processToken(String token) {
        if (token.charAt(0) == '"')
            token = token.substring(1, token.length() - 1);

        StringBuilder stringBuilder = new StringBuilder();
        boolean isEscaped = false;

        // Убираем экранные мета-символы
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == '\\') {
                if (isEscaped) {
                    stringBuilder.append(c);
                    isEscaped = false;
                    continue;
                }
                isEscaped = true;
                continue;
            } else if (isEscaped)
                isEscaped = false;
            stringBuilder.append(c);
        }


        return stringBuilder.toString();
    }

    /**
     * Разбивает команду на цельные токены, готовые к исполнению и обработке
     * Командным Процессором.
     *
     * <br>К примеру, команду:
     * <br><code>/groups create gftwl "Группа для любителей \"[ССЫЛКА\\ЗАБЛОКИРОВАНА]\""</code>
     * <br>Данный метод разобьет на:
     * <br><code>[groups, create, gftwl, Группа для любителей "[ССЫЛКА\ЗАБЛОКИРОВАНА]"]</code>
     * <br>
     * <br>!! Убедитесь, что перед токенизацией вы проверили строку
     * на валидность с помощью {@link CommandValidator#validate(String)}
     * чтобы избежать неожиданностей.
     *
     * @param input Сырая команда
     * @return Список токенов
     */
    public static List<String> tokenize(String input) {
        return CommandProcessor.pattern
            .matcher(input)
            .results()
            .map((it) -> it.group(1))
            .filter(Objects::nonNull)
            .map(CommandTokenizer::processToken)
            .toList();
    }
}
