import { CaseManagementRouteEnum } from '@/enums/routeEnum';

import { DEFAULT_LAYOUT } from '../base';
import type { AppRouteRecordRaw } from '../types';

const CaseManagement: AppRouteRecordRaw = {
  path: '/case-management',
  name: CaseManagementRouteEnum.CASE_MANAGEMENT,
  redirect: '/case-management/featureCase',
  component: DEFAULT_LAYOUT,
  meta: {
    locale: 'menu.caseManagement',
    collapsedLocale: 'menu.caseManagementShort',
    icon: 'icon-icon_functional_testing1',
    order: 3,
    hideChildrenInMenu: true,
    roles: ['FUNCTIONAL_CASE:READ', 'CASE_REVIEW:READ'],
  },
  children: [
    // 功能用例
    {
      path: 'featureCase',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
      component: () => import('@/views/case-management/caseManagementFeature/index.vue'),
      meta: {
        locale: 'menu.caseManagementShort',
        roles: ['FUNCTIONAL_CASE:READ'],
        isTopMenu: true,
      },
    },
    // 创建用例&编辑用例
    {
      path: 'featureCaseDetail/:mode?',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
      component: () => import('@/views/case-management/caseManagementFeature/components/caseDetail.vue'),
      meta: {
        locale: 'menu.caseManagement.featureCaseDetail',
        roles: ['FUNCTIONAL_CASE:READ+ADD', 'FUNCTIONAL_CASE:READ+UPDATE'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
            locale: 'menu.caseManagement.featureCase',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
            locale: 'menu.caseManagement.featureCaseDetail',
            editTag: 'id',
            editLocale: 'menu.caseManagement.featureCaseEdit',
          },
        ],
      },
    },
    // 创建用例成功
    {
      path: 'featureCaseCreateSuccess',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_CREATE_SUCCESS,
      component: () => import('@/views/case-management/caseManagementFeature/components/createSuccess.vue'),
      meta: {
        locale: 'menu.caseManagement.featureCaseCreateSuccess',
        roles: ['FUNCTIONAL_CASE:READ+ADD'],
      },
    },
    // 功能用例回收站
    {
      path: 'featureCaseRecycle',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_RECYCLE,
      component: () => import('@/views/case-management/caseManagementFeature/components/recycleCaseTable.vue'),
      meta: {
        locale: 'menu.caseManagement.featureCaseRecycle',
        roles: ['FUNCTIONAL_CASE:READ'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
            locale: 'menu.caseManagement.featureCaseList',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_RECYCLE,
            locale: 'menu.caseManagement.featureCaseRecycle',
          },
        ],
      },
    },
    // 用例评审
    {
      path: 'caseManagementReview',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
      component: () => import('@/views/case-management/caseReview/index.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementReviewShort',
        roles: ['CASE_REVIEW:READ'],
        isTopMenu: true,
      },
    },
    // 创建评审
    {
      path: 'caseManagementReviewCreate',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      component: () => import('@/views/case-management/caseReview/create.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementReviewCreate',
        roles: ['CASE_REVIEW:READ+ADD', 'CASE_REVIEW:READ+UPDATE'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
            locale: 'menu.caseManagement.caseManagementReview',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
            locale: 'menu.caseManagement.caseManagementReviewCreate',
            editTag: 'id',
            editLocale: 'menu.caseManagement.caseManagementCaseReviewEdit',
          },
        ],
      },
    },
    // 评审详情
    {
      path: 'caseManagementReviewDetail',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
      component: () => import('@/views/case-management/caseReview/detail.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementReviewDetail',
        roles: ['CASE_REVIEW:READ'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
            locale: 'menu.caseManagement.caseManagementReview',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
            locale: 'menu.caseManagement.caseManagementReviewDetail',
          },
        ],
      },
    },
    // 评审详情-用例详情
    {
      path: 'caseManagementReviewDetailCaseDetail',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      component: () => import('@/views/case-management/caseReview/caseDetail.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementCaseDetail',
        roles: ['CASE_REVIEW:READ'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW,
            locale: 'menu.caseManagement.caseManagementReview',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
            locale: 'menu.caseManagement.caseManagementReviewDetail',
            isBack: true,
            query: ['id'],
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
            locale: 'menu.caseManagement.caseManagementCaseDetail',
          },
        ],
      },
    },
  ],
};

export default CaseManagement;
