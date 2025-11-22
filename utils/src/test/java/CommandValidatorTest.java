import cli.CommandValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandValidatorTest {

    boolean test(String command) {
        System.out.println("Testing:\n" + command);
        var result = CommandValidator.validate(command);
        if(result == null) {
            System.out.println();
            return true;
        }
        result.explain();
        System.out.println();
        return false;
    }

    @Test
    public void successfulCommand() {
        Assertions.assertTrue(
                test("/groups create groupId \"Группа для тех, кто любит самые смачные...\" ")
        );
    }

    @Test
    public void emptyQuotes() {
        Assertions.assertTrue(
            test("/when the impostor is \"\"")
        );
    }

    @Test
    public void unsuccessfulCommand_escapedQuotes() {
        // " ... мессенджер \"MAX\" "
        Assertions.assertTrue(
            test("/broadcast \"ВНИМАНИЕ! Зачем вы здесь находитесь? Есть же прекрасный мессенджер \\\"MAX\\\"\" ")
        );
    }

    @Test
    public void unsuccessfulCommand_escapedEscapedQuotes() {
        Assertions.assertTrue(
            //                             /broadcast    "       \"MAX\"       иди знаешь куда    "
            //             /broadcast  " - /broadcast   \"     \\\"MAX\\\"     иди знаешь куда   \"  - "
            test("/broadcast \" - /broadcast \\\" \\\\\\\"MAX\\\\\\\" иди знаешь куда \\\" -\"")
        );
    }

    @Test
    public void largeGapBetweenTokens() {
        var result = CommandValidator.validate("/uwu  owo");
        Assertions.assertNotNull(result);
        result.explain();

        Assertions.assertEquals(5, result.start);
        Assertions.assertEquals(6, result.end);
    }

    @Test
    public void illegalSeparator() {
        var result = CommandValidator.validate("/uwu<owo>");
        Assertions.assertNotNull(result);
        result.explain();

        Assertions.assertEquals(4, result.start);
        Assertions.assertEquals(5, result.end);
    }

    @Test
    public void illegalWrapping() {
        var result = CommandValidator.validate("/uwu <owo>");
        Assertions.assertNotNull(result);
        result.explain();

        Assertions.assertEquals(5, result.start);
        Assertions.assertEquals(6, result.end);
    }

    @Test
    public void unfinishedQuotes() {
        var result = CommandValidator.validate("/uwu \"<owo>");
        Assertions.assertNotNull(result);
        result.explain();

        Assertions.assertEquals(5, result.start);
        Assertions.assertEquals(11, result.end);

        var result2 = CommandValidator.validate("/uwu \" <owo>");
        Assertions.assertNotNull(result2);
        result2.explain();

        Assertions.assertEquals(5, result2.start);
        Assertions.assertEquals(12, result2.end);
    }

    @Test
    public void unfinishedQuotes_withEscapedQuotes() {
        var result = CommandValidator.validate("/uwu \"<owo>\\\"");
        Assertions.assertNotNull(result);
        result.explain();

        Assertions.assertEquals(5, result.start);
        Assertions.assertEquals(13, result.end);
    }
}
