package cli.utils;

import cli.Context;
import utils.kt.Check;
import utils.kt.CheckIf;

public class Condition<T> {
    public final String message;
    public final CheckIf<Context<T>> checker;

    public Condition(String message, CheckIf<Context<T>> checker) {
        this.message = message;
        this.checker = checker;
    }

    public Condition(String message, Check checker) {
        this.message = message;
        this.checker = (it) -> checker.check();
    }
}