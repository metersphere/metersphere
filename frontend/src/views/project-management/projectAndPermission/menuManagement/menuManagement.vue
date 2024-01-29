<template>
  <div class="flex flex-row items-center">
    <div class="text-[var(--color-text-1)]"> {{ t('project.menu.management') }}</div>
    <a-tooltip :content="t('project.menu.manageTip')" position="bl">
      <div>
        <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
      </div>
    </a-tooltip>
  </div>
  <MsBaseTable
    ref="tableRef"
    class="mt-[16px]"
    v-bind="propsRes"
    row-class="cursor-pointer"
    :expanded-keys="expandedKeys"
    @row-click="handleRowClick"
    @expand="expandChange"
    v-on="propsEvent"
  >
    <template #module="{ record }">
      <div v-if="record.children">
        <MsIcon class="text-[var(--color-text-4)]" :type="getMenuIcon(record.module)" />
        <span class="ml-[4px]">{{ t(`menu.${record.module}`) }}</span>
      </div>
      <div v-else>
        <span class="ml-[4px]">{{ t(`project.menu.${record.type}`) }}</span>
      </div>
    </template>
    <template #description="{ record }">
      <div v-if="record.type === 'WORKSTATION_SYNC_RULE'">
        <!-- 工作台 接口测试待更新同步规则 -->
        {{ t('project.menu.row1') }}
      </div>
      <div v-if="record.type === 'TEST_PLAN_CLEAN_REPORT'">
        <!-- 测试计划 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['TEST_PLAN_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('TEST_PLAN_CLEAN_REPORT',v,MenuEnum.testPlan)"
        />
      </div>
      <div v-if="record.type === 'TEST_PLAN_SHARE_REPORT'">
        <!-- 测试计划 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['TEST_PLAN_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('TEST_PLAN_SHARE_REPORT',v,MenuEnum.testPlan)"
        />
      </div>
      <template v-if="record.type === 'BUG_SYNC'">
        <!-- 同步缺陷 -->
        <span>{{ t('project.menu.row2') }}</span>
        <div
          v-permission="['PROJECT_APPLICATION_BUG:READ+UPDATE']"
          class="ml-[8px] cursor-pointer text-[rgb(var(--primary-7))]"
          @click="showDefectDrawer"
          >{{ t('project.menu.BUG_SYNC') }}</div
        >
      </template>
      <div v-if="record.type === 'CASE_PUBLIC'">
        <!-- 用例 公共用例库 -->
        {{ t('project.menu.row3') }}
      </div>
      <div v-if="record.type === 'CASE_RELATED'" class="flex flex-row">
        <!-- 用例 关联需求 -->
        <div>{{ t('project.menu.row4') }}</div>
        <div
          v-permission="['PROJECT_APPLICATION_CASE:READ+UPDATE']"
          class="ml-[8px] cursor-pointer text-[rgb(var(--primary-7))]"
          @click="showRelatedCaseDrawer"
          >{{ t('project.menu.CASE_RELATED') }}</div
        >
      </div>
      <div v-if="record.type === 'CASE_RE_REVIEW'">
        <!-- 用例 重新提审 -->
        {{ t('project.menu.row5') }}
      </div>
      <div v-if="record.type === 'API_URL_REPEATABLE'">
        <!-- 接口测试 接口定义URL可重复  -->
        {{ t('project.menu.row6') }}
      </div>
      <div v-if="record.type === 'API_CLEAN_REPORT'">
        <MsTimeSelectorVue
          v-model="allValueMap['API_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('API_CLEAN_REPORT',v,MenuEnum.apiTest)"
        />
      </div>
      <div v-if="record.type === 'API_SHARE_REPORT'">
        <!--接口测试 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['API_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('API_SHARE_REPORT',v,MenuEnum.apiTest)"
        />
      </div>
      <div v-if="record.type === 'API_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--接口测试 执行资源池 -->
        <a-select
          v-model="allValueMap['API_RESOURCE_POOL_ID']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="apiPoolOption"
          class="w-[120px]"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
          @change="(v: SelectValue) => handleMenuStatusChange('API_RESOURCE_POOL_ID',v as string,MenuEnum.apiTest)"
        />
        <a-tooltip
          :content="
            t('project.menu.API_RESOURCE_POOL_TIP', {
              name: getPoolTipName(allValueMap['API_RESOURCE_POOL_ID'], MenuEnum.apiTest),
            })
          "
          position="right"
        >
          <div>
            <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--接口测试 脚本审核 -->
        <a-select
          v-model="allValueMap['API_SCRIPT_REVIEWER_ID']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="apiAuditorOption"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
          class="w-[120px]"
          @change="(v: SelectValue) => handleMenuStatusChange('API_SCRIPT_REVIEWER_ID',v as string,MenuEnum.apiTest)"
        />
        <a-tooltip :content="t('project.menu.API_SCRIPT_REVIEWER_TIP')" position="right">
          <div>
            <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_ERROR_REPORT_RULE'" class="flex w-[100%] flex-row items-center">
        <!--接口测试 误报规则 -->
        <div class="error-report">
          <a-input-number
            v-model="allValueMap['FAKE_ERROR_NUM']"
            class="w-[120px]"
            disabled
            :placeholder="t('project.menu.pleaseConfig')"
          >
            <template #append>
              <div>{{ t('project.menu.count') }}</div>
            </template>
          </a-input-number>
        </div>
        <div
          v-permission="['PROJECT_APPLICATION_API:READ+UPDATE']"
          class="ml-[8px] cursor-pointer text-[rgb(var(--primary-7))]"
          @click="pushFar"
          >{{ t('project.menu.API_ERROR_REPORT_RULE') }}</div
        >
        <a-tooltip :content="t('project.menu.API_ERROR_REPORT_RULE_TIP')" position="right">
          <div>
            <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SYNC_CASE'">{{ t('project.menu.row7') }} </div>
      <div v-if="record.type === 'UI_CLEAN_REPORT'">
        <!--UI 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['UI_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('UI_CLEAN_REPORT',v,MenuEnum.uiTest)"
        />
      </div>
      <div v-if="record.type === 'UI_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['UI_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('UI_SHARE_REPORT',v,MenuEnum.uiTest)"
        />
      </div>
      <div v-if="record.type === 'UI_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--UI 执行资源池 -->
        <a-select
          v-model="allValueMap['UI_RESOURCE_POOL_ID']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="uiPoolOption"
          class="w-[120px]"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:READ+UPDATE'])"
          @change="(v: SelectValue) => handleMenuStatusChange('UI_RESOURCE_POOL_ID',v as string,MenuEnum.uiTest)"
        />
        <a-tooltip
          :content="
            t('project.menu.UI_RESOURCE_POOL_TIP', {
              name: getPoolTipName(allValueMap['UI_RESOURCE_POOL_ID'], MenuEnum.uiTest),
            })
          "
          position="right"
        >
          <div>
            <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_CLEAN_REPORT'">
        <!--性能测试 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('PERFORMANCE_TEST_CLEAN_REPORT',v,MenuEnum.loadTest)"
        />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SHARE_REPORT'">
        <!--性能测试 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ+UPDATE'])"
          @change="(v: string) => handleMenuStatusChange('PERFORMANCE_TEST_SHARE_REPORT',v,MenuEnum.loadTest)"
        />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--性能测试 脚本审核 -->
        <a-select
          v-model="allValueMap['PERFORMANCE_TEST_SCRIPT_REVIEWER_ID']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="performanceAuditorOption"
          class="w-[120px]"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ+UPDATE'])"
          @change="(v: SelectValue) => handleMenuStatusChange('PERFORMANCE_TEST_SCRIPT_REVIEWER_ID',v as string,MenuEnum.loadTest)"
        />
        <a-tooltip :content="t('project.menu.PERFORMANCE_TEST_SCRIPT_REVIEWER_TIP')" position="right">
          <div>
            <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
    </template>
    <template #operation="{ record }">
      <!-- 缺陷 同步缺陷状态 -->
      <div v-permission="['PROJECT_APPLICATION_BUG:READ+UPDATE']">
        <a-tooltip v-if="record.type === 'BUG_SYNC' && !allValueMap['BUG_SYNC_SYNC_ENABLE']" position="tr">
          <template #content>
            <span>
              {{ t('project.menu.notConfig') }}
              <span class="cursor-pointer text-[rgb(var(--primary-4))]" @click="showDefectDrawer">{{
                t(`project.menu.${record.type}`)
              }}</span>
              {{ t('project.menu.configure') }}
            </span>
          </template>

          <a-switch
            checked-value="true"
            unchecked-value="false"
            :disabled="!hasAnyPermission(['PROJECT_APPLICATION_BUG:READ+UPDATE'])"
            :value="allValueMap['BUG_SYNC_SYNC_ENABLE']"
            size="small"
            type="line"
            @change="(v: boolean | string| number) => handleMenuStatusChange('BUG_SYNC_SYNC_ENABLE',v as boolean, MenuEnum.bugManagement)"
          />
        </a-tooltip>
      </div>

      <a-switch
        v-if="record.type === 'BUG_SYNC' && allValueMap['BUG_SYNC_SYNC_ENABLE']"
        checked-value="true"
        unchecked-value="false"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_BUG:READ+UPDATE'])"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange('BUG_SYNC_SYNC_ENABLE',v as boolean, MenuEnum.bugManagement)"
      />
      <!-- 功能测试 同步缺陷 -->
      <div v-permission="['PROJECT_APPLICATION_BUG:READ+UPDATE']">
        <a-tooltip v-if="record.type === 'CASE_RELATED' && !allValueMap['CASE_RELATED_CASE_ENABLE']" position="tr">
          <template #content>
            <span>
              {{ t('project.menu.notConfig') }}
              <span class="cursor-pointer text-[rgb(var(--primary-4))]" @click="showDefectDrawer">{{
                t(`project.menu.${record.type}`)
              }}</span>
              {{ t('project.menu.configure') }}
            </span>
          </template>
          <a-switch
            checked-value="true"
            unchecked-value="false"
            :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:READ+UPDATE'])"
            :value="allValueMap['CASE_RELATED_CASE_ENABLE']"
            size="small"
            type="line"
            @change="(v: boolean | string| number) => handleMenuStatusChange('CASE_RELATED_CASE_ENABLE',v as boolean, MenuEnum.caseManagement)"
          />
        </a-tooltip>
      </div>
      <a-switch
        v-if="record.type === 'CASE_RELATED' && allValueMap['CASE_RELATED_CASE_ENABLE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_BUG:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange('CASE_RELATED_CASE_ENABLE',v as boolean, MenuEnum.caseManagement)"
      />
      <!-- 其他配置项 -->
      <!-- 接口测试待更新同步规则 Switch-->
      <a-switch
        v-if="record.type === 'WORKSTATION_SYNC_RULE'"
        v-model="allValueMap['WORKSTATION_SYNC_RULE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange(record.type,v as boolean,MenuEnum.workstation)"
      />
      <!-- 用例 公共用例 Switch-->
      <a-switch
        v-if="record.type === 'CASE_PUBLIC'"
        v-model="allValueMap['CASE_PUBLIC']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange(record.type,v as boolean,MenuEnum.caseManagement)"
      />
      <!-- 用例 重新提审 Switch-->
      <a-switch
        v-if="record.type === 'CASE_RE_REVIEW'"
        v-model="allValueMap['CASE_RE_REVIEW']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange(record.type,v as boolean,MenuEnum.caseManagement)"
      />
      <!-- 接口测试 接口定义URL可重复 Switch-->
      <a-switch
        v-if="record.type === 'API_URL_REPEATABLE'"
        v-model="allValueMap['API_URL_REPEATABLE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange(record.type,v as boolean,MenuEnum.apiTest)"
      />
      <!-- 接口测试 用例同步 Switch-->
      <a-switch
        v-if="record.type === 'API_SYNC_CASE'"
        v-model="allValueMap['API_SYNC_CASE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange(record.type,v as boolean,MenuEnum.apiTest)"
      />
      <!-- 性能测试 脚本审核 Switch-->
      <a-switch
        v-if="record.type === 'PERFORMANCE_TEST_SCRIPT_REVIEWER'"
        v-model="allValueMap['PERFORMANCE_TEST_SCRIPT_REVIEWER_ENABLE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ+UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange('PERFORMANCE_TEST_SCRIPT_REVIEWER_ENABLE',v as boolean,MenuEnum.loadTest)"
      />
    </template>
  </MsBaseTable>
  <DefectSync v-model:visible="defectDrawerVisible" @cancel="defectDrawerVisible = false" />
  <RelatedCase v-model:visible="relatedCaseDrawerVisible" @cancel="relatedCaseDrawerVisible = false" />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-菜单管理
   */
  import { useRouter } from 'vue-router';
  import { Message, TableData } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTimeSelectorVue from '@/components/pure/ms-time-selector/MsTimeSelector.vue';
  import DefectSync from './components/defectSync.vue';
  import RelatedCase from './components/relatedCase.vue';

  import {
    getAuditorOptions,
    getConfigByMenuItem,
    getPoolOptions,
    postTabletList,
    postUpdateMenu,
  } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import { hasAnyPermission } from '@/utils/permission';

  import { MenuTableConfigItem, PoolOption, SelectValue } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';

  const appStore = useAppStore();
  const router = useRouter();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();
  const defectDrawerVisible = ref(false);
  const relatedCaseDrawerVisible = ref(false);
  const apiPoolOption = ref<PoolOption[]>([]);
  const uiPoolOption = ref<PoolOption[]>([]);
  const apiAuditorOption = ref<PoolOption[]>([]);
  const performanceAuditorOption = ref<PoolOption[]>([]);
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  const expandedKeys = ref<string[]>([]);

  // 默认初始值的配置项
  const defaultValueMap = {
    TEST_PLAN_CLEAN_REPORT: '3M',
    TEST_PLAN_SHARE_REPORT: '1D',
    API_CLEAN_REPORT: '3M',
    API_SHARE_REPORT: '1D',
    UI_CLEAN_REPORT: '3M',
    UI_SHARE_REPORT: '1D',
    PERFORMANCE_TEST_CLEAN_REPORT: '3M',
    PERFORMANCE_TEST_SHARE_REPORT: '1D',
    WORKSTATION_SYNC_RULE: true,
    CASE_RELATED: true,
    CASE_RE_REVIEW: true,
    PERFORMANCE_TEST_SCRIPT_REVIEWER: true,
  };

  const allValueMap = ref<MenuTableConfigItem>(defaultValueMap);

  const hasTitleColumns = [
    {
      title: 'project.menu.name',
      dataIndex: 'module',
      slotName: 'module',
      width: 221,
      headerCellClass: 'pl-[16px]',
    },
    {
      title: 'project.menu.description',
      slotName: 'description',
      dataIndex: 'description',
      showDrag: true,
      width: 515,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'moduleEnable',
      width: 58,
    },
  ];

  const noTitleColumns = [
    {
      title: 'project.menu.name',
      dataIndex: 'module',
      slotName: 'module',
      width: 221,
      headerCellClass: 'pl-[16px]',
    },
    {
      title: '',
      slotName: 'description',
      dataIndex: 'description',
      showDrag: true,
      width: 515,
    },
    {
      title: '',
      slotName: 'operation',
      dataIndex: 'moduleEnable',
      width: 58,
    },
  ];

  const getChildren = (record: TableData) => {
    let children: MenuTableConfigItem[] = [];
    switch (record.module) {
      case MenuEnum.workstation: {
        children = [
          {
            type: 'WORKSTATION_SYNC_RULE', // 待更新列表
          },
        ];
        break;
      }
      case MenuEnum.testPlan: {
        children = [
          {
            type: 'TEST_PLAN_CLEAN_REPORT', // 报告保留时间范围
          },
          {
            type: 'TEST_PLAN_SHARE_REPORT', // 报告链接有效期
          },
        ];
        break;
      }
      case MenuEnum.bugManagement: {
        children = [
          {
            type: 'BUG_SYNC', // 缺陷同步
            typeEnable: 'BUG_SYNC_SYNC_ENABLE', // 缺陷同步开关
          },
        ];
        break;
      }
      case MenuEnum.caseManagement: {
        children = [
          {
            type: 'CASE_PUBLIC', // 公共用例库
          },
          {
            type: 'CASE_RE_REVIEW', // 重新提审
          },
          {
            type: 'CASE_RELATED',
            typeEnable: 'CASE_RELATED_CASE_ENABLE', // 关联需求开关
          },
        ];
        break;
      }
      case MenuEnum.apiTest: {
        children = [
          {
            type: 'API_URL_REPEATABLE', // 接口定义URL可重复
          },
          {
            type: 'API_CLEAN_REPORT', // 报告保留时间范围
          },
          {
            type: 'API_SHARE_REPORT', // 报告链接有效期
          },
          {
            type: 'API_RESOURCE_POOL', // 执行资源池
          },
          {
            type: 'API_SCRIPT_REVIEWER', // 脚本审核
          },
          {
            type: 'API_ERROR_REPORT_RULE', // 误报规则
          },
          {
            type: 'API_SYNC_CASE', // 用例同步
          },
        ];
        break;
      }
      case MenuEnum.uiTest: {
        children = [
          {
            type: 'UI_CLEAN_REPORT', // 报告保留时间范围
          },
          {
            type: 'UI_SHARE_REPORT', // 报告链接有效期
          },
          {
            type: 'UI_RESOURCE_POOL', // 执行资源池
          },
        ];
        break;
      }
      default: {
        children = [
          {
            type: 'PERFORMANCE_TEST_CLEAN_REPORT', // 报告保留时间范围
          },
          {
            type: 'PERFORMANCE_TEST_SHARE_REPORT', // 报告链接有效期
          },
          {
            type: 'PERFORMANCE_TEST_SCRIPT_REVIEWER', // 脚本审核
          },
        ];
      }
    }
    return children;
  };

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    postTabletList,
    {
      showPagination: false,
      columns: noTitleColumns,
      selectable: false,
      scroll: { x: '100%' },
      noDisable: true,
      rowKey: 'module',
      showExpand: true,
      emptyDataShowLine: false,
    },
    (item: TableData) => {
      const children = getChildren(item);
      return { ...item, children };
    }
  );

  const getMenuConfig = async (type: string) => {
    try {
      let hasAuth = false;
      switch (type) {
        case MenuEnum.workstation:
          if (hasAnyPermission(['PROJECT_APPLICATION_WORKSTATION:READ'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.apiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_API:READ'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.bugManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_BUG:READ'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.caseManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_CASE:READ'])) {
            hasAuth = true;
          }
          break;
        case 'loadTest':
          if (hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.testPlan:
          if (hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:READ'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.uiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_UI:READ'])) {
            hasAuth = true;
          }
          break;
        default:
          break;
      }

      if (!hasAuth) {
        Message.error(t('common.noPermission'));
        return;
      }

      const resultObj = await getConfigByMenuItem({
        projectId: currentProjectId.value,
        type,
      });
      allValueMap.value = { ...allValueMap.value, ...resultObj };
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };

  const expandChange = async (record: TableData) => {
    try {
      if (expandedKeys.value.includes(record.module)) {
        // 收起
        expandedKeys.value = expandedKeys.value.filter((item) => item !== record.module);
        return;
      }
      expandedKeys.value = [...expandedKeys.value, record.module];
      getMenuConfig(record.module);
      if (record.module === MenuEnum.apiTest && !apiPoolOption.value.length) {
        apiPoolOption.value = await getPoolOptions(currentProjectId.value, record.module);
      } else if (record.module === MenuEnum.uiTest && !uiPoolOption.value.length) {
        uiPoolOption.value = await getPoolOptions(currentProjectId.value, record.module);
      } else if (record.module === MenuEnum.apiTest && !apiAuditorOption.value.length) {
        apiAuditorOption.value = await getAuditorOptions(currentProjectId.value, record.module);
      } else if (record.module === MenuEnum.loadTest && !performanceAuditorOption.value.length) {
        performanceAuditorOption.value = await getAuditorOptions(currentProjectId.value, record.module);
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };
  const getMenuIcon = (type: MenuEnum) => {
    switch (type) {
      case MenuEnum.workstation:
        return 'icon-icon_pc_filled';
      case MenuEnum.testPlan:
        return 'icon-icon_test-tracking_filled';
      case MenuEnum.bugManagement:
        return 'icon-icon_defect';
      case MenuEnum.caseManagement:
        return 'icon-icon_functional_testing';
      case MenuEnum.apiTest:
        return 'icon-icon_api-test-filled';
      case MenuEnum.uiTest:
        return 'icon-icon_ui-test-filled';
      default:
        return 'icon-icon_performance-test-filled';
    }
  };

  const handleMenuStatusChange = async (type: string, typeValue: string | boolean, suffix: string) => {
    try {
      let hasAuth = false;
      switch (suffix) {
        case MenuEnum.workstation:
          if (hasAnyPermission(['PROJECT_APPLICATION_WORKSTATION:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.apiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_API:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.bugManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_BUG:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.caseManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_CASE:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.loadTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.testPlan:
          if (hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.uiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_UI:READ+UPDATE'])) {
            hasAuth = true;
          }
          break;
        default:
          // eslint-disable-next-line no-console
          console.log('no ');
          break;
      }

      if (!hasAuth) {
        Message.error(t('common.noPermission'));
        return;
      }
      await postUpdateMenu(
        {
          projectId: currentProjectId.value,
          type,
          typeValue: typeof typeValue === 'boolean' ? typeValue.toString() : typeValue,
        },
        suffix
      );
      Message.success(t('common.operationSuccess'));
      getMenuConfig(suffix);
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };
  const fetchData = async () => {
    await loadList();
  };

  // 打开同步缺陷
  const showDefectDrawer = () => {
    defectDrawerVisible.value = true;
  };
  // 打开关联需求
  const showRelatedCaseDrawer = () => {
    relatedCaseDrawerVisible.value = true;
  };
  // 跳转到误报规则列表页
  const pushFar = () => {
    router.push({ name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE });
  };

  // 获取执行资源池的名称
  const getPoolTipName = (id: string, type: MenuEnum) => {
    const poolOption = type === MenuEnum.apiTest ? apiPoolOption.value : uiPoolOption.value;
    const pool = poolOption.find((item) => item.id === id);
    return pool?.name;
  };

  const initExpendKeys = async () => {
    if (router.currentRoute.value.redirectedFrom) {
      // 从误报规则跳转回来的
      await expandChange({ module: MenuEnum.apiTest });
    } else {
      expandedKeys.value = [];
    }
  };

  // 点击展开
  const handleRowClick = (record: TableData, ev: Event) => {
    if (record.module) {
      expandChange(record);
    }
  };

  onMounted(() => {
    setLoadListParams({ projectId: currentProjectId.value });
    fetchData();
    initExpendKeys();
  });
  watch(currentProjectId, () => {
    fetchData();
  });
  watch(
    () => expandedKeys.value.length,
    (val) => {
      if (!val) {
        tableRef.value?.initColumn(noTitleColumns);
      } else {
        tableRef.value?.initColumn(hasTitleColumns);
      }
    }
  );
</script>

<style scoped></style>
