<template>
  <div class="flex flex-row items-center">
    <div class="text-[var(--color-text-1)]"> {{ t('project.menu.management') }}</div>
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
      <div v-if="record.children" class="flex items-center">
        <div class="icon-class">
          <MsIcon class="text-[12px] text-[rgb(var(--primary-4))]" :type="getMenuIcon(record.module)" />
        </div>
        <span>{{ t(`menu.${record.module}`) }}</span>
      </div>
      <div v-else>
        <span class="ml-[28px]">{{ t(`project.menu.${record.type}`) }}</span>
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
          v-model:model-value="allValueMap.TEST_PLAN_CLEAN_REPORT"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:UPDATE'])"
          :default-value="defaultValueMap.TEST_PLAN_CLEAN_REPORT"
          @change="(v: string) => handleMenuStatusChange('TEST_PLAN_CLEAN_REPORT',v,MenuEnum.testPlan)"
        />
      </div>
      <div v-if="record.type === 'TEST_PLAN_SHARE_REPORT'">
        <!-- 测试计划 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model:model-value="allValueMap.TEST_PLAN_SHARE_REPORT"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:UPDATE'])"
          :default-value="defaultValueMap.TEST_PLAN_SHARE_REPORT"
          @change="(v: string) => handleMenuStatusChange('TEST_PLAN_SHARE_REPORT',v,MenuEnum.testPlan)"
        />
      </div>
      <template v-if="record.type === 'BUG_SYNC'">
        <!-- 同步缺陷 -->
        <span>{{ t('project.menu.row2') }}</span>
        <!-- TODO 接口没有字段，先不上 -->
        <!-- <a-tooltip :content="t('project.menu.bugThirdIntegrationTip')" :mouse-enter-delay="300"> -->
        <MsButton class="ml-[8px]" @click="showDefectDrawer"> {{ t('project.menu.BUG_SYNC') }}</MsButton>
        <!-- </a-tooltip> -->
      </template>
      <div v-if="record.type === 'CASE_PUBLIC'">
        <!-- 用例 公共用例库 -->
        {{ t('project.menu.row3') }}
      </div>
      <div v-if="record.type === 'CASE_RELATED'" class="flex flex-row">
        <!-- 用例 关联需求 -->
        <div>{{ t('project.menu.row4') }}</div>
        <div class="ml-[8px] cursor-pointer text-[rgb(var(--primary-5))]" @click="showRelatedCaseDrawer">
          {{ t('project.menu.CASE_RELATED') }}
        </div>
      </div>
      <div v-if="record.type === 'CASE_RE_REVIEW'">
        <!-- 用例 重新提审 -->
        <span>{{ t('project.menu.row5') }}</span>
        <a-tooltip :content="t('project.menu.reArraignment')" position="top" :mouse-enter-delay="300">
          <MsIcon
            class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
            type="icon-icon-maybe_outlined"
          />
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_URL_REPEATABLE'">
        <!-- 接口测试 接口定义URL可重复  -->
        {{ t('project.menu.row6') }}
      </div>
      <div v-if="record.type === 'API_CLEAN_REPORT'">
        <MsTimeSelectorVue
          v-model="allValueMap['API_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
          :default-value="defaultValueMap.API_CLEAN_REPORT"
          @change="(v: string) => handleMenuStatusChange('API_CLEAN_REPORT',v,MenuEnum.apiTest)"
        />
      </div>
      <div v-if="record.type === 'API_SHARE_REPORT'">
        <!--接口测试 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['API_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
          :default-value="defaultValueMap.API_SHARE_REPORT"
          @change="(v: string) => handleMenuStatusChange('API_SHARE_REPORT',v,MenuEnum.apiTest)"
        />
      </div>
      <div v-if="record.type === 'API_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--接口测试 执行资源池 -->
        <div class="w-[200px]">
          <MsSelect
            v-model="allValueMap['API_RESOURCE_POOL_ID']"
            label-key="name"
            value-key="id"
            :options="apiPoolOption"
            :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
            :option-not-exits-text="t('system.resourcePool.notExit')"
            @change="(v: SelectValue) => handleMenuStatusChange('API_RESOURCE_POOL_ID',v as string,MenuEnum.apiTest)"
          />
        </div>
        <a-tooltip :content="t('project.menu.API_RESOURCE_POOL_TIP')" position="right">
          <div>
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--接口测试 脚本审核 -->
        <a-select
          v-model="allValueMap['API_SCRIPT_REVIEWER_ID']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="apiAuditorOption"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
          class="w-[120px]"
          @change="(v: SelectValue) => handleMenuStatusChange('API_SCRIPT_REVIEWER_ID',v as string,MenuEnum.apiTest)"
        />
        <a-tooltip :content="t('project.menu.API_SCRIPT_REVIEWER_TIP')" position="right">
          <div>
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_ERROR_REPORT_RULE'" class="flex w-[100%] flex-row items-center">
        <!--接口测试 误报规则 -->
        <div class="error-report">
          {{ t('project.menu.rule.hasBeenEnabled') }}
          <span class="text-[rgb(var(--primary-5))]" @click="pushFar(true)">
            {{ allValueMap['ENABLE_FAKE_ERROR_NUM'] || 0 }}
          </span>
          {{ t('project.menu.rule.bar') }}
        </div>
        <div class="ml-[8px] cursor-pointer font-medium text-[rgb(var(--primary-5))]" @click="pushFar(false)">
          {{ t('project.menu.rule.ruleAlertList') }}
        </div>
        <a-tooltip :content="t('project.menu.API_ERROR_REPORT_RULE_TIP')" position="right">
          <div>
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SYNC_CASE'">{{ t('project.menu.row7') }} </div>
      <div v-if="record.type === 'TASK_CLEAN_REPORT'">
        <MsTimeSelectorVue
          v-model="allValueMap['TASK_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TASK:UPDATE'])"
          :default-value="defaultValueMap.TASK_CLEAN_REPORT"
          @change="(v: string) =>
        handleMenuStatusChange('TASK_CLEAN_REPORT', v, MenuEnum.taskCenter)"
        />
      </div>
      <div v-if="record.type === 'TASK_RECORD'">
        <MsTimeSelectorVue
          v-model="allValueMap['TASK_RECORD']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_TASK:UPDATE'])"
          :default-value="defaultValueMap.TASK_RECORD"
          @change="(v: string) =>
        handleMenuStatusChange('TASK_RECORD', v, MenuEnum.taskCenter)"
        />
      </div>
      <div v-if="record.type === 'UI_CLEAN_REPORT'">
        <!--UI 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['UI_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:UPDATE'])"
          :default-value="defaultValueMap.UI_CLEAN_REPORT"
          @change="(v: string) => handleMenuStatusChange('UI_CLEAN_REPORT',v,MenuEnum.uiTest)"
          @blur="(v: string) => handleMenuStatusChange('UI_CLEAN_REPORT',v,MenuEnum.uiTest)"
        />
      </div>
      <div v-if="record.type === 'UI_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['UI_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:UPDATE'])"
          :default-value="defaultValueMap.UI_SHARE_REPORT"
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
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_UI:UPDATE'])"
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
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_CLEAN_REPORT'">
        <!--性能测试 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_CLEAN_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE'])"
          :default-value="defaultValueMap.PERFORMANCE_TEST_CLEAN_REPORT"
          @change="(v: string) => handleMenuStatusChange('PERFORMANCE_TEST_CLEAN_REPORT',v,MenuEnum.loadTest)"
        />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SHARE_REPORT'">
        <!--性能测试 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_SHARE_REPORT']"
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE'])"
          :default-value="defaultValueMap.PERFORMANCE_TEST_SHARE_REPORT"
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
          :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE'])"
          @change="(v: SelectValue) => handleMenuStatusChange('PERFORMANCE_TEST_SCRIPT_REVIEWER_ID',v as string,MenuEnum.loadTest)"
        />
        <a-tooltip :content="t('project.menu.PERFORMANCE_TEST_SCRIPT_REVIEWER_TIP')" position="right">
          <div>
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
    </template>
    <template #operation="{ record }">
      <!-- 缺陷 同步缺陷状态 -->
      <div v-permission="['PROJECT_APPLICATION_BUG:UPDATE']">
        <a-tooltip
          v-if="record.type === 'BUG_SYNC' && !allValueMap['BUG_SYNC_SYNC_ENABLE']"
          class="ms-tooltip-white"
          position="br"
          :mouse-enter-delay="300"
        >
          <template #content>
            <span class="text-[var(--color-text-1)]">
              {{ t('project.menu.notConfig') }}
              <span class="cursor-pointer text-[rgb(var(--primary-4))]" @click="showDefectDrawer">
                {{ t(`project.menu.${record.type}`) }}
              </span>
              {{ t('project.menu.configure') }}
            </span>
          </template>
          <a-switch
            v-model="allValueMap['BUG_SYNC_SYNC_ENABLE']"
            checked-value="true"
            unchecked-value="false"
            :disabled="!hasAnyPermission(['PROJECT_APPLICATION_BUG:UPDATE']) || !allValueMap['BUG_SYNC_SYNC_ENABLE']"
            :value="allValueMap['BUG_SYNC_SYNC_ENABLE']"
            size="small"
            type="line"
            @change="(v: boolean | string| number) => handleMenuStatusChange('BUG_SYNC_SYNC_ENABLE',v as boolean, MenuEnum.bugManagement)"
          />
        </a-tooltip>
      </div>

      <a-switch
        v-if="record.type === 'BUG_SYNC' && allValueMap['BUG_SYNC_SYNC_ENABLE']"
        v-model="allValueMap['BUG_SYNC_SYNC_ENABLE']"
        checked-value="true"
        unchecked-value="false"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_BUG:UPDATE'])"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange('BUG_SYNC_SYNC_ENABLE',v as boolean, MenuEnum.bugManagement)"
      />
      <!-- 测试用例 关联需求 -->
      <div v-permission="['PROJECT_APPLICATION_CASE:UPDATE']">
        <a-tooltip
          v-if="record.type === 'CASE_RELATED' && !allValueMap['CASE_RELATED_CASE_ENABLE']"
          class="ms-tooltip-white"
          position="left"
          :mouse-enter-delay="300"
        >
          <template #content>
            <span class="text-[var(--color-text-1)]">
              {{ t('project.menu.notConfig') }}
              <span class="cursor-pointer text-[rgb(var(--primary-4))]" @click="showRelatedCaseDrawer">
                {{ t(`project.menu.${record.type}`) }}
              </span>
              {{ t('project.menu.configure') }}
            </span>
          </template>
          <a-switch
            checked-value="true"
            unchecked-value="false"
            :disabled="
              !hasAnyPermission(['PROJECT_APPLICATION_CASE:UPDATE']) || !allValueMap['CASE_RELATED_CASE_ENABLE']
            "
            :value="allValueMap['CASE_RELATED_CASE_ENABLE']"
            size="small"
            type="line"
            @change="(v: boolean | string| number) => handleMenuStatusChange('CASE_RELATED_CASE_ENABLE',v as boolean, MenuEnum.caseManagement)"
          />
        </a-tooltip>
      </div>
      <a-switch
        v-if="record.type === 'CASE_RELATED' && allValueMap['CASE_RELATED_CASE_ENABLE']"
        v-model="allValueMap['CASE_RELATED_CASE_ENABLE']"
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_CASE:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])"
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
        :disabled="!hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE'])"
        checked-value="true"
        unchecked-value="false"
        size="small"
        type="line"
        @change="(v: boolean | string| number) => handleMenuStatusChange('PERFORMANCE_TEST_SCRIPT_REVIEWER_ENABLE',v as boolean,MenuEnum.loadTest)"
      />
    </template>
  </MsBaseTable>
  <DefectSync
    v-model:visible="defectDrawerVisible"
    @cancel="defectDrawerVisible = false"
    @ok="getMenuConfig(MenuEnum.bugManagement)"
  />
  <RelatedCase
    v-model:visible="relatedCaseDrawerVisible"
    @cancel="relatedCaseDrawerVisible = false"
    @ok="getMenuConfig(MenuEnum.caseManagement)"
  />
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-菜单管理
   */
  import { useRoute, useRouter } from 'vue-router';
  import { Message, TableData } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTimeSelectorVue from '@/components/pure/ms-time-selector/MsTimeSelector.vue';
  import MsSelect from '@/components/business/ms-select';
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
  const route = useRoute();
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
    TASK_CLEAN_REPORT: '3M',
    TASK_RECORD: '3M',
    UI_CLEAN_REPORT: '3M',
    UI_SHARE_REPORT: '1D',
    PERFORMANCE_TEST_CLEAN_REPORT: '3M',
    PERFORMANCE_TEST_SHARE_REPORT: '1D',
    WORKSTATION_SYNC_RULE: true,
    CASE_RELATED: true,
    CASE_RE_REVIEW: true,
    PERFORMANCE_TEST_SCRIPT_REVIEWER: true,
    CASE_ENABLE: true,
    BUG_SYNC_SYNC_ENABLE: false,
    CASE_RELATED_CASE_ENABLE: false,
  };

  const allValueMap = ref<MenuTableConfigItem>(cloneDeep(defaultValueMap));

  const hasTitleColumns = [
    {
      title: 'project.menu.name',
      dataIndex: 'module',
      slotName: 'module',
      width: 221,
      headerCellClass: 'pl-[40px]',
    },
    {
      title: 'common.desc',
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
      headerCellClass: 'pl-[40px]',
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
          /*  {
            type: 'CASE_PUBLIC', // 公共用例库
          }, */
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
          /* {
            type: 'API_URL_REPEATABLE', // 接口定义URL可重复
          }, */
          {
            type: 'API_CLEAN_REPORT', // 报告保留时间范围
          },
          {
            type: 'API_SHARE_REPORT', // 报告链接有效期
          },
          {
            type: 'API_RESOURCE_POOL', // 执行资源池
          },
          /* {
            type: 'API_SCRIPT_REVIEWER', // 脚本审核
          }, */
          {
            type: 'API_ERROR_REPORT_RULE', // 误报规则
          },
          /* {
            type: 'API_SYNC_CASE', // 用例同步
          }, */
        ];
        break;
      }
      case MenuEnum.taskCenter: {
        children = [
          {
            type: 'TASK_RECORD', // 即时任务保留时间
          },
          {
            type: 'TASK_CLEAN_REPORT', // 任务执行结果保留时间范围
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
        case MenuEnum.taskCenter:
          if (hasAnyPermission(['PROJECT_APPLICATION_TASK:READ'])) {
            hasAuth = true;
          }
          break;
        default:
          break;
      }

      if (!hasAuth) {
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

  async function expanded(record: TableData) {
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
  }

  const expandChange = async (record: TableData) => {
    try {
      if (expandedKeys.value.includes(record.module)) {
        // 收起
        expandedKeys.value = expandedKeys.value.filter((item) => item !== record.module);
        return;
      }

      switch (record.module) {
        case MenuEnum.workstation:
          if (hasAnyPermission(['PROJECT_APPLICATION_WORKSTATION:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.testPlan:
          if (hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.bugManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_BUG:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.caseManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_CASE:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.apiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_API:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.uiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_UI:READ'])) {
            await expanded(record);
          }
          break;
        case MenuEnum.taskCenter:
          if (hasAnyPermission(['PROJECT_APPLICATION_TASK:READ'])) {
            await expanded(record);
          }
          break;
        default:
          if (hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:READ'])) {
            await expanded(record);
          }
          break;
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
        return 'icon-a-icon_test-tracking_filled1';
      case MenuEnum.bugManagement:
        return 'icon-icon_defect';
      case MenuEnum.caseManagement:
        return 'icon-icon_functional_testing1';
      case MenuEnum.apiTest:
        return 'icon-icon_api-test-filled2';
      case MenuEnum.taskCenter:
        return 'icon-icon_task_center';
      default:
        return '';
    }
  };

  const handleMenuStatusChange = async (type: string, typeValue: string | boolean, suffix: string) => {
    try {
      let hasAuth = false;
      switch (suffix) {
        case MenuEnum.workstation:
          if (hasAnyPermission(['PROJECT_APPLICATION_WORKSTATION:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.apiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_API:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.bugManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_BUG:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.caseManagement:
          if (hasAnyPermission(['PROJECT_APPLICATION_CASE:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.loadTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_PERFORMANCE_TEST:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.testPlan:
          if (hasAnyPermission(['PROJECT_APPLICATION_TEST_PLAN:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.uiTest:
          if (hasAnyPermission(['PROJECT_APPLICATION_UI:UPDATE'])) {
            hasAuth = true;
          }
          break;
        case MenuEnum.taskCenter:
          if (hasAnyPermission(['PROJECT_APPLICATION_TASK:READ'])) {
            hasAuth = true;
          }
          break;
        default:
          // eslint-disable-next-line no-console
          console.log('no ');
          break;
      }

      if (!hasAuth) {
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

      if (type.includes('REPORT') && !type.includes('CLEAN')) {
        Message.success(t('project.application.report.tips'));
      } else {
        Message.success(t('common.operationSuccess'));
      }

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
  const pushFar = (isEnable: boolean) => {
    router.push({
      name: ProjectManagementRouteEnum.PROJECT_MANAGEMENT_MENU_MANAGEMENT_ERROR_REPORT_RULE,
      query: {
        status: isEnable ? 'enable' : 'all',
      },
    });
  };

  // 获取执行资源池的名称
  const getPoolTipName = (id: string, type: MenuEnum) => {
    const poolOption = type === MenuEnum.apiTest ? apiPoolOption.value : uiPoolOption.value;
    const pool = poolOption.find((item) => item.id === id);
    return pool?.name;
  };

  const initExpendKeys = async () => {
    if (route.query.module) {
      await expandChange({ module: route.query.module as MenuEnum });
      switch (route.query.module as MenuEnum) {
        case MenuEnum.bugManagement:
          showDefectDrawer();
          break;
        case MenuEnum.caseManagement:
          showRelatedCaseDrawer();
          break;
        default:
          break;
      }
    } else if (router.currentRoute.value.redirectedFrom) {
      // 从误报规则跳转回来的
      await expandChange({ module: MenuEnum.apiTest });
    } else {
      expandedKeys.value = [];
    }
  };

  async function initMenuData() {
    setLoadListParams({ projectId: currentProjectId.value });
    await fetchData();
    await initExpendKeys();
  }

  // 点击展开
  const handleRowClick = (record: TableData) => {
    if (record.module) {
      expandChange(record);
    }
  };

  onMounted(() => {
    initMenuData();
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

<style scoped lang="less">
  .icon-class {
    margin: 0 8px 0 16px;
    width: 20px;
    height: 20px;
    background-color: rgba(var(--primary-1));
    @apply flex items-center justify-center rounded-full;
  }
</style>
