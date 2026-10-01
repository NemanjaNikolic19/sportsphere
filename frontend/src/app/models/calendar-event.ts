export type CalendarEventType = 'RESERVATION' | 'TRAINING';

export interface CalendarEvent {
  eventType: CalendarEventType;
  id: number;
  athleteId: number;
  trainerId: number | null;
  facilityId: number;
  resourceId: number;
  sportId: number;
  startTime: string;
  endTime: string;
  status: string;
}
