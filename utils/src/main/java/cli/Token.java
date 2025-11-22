package cli;

public record Token(String content, int start, int end, boolean isArgument) {

    public boolean isFunctional(String obj) {
        return content.equals(obj) && !isArgument;
    }

    public boolean is(String obj) {
        return content.equals(obj);
    }

}
