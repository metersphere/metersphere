import type { RouteRecordName } from 'vue-router';

export interface BreadcrumbItem {
  name: RouteRecordName; // 路由名称
  locale: string; // 国际化语言单词
  editTag?: string; // 编辑标识，指路由地址参数，面包屑组件会根据此参数判断是否为编辑页面
  editLocale?: string; // 编辑国际化语言单词
}
