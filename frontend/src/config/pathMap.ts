import { RouteEnum } from '@/enums/routeEnum';

export const MENU_LEVEL = ['SYSTEM', 'ORGANIZATION', 'PROJECT'] as const; // 菜单级别

export type PathMapKey = keyof typeof RouteEnum;

export type PathMapRoute = (typeof RouteEnum)[PathMapKey];

export interface PathMapItem {
  key: PathMapKey | string; // 系统设置
  locale: string;
  route: PathMapRoute | string;
  permission?: [];
  level: (typeof MENU_LEVEL)[number]; // 系统设置里有系统级别也有组织级别，按最低权限级别配置
  children?: PathMapItem[];
  routeQuery?: Record<string, any>;
  hideInModule?: boolean; // 在日志选项中隐藏
}

/**
 * 路由与菜单、tab、权限、国际化信息的映射关系，用于通过路由直接跳转到各页面及携带 tab 参数
 * key 是与后台商定的映射 key
 * locale 是国际化的 key
 * route 是路由的 name
 * routeQuery 是路由的固定参数集合，与routeParamKeys互斥，用于跳转同一个路由但不同 tab 时或其他需要固定参数的情况
 * permission 是权限的 key 集合
 * level 是菜单级别，用于筛选不同级别的路由/tab
 * children 是子路由/tab集合
 */
export const pathMap: PathMapItem[] = [
  {
    key: 'API_TEST', // 接口测试
    locale: 'menu.apiTest',
    route: RouteEnum.API_TEST,
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'API_TEST_DEBUG_MANAGEMENT', // 接口测试-接口调试
        locale: 'menu.apiTest.debug',
        route: RouteEnum.API_TEST_DEBUG_MANAGEMENT,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'API_TEST_DEBUG_MANAGEMENT_MODULE', // 接口测试-接口调试-模块
            locale: 'common.module',
            route: RouteEnum.API_TEST_DEBUG_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'API_TEST_DEBUG_MANAGEMENT_DEBUG', // 接口测试-接口调试-调试
            locale: 'menu.apiTest.debug.debug',
            route: RouteEnum.API_TEST_DEBUG_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'API_TEST_MANAGEMENT', // 接口测试-接口定义
        locale: 'menu.apiTest.management',
        route: RouteEnum.API_TEST_MANAGEMENT,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'API_TEST_MANAGEMENT_MODULE', // 接口测试-接口定义-模块
            locale: 'common.module',
            route: RouteEnum.API_TEST_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'API_TEST_MANAGEMENT_DEFINITION', // 接口测试-接口定义
            locale: 'menu.apiTest.management.definition',
            route: RouteEnum.API_TEST_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              tab: 'api',
            },
            children: [
              {
                key: 'API_TEST_MANAGEMENT_DEFINITION_SHARE', // 接口测试-接口定义-模块
                locale: 'common.share',
                route: RouteEnum.API_TEST_MANAGEMENT,
                permission: [],
                level: MENU_LEVEL[2],
              },
            ],
          },
          {
            key: 'API_TEST_MANAGEMENT_MOCK', // 接口测试-接口定义-mock
            locale: 'MOCK',
            route: RouteEnum.API_TEST_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              tab: 'mock',
            },
          },
          {
            key: 'API_TEST_MANAGEMENT_CASE', // 接口测试-接口定义-case
            locale: 'CASE',
            route: RouteEnum.API_TEST_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              tab: 'case',
            },
          },
          {
            key: 'API_TEST_MANAGEMENT_RECYCLE', // 接口测试-回收站
            locale: 'menu.apiTest.scenario.recycle',
            route: RouteEnum.API_TEST_MANAGEMENT_RECYCLE,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'API_TEST_SCENARIO_MANAGEMENT', // 接口测试-场景
        locale: 'menu.apiTest.scenario',
        route: RouteEnum.API_TEST_SCENARIO,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'API_TEST_SCENARIO_MANAGEMENT_SCENARIO', // 接口测试-场景
            locale: 'menu.apiTest.scenario',
            route: RouteEnum.API_TEST_SCENARIO,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'API_TEST_SCENARIO_MANAGEMENT_MODULE', // 接口测试-场景-模块
            locale: 'common.module',
            route: RouteEnum.API_TEST_SCENARIO,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'API_TEST_SCENARIO_MANAGEMENT_RECYCLE', // 接口测试-场景-回收站
            locale: 'menu.apiTest.scenario.recycle',
            route: RouteEnum.API_TEST_SCENARIO_RECYCLE,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'API_TEST_REPORT', // 接口测试-报告
        locale: 'menu.apiTest.report',
        route: RouteEnum.API_TEST_REPORT,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'API_TEST_REPORT_SCENARIO', // 接口测试-场景报告
            locale: 'report.api.scenario',
            route: RouteEnum.API_TEST_REPORT,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              type: 'API_SCENARIO',
            },
          },
          {
            key: 'API_TEST_REPORT_CASE', // 接口测试-用例报告
            locale: 'report.api.case',
            route: RouteEnum.API_TEST_REPORT,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              type: 'API_CASE',
            },
          },
        ],
      },
    ],
  },
  {
    key: 'BUG_MANAGEMENT', // 缺陷管理
    locale: 'menu.bugManagement',
    route: RouteEnum.BUG_MANAGEMENT,
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'BUG_MANAGEMENT_BUG_INDEX', // 缺陷管理
        locale: 'menu.bugManagement.bugDetail',
        route: RouteEnum.BUG_MANAGEMENT_INDEX,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'BUG_MANAGEMENT_BUG_RECYCLE', // 缺陷管理-回收站
        locale: 'menu.bugManagement.bugRecycle',
        route: RouteEnum.BUG_MANAGEMENT_RECYCLE,
        permission: [],
        level: MENU_LEVEL[2],
      },
    ],
  },
  {
    key: 'CASE_MANAGEMENT', // 功能测试
    locale: 'menu.caseManagement',
    route: RouteEnum.CASE_MANAGEMENT,
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'CASE_MANAGEMENT_CASE', // 功能测试-功能用例
        locale: 'menu.caseManagement.featureCase',
        route: RouteEnum.CASE_MANAGEMENT_CASE,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'CASE_MANAGEMENT_CASE_MODULE', // 功能测试-功能用例-模块
            locale: 'common.module',
            route: RouteEnum.CASE_MANAGEMENT_CASE,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'CASE_MANAGEMENT_CASE_CASE', // 功能测试-功能用例-用例列表
            locale: 'common.case',
            route: RouteEnum.CASE_MANAGEMENT_CASE,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'CASE_MANAGEMENT_CASE_RECYCLE', // 功能测试-功能用例-回收站
            locale: 'menu.caseManagement.featureCaseRecycle',
            route: RouteEnum.CASE_MANAGEMENT_CASE_RECYCLE,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'CASE_MANAGEMENT_REVIEW', // 功能测试-功能用例-用例评审
        locale: 'menu.caseManagement.caseManagementReview',
        route: RouteEnum.CASE_MANAGEMENT_REVIEW,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'CASE_MANAGEMENT_REVIEW_REVIEW', // 功能测试-功能用例-用例评审
            locale: 'menu.caseManagement.caseManagementReview',
            route: RouteEnum.CASE_MANAGEMENT_REVIEW,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'CASE_MANAGEMENT_REVIEW_DETAIL', // 功能测试-功能用例-评审详情
            locale: 'menu.caseManagement.caseManagementReviewDetail',
            route: RouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'CASE_MANAGEMENT_REVIEW_REVIEW_MODULE', // 功能测试-功能用例-用例评审-模块
            locale: 'common.module',
            route: RouteEnum.CASE_MANAGEMENT_REVIEW,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
    ],
  },
  {
    key: 'SETTING', // 系统设置
    locale: 'menu.settings',
    route: RouteEnum.SETTING,
    permission: [],
    level: MENU_LEVEL[1], // 系统设置里有系统级别也有组织级别，按最低权限级别配置
    children: [
      {
        key: 'SETTING_SYSTEM', // 系统设置-系统
        locale: 'menu.settings.system',
        route: RouteEnum.SETTING_SYSTEM,
        permission: [],
        level: MENU_LEVEL[0],
        children: [
          {
            key: 'SETTING_SYSTEM_USER_SINGLE', // 系统设置-系统-用户
            locale: 'menu.settings.system.user',
            route: RouteEnum.SETTING_SYSTEM_USER_SINGLE,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_USER_GROUP', // 系统设置-系统-用户组
            locale: 'menu.settings.system.usergroup',
            route: RouteEnum.SETTING_SYSTEM_USER_GROUP,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_ORGANIZATION', // 系统设置-系统-组织与项目
            locale: 'menu.settings.system.organizationAndProject',
            route: RouteEnum.SETTING_SYSTEM_ORGANIZATION,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_PARAMETER', // 系统设置-系统-系统参数
            locale: 'menu.settings.system.parameter',
            route: RouteEnum.SETTING_SYSTEM_PARAMETER,
            permission: [],
            level: MENU_LEVEL[0],
            children: [
              {
                key: 'SETTING_SYSTEM_PARAMETER_BASE_CONFIG', // 系统设置-系统-系统参数-基础设置
                locale: 'system.config.baseConfig',
                route: RouteEnum.SETTING_SYSTEM_PARAMETER,
                permission: [],
                level: MENU_LEVEL[0],
              },
              {
                key: 'SETTING_SYSTEM_PARAMETER_PAGE_CONFIG', // 系统设置-系统-系统参数-界面设置
                locale: 'system.config.pageConfig',
                route: RouteEnum.SETTING_SYSTEM_PARAMETER,
                permission: [],
                routeQuery: {
                  tab: 'pageConfig',
                },
                level: MENU_LEVEL[0],
              },
              {
                key: 'SETTING_SYSTEM_PARAMETER_AUTH_CONFIG', // 系统设置-系统-系统参数-认证设置
                locale: 'system.config.authConfig',
                route: RouteEnum.SETTING_SYSTEM_PARAMETER,
                permission: [],
                routeQuery: {
                  tab: 'authConfig',
                },
                level: MENU_LEVEL[0],
              },
            ],
          },
          {
            key: 'SETTING_SYSTEM_RESOURCE_POOL', // 系统设置-系统-资源池
            locale: 'menu.settings.system.resourcePool',
            route: RouteEnum.SETTING_SYSTEM_RESOURCE_POOL,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_AUTHORIZED_MANAGEMENT', // 系统设置-系统-授权管理
            locale: 'License',
            route: RouteEnum.SETTING_SYSTEM_AUTHORIZED_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_LOG', // 系统设置-系统-日志
            locale: 'menu.settings.system.log',
            route: RouteEnum.SETTING_SYSTEM_LOG,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_TASK_CENTER', // 系统设置-系统-任务中心
            locale: 'menu.projectManagement.taskCenter',
            route: RouteEnum.SETTING_SYSTEM_TASK_CENTER,
            permission: [],
            level: MENU_LEVEL[0],
          },
          {
            key: 'SETTING_SYSTEM_PLUGIN_MANAGEMENT', // 系统设置-系统-插件管理
            locale: 'menu.settings.system.pluginManager',
            route: RouteEnum.SETTING_SYSTEM_PLUGIN_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[0],
          },
        ],
      },
      {
        key: 'SETTING_ORGANIZATION', // 系统设置-组织
        locale: 'menu.settings.organization',
        route: RouteEnum.SETTING_ORGANIZATION,
        permission: [],
        level: MENU_LEVEL[1],
        children: [
          {
            key: 'SETTING_ORGANIZATION_MEMBER', // 系统设置-组织-成员
            locale: 'menu.settings.organization.member',
            route: RouteEnum.SETTING_ORGANIZATION_MEMBER,
            permission: [],
            level: MENU_LEVEL[1],
          },
          {
            key: 'SETTING_ORGANIZATION_USER_ROLE', // 系统设置-组织-用户组
            locale: 'menu.settings.organization.userGroup',
            route: RouteEnum.SETTING_ORGANIZATION_USER_GROUP,
            permission: [],
            level: MENU_LEVEL[1],
          },
          {
            key: 'SETTING_ORGANIZATION_PROJECT', // 系统设置-组织-项目
            locale: 'menu.settings.organization.project',
            route: RouteEnum.SETTING_ORGANIZATION_PROJECT,
            permission: [],
            level: MENU_LEVEL[1],
          },
          {
            key: 'SETTING_ORGANIZATION_SERVICE', // 系统设置-组织-服务集成
            locale: 'menu.settings.organization.serviceIntegration',
            route: RouteEnum.SETTING_ORGANIZATION_SERVICE,
            permission: [],
            level: MENU_LEVEL[1],
          },
          {
            key: 'SETTING_ORGANIZATION_TASK_CENTER', // 系统设置-组织-任务中心
            locale: 'menu.projectManagement.taskCenter',
            route: RouteEnum.SETTING_ORGANIZATION_TASK_CENTER,
            permission: [],
            level: MENU_LEVEL[1],
          },
          {
            key: 'SETTING_ORGANIZATION_TEMPLATE', // 系统设置-组织-模板管理
            locale: 'menu.projectManagement.templateManager',
            route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE,
            permission: [],
            level: MENU_LEVEL[1],
            children: [
              {
                key: 'SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL', // 模板管理-用例模板
                locale: 'system.orgTemplate.caseTemplates',
                route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'FUNCTIONAL',
                },
                level: MENU_LEVEL[1],
                children: [
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_FIELD', // 模板管理-用例模板-用例模板字段管理
                    locale: 'system.orgTemplate.field',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                    permission: [],
                    routeQuery: {
                      type: 'FUNCTIONAL',
                    },
                    level: MENU_LEVEL[1],
                  },
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_FUNCTIONAL_TEMPLATE', // 模板管理-用例模板-用例模板管理
                    locale: 'system.orgTemplate.caseTemplateManagement',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                    permission: [],
                    routeQuery: {
                      type: 'FUNCTIONAL',
                    },
                    level: MENU_LEVEL[1],
                  },
                ],
              },
              {
                key: 'SETTING_ORGANIZATION_TEMPLATE_API', // 模板管理-接口模板
                locale: 'system.orgTemplate.APITemplates',
                route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'API',
                },
                level: MENU_LEVEL[1],
                children: [
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_API_FIELD', // 模板管理-接口模板-接口模板字段管理
                    locale: 'system.orgTemplate.field',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                    permission: [],
                    routeQuery: {
                      type: 'API',
                    },
                    level: MENU_LEVEL[1],
                  },
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_API_TEMPLATE', // 模板管理-接口模板-接口模板管理
                    locale: 'system.orgTemplate.apiTemplateManagement',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                    permission: [],
                    routeQuery: {
                      type: 'API',
                    },
                    level: MENU_LEVEL[1],
                  },
                ],
              },
              {
                key: 'SETTING_ORGANIZATION_TEMPLATE_BUG', // 模板管理-缺陷模板
                locale: 'system.orgTemplate.defectTemplates',
                route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'BUG',
                },
                level: MENU_LEVEL[1],
                children: [
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_BUG_FIELD', // 模板管理-缺陷模板管理-字段管理
                    locale: 'system.orgTemplate.field',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_FILED_SETTING,
                    permission: [],
                    routeQuery: {
                      type: 'BUG',
                    },
                    level: MENU_LEVEL[1],
                  },
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_BUG_TEMPLATE', // 模板管理-缺陷模板-缺陷模板管理
                    locale: 'system.orgTemplate.bugTemplateManagement',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT,
                    permission: [],
                    routeQuery: {
                      type: 'BUG',
                    },
                    level: MENU_LEVEL[1],
                  },
                  {
                    key: 'SETTING_ORGANIZATION_TEMPLATE_BUG_WORKFLOW', // 模板管理-缺陷模板-缺陷工作流
                    locale: 'menu.settings.organization.templateManagementWorkFlow',
                    route: RouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_WORKFLOW,
                    permission: [],
                    level: MENU_LEVEL[1],
                  },
                ],
              },
            ],
          },
          {
            key: 'SETTING_ORGANIZATION_LOG', // 系统设置-组织-日志
            locale: 'menu.settings.organization.log',
            route: RouteEnum.SETTING_ORGANIZATION_LOG,
            permission: [],
            level: MENU_LEVEL[1],
            hideInModule: true,
          },
        ],
      },
    ],
  },
  {
    key: 'PROJECT_MANAGEMENT', // 项目管理
    locale: 'menu.projectManagement',
    route: RouteEnum.PROJECT_MANAGEMENT,
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'PROJECT_MANAGEMENT_PERMISSION', // 项目管理-项目与权限
        locale: 'menu.projectManagement.projectPermission',
        route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO', // 项目管理-项目与权限-基本信息
            locale: 'project.permission.basicInfo',
            route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION_BASIC_INFO,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT', // 项目管理-项目与权限-菜单管理
            locale: 'project.permission.menuManagement',
            route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
            children: [
              {
                key: 'PROJECT_MANAGEMENT_PERMISSION_MENU_MANAGEMENT_FAKE', // 项目管理-项目与权限-菜单管理-误报
                locale: 'menu.projectManagement.fakeError',
                route: RouteEnum.PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE,
                permission: [],
                level: MENU_LEVEL[2],
              },
            ],
          },
          {
            key: 'PROJECT_MANAGEMENT_PERMISSION_VERSION', // 项目管理-项目与权限-项目版本
            locale: 'project.permission.projectVersion',
            route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION_VERSION,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'PROJECT_MANAGEMENT_PERMISSION_MEMBER', // 项目管理-项目与权限-成员
            locale: 'project.permission.member',
            route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION_MEMBER,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'PROJECT_MANAGEMENT_PERMISSION_USER_GROUP', // 项目管理-项目与权限-用户组
            locale: 'project.permission.userGroup',
            route: RouteEnum.PROJECT_MANAGEMENT_PERMISSION_USER_GROUP,
            permission: [],
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'PROJECT_MANAGEMENT_TEMPLATE', // 系统设置-组织-模板管理
        locale: 'menu.projectManagement.templateManager',
        route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL', // 模板管理-用例模板
            locale: 'system.orgTemplate.caseTemplates',
            route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            permission: [],
            routeQuery: {
              type: 'FUNCTIONAL',
            },
            level: MENU_LEVEL[2],
            children: [
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_FIELD', // 模板管理-用例模板-用例模板字段管理
                locale: 'system.orgTemplate.field',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
                permission: [],
                routeQuery: {
                  type: 'FUNCTIONAL',
                },
                level: MENU_LEVEL[2],
              },
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_FUNCTIONAL_TEMPLATE', // 模板管理-用例模板-用例模板管理
                locale: 'system.orgTemplate.caseTemplateManagement',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'FUNCTIONAL',
                },
                level: MENU_LEVEL[2],
              },
            ],
          },
          {
            key: 'PROJECT_MANAGEMENT_TEMPLATE_API', // 模板管理-接口模板
            locale: 'system.orgTemplate.APITemplates',
            route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            permission: [],
            routeQuery: {
              type: 'API',
            },
            level: MENU_LEVEL[2],
            children: [
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_API_FIELD', // 模板管理-接口模板-接口模板字段管理
                locale: 'system.orgTemplate.field',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
                permission: [],
                routeQuery: {
                  type: 'API',
                },
                level: MENU_LEVEL[2],
              },
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_API_TEMPLATE', // 模板管理-接口模板-接口模板管理
                locale: 'system.orgTemplate.apiTemplateManagement',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'API',
                },
                level: MENU_LEVEL[2],
              },
            ],
          },
          {
            key: 'PROJECT_MANAGEMENT_TEMPLATE_BUG', // 模板管理-缺陷模板
            locale: 'system.orgTemplate.defectTemplates',
            route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
            permission: [],
            routeQuery: {
              type: 'BUG',
            },
            level: MENU_LEVEL[2],
            children: [
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_BUG_FIELD', // 模板管理-缺陷模板管理-字段管理
                locale: 'system.orgTemplate.field',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_FIELD_SETTING,
                permission: [],
                routeQuery: {
                  type: 'BUG',
                },
                level: MENU_LEVEL[2],
              },
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_BUG_TEMPLATE', // 模板管理-缺陷模板-缺陷模板管理
                locale: 'system.orgTemplate.bugTemplateManagement',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT,
                permission: [],
                routeQuery: {
                  type: 'BUG',
                },
                level: MENU_LEVEL[2],
              },
              {
                key: 'PROJECT_MANAGEMENT_TEMPLATE_BUG_WORKFLOW', // 模板管理-缺陷模板-缺陷工作流
                locale: 'menu.settings.organization.templateManagementWorkFlow',
                route: RouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_WORKFLOW,
                permission: [],
                level: MENU_LEVEL[2],
              },
            ],
          },
        ],
      },

      {
        key: 'PROJECT_MANAGEMENT_FILE_MANAGEMENT', // 项目管理-文件管理
        locale: 'menu.projectManagement.fileManagement',
        route: RouteEnum.PROJECT_MANAGEMENT_FILE_MANAGEMENT,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT', // 项目管理-消息管理
        locale: 'menu.projectManagement.messageManagement',
        route: RouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_CONFIG', // 项目管理-消息管理-消息设置
            locale: 'project.messageManagement.config',
            route: RouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT,
            permission: [],
            level: MENU_LEVEL[2],
          },
          {
            key: 'PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_ROBOT', // 项目管理-消息管理-机器人列表
            locale: 'project.messageManagement.botList',
            route: RouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT,
            permission: [],
            routeQuery: {
              tab: 'botList',
            },
            level: MENU_LEVEL[2],
          },
        ],
      },
      {
        key: 'PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT', // 项目管理-消息管理-编辑
        locale: 'menu.projectManagement.messageManagementEdit',
        route: RouteEnum.PROJECT_MANAGEMENT_MESSAGE_MANAGEMENT_EDIT,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PROJECT_MANAGEMENT_COMMON_SCRIPT', // 项目管理-公共脚本
        locale: 'menu.projectManagement.commonScript',
        route: RouteEnum.PROJECT_MANAGEMENT_COMMON_SCRIPT,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PROJECT_MANAGEMENT_LOG', // 项目管理-日志
        locale: 'menu.projectManagement.log',
        route: RouteEnum.PROJECT_MANAGEMENT_LOG,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PROJECT_MANAGEMENT_ENVIRONMENT', // 项目管理-环境管理
        locale: 'menu.projectManagement.environmentManagement',
        route: RouteEnum.PROJECT_MANAGEMENT_ENVIRONMENT_MANAGEMENT,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PROJECT_MANAGEMENT_TASK_CENTER', // 项目管理-任务中心
        locale: 'menu.projectManagement.taskCenter',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
    ],
  },
  // 测试计划
  {
    key: 'TEST_PLAN', // 测试计划
    locale: 'menu.testPlan',
    route: RouteEnum.TEST_PLAN,
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'TEST_PLAN_MODULE', // 测试计划-模块
        locale: 'testPlan.testPlanGroup.module',
        route: RouteEnum.TEST_PLAN,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'TEST_PLAN_PLAN', // 测试计划-计划
        locale: 'menu.testPlanShort',
        route: RouteEnum.TEST_PLAN_INDEX_DETAIL,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'TEST_PLAN_GROUP', // 测试计划-计划组
        locale: 'menu.testPlanGroup',
        route: RouteEnum.TEST_PLAN_INDEX,
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'TEST_PLAN_REPORT', // 测试计划报告
        locale: 'menu.apiTest.report',
        route: RouteEnum.TEST_PLAN_REPORT,
        permission: [],
        level: MENU_LEVEL[2],
        children: [
          {
            key: 'TEST_PLAN_REPORT_TEST_PLAN', // 测试计划报告
            locale: 'menu.apiTest.reportTestPlan',
            route: RouteEnum.TEST_PLAN_REPORT_DETAIL,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              type: 'TEST_PLAN',
            },
          },
          {
            key: 'TEST_PLAN_REPORT_TEST_PLAN_GROUP', // 测试计划组报告
            locale: 'menu.apiTest.reportTestGroupPlan',
            route: RouteEnum.TEST_PLAN_REPORT_DETAIL,
            permission: [],
            level: MENU_LEVEL[2],
            routeQuery: {
              type: 'GROUP',
            },
          },
        ],
      },
    ],
  },
  {
    key: 'PERSONAL_INFORMATION', // 个人信息
    locale: 'ms.personal',
    route: '',
    permission: [],
    level: MENU_LEVEL[2],
    children: [
      {
        key: 'PERSONAL_INFORMATION_BASE_INFO', // 个人信息-基本信息
        locale: 'ms.personal.baseInfo',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PERSONAL_INFORMATION_PSW', // 个人信息-密码设置
        locale: 'ms.personal.setPsw',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PERSONAL_INFORMATION_APIKEYS', // 个人信息-ApiKeys
        locale: 'APIKEY',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PERSONAL_INFORMATION_LOCAL_EXECUTE', // 个人信息-本地执行
        locale: 'ms.personal.localExecution',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
      {
        key: 'PERSONAL_INFORMATION_TRIPARTITE', // 个人信息-三方平台账号
        locale: 'ms.personal.tripartite',
        route: '',
        permission: [],
        level: MENU_LEVEL[2],
      },
    ],
  },
  // 系统
  {
    key: 'SYSTEM',
    locale: 'menu.settings.system', // 系统
    route: '',
    permission: [],
    level: MENU_LEVEL[0],
  },
  {
    key: 'WORKBENCH', // 工作台
    locale: 'menu.workbench',
    route: RouteEnum.WORKBENCH,
    permission: [],
    level: MENU_LEVEL[1],
    children: [
      {
        key: 'WORKBENCH_INDEX', // 工作台-首页
        locale: 'menu.workbenchHomeSort',
        route: RouteEnum.WORKBENCH_INDEX,
        permission: [],
        level: MENU_LEVEL[1],
      },
      {
        key: 'WORKBENCH_INDEX_WAIT', // 工作台-待办
        locale: 'menu.workbenchWaitSort',
        route: RouteEnum.WORKBENCH_INDEX_WAIT,
        permission: [],
        level: MENU_LEVEL[1],
      },
      {
        key: 'WORKBENCH_INDEX_FOLLOW', // 工作台-我关注的
        locale: 'menu.workbenchFollowSort',
        route: RouteEnum.WORKBENCH_INDEX_FOLLOW,
        permission: [],
        level: MENU_LEVEL[1],
      },
      {
        key: 'WORKBENCH_INDEX_CREATED', // 工作台-我创建的
        locale: 'menu.workbenchCreatedSort',
        route: RouteEnum.WORKBENCH_INDEX_CREATED,
        permission: [],
        level: MENU_LEVEL[1],
      },
    ],
  },
];
