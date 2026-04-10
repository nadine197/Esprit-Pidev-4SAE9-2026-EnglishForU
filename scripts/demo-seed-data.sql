BEGIN;

-- Full reset for demo environment data.
TRUNCATE TABLE
    notifications,
    report_activities,
    report_comments,
    reports,
    discussion_reactions,
    discussion_comments,
    discussion_posts,
    password_reset_tokens,
    student,
    tutor,
    users
RESTART IDENTITY CASCADE;

-- Password for all demo users: password
-- Teacher role is represented by TUTOR in current backend enum.
INSERT INTO users (
    id, active, pays, postal_code, region, rue, ville,
    email, last_name, name, password, phone, prefix, role
)
VALUES
    ('00000000-0000-0000-0000-000000000101', TRUE, 'Tunisia', '4000', 'Sousse', 'Rue des Formateurs 10', 'Sousse', 'teacher.demo@demo.englishforu.local', 'Ben Salah', 'Teacher', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5j4vYX0fOkHn84qxdjQ671O5jM90oS.', '+21650000101', '+216', 'TUTOR'),
    ('00000000-0000-0000-0000-000000000102', TRUE, 'Tunisia', '1002', 'Tunis', 'Rue de l''Etudiant 22', 'Tunis', 'student.demo@demo.englishforu.local', 'Jebali', 'Student', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5j4vYX0fOkHn84qxdjQ671O5jM90oS.', '+21650000102', '+216', 'STUDENT'),
    ('00000000-0000-0000-0000-000000000103', TRUE, 'Tunisia', '2035', 'Ariana', 'Rue Support 8', 'Ariana', 'helpdesk.demo@demo.englishforu.local', 'Trabelsi', 'Helpdesk', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5j4vYX0fOkHn84qxdjQ671O5jM90oS.', '+21650000103', '+216', 'HELP_DESK');

INSERT INTO tutor (id, availability_status, experience_years, rating, verified)
VALUES
    ('00000000-0000-0000-0000-000000000101', 'AVAILABLE', 7, 5, TRUE);

INSERT INTO student (id, daily_goal_minutes, english_level, learning_goal)
VALUES
    ('00000000-0000-0000-0000-000000000102', 35, 'B1', 'Prepare for internship interviews and improve speaking fluency');

-- Threaded community feed with all post types, including PDF media.
INSERT INTO discussion_posts (
    course_id, type, content, image_path, quiz_payload,
    author_email, author_role, author_level, target_role, target_level,
    created_at, updated_at
)
VALUES
    ('B1', 'TEXT', 'Demo thread: I keep confusing present perfect and past simple. Any fast way to remember?', NULL, NULL, 'student.demo@demo.englishforu.local', 'STUDENT', 'B1', NULL, 'B1', NOW() - INTERVAL '8 hours', NOW() - INTERVAL '7 hours 40 minutes'),
    ('B1', 'TEXT', 'Demo thread: Pronunciation tip of the day - practice TH with short daily recordings.', NULL, NULL, 'teacher.demo@demo.englishforu.local', 'TUTOR', 'C1', NULL, 'B1', NOW() - INTERVAL '7 hours', NOW() - INTERVAL '6 hours 45 minutes'),
    ('B2', 'IMAGE', 'Demo media post: Whiteboard snapshot from writing workshop.', 'https://picsum.photos/seed/efu-demo-whiteboard/1200/800', NULL, 'teacher.demo@demo.englishforu.local', 'TUTOR', 'C1', NULL, 'B2', NOW() - INTERVAL '6 hours', NOW() - INTERVAL '5 hours 40 minutes'),
    ('B2', 'IMAGE', 'Demo media post: PDF checklist for essay structure.', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', NULL, 'teacher.demo@demo.englishforu.local', 'TUTOR', 'C1', NULL, 'B2', NOW() - INTERVAL '5 hours', NOW() - INTERVAL '4 hours 45 minutes'),
    ('ALL_LEVELS', 'QUIZ', 'Demo quiz: Pick the grammatically correct sentence.', NULL, '{"question":"Which sentence is correct?","choices":["She go to class every day.","She goes to class every day.","She going to class every day."],"answer":"She goes to class every day."}', 'teacher.demo@demo.englishforu.local', 'TUTOR', 'C1', NULL, NULL, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '3 hours 30 minutes'),
    ('B1', 'TEXT', 'Demo thread: Reporting flow feedback - submit worked smoothly and help desk replied fast.', NULL, NULL, 'student.demo@demo.englishforu.local', 'STUDENT', 'B1', NULL, 'B1', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '90 minutes');

INSERT INTO discussion_comments (post_id, author_email, message, created_at)
VALUES
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: I keep confusing present perfect and past simple. Any fast way to remember?' LIMIT 1), 'teacher.demo@demo.englishforu.local', 'Use this rule: present perfect links past action to now, past simple is fully finished.', NOW() - INTERVAL '7 hours 20 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: I keep confusing present perfect and past simple. Any fast way to remember?' LIMIT 1), 'helpdesk.demo@demo.englishforu.local', 'Great question. We also turned this into a FAQ idea.', NOW() - INTERVAL '6 hours 50 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo media post: PDF checklist for essay structure.' LIMIT 1), 'student.demo@demo.englishforu.local', 'This PDF checklist helped me organize my paragraphs better.', NOW() - INTERVAL '4 hours 20 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo quiz: Pick the grammatically correct sentence.' LIMIT 1), 'student.demo@demo.englishforu.local', 'I answered correctly on second try. Useful quiz.', NOW() - INTERVAL '3 hours 5 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: Reporting flow feedback - submit worked smoothly and help desk replied fast.' LIMIT 1), 'helpdesk.demo@demo.englishforu.local', 'Thanks for the feedback. We are improving triage timing even more.', NOW() - INTERVAL '70 minutes');

INSERT INTO discussion_reactions (post_id, author_email, type, created_at, updated_at)
VALUES
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: I keep confusing present perfect and past simple. Any fast way to remember?' LIMIT 1), 'teacher.demo@demo.englishforu.local', 'INSIGHTFUL', NOW() - INTERVAL '7 hours 10 minutes', NOW() - INTERVAL '7 hours 10 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: Pronunciation tip of the day - practice TH with short daily recordings.' LIMIT 1), 'student.demo@demo.englishforu.local', 'LOVE', NOW() - INTERVAL '6 hours 20 minutes', NOW() - INTERVAL '6 hours 20 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo media post: Whiteboard snapshot from writing workshop.' LIMIT 1), 'student.demo@demo.englishforu.local', 'CLAP', NOW() - INTERVAL '5 hours 10 minutes', NOW() - INTERVAL '5 hours 10 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo media post: PDF checklist for essay structure.' LIMIT 1), 'helpdesk.demo@demo.englishforu.local', 'LIKE', NOW() - INTERVAL '4 hours 10 minutes', NOW() - INTERVAL '4 hours 10 minutes'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo quiz: Pick the grammatically correct sentence.' LIMIT 1), 'helpdesk.demo@demo.englishforu.local', 'CLAP', NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),
    ((SELECT id FROM discussion_posts WHERE content = 'Demo thread: Reporting flow feedback - submit worked smoothly and help desk replied fast.' LIMIT 1), 'teacher.demo@demo.englishforu.local', 'LOVE', NOW() - INTERVAL '65 minutes', NOW() - INTERVAL '65 minutes');

-- Reported bugs by real app users (student + teacher).
INSERT INTO reports (
    created_at, updated_at, created_by_user_id, title, category, severity,
    description, steps_to_reproduce, expected_result, actual_result,
    page_url, user_agent, app_version, status, assigned_to_user_id
)
VALUES
    (NOW() - INTERVAL '10 hours', NOW() - INTERVAL '8 hours', '00000000-0000-0000-0000-000000000102', 'Demo: Notification bell does not refresh without reload', 'BUG', 'HIGH', 'New notifications appear only after full page refresh for student account.', '1. Keep dashboard open. 2. Trigger report update. 3. Observe bell counter.', 'Counter should update in real time.', 'Counter updates only after manual refresh.', '/main', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'v1.4.2-demo', 'TRIAGED', '00000000-0000-0000-0000-000000000103'),
    (NOW() - INTERVAL '9 hours', NOW() - INTERVAL '6 hours', '00000000-0000-0000-0000-000000000101', 'Demo: PDF preview opens blank tab on first click', 'ISSUE', 'MEDIUM', 'On some browsers, first click opens a blank PDF tab, second click works.', '1. Open thread with PDF. 2. Click Open PDF once.', 'PDF should open correctly on first click.', 'Blank tab appears on first click.', '/community/feed', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0)', 'v1.4.2-demo', 'IN_PROGRESS', '00000000-0000-0000-0000-000000000103'),
    (NOW() - INTERVAL '7 hours', NOW() - INTERVAL '5 hours', '00000000-0000-0000-0000-000000000102', 'Demo: Helpdesk drag-drop card briefly jumps back', 'BUG', 'HIGH', 'When moving card across columns, it briefly returns then updates later.', '1. Open helpdesk board. 2. Move NEW ticket to TRIAGED.', 'Card should remain in target status directly.', 'Card jumps back for a moment before final status.', '/helpdesk/board', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'v1.4.2-demo', 'NEW', NULL),
    (NOW() - INTERVAL '4 hours', NOW() - INTERVAL '2 hours', '00000000-0000-0000-0000-000000000101', 'Demo: Add quick keyboard shortcuts for helpdesk triage', 'FEATURE_REQUEST', 'LOW', 'Help desk can triage faster with shortcuts for status transition and save.', 'N/A', 'Keyboard shortcuts should be available for frequent actions.', 'Feature not implemented yet.', '/helpdesk/board', 'Mozilla/5.0 (X11; Linux x86_64)', 'v1.4.2-demo', 'DONE', '00000000-0000-0000-0000-000000000103');

INSERT INTO report_comments (report_id, author_user_id, message, created_at)
VALUES
    ((SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'Acknowledged. We are validating websocket reconnect behavior.', NOW() - INTERVAL '8 hours 30 minutes'),
    ((SELECT id FROM reports WHERE title = 'Demo: PDF preview opens blank tab on first click' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'Please share browser version. We suspect popup-handling policy.', NOW() - INTERVAL '6 hours 10 minutes'),
    ((SELECT id FROM reports WHERE title = 'Demo: Helpdesk drag-drop card briefly jumps back' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'Investigating optimistic UI and status sync timing.', NOW() - INTERVAL '5 hours 15 minutes'),
    ((SELECT id FROM reports WHERE title = 'Demo: Add quick keyboard shortcuts for helpdesk triage' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'Great idea. Added to backlog after discussion with team.', NOW() - INTERVAL '2 hours 10 minutes');

INSERT INTO report_activities (report_id, actor_user_id, type, from_status, to_status, details, created_at)
VALUES
    ((SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), '00000000-0000-0000-0000-000000000102', 'REPORT_CREATED', NULL, NULL, 'Student created bug report from main page.', NOW() - INTERVAL '10 hours'),
    ((SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'ASSIGNED', NULL, NULL, 'Assigned to Help Desk.', NOW() - INTERVAL '9 hours 30 minutes'),
    ((SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'STATUS_CHANGED', 'NEW', 'TRIAGED', 'Initial triage completed.', NOW() - INTERVAL '8 hours'),
    ((SELECT id FROM reports WHERE title = 'Demo: PDF preview opens blank tab on first click' LIMIT 1), '00000000-0000-0000-0000-000000000101', 'REPORT_CREATED', NULL, NULL, 'Teacher created issue from thread page.', NOW() - INTERVAL '9 hours'),
    ((SELECT id FROM reports WHERE title = 'Demo: PDF preview opens blank tab on first click' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'STATUS_CHANGED', 'TRIAGED', 'IN_PROGRESS', 'Fix under implementation.', NOW() - INTERVAL '6 hours'),
    ((SELECT id FROM reports WHERE title = 'Demo: Add quick keyboard shortcuts for helpdesk triage' LIMIT 1), '00000000-0000-0000-0000-000000000103', 'STATUS_CHANGED', 'IN_PROGRESS', 'DONE', 'Prototype completed for demo.', NOW() - INTERVAL '2 hours');

INSERT INTO notifications (recipient_user_id, report_id, created_at, read_at, type, title, message, link)
VALUES
    ('00000000-0000-0000-0000-000000000103', (SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), NOW() - INTERVAL '9 hours 45 minutes', NULL, 'REPORT_CREATED', 'Demo: New bug report', 'Student submitted a high-priority notification issue.', '/helpdesk/board'),
    ('00000000-0000-0000-0000-000000000102', (SELECT id FROM reports WHERE title = 'Demo: Notification bell does not refresh without reload' LIMIT 1), NOW() - INTERVAL '8 hours', NULL, 'REPORT_STATUS_CHANGED', 'Demo: Ticket triaged', 'Your bug report moved to TRIAGED.', '/helpdesk/board'),
    ('00000000-0000-0000-0000-000000000101', (SELECT id FROM reports WHERE title = 'Demo: PDF preview opens blank tab on first click' LIMIT 1), NOW() - INTERVAL '6 hours', NULL, 'REPORT_ASSIGNED', 'Demo: Help desk is working on your issue', 'Your PDF preview ticket is now IN_PROGRESS.', '/helpdesk/board'),
    ('00000000-0000-0000-0000-000000000102', (SELECT id FROM reports WHERE title = 'Demo: Helpdesk drag-drop card briefly jumps back' LIMIT 1), NOW() - INTERVAL '5 hours', NULL, 'REPORT_COMMENT_ADDED', 'Demo: New help desk comment', 'Help desk asked for extra reproduction details.', '/helpdesk/board'),
    ('00000000-0000-0000-0000-000000000101', NULL, NOW() - INTERVAL '4 hours 30 minutes', NULL, 'DISCUSSION_POST_CREATED', 'Demo: New thread activity', 'A learner posted a new grammar question in B1.', '/community/feed'),
    ('00000000-0000-0000-0000-000000000102', NULL, NOW() - INTERVAL '4 hours', NULL, 'DISCUSSION_COMMENT_ADDED', 'Demo: Teacher replied to your thread', 'Your grammar thread received a tutor answer.', '/community/feed'),
    ('00000000-0000-0000-0000-000000000101', NULL, NOW() - INTERVAL '3 hours 30 minutes', NULL, 'DISCUSSION_REACTION_ADDED', 'Demo: Learner reacted to your post', 'Your pronunciation tip got new reactions.', '/community/feed'),
    ('00000000-0000-0000-0000-000000000103', NULL, NOW() - INTERVAL '3 hours', NULL, 'SYSTEM_ALERT', 'Demo: Help desk daily summary', '3 active tickets and 1 resolved in the last cycle.', '/helpdesk/board'),
    ('00000000-0000-0000-0000-000000000102', NULL, NOW() - INTERVAL '2 hours 30 minutes', NULL, 'SYSTEM_ALERT', 'Demo: Practice reminder', 'Continue your daily speaking challenge to stay on track.', '/main'),
    ('00000000-0000-0000-0000-000000000101', (SELECT id FROM reports WHERE title = 'Demo: Add quick keyboard shortcuts for helpdesk triage' LIMIT 1), NOW() - INTERVAL '90 minutes', NULL, 'REPORT_STATUS_CHANGED', 'Demo: Feature request completed', 'Help desk shortcuts request was marked DONE for demo.', '/helpdesk/board');

COMMIT;
