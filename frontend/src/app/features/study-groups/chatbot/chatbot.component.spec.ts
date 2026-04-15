import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChatbotComponent } from './chatbot.component';
import { StudyGroupService } from '../../../services/study-group.service';
import { of, throwError } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ChatbotComponent', () => {
  let component: ChatbotComponent;
  let fixture: ComponentFixture<ChatbotComponent>;
  let studyGroupService: jasmine.SpyObj<StudyGroupService>;

  beforeEach(() => {
    const spy = jasmine.createSpyObj('StudyGroupService', ['chat']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, FormsModule],
      declarations: [ChatbotComponent],
      providers: [
        { provide: StudyGroupService, useValue: spy }
      ]
    });

    fixture = TestBed.createComponent(ChatbotComponent);
    component = fixture.componentInstance;
    studyGroupService = TestBed.inject(StudyGroupService) as jasmine.SpyObj<StudyGroupService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should toggle chat visibility', () => {
    expect(component.isOpen).toBeFalse();
    component.toggleChat();
    expect(component.isOpen).toBeTrue();
    component.toggleChat();
    expect(component.isOpen).toBeFalse();
  });

  it('should not send message if input is empty', () => {
    component.userInput = '   ';
    component.sendMessage();
    expect(studyGroupService.chat).not.toHaveBeenCalled();
  });

  it('should send message and handle successful response', () => {
    const userMsg = 'Hello chatbot';
    const botReply = 'Hi there!';
    component.userInput = userMsg;
    component.groupId = 123;
    studyGroupService.chat.and.returnValue(of({ reply: botReply }));

    component.sendMessage();

    expect(component.messages.length).toBe(3); // Initial + User + Bot
    expect(component.messages[1]).toEqual({ role: 'user', text: userMsg });
    expect(component.messages[2]).toEqual({ role: 'bot', text: botReply });
    expect(studyGroupService.chat).toHaveBeenCalledWith(userMsg, 123);
    expect(component.userInput).toBe('');
    expect(component.loading).toBeFalse();
  });

  it('should handle error during sendMessage', () => {
    component.userInput = 'Trigger error';
    studyGroupService.chat.and.returnValue(throwError(() => new Error('API Error')));

    component.sendMessage();

    expect(component.messages.length).toBe(3);
    expect(component.messages[2].text).toBe('Erreur de connexion au chatbot.');
    expect(component.loading).toBeFalse();
  });

  it('should call sendMessage on Enter key down', () => {
    spyOn(component, 'sendMessage');
    const event = new KeyboardEvent('keydown', { key: 'Enter' });
    component.onKeyDown(event);
    expect(component.sendMessage).toHaveBeenCalled();
  });

  it('should not call sendMessage on other key down', () => {
    spyOn(component, 'sendMessage');
    const event = new KeyboardEvent('keydown', { key: 'Shift' });
    component.onKeyDown(event);
    expect(component.sendMessage).not.toHaveBeenCalled();
  });
});
