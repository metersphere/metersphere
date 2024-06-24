export default {};

export const dropPositionMap: Record<string, any> = {
  '-1': 'BEFORE',
  '0': 'APPEND',
  '1': 'AFTER',
};

// 操作类型
export const operationTypeOptions = [
  {
    label: 'system.log.operateType.all',
    value: '',
  },
  {
    label: 'system.log.operateType.add',
    value: 'ADD',
  },
  {
    label: 'system.log.operateType.delete',
    value: 'DELETE',
  },
  {
    label: 'system.log.operateType.update',
    value: 'UPDATE',
  },
  {
    label: 'system.log.operateType.debug',
    value: 'DEBUG',
  },
  {
    label: 'system.log.operateType.review',
    value: 'REVIEW',
  },
  {
    label: 'system.log.operateType.copy',
    value: 'COPY',
  },
  {
    label: 'system.log.operateType.execute',
    value: 'EXECUTE',
  },
  {
    label: 'system.log.operateType.share',
    value: 'SHARE',
  },
  {
    label: 'system.log.operateType.restore',
    value: 'RESTORE',
  },
  {
    label: 'system.log.operateType.import',
    value: 'IMPORT',
  },
  {
    label: 'system.log.operateType.export',
    value: 'EXPORT',
  },
  {
    label: 'system.log.operateType.login',
    value: 'LOGIN',
  },
  {
    label: 'system.log.operateType.select',
    value: 'SELECT',
  },
  {
    label: 'system.log.operateType.recover',
    value: 'RECOVER',
  },
  {
    label: 'system.log.operateType.logout',
    value: 'LOGOUT',
  },
  {
    label: 'system.log.operateType.associate',
    value: 'ASSOCIATE',
  },
  {
    label: 'system.log.operateType.disassociate',
    value: 'DISASSOCIATE',
  },
  {
    label: 'system.log.operateType.archived',
    value: 'ARCHIVED',
  },
  {
    label: 'system.log.operateType.stop',
    value: 'STOP',
  },
];
