export type FollowUpType = 'EMAIL' | 'PHONE' | 'LINKEDIN' | 'OTHER';
export type FollowUpStatus = 'PLANNED' | 'COMPLETED' | 'CANCELLED';

export interface FollowUp {
  id: string;
  applicationId: string;
  companyName?: string;
  jobOfferTitle?: string;
  followUpDate: string;
  type: FollowUpType;
  status: FollowUpStatus;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface FollowUpRequest {
  applicationId: string;
  followUpDate: string;
  type: FollowUpType;
  status?: FollowUpStatus;
  notes?: string;
}

export interface FollowUpStats {
  dueToday: number;
  overdue: number;
  dueThisWeek: number;
}
