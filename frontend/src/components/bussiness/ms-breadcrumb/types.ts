import type { RouteRecordName } from 'vue-router';

export interface BreadcrumbItem {
  name: RouteRecordName;
  locale: string;
}
