export interface MonthlyCount {
  month: string;
  count: number;
}

export interface StatusCount {
  status: string;
  count: number;
}

export interface FunnelStage {
  stage: string;
  count: number;
}

export interface CompanyCount {
  companyName: string;
  count: number;
}

export interface SourceStat {
  source: string;
  totalApplications: number;
  effectivenessRate: number;
}

export interface Dashboard {
  totalApplications: number;
  activeApplications: number;
  interviews: number;
  offersReceived: number;
  acceptedApplications: number;
  rejectedApplications: number;
  responseRate: number;
  conversionRate: number;
  applicationsByMonth: MonthlyCount[];
  statusDistribution: StatusCount[];
  conversionFunnel: FunnelStage[];
  topCompanies: CompanyCount[];
  sourceEffectiveness: SourceStat[];
}
