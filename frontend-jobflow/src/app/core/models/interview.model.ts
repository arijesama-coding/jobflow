export type InterviewType = 'PHONE' | 'VIDEO' | 'ONSITE' | 'TECHNICAL' | 'HR' | 'FINAL';
export type InterviewResult = 'PENDING' | 'PASSED' | 'FAILED' | 'RESCHEDULED';

export interface Interview {
  id: string;
  applicationId: string;
  companyName?: string;
  jobOfferTitle?: string;
  type: InterviewType;
  scheduledAt: string;
  durationMinutes?: number;
  location?: string;
  meetingUrl?: string;
  interviewer?: string;
  notes?: string;
  feedback?: string;
  result: InterviewResult;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewRequest {
  applicationId: string;
  type: InterviewType;
  scheduledAt: string;
  durationMinutes?: number;
  location?: string;
  meetingUrl?: string;
  interviewer?: string;
  notes?: string;
  feedback?: string;
  result?: InterviewResult;
}
