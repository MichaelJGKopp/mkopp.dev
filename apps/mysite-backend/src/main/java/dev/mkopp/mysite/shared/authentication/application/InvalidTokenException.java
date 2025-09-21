package dev.mkopp.mysite.shared.authentication.application;

public class InvalidTokenException extends AuthenticationException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
}