import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpBackend } from '@angular/common/http';
import { Observable, map, switchMap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ChatMessage {
  role: 'system' | 'user' | 'assistant';
  content: string;
}

@Injectable({ providedIn: 'root' })
export class AiChatService {
  private apiUrl = 'https://api.groq.com/openai/v1/chat/completions';
  private apiKey = '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';
  private gatewayUrl = environment.gatewayUrl;
  private conversationHistory: ChatMessage[] = [];
  private httpDirect: HttpClient;

  constructor(private http: HttpClient, handler: HttpBackend) {
    this.httpDirect = new HttpClient(handler);
  }

  private getSystemPrompt(clubs: any[], events: any[]): string {
    const base = window.location.origin;

    const clubLinks = clubs.map(c =>
      `- **${c.name}** — ${c.description || 'No description'} | ${c.memberCount || 0} members, ${c.eventCount || 0} events | Link: ${base}/student/clubs/${c.id}`
    ).join('\n');

    const eventLinks = events.map(e =>
      `- **${e.title}** — ${e.description || ''} | Club: ${e.clubName || '?'} | Date: ${e.eventDate || '?'} | Location: ${e.location || '?'} | ${e.paid ? 'Paid: ' + e.price + ' TND' : 'Free'} | Link: ${base}/student/events/${e.id}`
    ).join('\n');

    return `You are LinguaBot, the friendly AI assistant for LinguaAcademy. You help students discover clubs, find events, and navigate the app.

CLUBS:
${clubLinks || 'No clubs yet.'}

EVENTS:
${eventLinks || 'No events yet.'}

PAGE LINKS (use these exact URLs when directing users):
- All Clubs: ${base}/student/clubs
- All Events: ${base}/student/events
- My Tickets: ${base}/student/my-tickets
- Create a Club: ${base}/student/clubs/create

RULES:
- NEVER show raw URLs or routes. Instead use markdown links like [Browse Clubs](${base}/student/clubs) so the user sees friendly clickable text.
- Examples: [View esprit teams](${base}/student/clubs/2), [See all events](${base}/student/events), [Create a club](${base}/student/clubs/create)
- When recommending a club or event, always include a clickable link with a friendly label.
- Be short, warm, and conversational (2-3 sentences max).
- Only talk about things in the app — clubs, events, tickets, courses.
- Never invent clubs or events that are not in the data above.
- For paid events, always mention the price.
- If nothing matches what the user wants, suggest they create their own club.`;
  }

  private fetchContext(): Observable<{ clubs: any[]; events: any[] }> {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    const userId = user ? JSON.parse(user).email : '';
    const headers = token
      ? new HttpHeaders().set('Authorization', `Bearer ${token}`)
      : undefined;

    return this.http.get<any[]>(`${this.gatewayUrl}/api/clubs?userId=${userId}`, { headers }).pipe(
      switchMap(clubs =>
        this.http.get<any[]>(`${this.gatewayUrl}/api/events?userId=${userId}`, { headers }).pipe(
          map(events => ({ clubs, events }))
        )
      )
    );
  }

  sendMessage(userMessage: string): Observable<string> {
    return this.fetchContext().pipe(
      switchMap(({ clubs, events }) => {
        if (this.conversationHistory.length === 0) {
          this.conversationHistory.push({
            role: 'system',
            content: this.getSystemPrompt(clubs, events)
          });
        } else {
          this.conversationHistory[0] = {
            role: 'system',
            content: this.getSystemPrompt(clubs, events)
          };
        }

        this.conversationHistory.push({ role: 'user', content: userMessage });

        const headers = new HttpHeaders({
          'Authorization': `Bearer ${this.apiKey}`,
          'Content-Type': 'application/json'
        });

        const body = {
          model: 'llama-3.3-70b-versatile',
          messages: this.conversationHistory,
          temperature: 0.7,
          max_tokens: 512
        };

        return this.httpDirect.post<any>(this.apiUrl, body, { headers }).pipe(
          map(res => {
            const reply = res.choices[0].message.content;
            this.conversationHistory.push({ role: 'assistant', content: reply });
            return reply;
          })
        );
      })
    );
  }

  clearHistory() {
    this.conversationHistory = [];
  }
}
