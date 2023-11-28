import type { RouteRecordName } from 'vue-router';

export interface BreadcrumbItem {
  name: RouteRecordName; // 路由名称
  locale: string; // 国际化语言单词
  isBack?: boolean; // 是否为返回上一个历史记录（当遇到父级页面也是携带 ID 参数打开的详情页面包屑时，设置 true 可以使面包屑跳转变成回退）
  editTag?: string; // 编辑标识，指路由地址参数，面包屑组件会根据此参数判断是否为编辑页面
  editLocale?: string; // 编辑国际化语言单词
  query?: string[]; // 路由地址参数，面包屑组件会根据此参数拼接路由地址
}
