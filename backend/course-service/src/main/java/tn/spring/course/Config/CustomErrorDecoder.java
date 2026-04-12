package tn.spring.course.Config;

import feign.Response;
import feign.codec.ErrorDecoder;
import tn.spring.course.Exceptions.ResourceNotFoundException;
import tn.spring.course.Exceptions.UnauthorizedException;

public class CustomErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new ResourceNotFoundException("Le tuteur avec cet ID n'existe pas dans le système User.");
            case 403 -> new UnauthorizedException("Accès refusé au service User. Le token est manquant, expiré, ou n'a pas les droits ADMIN.");
            case 401 -> new UnauthorizedException("Non authentifié pour appeler le service User.");
            default -> defaultErrorDecoder.decode(methodKey, response);
        };
    }
}
