import utils.cli.CommandTokenizer;
import utils.cli.utils.Token;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandTokenizerTest {

    void test(String command, Token... args) {
        System.out.println("Testing:\n" + command);
        var result = CommandTokenizer.tokenize(command);

        Assertions.assertArrayEquals(args, result.toArray());
        System.out.println(result);
    }

    @Test
    public void successfulCommand() {
        test("/groups create groupId \"Группа для тех, кто любит самые смачные...\" ",
            new Token("groups", 1, 7, false),
            new Token("create", 8, 14, false),
            new Token("groupId", 15, 22, false),
            new Token("Группа для тех, кто любит самые смачные...", 23, 67, true)
        );
    }

    @Test
    public void emptyQuotes() {
        test("/when the impostor is \"\"",
            new Token("when", 1, 5, false),
            new Token("the", 6, 9, false),
            new Token("impostor", 10, 18, false),
            new Token("is", 19, 21, false),
            new Token("", 22, 24, true)
        );
    }

    @Test
    public void successfulCommand_escapedQuotes() {
        // " ... мессенджер \"MAX\" "
        test("/broadcast \"ВНИМАНИЕ! Зачем вы здесь находитесь? Есть же прекрасный мессенджер \\\"MAX\\\"\" ",
            new Token("broadcast", 1, 10, false),
            new Token("ВНИМАНИЕ! Зачем вы здесь находитесь? Есть же прекрасный мессенджер \"MAX\"",
                11, 87, true)
        );

    }

    @Test
    public void successfulCommand_escapedEscapedQuotes() {
        //                             /broadcast    "       \"MAX\"       иди знаешь куда    "
        //             /broadcast  " - /broadcast   \"     \\\"MAX\\\"     иди знаешь куда   \"  - "
        test("/broadcast \" - /broadcast \\\" \\\\\\\"MAX\\\\\\\" иди знаешь куда \\\" -\"",
            new Token("broadcast", 1, 10, false),
            new Token(" - /broadcast \" \\\"MAX\\\" иди знаешь куда \" -", 11, 62, true)
        );
    }
}
