import cli.CustomCommandProcessor;
import cli.utils.Condition;
import cli.utils.ContextData;
import elements.Client;
import elements.Group;
import elements.User;
import utils.Ansi;

public class ClientCommands {

    private static final Condition<ClientContextData> requireAuth =
        new Condition<>("You aren't logged in.", (ctx) ->
            ctx.data.isAuthenticated()
        );

    public static class ClientContextData extends ContextData {
        public Client client;
        public User user;
        public Group group;

        public ClientContextData(Client client, User user, Group group) {
            this.client = client;
            this.user = user;
            this.group = group;
        }

        public boolean isAuthenticated() {
            return user != null;
        }
    }

    public static CustomCommandProcessor<ClientContextData> processor =
        new CustomCommandProcessor<>();

    public static void init() {
        accountCategoryInit();
        friendsCategoryInit();
        groupsCategoryInit();
    }

    private static void accountCategoryInit() {
        processor.register("register", (a) -> a
            .description("Создание нового аккаунта")
            .require("You are already logged in.", (ctx) ->
                !ctx.data.isAuthenticated()
            )
            .requireArgument("username")
            .requireArgument("password")
            .executes((ctx) ->
                ctx.out.println("Register")
            )
        );
        processor.register("login", (a) -> a
            .description("Вход в существующий аккаунт")
            .require("You are already logged in.", (ctx) ->
                !ctx.data.isAuthenticated()
            )
            .requireArgument("username")
            .requireArgument("password")
            .executes((ctx) ->
                ctx.out.println("Login")
            )
        );
        processor.register("logout", (a) -> a
            .description("Выход из учетной записи")
            .require(requireAuth)
            .executes((ctx) -> {
                ctx.data.user = null;
                ctx.out.println("Successfully logged out.");
            })
        );
        processor.register("changeName", (a) -> a
            .description("Устанавливает новый ник")
            .require(requireAuth)
            .requireArgument("nickname")
            .executes((ctx) -> {
                ctx.data.user.setName(ctx.getString("nickname"));
                ctx.out.println("Successfully changed nickname.");
            })
        );
        processor.register("changePassword", (a) -> a
            .description("Устанавливает новый пароль")
            .require(requireAuth)
            .requireArgument("oldPassword")
            .requireArgument("password")
            .requireArgument("passwordAgain")
            .executes((ctx) -> {
                var old = ctx.getString("oldPassword");
                var password = ctx.getString("password");
                var again = ctx.getString("passwordAgain");

                if (!ctx.data.user.getPassword().equals(old)) {
                    ctx.out.println(Ansi.Colors.RED.apply("Invalid password."));
                    return;
                }

                if (!password.equals(again)) {
                    ctx.out.println(Ansi.Colors.RED.apply("Passwords do not match."));
                    return;
                }
                ctx.out.println("Successfully changed password.");
                // TODO: смена пароля
            })
        );
        processor.register("profile", (a) -> a
            .description(
                "Показывает профиль пользователя @username."
                    + "Если второй аргумент не задан, показывается свой профиль"
            )
            .require(requireAuth)
            .findArgument("username")
            .executes((ctx) -> {
                if (ctx.hasArgument("username")) {
                    // TODO: получаем и отображаем профиль человека
                    ctx.out.println("Профиль: " + ctx.getString("username"));
                } else {
                    ctx.out.println(ctx.data.user.getProfile());
                }
            })
        );
    }

    private static void friendsCategoryInit() {
        processor.register("friends", (a) -> a
            .description("Управление списком друзей")
            .require(requireAuth)
            .subcommand("list", (b) -> b
                .description("Выводит список друзей")
                .executes((ctx) -> {
                    for (int friend : ctx.data.user.getFriends())
                        ctx.out.println(friend);
                })
            )
            .subcommand("request", (b) -> b
                .description("Отправка запроса на дружбу пользователю @username")
                .requireArgument("username")
                .executes((ctx) -> ctx.data.user
                    .sendFriendRequest(ctx.getString("username"))
                )
            )
            .subcommand("accept", (b) -> b
                .description(
                    "Принятие запроса на дружбу от @username, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("username")
                .executes((ctx) ->
                    // TODO: да
                    ctx.out.println("Accepted")
                )
            )
            .subcommand("deny", (b) -> b
                .description(
                    "Отклонение запроса на дружбу от @username, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("username")
                .executes((ctx) ->
                    // TODO: да
                    ctx.out.println("Declined")
                )
            )
            .subcommand("remove", (b) -> b
                .description(
                    "Удаление запроса на дружбу от @username из друзей"
                )
                .requireArgument("username")
                .executes((ctx) ->
                    // TODO: да
                    ctx.out.println("Remove")
                )
            )
        );
    }

    private static void groupsCategoryInit() {
        processor.register("groups", (a) -> a
            .description("Управление группами")
            .require(requireAuth)
            .subcommand("create", (b) -> b
                .description("Создает группу #groupname с названием name.")
                .requireArgument("groupname")
                .findArgument("name")
                .executes((ctx) -> {
                    // TODO: yea
                    ctx.out.println("Created");
                })
            )
            .subcommand("delete", (b) -> b
                .description("Удаляет группу")
                .executes((ctx) -> {
                    // TODO: yea....
                    ctx.out.println("Deleted");
                })
            )
            .subcommand("invite", (b) -> b
                .description("Приглашает @username в группу.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ...
                    ctx.out.println("Invited");
                })
            )
            .subcommand("accept", (b) -> b
                .description(
                    "Принятие приглашения в группу #groupname, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("groupname")
                .executes((ctx) ->
                    // TODO: .......
                    ctx.out.println("Accepted")
                )
            )
            .subcommand("deny", (b) -> b
                .description(
                    "Отклонение приглашения в группу #groupname, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("groupname")
                .executes((ctx) ->
                    // TODO: ..... *глубокий затяг*
                    ctx.out.println("Declined")
                )
            )
            .subcommand("kick", (b) -> b
                .description("Исключает @username из группы.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ...
                    ctx.out.println("Kicked");
                })
            )
            .subcommand("ban", (b) -> b
                .description("Банит @username из группы.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ..Окей, это уже неловко
                    ctx.out.println("Banned");
                })
            )
            .subcommand("to", (b) -> b
                .description("Конвертирует группу")
                .subcommand("channel", (c) -> c
                    .executes((ctx) -> {
                        // TODO: Не сбавляем темп неловкости
                        ctx.out.println("To channel");
                    })
                )
                .subcommand("group", (c) -> c
                    .executes((ctx) -> {
                        // TODO: Стенли был человек молодой
                        ctx.out.println("To group");
                    })
                )
            )
            .subcommand("changeName", (b) -> b
                .description("Меняет название группы.")
                .requireArgument("newName")
                .executes((ctx) -> {
                    // TODO: Хоть все и думали, что он пожилой
                    ctx.out.println("Changed name");
                })
            )
            .subcommand("op", (b) -> b
                .description("Делает @username админом группы.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: Ну а истина в том, что он был молодцом...
                    ctx.out.println("Op");
                })
            )
            .subcommand("deop", (b) -> b
                .description("Исключает @username из админов группы.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ... Просто немного странным.
                    ctx.out.println("deop");
                })
            )
            .subcommand("chown", (b) -> b
                .description("Передаёт права на группу @username.")
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: [[Ссылка удалена]]
                    ctx.out.println("chown");
                })
            )

        );
    }
}
