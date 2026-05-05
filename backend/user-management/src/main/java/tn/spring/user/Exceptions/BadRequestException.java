package tn.spring.user.Exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}