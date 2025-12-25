import utils.cli.CommandProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Ansi;

public class HelpCommandTest {

    CommandProcessor processor = new CommandProcessor();

    private boolean execute(CommandProcessor p, String command) {
        System.out.println(
            Ansi.applyStyle(
                "\nExecuting command: " + command,
                Ansi.Colors.fromRgb(217, 76, 118)
            )
        );
        return p.executeAndExplain(command);
    }

    private void assertHelp(String expected) {
        Assertions.assertTrue(execute(processor, "/help"));
        Assertions.assertEquals(expected, processor.getOutput());
    }

    @Test
    public void test() {


        assertHelp("/help <subcommands> - выводит все доступные команды\n");

        processor.register("invis", (rc)->rc
            .isInvisible()
            .executes(()->{})
        );

        assertHelp("/help <subcommands> - выводит все доступные команды\n");

        Assertions.assertTrue(execute(processor, "/help invis"));
        Assertions.assertEquals(
            Ansi.applyStyle( "Unknown command invis.", Ansi.Colors.RED) + "\n",
            processor.getOutput()
        );

        processor.register("noDescription", (rc)->rc
            .executes(()->{})
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            """);

        processor.register("description", (rc)->rc
            .description("description")
            .executes(()->{})
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            /description - description
            """);

        processor.register("args", (rc)->rc
            .requireArgument("arg")
            .description("description")
            .executes(()->{})
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            /description - description
            /args <arg> - description
            """);

        processor.register("optArg", (rc)->rc
            .findArgument("arg")
            .description("description")
            .executes(()->{})
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            /description - description
            /args <arg> - description
            /optArg [arg] - description
            """);

        processor.register("sevArgs", (rc)->rc
            .requireArgument("arg1")
            .findArgument("arg2")
            .description("description")
            .executes(()->{})
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            /description - description
            /args <arg> - description
            /optArg [arg] - description
            /sevArgs <arg1> [arg2] - description
            """);

        Assertions.assertTrue(execute(processor, "/help help"));
        Assertions.assertEquals("""
            /help <subcommands> - выводит все доступные команды
            """, processor.getOutput());

        Assertions.assertTrue(execute(processor, "/help noDescription"));
        Assertions.assertEquals("""
            /noDescription
            """, processor.getOutput());

        processor.register("sub", (rc)->rc
            .subcommand("subcommand1", (s)->s
                .description("description1")
                .executes(()->{})
            )
            .subcommand("subcommand2", (s)->s
                .description("description2")
                .subcommand("subsub_command2", (s1)->s1
                    .description("sub_description2")
                    .executes(()->{})
                )
            )
            .subcommand("subcommand3", (s)->s
                .description("description3")
                .executes(()->{})
                .subcommand("subsub_command3", (s1)->s1
                    .description("sub_description3")
                    .executes(()->{})
                )
            )
        );

        assertHelp("""
            /help <subcommands> - выводит все доступные команды
            /noDescription
            /description - description
            /args <arg> - description
            /optArg [arg] - description
            /sevArgs <arg1> [arg2] - description
            /sub
            """);

        Assertions.assertTrue(execute(processor, "/help sub"));
        Assertions.assertEquals("""
            /sub subcommand1 - description1
            /sub subcommand2 - description2
            /sub subcommand3 - description3
            """, processor.getOutput());

        Assertions.assertTrue(execute(processor, "/help sub subcommand2"));
        Assertions.assertEquals("""
            /sub subcommand2 subsub_command2 - sub_description2
            """, processor.getOutput());

        Assertions.assertTrue(execute(processor, "/help sub subcommand3"));
        Assertions.assertEquals("""
            /sub subcommand3 - description3
            /sub subcommand3 subsub_command3 - sub_description3
            """, processor.getOutput());


    }
}
