export type DocumentType = 'CV' | 'COVER_LETTER' | 'CERTIFICATE' | 'PORTFOLIO' | 'OTHER';

export interface AppDocument {
  id: string;
  applicationId?: string;
  type: DocumentType;
  fileName: string;
  version: number;
  primary: boolean;
  createdAt: string;
  updatedAt: string;
}
