package dev.mkopp.mysite.shared.error.domain;

// You can place this in your domain error package
public class InvalidUserIdException extends RuntimeException {

    private final String value;

    public InvalidUserIdException(String value, Throwable cause) {
        super("Invalid format for UserId: \"" + value + "\"", cause);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}