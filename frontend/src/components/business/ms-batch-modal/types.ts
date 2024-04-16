import type { TableQueryParams } from '@/models/common';

export interface BatchModel extends TableQueryParams {
  id?: string;
  organizationId?: string;
  memberIds?: string[];
  userRoleIds?: string[];
  projectIds?: string[];
}
