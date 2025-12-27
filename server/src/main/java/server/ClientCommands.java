package server;

import utils.cli.CustomCommandProcessor;
import utils.cli.utils.Condition;
import server.elements.Client;
import server.elements.Group;
import server.elements.ServerData;
import server.elements.User;
import utils.Ansi;
import utils.elements.ClientTypes;
import utils.extensions.CollectionExt;

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
        new Condition<>(Ansi.applyHtml("You aren't logged in.", Ansi.Colors.RED), (ctx) ->
            ctx.data.isAuthenticated()
        );

    public static final CustomCommandProcessor<ClientContextData> processor =
        new CustomCommandProcessor<>();

    public static void init() {
        accountCategoryInit();
        friendsCategoryInit();
        groupsCategoryInit();
    }

    private static void accountCategoryInit() {
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
//                ctx.out.println("Successfully logged out.");
                ctx.out.stylePrint(Ansi.Colors.GREEN,
                        "Successfully logged out.");
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
                    var user = User.getUserByUsername(ctx.getString("username"));
                    if (user == null) {
                        ctx.out.stylePrint(Ansi.Colors.RED, "User not found.");
                        return;
                    }

//                    ctx.out.print(user.getProfile());
                    ctx.out.println(user.getProfile(ctx.out.printAsHtml));
                    // TODO: получаем и отображаем профиль человека
                } else {
                    ctx.out.println(ctx.data.user.getProfile(ctx.out.printAsHtml));
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
                    ctx.out.stylePrint(Ansi.Colors.RED, "Group not found.");
                    return;
                }

                if (CollectionExt.findBy(
                    group.getMembersId(),
                    (it) -> it.equals(ctx.data.client.user.getId())
                ) == null) {
                    ctx.out.stylePrintln(Ansi.Colors.RED, "You are not a member of that group.");
                    return;
                }
                ctx.data.client.group = group;
                boolean isHtml = ctx.data.client.type == ClientTypes.GUI;
                for (var m : group.getMessages())
                    ctx.data.client.sendln(m.getSenderId() == ctx.data.client.user.getId()
                        ? m.getFormattedSelf(isHtml)
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
                    if (list.isEmpty()) {
                        ctx.out.println("No friends.");
                        ctx.out.stylePrint(Ansi.Colors.RED, "No friends.");
                    } else
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
            .subcommand("list", (b) -> b
                .subcommand("members", (c) -> c
                    .require("Open group first", (ctx) -> ctx.data.group != null)
                    .executes((ctx) -> {
                        var group = ctx.data.group;
                        group.loadMemberList();
                        ctx.out.print(Ansi.Colors.Bright.BLACK);
                        ctx.out.println("Admins of \"" + group.getName() + "\"");
                        ctx.out.println("---------------");
                        ctx.out.print(Ansi.Modes.RESET);
                        group.admins.forEach(ctx.out::println);
                        ctx.out.print(Ansi.Colors.Bright.BLACK);
                        ctx.out.println("Members of \"" + group.getName() + "\"");
                        ctx.out.println("---------------");
                        ctx.out.print(Ansi.Modes.RESET);
                        group.members.forEach(ctx.out::println);
                        ctx.out.println();
                    })
                )
                .executes((ctx) -> {
                    var groups = ctx.data.user.getGroups();
                    if (groups == null) {
                        ctx.out.println("Something went wrong!");
                        return;
                    }
                    ctx.out.println(" Your groups");
                    ctx.out.println("---------------");
                    for (Group group : groups) {
                        ctx.out.println(group.getName());
                    }
                })
            )
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
                .require("Open group first", (ctx) -> ctx.data.group != null)
                .require("You don't have permission to edit this group",
                    (ctx) -> ctx.data.group.hasAdmin(ctx.data.user)
                )
                .executes((ctx) -> {
                    var group = ctx.data.group;
                    ctx.data.client.sendln(
                        "/ask deletion " + group.getGroupname() + " " + group.getName()
                    );
                })
            )
            .subcommand("invite", (b) -> b
                .require("Open group first", (ctx) -> ctx.data.group != null)
                .require("You don't have permission to add members",
                    (ctx) -> ctx.data.group.hasAdmin(ctx.data.user)
                )
                .requireArgument("username")
                .executes((ctx) ->
                    ctx.data.group.invite(
                        ctx.out,
                        ctx.getString("username"),
                        ctx.data.group.getIdGroup()
                    ))
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
                .require("Open group first", (ctx) -> ctx.data.group != null)
                .require("You don't have permission to kick members",
                    (ctx) -> ctx.data.group.hasAdmin(ctx.data.user)
                )
                .requireArgument("username")
                .executes((ctx) ->
                    ctx.data.group.kick(
                        ctx.out,
                        ctx.getString("username"),
                        ctx.data.group.getGroupname()
                    )
                )
            )
            .subcommand("exit", (b) -> b
                .require("Open group first", (ctx) -> ctx.data.group != null)
                .require("Chown ownership before exiting.",
                    (ctx) -> !ctx.data.group.isOwner(ctx.data.user)
                )
                .executes((ctx) -> {
                    var group = ctx.data.group;
                    ctx.data.client.sendln(
                        "/ask exit_group " + group.getGroupname() + " " + group.getName()
                    );
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
