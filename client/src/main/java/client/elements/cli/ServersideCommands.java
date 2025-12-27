package client.elements.cli;

import utils.cli.CommandProcessor;

/**
 * Описания команд, которые клиент может отправлять серверу.
 */
public class ServersideCommands {

    public static void init(CommandProcessor processor) {
        accountCategoryInit(processor);
        friendsCategoryInit(processor);
        groupsCategoryInit(processor);
    }

    private static void accountCategoryInit(CommandProcessor processor) {
        processor.register("register", (a) -> a
            .isPhantom()
            .description("Создание нового аккаунта")
            .requireArgument("username")
            .requireArgument("password")
        );
        processor.register("login", (a) -> a
            .isPhantom()
            .description("Вход в существующий аккаунт")
            .requireArgument("username")
            .requireArgument("password")
        );
        processor.register("logout", (a) -> a
            .isPhantom()
            .description("Выход из учетной записи")
        );
        processor.register("changeName", (a) -> a
            .isPhantom()
            .description("Устанавливает новый ник")
            .requireArgument("nickname")
        );
        processor.register("changePassword", (a) -> a
            .isPhantom()
            .description("Устанавливает новый пароль")
            .requireArgument("oldPassword")
            .requireArgument("password")
            .requireArgument("passwordAgain")
        );
        processor.register("profile", (a) -> a
            .isPhantom()
            .description(
                "Показывает профиль пользователя @username."
                    + "Если второй аргумент не задан, показывается свой профиль"
            )
            .findArgument("username")
        );
        processor.register("open", (a) -> a
            .isPhantom()
            .description("Открывает чат")
            .requireArgument("groupname")
        );
    }

    private static void friendsCategoryInit(CommandProcessor processor) {
        processor.register("friends", (a) -> a
            .isPhantom()
            .description("Управление списком друзей")
            .subcommand("list", (b) -> b
                .description("Выводит список друзей")
            )
            .subcommand("request", (b) -> b
                .description("Отправка запроса на дружбу пользователю @username")
                .requireArgument("username")
                .subcommand("dismiss", (c) -> c
                    .description("Отзыв отправленного запроса на дружбу пользователю @username")
                )
            )
            .subcommand("accept", (b) -> b
                .description(
                    "Принятие запроса на дружбу от @username, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("username")
            )
            .subcommand("deny", (b) -> b
                .description(
                    "Отклонение запроса на дружбу от @username, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("username")
            )
            .subcommand("remove", (b) -> b
                .description(
                    "Удаление запроса на дружбу от @username из друзей"
                )
                .requireArgument("username")
            )
        );
    }

    private static void groupsCategoryInit(CommandProcessor processor) {
        processor.register("groups", (a) -> a
            .isPhantom()
            .description("Управление группами")
            .subcommand("list", (b) -> b
                .description("Вывод списка всех групп, в которых вы состоите")
                .subcommand("members", (c) -> c
                    .description("Вывод списка всех участников текущей группы")
                )
            )
            .subcommand("create", (b) -> b
                .description("Создает группу #groupname с названием name.")
                .requireArgument("groupname")
                .findArgument("name")
            )
            .subcommand("delete", (b) -> b
                .description("Удаляет группу")
            )
            .subcommand("invite", (b) -> b
                .description("Приглашает @username в группу.")
                .requireArgument("username")
            )
            .subcommand("accept", (b) -> b
                .description(
                    "Принятие приглашения в группу #groupname, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("groupname")
            )
            .subcommand("deny", (b) -> b
                .description(
                    "Отклонение приглашения в группу #groupname, либо,"
                        + "если аргумент не дан, первого попавшегося"
                )
                .findArgument("groupname")
            )
            .subcommand("kick", (b) -> b
                .description("Исключает @username из группы.")
                .requireArgument("username")
            )
            .subcommand("exit", (b) -> b
                .description("Позволяет выйти из группы")
            )
            .subcommand("ban", (b) -> b
                .description("Банит @username из группы.")
                .requireArgument("username")
            )
            .subcommand("to", (b) -> b
                .description("Конвертирует группу в указанный тип")
                .subcommand("channel", (c) -> c
                    .description("Конвертирует группу в канал")
                )
                .subcommand("group", (c) -> c
                    .description("Конвертирует группу в группу")
                )
            )
            .subcommand("changeName", (b) -> b
                .description("Меняет название группы.")
                .requireArgument("newName")
            )
            .subcommand("op", (b) -> b
                .description("Делает @username админом группы.")
                .requireArgument("username")
            )
            .subcommand("deop", (b) -> b
                .description("Исключает @username из админов группы.")
                .requireArgument("username")
            )
            .subcommand("chown", (b) -> b
                .description("Передаёт права на группу @username.")
                .requireArgument("username")
            )
        );
    }
}
