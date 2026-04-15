package tn.spring.appointment.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Le préfixe pour recevoir des messages (le "Live")
        config.enableSimpleBroker("/topic");
        // Le préfixe pour envoyer des messages depuis le client
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // L'URL de connexion que ton Angular va appeler
        registry.addEndpoint("/ws-chat")
                .setAllowedOrigins("http://localhost:4200")
                .withSockJS();
    }
}