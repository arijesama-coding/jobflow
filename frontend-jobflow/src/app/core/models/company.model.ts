export interface Company {
  id: string;
  name: string;
  logoUrl?: string;
  website?: string;
  industry?: string;
  location?: string;
  description?: string;
  size?: string;
  linkedinUrl?: string;
  notes?: string;
  favorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CompanyRequest {
  name: string;
  logoUrl?: string;
  website?: string;
  industry?: string;
  location?: string;
  description?: string;
  size?: string;
  linkedinUrl?: string;
  notes?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
