export enum GitPlatformEnum {
  GITEA = 'Gitea',
  GITHUB = 'Github',
  GITLAB = 'Gitlab',
  GITEE = 'Gitee',
  OTHER = 'Other',
}
export enum AuthScopeEnum {
  SYSTEM = 'SYSTEM',
  ORGANIZATION = 'ORGANIZATION',
  PROJECT = 'PROJECT',
}

export enum MenuEnum {
  workstation = 'workstation',
  loadTest = 'loadTest',
  testPlan = 'testPlan',
  bugManagement = 'bugManagement',
  caseManagement = 'caseManagement',
  apiTest = 'apiTest',
  uiTest = 'uiTest',
  taskCenter = 'taskCenter',
}

export enum ShapeFlags {
  ELEMENT = 1,
  FUNCTIONAL_COMPONENT = 2,
  STATEFUL_COMPONENT = 4,
  COMPONENT = FUNCTIONAL_COMPONENT + STATEFUL_COMPONENT,
  TEXT_CHILDREN = 8,
  ARRAY_CHILDREN = 16,
  SLOTS_CHILDREN = 32,
  TELEPORT = 64,
  SUSPENSE = 128,
  COMPONENT_SHOULD_KEEP_ALIVE = 256,
  COMPONENT_KEPT_ALIVE = 512,
}

export enum TagUpdateTypeEnum {
  UPDATE = 'UPDATE',
  APPEND = 'APPEND',
  CLEAR = 'CLEAR',
}

export enum GlobalEventNameEnum {
  OPEN_TASK_CENTER = 'openTaskCenter',
}
