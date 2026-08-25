export interface Contact {
  id: string;
  name: string;
  email?: string;
  phone?: string;
  position?: string;
  companyId?: string;
  companyName?: string;
  linkedinUrl?: string;
  notes?: string;
  applicationIds: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ContactRequest {
  name: string;
  email?: string;
  phone?: string;
  position?: string;
  companyId?: string;
  linkedinUrl?: string;
  notes?: string;
}
