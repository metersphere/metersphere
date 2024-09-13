export interface AddUserToOrgOrProjectParams {
  userIds?: string[];
  organizationId?: string;
  projectId?: string;
  // 等待接口改动 将要废弃，以后用userIds
  memberIds?: string[];
  userRoleIds?: string[];
}
