import { RouteEnum } from '@/enums/routeEnum';

export const MENU_LEVEL = ['SYSTEM', 'ORGANIZATION', 'PROJECT'] as const; // 菜单级别

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
export const pathMap = [
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
            locale: 'menu.settings.system.authorizedManagement',
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
    ],
  },
];
