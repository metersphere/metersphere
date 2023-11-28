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
    icon: 'icon-icon_functional_testing',
    order: 3,
    hideChildrenInMenu: true,
  },
  children: [
    // 功能用例
    {
      path: 'featureCase',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
      component: () => import('@/views/case-management/caseManagementFeature/index.vue'),
      meta: {
        locale: 'menu.caseManagement.featureCase',
        roles: ['*'],
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
        roles: ['*'],
        breadcrumbs: [
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE,
            locale: 'menu.caseManagement.featureCase',
          },
          {
            name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_DETAIL,
            editTag: 'id',
            locale: 'menu.caseManagement.featureCaseDetail',
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
        roles: ['*'],
      },
    },
    // 功能用例回收站
    {
      path: 'featureCaseRecycle',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_CASE_RECYCLE,
      component: () => import('@/views/case-management/caseManagementFeature/components/recycleCaseTable.vue'),
      meta: {
        locale: 'menu.caseManagement.featureCaseRecycle',
        roles: ['*'],
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
        locale: 'menu.caseManagement.caseManagementReview',
        roles: ['*'],
        isTopMenu: true,
      },
    },
    {
      path: 'caseManagementReviewCreate',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_CREATE,
      component: () => import('@/views/case-management/caseReview/create.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementReviewCreate',
        roles: ['*'],
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
    {
      path: 'caseManagementReviewDetail',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL,
      component: () => import('@/views/case-management/caseReview/detail.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementReviewDetail',
        roles: ['*'],
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
    {
      path: 'caseManagementReviewDetailCaseDetail',
      name: CaseManagementRouteEnum.CASE_MANAGEMENT_REVIEW_DETAIL_CASE_DETAIL,
      component: () => import('@/views/case-management/caseReview/caseDetail.vue'),
      meta: {
        locale: 'menu.caseManagement.caseManagementCaseDetail',
        roles: ['*'],
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
