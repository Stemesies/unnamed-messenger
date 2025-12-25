package utils.cli;

import utils.cli.utils.Token;

import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;

public class CommandTokenizer {

    private static Token processToken(MatchResult result) {
        var token = result.group(1);

        if (token == null)
            return null;

        var isArgument = token.charAt(0) == '"';
        if (isArgument)
            token = token.substring(1, token.length() - 1);

        return new Token(
            removeEscapeMetasymbols(token),
            result.start(1),
            result.end(1),
            isArgument
        );
    }

    private static String removeEscapeMetasymbols(String token) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isEscaped = false;

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
            }
            if (isEscaped)
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
    public static List<Token> tokenize(String input) {
        return CommandProcessor.pattern
            .matcher(input)
            .results()
            .map(CommandTokenizer::processToken)
            .filter(Objects::nonNull)
            .toList();
    }

}
