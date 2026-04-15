package tn.spring.appointment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.appointment.Models.ChatMessage;
import tn.spring.appointment.Repositories.ChatMessageRepository;
import tn.spring.appointment.Services.ChatService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("Logique : Attribution auto de l'heure du message")
    void testSaveMessageSetsTime() {
        ChatMessage msg = new ChatMessage();
        msg.setContent("Hello World");
        when(chatMessageRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ChatMessage saved = chatService.saveMessage(msg);

        assertNotNull(saved.getTimestamp());
        verify(chatMessageRepository).save(any());
    }
}
