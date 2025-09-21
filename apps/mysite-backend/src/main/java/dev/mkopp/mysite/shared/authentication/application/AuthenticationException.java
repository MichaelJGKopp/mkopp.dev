package dev.mkopp.mysite.shared.authentication.application;

abstract class AuthenticationException extends RuntimeException {
    
    AuthenticationException() {
        super();
    }
    
    AuthenticationException(String message) {
        super(message);
    }
}