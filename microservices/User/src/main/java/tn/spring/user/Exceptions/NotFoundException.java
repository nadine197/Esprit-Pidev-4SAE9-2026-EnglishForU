package tn.spring.user.Exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}