export type RemoteType = 'REMOTE' | 'HYBRID' | 'ONSITE';
export type ContractType = 'CDI' | 'CDD' | 'STAGE' | 'FREELANCE' | 'ALTERNANCE' | 'INTERIM';
export type JobSource = 'LINKEDIN' | 'INDEED' | 'WELCOME_TO_THE_JUNGLE' | 'GLASSDOOR' | 'COMPANY_WEBSITE' | 'OTHER';

export interface JobOffer {
  id: string;
  title: string;
  companyId?: string;
  companyName?: string;
  description?: string;
  location?: string;
  remoteType?: RemoteType;
  contractType?: ContractType;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  jobUrl?: string;
  source?: JobSource;
  skills: string[];
  publicationDate?: string;
  deadline?: string;
  deadlinePassed: boolean;
  notes?: string;
  favorite: boolean;
  archived: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface JobOfferRequest {
  title: string;
  companyId?: string;
  description?: string;
  location?: string;
  remoteType?: RemoteType;
  contractType?: ContractType;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  jobUrl?: string;
  source?: JobSource;
  skills?: string[];
  publicationDate?: string;
  deadline?: string;
  notes?: string;
}
