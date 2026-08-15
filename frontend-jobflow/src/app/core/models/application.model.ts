export type ApplicationStatus =
  | 'WISHLIST' | 'TO_APPLY' | 'APPLIED' | 'SCREENING' | 'INTERVIEW'
  | 'TECHNICAL_INTERVIEW' | 'FINAL_INTERVIEW' | 'OFFER' | 'ACCEPTED' | 'REJECTED' | 'WITHDRAWN';

export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type ApplicationSource = 'LINKEDIN' | 'INDEED' | 'WEBSITE' | 'REFERRAL' | 'OTHER';

export const APPLICATION_STATUSES: ApplicationStatus[] = [
  'WISHLIST', 'TO_APPLY', 'APPLIED', 'SCREENING', 'INTERVIEW',
  'TECHNICAL_INTERVIEW', 'FINAL_INTERVIEW', 'OFFER', 'ACCEPTED', 'REJECTED', 'WITHDRAWN',
];

export interface Application {
  id: string;
  companyId?: string;
  companyName?: string;
  jobOfferId?: string;
  jobOfferTitle?: string;
  status: ApplicationStatus;
  applicationDate?: string;
  salaryExpectation?: number;
  source?: ApplicationSource;
  priority: Priority;
  notes?: string;
  nextFollowUpDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ApplicationRequest {
  companyId?: string;
  jobOfferId?: string;
  status?: ApplicationStatus;
  applicationDate?: string;
  salaryExpectation?: number;
  source?: ApplicationSource;
  priority?: Priority;
  notes?: string;
  nextFollowUpDate?: string;
}

export interface ApplicationStatusHistoryEntry {
  fromStatus: ApplicationStatus | null;
  toStatus: ApplicationStatus;
  changedAt: string;
}
