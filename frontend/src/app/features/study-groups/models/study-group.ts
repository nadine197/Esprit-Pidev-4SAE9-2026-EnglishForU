// src/app/features/study-groups/models/study-group.ts

export type StudyGroupStatus = 'ACTIVE' | 'COMPLETED' | 'PLANNED' | 'CANCELLED';

export interface StudyGroup {
  groupId:     number;
  name:         string;
  level:        string;
  location:     string;
  maxCapacity:  number;
  startdate:    string;
  enddate:      string;
  status:       StudyGroupStatus;
  courseId:     number;
  tutorId:      string;
  studentsIds?: string[];   // ← ? supprime le warning et protège contre null
}

export interface MarkedDates {
  [date: string]: string[];
}
