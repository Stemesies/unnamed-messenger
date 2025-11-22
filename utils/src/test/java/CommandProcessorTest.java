import cli.Command;
import cli.CommandProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CommandProcessorTest {

    @Test
    public void simpleCommand_noContinuation() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.registerCommand(
                Command.create("/uwu").build()
            )
        );
    }

    @Test
    public void simpleCommand() {
        var processor = new CommandProcessor();
        AtomicInteger uwu = new AtomicInteger();

        processor.registerCommand("uwu", (rc) -> rc
            .executes(() -> {
                System.out.println("Set uwu to 1");
                uwu.set(1);
            })

        );

        Assertions.assertTrue(processor.processCommand("/uwu"));
        Assertions.assertEquals(1, uwu.get());
    }

    @Test
    public void simpleSubcommand() {
        var processor = new CommandProcessor();
        AtomicInteger uwu = new AtomicInteger();

        processor.registerCommand("uwu", (rc) -> rc
            .subcommand("owo", it -> it
                .executes(() -> {
                    System.out.println("Set uwu to 1");
                    uwu.set(1);
                })
            )
        );

        Assertions.assertTrue(processor.processCommand("/uwu"));
        Assertions.assertEquals("Further subcommands expected.", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/uwu owo"));
        Assertions.assertEquals(1, uwu.get());

        processor.registerCommand("u_u", (rc) -> rc
            .executes(() -> {
                System.out.println("Set uwu to 3");
                uwu.set(3);
            })
            .subcommand("_v_", it -> it
                .executes(() -> {
                    System.out.println("Set uwu to -1");
                    uwu.set(-1);
                })
            )
            .subcommand("owo", it -> it
                .executes(() -> {
                    System.out.println("Set uwu to 2");
                    uwu.set(2);
                })
            )
        );

        Assertions.assertTrue(processor.processCommand("/u_u"));
        Assertions.assertEquals(3, uwu.get());

        Assertions.assertTrue(processor.processCommand("/uwu owo"));
        Assertions.assertEquals(1, uwu.get());

        Assertions.assertTrue(processor.processCommand("/u_u owo"));
        Assertions.assertEquals(2, uwu.get());

        Assertions.assertTrue(processor.processCommand("/u_u _v_"));
        Assertions.assertEquals(-1, uwu.get());

        Assertions.assertTrue(processor.processCommand("/uwu _v_"));
        Assertions.assertEquals(2, uwu.get());
    }

    @Test
    public void invalidSubcommand() {
        var processor = new CommandProcessor();

        processor.registerCommand("noArguments", (rc) -> rc
            .subcommand("owo", it -> it
                .executes(() -> System.out.println("How did we get here?"))
            )
        );
        processor.registerCommand("arguments", (rc) -> rc
            .requireArgument("why")
            .findArgument("why2")
            .subcommand("owo", it -> it
                .executes(() -> System.out.println("How did we get here?"))
            )
        );

        Assertions.assertTrue(processor.processCommand("/noArguments _v_"));
        Assertions.assertEquals("Invalid subcommand.", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/noArguments _v_ wrong2"));
        Assertions.assertEquals("Invalid subcommand.", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/arguments _v_"));
        Assertions.assertEquals("Further subcommands expected.", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/arguments _v_ optional"));
        Assertions.assertEquals("Further subcommands expected.", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/arguments _v_ optional wrongCommand"));
        Assertions.assertEquals("Invalid subcommand.", processor.getLastError().type);
    }

    @Test
    public void simpleArgumentCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("invite", (rc) -> rc
            .requireArgument("user")
            .executes((ctx) -> {
                result.set("invited " + ctx.getString("user"));
                System.out.println(result.get());
            })
        );

        Assertions.assertTrue(processor.processCommand("/invite username"));
        Assertions.assertEquals("invited username", result.get());
    }

    @Test
    public void simpleMultiArgumentCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("invite", (rc) -> rc
            .requireArgument("group")
            .requireArgument("user")
            .executes((ctx) -> {
                result.set("invited "
                    + ctx.getString("user")
                    + " to "
                    + ctx.getString("group")
                );
                System.out.println(result.get());
            })
        );

        Assertions.assertTrue(processor.processCommand("/invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());
    }

    @Test
    public void simpleArgumentSubCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("groups", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("user")
                .executes((ctx) -> {
                    result.set("invited " + ctx.getString("user"));
                    System.out.println(result.get());
                }))
        );

        Assertions.assertTrue(processor.processCommand("/groups invite username"));
        Assertions.assertEquals("invited username", result.get());
    }

    @Test
    public void simpleMultiArgumentSubCommand() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("groups", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("group")
                .requireArgument("user")
                .executes((ctx) -> {
                    result.set("invited "
                        + ctx.getString("user")
                        + " to "
                        + ctx.getString("group")
                    );
                    System.out.println(result.get());
                })
            ));

        Assertions.assertTrue(processor.processCommand("/groups invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());
    }

    @Test
    public void optimalArgument() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("groups", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("group")
                .requireArgument("user")
                .findArgument("not_required")
                .executes((ctx) -> {
                    var additional = ctx.hasArgument("not_required") ?
                        " and also " + ctx.getString("not_required") :
                        "";
                    result.set("invited "
                        + ctx.getString("user")
                        + " to "
                        + ctx.getString("group")
                        + additional
                    );
                    System.out.println(result.get());
                })
            ));

        Assertions.assertTrue(processor.processCommand("/groups invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());

        Assertions.assertTrue(processor.processCommand(
            "/groups invite groupname username uwu"
        ));
        Assertions.assertEquals(
            "invited username to groupname and also uwu",
            result.get()
        );
    }

    @Test
    public void multiArgumentNotEnoughArguments() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.registerCommand("groups", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("group")
                .requireArgument("user")
                .executes((ctx) -> {
                    result.set("invited "
                        + ctx.getString("user")
                        + " to "
                        + ctx.getString("group")
                    );
                    System.out.println(result.get());
                })
            ));

        Assertions.assertTrue(processor.processCommand("/groups invite groupname"));
        Assertions.assertNotNull(processor.getLastError());
        Assertions.assertEquals(
            "Missing required argument \"user\".",
            processor.getLastError().type
        );
    }

    @Test
    public void subcommandAfterArguments() {
        var processor = new CommandProcessor();
        AtomicReference<String> a = new AtomicReference<>("");

        processor.registerCommand("uwu", (rc) -> rc
            .requireArgument("why")
            .subcommand("owo", it -> it
                .executes((ctx) -> {
                    System.out.println(ctx.getString("why"));
                    a.set(ctx.getString("why"));
                })
            )
        );

        Assertions.assertTrue(processor.processCommand("/uwu owo"));
        Assertions.assertEquals("Missing required argument \"why\".", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/uwu whywwww owo"));
        Assertions.assertEquals("whywwww", a.get());

        processor.registerCommand("optionalArgument", (rc) -> rc
            .requireArgument("why")
            .findArgument("why2")
            .subcommand("owo", it -> it
                .executes((ctx) -> {
                    var why2 = ctx.hasArgument("why2")
                        ? ctx.getString("why2")
                        : "";
                    System.out.println(ctx.getString("why") + why2);
                    a.set(ctx.getString("why") + why2);
                })
            )
        );

        Assertions.assertTrue(processor.processCommand("/optionalArgument owo"));
        Assertions.assertEquals("Missing required argument \"why\".", processor.getLastError().type);

        Assertions.assertTrue(processor.processCommand("/optionalArgument whywwww owo"));
        Assertions.assertEquals("whywwww", a.get());

        Assertions.assertTrue(processor.processCommand("/optionalArgument whywwww aaa owo"));
        Assertions.assertEquals("whywwwwaaa", a.get());
    }

}
