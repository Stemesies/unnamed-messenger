package server;

import utils.cli.CustomCommandProcessor;
import utils.cli.utils.Condition;
import server.elements.Client;
import server.elements.Group;
import server.elements.ServerData;
import server.elements.User;
import utils.Ansi;
import utils.extensions.CollectionExt;

import server.managers.DatabaseManager;

public class ClientCommands {
    public static class ClientContextData {
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

    private static final Condition<ClientContextData> requireAuth =
        new Condition<>("You aren't logged in.", (ctx) ->
            ctx.data.isAuthenticated()
        );

    public static final CustomCommandProcessor<ClientContextData> processor =
        new CustomCommandProcessor<>();

    public static void init() {
        // TODO: Заменить
        accountCategoryInit();
        friendsCategoryInit();
        groupsCategoryInit();

        DatabaseManager.init();
    }

    private static void accountCategoryInit() {
        processor.register("require", (client) -> client
                .isInvisible()
                .subcommand("type", (type) -> type
                        .executes((smth) -> {
                            return;
                        })
                ));
        processor.register("register", (a) -> a
            .require("You are already logged in.", (ctx) -> !ctx.data.isAuthenticated())
            .requireArgument("username")
            .requireArgument("password")
            .executes((ctx) -> {
                ctx.data.client.user = User.register(
                    ctx.out,
                    ctx.getString("username"),
                    ctx.getString("password")
                );
                if (ctx.data.client.user != null)
                    ServerData.getRegisteredClients().add(ctx.data.client);
            })
        );
        processor.register("login", (a) -> a
            .require("You are already logged in.", (ctx) -> !ctx.data.isAuthenticated())
            .requireArgument("username")
            .requireArgument("password")
            .executes((ctx) -> {
                ctx.data.client.user = User.logIn(
                    ctx.out,
                    ctx.getString("username"),
                    ctx.getString("password")
                );
                if (ctx.data.client.user != null)
                    ServerData.getRegisteredClients().add(ctx.data.client);
            })
        );
        processor.register("logout", (a) -> a
            .require(requireAuth)
            .executes((ctx) -> {
                ctx.data.client.user = null;
                ServerData.getRegisteredClients().remove(ctx.data.client);
                ctx.out.println("Successfully logged out.");
            })
        );
        processor.register("changeName", (a) -> a
            .require(requireAuth)
            .requireArgument("nickname")
            .executes((ctx) -> ctx.data.user.changeName(
                ctx.out,
                ctx.getString("nickname")
            ))
        );
        processor.register("changePassword", (a) -> a
            .require(requireAuth)
            .requireArgument("oldPassword")
            .requireArgument("password")
            .requireArgument("passwordAgain")
            .executes((ctx) -> ctx.data.user.changePassword(
                ctx.out,
                ctx.getString("oldPassword"),
                ctx.getString("password"),
                ctx.getString("passwordAgain")
            ))
        );
        processor.register("profile", (a) -> a
            .require(requireAuth)
            .findArgument("username")
            .executes((ctx) -> {
                if (ctx.hasArgument("username")) {
                    var username = ctx.getString("username");
                    var user = CollectionExt.findBy(
                            ServerData.getRegisteredUsers(),
                            (it) -> it.getUserName().equals(username)
                    );
                    if (user == null) {
                        ctx.out.println(Ansi.Colors.RED.apply("User not found."));
                        return;
                    }

                    ctx.out.print(user.getProfile());
                    // TODO: получаем и отображаем профиль человека
                } else {
                    ctx.out.println(ctx.data.user.getProfile());
                }
            })
        );

        processor.register("open", (a) -> a
            .require(requireAuth)
            .requireArgument("groupname")
            .executes((ctx) -> {
                var groupname = ctx.getString("groupname");
                Group group = Group.getGroupByName(groupname);
                if (group == null) {
                    ctx.out.println(Ansi.Colors.RED.apply("Group not found."));
                    return;
                }

                if (CollectionExt.findBy(
                    group.getMembersId(),
                    (it) -> it.equals(ctx.data.client.user.getId())
                ) == null) {
                    ctx.out.println(Ansi.Colors.RED.apply("You are not a member of that group."));
                    return;
                }
                ctx.data.client.group = group;
                for (var m : group.getMessages())
                    ctx.data.client.sendln(m.getSenderId() == ctx.data.client.user.getId()
                        ? m.getFormattedSelf()
                        : m.getFormatted()
                    );
            })
        );
    }

    private static void friendsCategoryInit() {
        processor.register("friends", (a) -> a
            .require(requireAuth)
            .subcommand("list", (b) -> b
                .executes((ctx) -> {
                    var list = ctx.data.user.getFriendsId();
                    if (list.isEmpty())
                        ctx.out.println("No friends.");
                    else
                        list.forEach(ctx.out::println);
                })
            )
            .subcommand("request", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> ctx.data.user
                    .sendFriendRequest(ctx.out, ctx.getString("username"))
                )
                .subcommand("dismiss", (c -> c
                    .executes((ctx) -> ctx.data.user
                        .dismissFriendRequest(ctx.getString("username"))
                    )
                ))
            )
            .subcommand("accept", (b) -> b
                .findArgument("username")
                .executes((ctx) ->
                    // TODO: да
                    ctx.out.println("Accepted")
                )
            )
            .subcommand("deny", (b) -> b
                .findArgument("username")
                .executes((ctx) ->
                    // TODO: да
                    ctx.out.println("Declined")
                )
            )
            .subcommand("remove", (b) -> b
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
            .require(requireAuth)
            .subcommand("create", (b) -> b
                .description("Создает группу #groupname с названием name.")
                .requireArgument("groupname")
                .findArgument("name")
                .executes((ctx) -> {
                    var groupname = ctx.getString("groupname");
                    var name = ctx.hasArgument("name")
                        ? ctx.getString("name")
                        : groupname;

                    Group.register(
                        ctx.out,
                        ctx.data.user,
                        groupname,
                        name
                    );
                })
            )
            .subcommand("delete", (b) -> b
                .executes((ctx) -> {
                    // TODO: ...
                    ctx.out.println("Deleted");
                })
            )
            .subcommand("invite", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    ctx.data.group.invite(ctx.out, ctx.getString("username"),
                            ctx.data.group.getIdGroup());
                })
            )
            .subcommand("accept", (b) -> b
                .findArgument("groupname")
                .executes((ctx) ->
                    // TODO: .......
                    ctx.out.println("Accepted")
                )
            )
            .subcommand("deny", (b) -> b
                .findArgument("groupname")
                .executes((ctx) ->
                    // TODO: ..... *глубокий затяг*
                    ctx.out.println("Declined")
                )
            )
            .subcommand("kick", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ...
                    ctx.out.println("Kicked");
                })
            )
            .subcommand("ban", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ..Окей, это уже неловко
                    ctx.out.println("Banned");
                })
            )
            .subcommand("to", (b) -> b
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
                .requireArgument("newName")
                .executes((ctx) -> {
                    // TODO: Хоть все и думали, что он пожилой
                    ctx.out.println("Changed name");
                })
            )
            .subcommand("op", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: Ну а истина в том, что он был молодцом...
                    ctx.out.println("Op");
                })
            )
            .subcommand("deop", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: ... Просто немного странным.
                    ctx.out.println("deop");
                })
            )
            .subcommand("chown", (b) -> b
                .requireArgument("username")
                .executes((ctx) -> {
                    // TODO: [[Ссылка удалена]]
                    ctx.out.println("chown");
                })
            )

        );
    }
}
