package tn.spring.packagee.Exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}