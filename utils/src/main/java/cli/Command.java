package cli;

import java.util.ArrayList;
import java.util.List;

public class Command {

    String baseCommand;
    List<Command> subcommands;

    Runnable action = null;
    Condition condition = null;

    @FunctionalInterface
    public interface Condition {
        boolean check();
    }

    private Command(
            String baseCommand,
            List<Command> subcommands,
            Runnable action,
            Condition condition
    ) {
        this.baseCommand = baseCommand;
        this.subcommands = subcommands;
        this.action = action;
        this.condition = condition;
    }

    // -------

    public static class Builder {

        String baseCommand;
        ArrayList<Builder> subcommands = new ArrayList<>();

        Runnable action = null;
        Condition condition = null;

        public Builder(String baseCommand) {
            this.baseCommand = baseCommand;
        }

        /**
         * Устанавливает условие, при котором команда должна
         * запускаться или проходить дальше по иерархии.
         *
         * @param condition условие прохода
         */
        public Builder require(Command.Condition condition) {
            this.condition = condition;
            return this;
        }

        public Builder subcommand(String subcommand, Runnable subcommandSettings) {
            subcommands.add(new Builder(subcommand));
            subcommandSettings.run();
            return this;
        }

        /**
         * Устанавливает действие, которое будет воспроизводиться
         * при успешном выполнении команды.
         *
         * @param action проверки и действия
         */
        public Builder run(Runnable action) {
            this.action = action;
            return this;
        }

        public Command build() {
            return new Command(
                    baseCommand,
                    subcommands.stream()
                            .map(Builder::build)
                            .toList(),
                    action,
                    condition
            );
        }
    }
}
