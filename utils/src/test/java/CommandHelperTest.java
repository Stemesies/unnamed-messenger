import utils.cli.Command;
import utils.cli.CommandErrors;
import utils.cli.CommandProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CommandHelperTest {

    @Test
    public void illegalPhantomCommand() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.register("phantom", (a) -> a
                .isPhantom()
                .executes(() -> System.out.println("How did we get here?"))
            ));

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.register("phantom2", (a) -> a
                .isPhantom()
                .subcommand("subphantom", (b) -> b
                    .isPhantom()
                    .executes(() -> System.out.println("How did we get here?"))
                )
            ));

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.register("phantom3", (a) -> a
                .subcommand("subphantom", (b) -> b
                    .isPhantom()
                    .executes(() -> System.out.println("How did we get here?"))
                )
            ));

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.register("phantom4", (a) -> a
                .isPhantom()
                .require("How", () -> true)
            ));

        Assertions.assertDoesNotThrow(() ->
            processor.register("phantom5", (a) -> a
                .description("uwu")
                .isPhantom()
            ));

        Assertions.assertDoesNotThrow(() ->
            processor.register("phantom6", (a) -> a
                .description("uwu")
                .isPhantom()
                .subcommand("sub", (b) -> b
                    .description("owo")
                )
            ));
    }

    @Test
    public void phantomDetection() {
        var processor = new CommandProcessor();

        processor.register("phantom", Command.Builder::isPhantom);
        processor.register("notPhantom", (a) -> a
            .executes(() -> System.out.println("Yeah."))
        );
        processor.execute("/phantom");
        Assertions.assertNotNull(processor.getLastError());
        Assertions.assertEquals(CommandErrors.PHANTOM_COMMAND, processor.getLastError().type);

        processor.execute("/notPhantom");
        Assertions.assertNull(processor.getLastError());

    }
}
