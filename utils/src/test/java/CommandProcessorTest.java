import utils.cli.CommandProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import utils.Ansi;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static utils.cli.CommandErrors.COMMAND_NOT_FOUND;
import static utils.cli.CommandErrors.FURTHER_SUBCOMMANDS_EXPECTED;
import static utils.cli.CommandErrors.INVALID_SUBCOMMAND;
import static utils.cli.CommandErrors.MISSING_REQUIRED_ARGUMENT;
import static utils.cli.CommandErrors.UNEXPECTED_SYMBOL;
import static utils.cli.CommandErrors.UNKNOWN_SUBCOMMAND;

public class CommandProcessorTest {

    private boolean execute(CommandProcessor p, String command) {
        System.out.println(
            Ansi.applyStyle(
                "\nExecuting command: " + command,
                Ansi.Colors.fromRgb(217, 76, 118)
            )
        );
        return p.executeAndExplain(command, null);
    }

    /**
     * Если у команды нет суб-команд и при этом также нет параметра executes,
     * программа должна выдавать IllegalStateException
     */
    @Test
    public void simpleCommand_noContinuation() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalStateException.class, () ->
            processor.register("uwu", (it) -> it)
        );
    }

    /**
     * Проверяется простая однословная команда.
     * <br>Также проверяется проверка на отсутствие команды в словаре
     */
    @Test
    public void simpleCommand() {
        var processor = new CommandProcessor();
        AtomicInteger uwu = new AtomicInteger();

        processor.register("set", (rc) -> rc
            .executes(() -> {
                System.out.println("Set to 1");
                uwu.set(1);
            })

        );

        Assertions.assertTrue(execute(processor, "/set"));
        Assertions.assertEquals(1, uwu.get());

        Assertions.assertFalse(execute(processor, "/unset"));
        Assertions.assertEquals(COMMAND_NOT_FOUND, processor.getLastError().type);
    }

    @Test
    public void repeatingRegister() {
        var processor = new CommandProcessor();

        processor.register("imRepeating", (rc) -> rc
            .executes(()->{})
        );
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
            processor.register("imRepeating", (rc) -> rc
            .executes(()->{})
            ));
    }

    @Test
    public void accidentalSlash() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
            processor.register("/welp", (rc) -> rc
                .executes(()->{})
            ));
    }

    @Test
    public void invisibleCommand() {
        var processor = new CommandProcessor();

        processor.register("invisible", (rc) -> rc
            .isInvisible()
            .executes(()->{})
        );

        processor.register("invisible2", (rc) -> rc
            .isInvisible()
            .requireArgument("arg1")
            .subcommand("sub", it -> it
                .executes(() -> {})
            )
        );

        Assertions.assertTrue(execute(processor, "/invisible"));

        Assertions.assertFalse(execute(processor, "/invisible amogus"));
        Assertions.assertEquals(COMMAND_NOT_FOUND, processor.getLastError().type);

        Assertions.assertTrue(execute(processor, "/invisible2 a sub"));

        Assertions.assertFalse(execute(processor, "/invisible2 a"));
        Assertions.assertEquals(COMMAND_NOT_FOUND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/invisible2 sub"));
        Assertions.assertEquals(COMMAND_NOT_FOUND, processor.getLastError().type);


    }

    /**
     * Идет проверка на правильной работы суб-команд.
     */
    @Test
    public void simpleSubcommand() {
        var processor = new CommandProcessor();
        AtomicInteger uwu = new AtomicInteger();

        System.out.println("""
            Доступные команды:
            /first set      ->  1
            /second         ->  3
            /second set_n1  -> -1
            /second set     ->  2
            """);

        processor.register("first", (rc) -> rc
            .subcommand("set", it -> it
                .executes(() -> {
                    System.out.println("Set to 1");
                    uwu.set(1);
                })
            )
        );

        processor.register("second", (rc) -> rc
            .executes(() -> {
                System.out.println("Set to 3");
                uwu.set(3);
            })
            .subcommand("set_n1", it -> it
                .executes(() -> {
                    System.out.println("Set to -1");
                    uwu.set(-1);
                })
            )
            .subcommand("set", it -> it
                .executes(() -> {
                    System.out.println("Set to 2");
                    uwu.set(2);
                })
            )
        );

        /* Доступные команды:
            /first set      ->  1
            /second         ->  3
            /second set_n1  -> -1
            /second set     ->  2
        */

        Assertions.assertFalse(execute(processor, "/first"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);

        Assertions.assertTrue(execute(processor, "/second"));
        Assertions.assertEquals(3, uwu.get());

        Assertions.assertTrue(execute(processor, "/first set"));
        Assertions.assertEquals(1, uwu.get());

        Assertions.assertTrue(execute(processor, "/second set"));
        Assertions.assertEquals(2, uwu.get());

        Assertions.assertTrue(execute(processor, "/second set_n1"));
        Assertions.assertEquals(-1, uwu.get());

        Assertions.assertFalse(execute(processor, "/first set_n1"));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);
    }


    @Test
    public void subcommandOverflow() {
        var processor = new CommandProcessor();

        processor.register("no_args", (rc) -> rc
            .executes(() -> System.out.println("А вы же знаете, что есть такой мессенджер, как MAX?"))
        );
        processor.register("args", (rc) -> rc
            .requireArgument("arg1")
            .findArgument("optionalArg2")
            .executes(() -> System.out.println("ААААААААААААААААААААААААААААААААА"))
        );

        Assertions.assertFalse(execute(processor, "/no_args subcommand"));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);

        // subcommand поглощается, как arg1
        Assertions.assertTrue(execute(processor, "/args subcommand"));

        // subcommand поглощается, как optionalArg2
        Assertions.assertTrue(execute(processor, "/args arg1 subcommand"));

        Assertions.assertFalse(execute(processor, "/args arg1 arg2 subcommand"));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);
    }

    /**
     * Проверка на совместную работу аргументов и суб-команд
     */
    @Test
    public void invalidSubcommand() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
            processor.register("no_args", (rc) -> rc
                .subcommand("subcommand", it -> it
                    .executes(() -> System.out.println("I'm tired"))
                )
                .subcommand("subcommand", it -> it
                    .executes(() -> System.out.println("I'm tired"))
                )
            ));

        processor.register("no_args", (rc) -> rc
            .subcommand("subcommand", it -> it
                .executes(() -> System.out.println("I'm tired"))
            )
        );
        processor.register("args", (rc) -> rc
            .requireArgument("arg1")
            .findArgument("optionalArg2")
            .subcommand("subcommand", it -> it
                .executes(() -> System.out.println("damn"))
            )
        );

        Assertions.assertTrue(execute(processor, "/no_args subcommand"));

        Assertions.assertFalse(execute(processor, "/no_args \"subcommand\""));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/no_args wrong"));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/no_args _v_ wrong"));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/args _v_"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/args subcommand"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);
        System.out.println("subcommand в этом случае считается следующей " +
            "суб-командой, а не аргументом (для более предсказуемого поведения)");

        Assertions.assertFalse(execute(processor, "/args subcommand subcommand"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/args \"subcommand\""));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        System.out.println("Для решения можно обрамить наше слово в кавычки. " +
            "Это в 100% случаев будет считаться аргументом");

        Assertions.assertFalse(execute(processor, "/args _v_ optional"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/args _v_ optional wrong"));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertTrue(execute(processor, "/args _v_ subcommand"));
        Assertions.assertTrue(execute(processor, "/args _v_ optional subcommand"));
        Assertions.assertTrue(execute(processor, "/args \"subcommand\" subcommand"));

        Assertions.assertFalse(execute(processor, "/args _v_ \"subcommand\""));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
    }

    @Test
    public void simpleArgumentCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("invite", (rc) -> rc
            .requireArgument("user")
            .executes((ctx) -> {
                result.set("invited " + ctx.getString("user"));
                System.out.println(result.get());
            })
        );

        Assertions.assertTrue(execute(processor, "/invite username"));
        Assertions.assertEquals("invited username", result.get());

        Assertions.assertTrue(execute(processor, "/invite comrade"));
        Assertions.assertEquals("invited comrade", result.get());
        Assertions.assertTrue(execute(processor, "/invite \"Товарищ майор\""));
        Assertions.assertEquals("invited Товарищ майор", result.get());

        Assertions.assertFalse(execute(processor, "/invite Товарищ майор"));
        Assertions.assertEquals(UNEXPECTED_SYMBOL, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/invite Comrade mayor"));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);
    }

    @Test
    public void simpleMultiArgumentCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("invite", (rc) -> rc
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

        Assertions.assertTrue(execute(processor, "/invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());

        Assertions.assertTrue(execute(processor, "/invite a b"));
        Assertions.assertEquals("invited b to a", result.get());

        Assertions.assertTrue(execute(processor, "/invite \"When the sus\" \"Amogus is sus\""));
        Assertions.assertEquals("invited Amogus is sus to When the sus", result.get());

        Assertions.assertFalse(execute(processor, "/invite When the sus \"Amogus is sus\""));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/invite When the \"Amogus is sus\""));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);
    }

    @Test
    public void simpleArgumentSubCommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("groups", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("user")
                .executes((ctx) -> {
                    result.set("invited " + ctx.getString("user"));
                    System.out.println(result.get());
                }))
        );

        Assertions.assertTrue(execute(processor, "/groups invite username"));
        Assertions.assertEquals("invited username", result.get());

        Assertions.assertTrue(execute(processor, "/groups invite a"));
        Assertions.assertEquals("invited a", result.get());

        Assertions.assertTrue(execute(processor, "/groups invite \"When the sus\""));
        Assertions.assertEquals("invited When the sus", result.get());

        Assertions.assertFalse(execute(processor, "/groups invite Amogus is sus"));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups invite When \"Amogus is sus\""));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);
    }

    @Test
    public void simpleMultiArgumentSubCommand() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("groups", (rc) -> rc
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

        Assertions.assertTrue(execute(processor, "/groups invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());

        Assertions.assertTrue(execute(processor, "/groups invite uwu owo"));
        Assertions.assertEquals("invited owo to uwu", result.get());

        Assertions.assertFalse(execute(processor, "/groups groupname username"));
        Assertions.assertEquals(INVALID_SUBCOMMAND, processor.getLastError().type);
    }

    @Test
    public void optimalArgument() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("groups", (rc) -> rc
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

        Assertions.assertTrue(execute(processor, "/groups invite groupname username"));
        Assertions.assertEquals("invited username to groupname", result.get());

        Assertions.assertTrue(execute(processor, "/groups invite groupname username uwu"));
        Assertions.assertEquals(
            "invited username to groupname and also uwu",
            result.get()
        );
    }


    @Test
    @SuppressWarnings("ExtractMethodRecommender")
    public void multiArgumentNotEnoughArguments() {
        var processor = new CommandProcessor();

        AtomicReference<String> result = new AtomicReference<>("");

        processor.register("groups", (rc) -> rc
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

        processor.register("groups2", (rc) -> rc
            .subcommand("invite", (it) -> it
                .requireArgument("group")
                .requireArgument("user")
                .subcommand("to", (it2) -> it2
                    .executes((ctx) -> {
                        result.set("invited "
                            + ctx.getString("user")
                            + " to "
                            + ctx.getString("group")
                        );
                        System.out.println(result.get());
                    })
                )
            ));

        Assertions.assertFalse(execute(processor, "/groups invite"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups invite groupname"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups2 invite"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups2 invite groupname"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups2 invite to"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertFalse(execute(processor, "/groups2 invite groupname to"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);
    }

    @Test
    public void subcommandAfterArguments() {
        var processor = new CommandProcessor();
        AtomicReference<String> a = new AtomicReference<>("");

        processor.register("uwu", (rc) -> rc
            .requireArgument("why")
            .subcommand("owo", it -> it
                .executes((ctx) -> {
                    System.out.println(ctx.getString("why"));
                    a.set(ctx.getString("why"));
                })
            )
        );

        Assertions.assertFalse(execute(processor, "/uwu owo"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertTrue(execute(processor, "/uwu whywwww owo"));
        Assertions.assertEquals("whywwww", a.get());

        processor.register("optionalArgument", (rc) -> rc
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

        Assertions.assertFalse(execute(processor, "/optionalArgument owo"));
        Assertions.assertEquals(MISSING_REQUIRED_ARGUMENT, processor.getLastError().type);

        Assertions.assertTrue(execute(processor, "/optionalArgument whywwww owo"));
        Assertions.assertEquals("whywwww", a.get());

        Assertions.assertTrue(execute(processor, "/optionalArgument whywwww aaa owo"));
        Assertions.assertEquals("whywwwwaaa", a.get());
    }

    @Test
    public void invalidArrayArgument() {
        var processor = new CommandProcessor();

        Assertions.assertThrowsExactly(IllegalStateException.class, ()->{
            processor.register("base", (rc) -> rc
                .requireArgument("arg")
                .requireArrayArgument("argArr")
                .requireArgument("arg2")
            );
        });
        Assertions.assertThrowsExactly(IllegalStateException.class, ()->{
            processor.register("base", (rc) -> rc
                .requireArgument("arg")
                .requireArrayArgument("argArr")
                .findArgument("arg2")
            );
        });
        Assertions.assertThrowsExactly(IllegalStateException.class, ()->{
            processor.register("base", (rc) -> rc
                .requireArgument("arg")
                .requireArrayArgument("argArr")
                .requireArrayArgument("argArr2")
            );
        });
        Assertions.assertThrowsExactly(IllegalStateException.class, ()->{
            processor.register("base", (rc) -> rc
                .findArgument("arg")
                .requireArrayArgument("argArr")
            );
        });
    }

    @Test
    public void arrayArgument() {
        var processor = new CommandProcessor();
        AtomicReference<String> a = new AtomicReference<>("");
        processor.register("base", (rc) -> rc
            .requireArrayArgument("arg")
            .executes((ctx)->
                ctx.getArray("arg")
                    .forEach((it) -> a.set(a.get() + " " + it)))
        );
        Assertions.assertTrue(execute(processor, "/base"));
        Assertions.assertEquals("", a.get());
        a.set("");

        Assertions.assertTrue(execute(processor, "/base arg1"));
        Assertions.assertEquals(" arg1", a.get());
        a.set("");

        Assertions.assertTrue(execute(processor, "/base arg1 arg2"));
        Assertions.assertEquals(" arg1 arg2", a.get());
        a.set("");
    }

    @Test
    public void invalidContextArgumentsUse() {
        var processor = new CommandProcessor();
        AtomicReference<String> a = new AtomicReference<>("");
        processor.register("base1", (rc) -> rc
            .requireArrayArgument("arg")
            .executes((ctx)->
                a.set(ctx.getString("arg"))
            ));
        processor.register("base2", (rc) -> rc
            .requireArgument("arg")
            .executes((ctx)->
                ctx.getArray("arg")
                    .forEach((it) -> a.set(a.get() + " " + it)))
            );
        processor.register("base3", (rc) -> rc
            .requireArgument("arg")
            .executes((ctx)->
                a.set(ctx.getString("arg"))
            ));
        processor.register("base4", (rc) -> rc
            .requireArrayArgument("arg")
            .executes((ctx)->
                ctx.getArray("arg")
                    .forEach((it) -> a.set(a.get() + " " + it)))
        );

        Assertions.assertThrowsExactly(IllegalArgumentException.class, ()->
            processor.execute("/base1")
        );
        Assertions.assertThrowsExactly(IllegalArgumentException.class, ()->
            processor.execute("/base2 a")
        );

        Assertions.assertTrue(execute(processor, "/base3 arg1"));
        Assertions.assertEquals("arg1", a.get());
        a.set("");

        Assertions.assertFalse(execute(processor, "/base3 arg1 arg2"));
        Assertions.assertEquals(UNKNOWN_SUBCOMMAND, processor.getLastError().type);
        a.set("");

        Assertions.assertTrue(execute(processor, "/base4 arg1"));
        Assertions.assertEquals(" arg1", a.get());
        a.set("");
        Assertions.assertTrue(execute(processor, "/base4 arg1 arg2"));
        Assertions.assertEquals(" arg1 arg2", a.get());
        a.set("");
    }

    @Test
    public void arrayArgumentSubcommand() {
        var processor = new CommandProcessor();
        AtomicReference<String> a = new AtomicReference<>("");
        processor.register("base", (rc) -> rc
            .requireArrayArgument("arg")
            .subcommand("sub", (sb) -> sb
                .executes((ctx)->
                    ctx.getArray("arg")
                        .forEach((it) -> a.set(a.get() + " " + it)))
            )
        );
        Assertions.assertFalse(execute(processor, "/base"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        a.set("");

        Assertions.assertFalse(execute(processor, "/base arg1"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        a.set("");

        Assertions.assertFalse(execute(processor, "/base arg1 arg2"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        a.set("");

        Assertions.assertFalse(execute(processor, "/base arg1 arg2 arg3"));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        a.set("");

        Assertions.assertFalse(execute(processor, "/base arg1 arg2 arg3 \"sub\""));
        Assertions.assertEquals(FURTHER_SUBCOMMANDS_EXPECTED, processor.getLastError().type);
        a.set("");

        Assertions.assertTrue(execute(processor, "/base sub"));
        Assertions.assertEquals("", a.get());
        a.set("");

        Assertions.assertTrue(execute(processor, "/base arg1 sub"));
        Assertions.assertEquals(" arg1", a.get());
        a.set("");

        Assertions.assertTrue(execute(processor, "/base arg1 arg2 sub"));
        Assertions.assertEquals(" arg1 arg2", a.get());
        a.set("");
    }

}
