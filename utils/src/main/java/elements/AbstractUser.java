package elements;

import utils.Ansi;
import utils.extensions.StringExt;

import java.util.ArrayList;

public abstract class AbstractUser {
    protected int id;
    protected String userName;
    protected String name;
    protected String password;
    protected ArrayList<Integer> friends; // массив id'шников друзей

    public ArrayList<Integer> request;

    public abstract void sendMessage(String text, int id);

    /*Реализация этого запроса будет переписана*/
    public ArrayList<Integer> joinGroup(int id) {
        // ? Extends Group
        // id группы, в которую вступает пользователь.
        // Возвращает переданный на сервер id пользователя?

        // отправка запроса на сервер на вступление в группу
        request.add(id);
        request.add(this.id); // id пользователя
        return request;
    }

    public String getProfile() {
        var boxSize = 50;
        var trimmedName = StringExt.limit(this.name, boxSize - 4);
        var trimmedUsername = StringExt.limit(this.userName, boxSize - 5);

        var headerColor = Ansi.BgColors.fromRgb(34, 55, 75);

        return """
            ┌%s┐
            │%s│
            │ @%s │
            │
            └%s┘
            """.formatted(
            "─".repeat(boxSize - 2),
            headerColor.apply(" ") + Ansi.Modes.BOLD.and(headerColor).apply(trimmedName)
                + headerColor.apply(" ".repeat(boxSize - 3 - trimmedName.length())),
            this.userName + " ".repeat(boxSize - 5 - trimmedUsername.length()),
            "─".repeat(boxSize - 2)
        );
    }

    public int getUserId() {
        return this.id;
    }

    public ArrayList<Integer> getFriends() {
        return this.friends;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public abstract void setName(String name);

    public abstract void setPassword(String password);

}
