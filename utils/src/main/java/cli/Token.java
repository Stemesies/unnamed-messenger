package cli;

public record Token(String content, int start, int end, boolean isArgument) {

    public boolean isFunctional() {
        return !isArgument;
    }

    public boolean is(String obj) {
        return content.equals(obj);
    }

}
