import cli.CommandTokenizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTokenizerTest {

    void test(String command, String... args) {
        System.out.println("Testing:\n" + command);
        var result = CommandTokenizer.tokenize(command);

        Assertions.assertArrayEquals(args, result.toArray());
        System.out.println(result);
    }

    @Test
    public void successfulCommand() {
        test("/groups create groupId \"Группа для тех, кто любит самые смачные...\" ",
            "groups", "create", "groupId", "Группа для тех, кто любит самые смачные...");
    }

    @Test
    public void emptyQuotes() {
        test("/when the impostor is \"\"",
            "when", "the", "impostor", "is", "");
    }

    @Test
    public void successfulCommand_escapedQuotes() {
        // " ... мессенджер \"MAX\" "
        test("/broadcast \"ВНИМАНИЕ! Зачем вы здесь находитесь? Есть же прекрасный мессенджер \\\"MAX\\\"\" ",
            "broadcast", "ВНИМАНИЕ! Зачем вы здесь находитесь? Есть же прекрасный мессенджер \"MAX\"");

    }

    @Test
    public void successfulCommand_escapedEscapedQuotes() {
        //                             /broadcast    "       \"MAX\"       иди знаешь куда    "
        //             /broadcast  " - /broadcast   \"     \\\"MAX\\\"     иди знаешь куда   \"  - "
        test("/broadcast \" - /broadcast \\\" \\\\\\\"MAX\\\\\\\" иди знаешь куда \\\" -\"",
            "broadcast", " - /broadcast \" \\\"MAX\\\" иди знаешь куда \" -");
    }
}
