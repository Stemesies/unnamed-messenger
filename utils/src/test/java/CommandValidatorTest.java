import cli.CommandResults;
import cli.CommandValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Ansi;

import static cli.CommandResults.INVALID_SEPARATOR;
import static cli.CommandResults.NO_SEPARATION;
import static cli.CommandResults.UNCLOSED_QUOTE;
import static cli.CommandResults.UNEXPECTED_SYMBOL;

public class CommandValidatorTest {

    boolean test(String command) {
        System.out.println(
            Ansi.applyStyle(
                "\nValidating command: " + command,
                Ansi.Colors.fromRgb(217, 76, 118)
            )
        );

        var result = CommandValidator.validate(command);
        if (result == null) {
            System.out.println("Command is valid.");
            return true;
        }
        result.explain();
        System.out.println();
        return false;
    }

    void assertError(String command, CommandResults type, int begin, int end) {
        System.out.println(
            Ansi.applyStyle(
                "\nValidating command: " + command,
                Ansi.Colors.fromRgb(217, 76, 118)
            )
        );
        var result = CommandValidator.validate(command);
        Assertions.assertNotNull(result);
        result.explain();
        Assertions.assertEquals(type, result.type);
        Assertions.assertEquals(begin, result.start);
        Assertions.assertEquals(end, result.end);
    }

    @Test
    public void successfulCommand() {
        Assertions.assertTrue(
            test("/groups create groupId \"Группа для тех, кто любит самые смачные...\" ")
        );
    }

    @Test
    public void emptyQuotes() {
        Assertions.assertTrue(test("/when the impostor is \"\""));
        Assertions.assertTrue(test("/when \"\" \"\" \"\""));
    }

    @Test
    public void noSeparation() {
        assertError("/uwu \"amogus\"\"uwu\"", NO_SEPARATION, 13, 18);
        assertError("/uwu \"\"\"\"", NO_SEPARATION, 7, 9);
    }

    @Test
    public void successfulCommand_escapedQuotes() {
        // " ... мессенджер \"MAX\" "
        Assertions.assertTrue(
            test("/broadcast \"ВНИМАНИЕ! Зачем вы здесь находитесь? " +
                "Есть же прекрасный мессенджер \\\"MAX\\\"\" ")
        );
    }

    @Test
    public void successfulCommand_escapedEscapedQuotes() {
        Assertions.assertTrue(
            //                             /broadcast    "       \"MAX\"       иди знаешь куда    "
            //             /broadcast  " - /broadcast   \"     \\\"MAX\\\"     иди знаешь куда   \"  - "
            test("/broadcast \" - /broadcast \\\" \\\\\\\"MAX\\\\\\\" иди знаешь куда \\\" -\"")
        );
    }

    @Test
    public void largeGapBetweenTokens() {
        assertError("/uwu  owo", UNEXPECTED_SYMBOL, 5, 6);
    }

    @Test
    public void illegalSeparator() {
        assertError("/uwu<owo>", INVALID_SEPARATOR, 4, 5);

        assertError("/uwu \\\"owo uwu", UNEXPECTED_SYMBOL, 5, 6);
    }

    @Test
    public void illegalWrapping() {
        assertError("/uwu <owo>", UNEXPECTED_SYMBOL, 5, 6);
    }

    @Test
    public void unfinishedQuotes() {
        assertError("/uwu \"<owo>", UNCLOSED_QUOTE, 5, 11);

        assertError("/uwu \" <owo>", UNCLOSED_QUOTE, 5, 12);

        assertError("/uwu \" <owo> uwu _v_", UNCLOSED_QUOTE, 5, 20);
    }

    @Test
    public void unfinishedQuotes_withEscapedQuotes() {
        assertError("/uwu \"<owo>\\\"", UNCLOSED_QUOTE, 5, 13);

        Assertions.assertTrue(test("/uwu \"<owo>\\\\\""));

        assertError("/uwu \"<owo>\\\\\\\"", UNCLOSED_QUOTE, 5, 15);

        Assertions.assertTrue(test("/uwu \"<owo>\\\\\\\\\""));
    }
}
