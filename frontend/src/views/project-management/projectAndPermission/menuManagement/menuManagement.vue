<template>
  <div class="flex flex-row items-center">
    <div class="text-[var(--color-text-1)]"> {{ t('project.menu.management') }}</div>
    <a-tooltip :content="t('project.menu.manageTip')" position="bl">
      <div>
        <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
      </div>
    </a-tooltip>
  </div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" @expand="expandChange" v-on="propsEvent">
    <template #module="{ record }">
      <div v-if="record.children">
        <MsIcon class="text-[var(--color-text-4)]" :type="getMenuIcon(record.module)" />
        <span class="ml-[4px]">{{ t(`menu.${record.module}`) }}</span>
      </div>
      <div v-else>
        <span class="ml-[4px]">{{ t(`project.menu.${record.type}`) }}</span>
      </div>
    </template>
    <template #moduleEnable="{ record }">
      <a-switch v-if="record.children" v-model="record.moduleEnable" @change="handleMenuStatusChange(record)" />
      <a-switch
        v-else-if="showEnableConfigList.includes(record.type)"
        v-model="record.moduleEnable"
        :default-checked="defaultChecked.includes(record.type)"
        @change="handleMenuStatusChange(record)"
      />
    </template>
    <template #description="{ record }">
      <div v-if="record.type === 'WORKSTATION_SYNC_RULE'">
        <!-- 工作台 接口测试待更新同步规则 -->
        {{ t('project.menu.row1') }}
      </div>
      <div v-if="record.type === 'TEST_PLAN_CLEAN_REPORT'">
        <!-- 测试计划 报告保留时间范围 -->
        <MsTimeSelectorVue v-model="allValueMap['TEST_PLAN_CLEAN_REPORT']" />
      </div>
      <div v-if="record.type === 'TEST_PLAN_SHARE_REPORT'">
        <!-- 测试计划 报告链接有效期 -->
        <MsTimeSelectorVue v-model="allValueMap['TEST_PLAN_SHARE_REPORT']" />
      </div>
      <div v-if="record.type === 'BUG_SYNC'">
        <!-- 缺陷同步 -->
        <span>{{ t('project.menu.row2') }}</span>
        <div class="ml-[8px] text-[rgb(var(--primary-7))]" @click="showDefectDrawer">{{ t('project.menu.sd') }}</div>
      </div>
      <div v-if="record.type === 'CASE_PUBLIC'">
        <!-- 用例 公共用例库 -->
        {{ t('project.menu.row3') }}
      </div>
      <div v-if="record.type === 'CASE_RELATED'" class="flex flex-row">
        <!-- 用例 关联需求 -->
        <div>{{ t('project.menu.row4') }}</div>
        <div class="ml-[8px] text-[rgb(var(--primary-7))]" @click="showDefectDrawer">{{ t('project.menu.rr') }}</div>
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
        <MsTimeSelectorVue v-model="allValueMap['API_CLEAN_REPORT']" />
      </div>
      <div v-if="record.type === 'API_SHARE_REPORT'">
        <!--接口测试 报告链接有效期 -->
        <MsTimeSelectorVue v-model="allValueMap['API_SHARE_REPORT']" />
      </div>
      <div v-if="record.type === 'API_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--接口测试 执行资源池 -->
        <a-select
          v-model="allValueMap['API_RESOURCE_POOL']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="apiPoolOption"
          class="w-[120px]"
        />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--接口测试 脚本审核 -->
        <a-select
          v-model="allValueMap['API_SCRIPT_REVIEWER']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="apiAuditorOption"
          class="w-[120px]"
        />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_ERROR_REPORT_RULE'" class="flex w-[100%] flex-row items-center">
        <!--接口测试 误报规则 -->
        <div class="error-report">
          <a-input
            v-model="allValueMap['API_ERROR_REPORT_RULE']"
            class="w-[120px]"
            disabled
            :placeholder="t('project.menu.pleaseConfig')"
          >
            <template #append>
              <div>{{ t('project.menu.count') }}</div>
            </template>
          </a-input>
        </div>
        <div class="ml-[8px] text-[rgb(var(--primary-7))]" @click="showDefectDrawer">{{ t('project.menu.far') }}</div>
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SYNC_CASE'">{{ t('project.menu.row7') }} </div>
      <div v-if="record.type === 'UI_CLEAN_REPORT'">
        <!--UI 报告保留时间范围 -->
        <MsTimeSelectorVue v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'UI_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <MsTimeSelectorVue v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'UI_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--UI 执行资源池 -->
        <a-select
          v-model="record.typeValue"
          :field-names="{ label: 'name', value: 'id' }"
          :options="uiPoolOption"
          class="w-[120px]"
        />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_CLEAN_REPORT'">
        <!--性能测试 报告保留时间范围 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_CLEAN_REPORT']"
          :field-names="{ label: 'name', value: 'id' }"
          :default-value="defaultValueMap['PERFORMANCE_TEST_CLEAN_REPORT']"
        />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <MsTimeSelectorVue
          v-model="allValueMap['PERFORMANCE_TEST_SHARE_REPORT']"
          :default-value="defaultValueMap['PERFORMANCE_TEST_SHARE_REPORT']"
        />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--UI 脚本审核 -->
        <a-select
          v-model="allValueMap['PERFORMANCE_TEST_SCRIPT_REVIEWER']"
          :field-names="{ label: 'name', value: 'id' }"
          :options="performanceAuditorOption"
          class="w-[120px]"
        />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
    </template>
  </MsBaseTable>
  <MsDrawer
    v-model:visible="defectDrawerVisible"
    :title="t('project.menu.BUG_SYNC')"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="false"
    :get-container="false"
    :body-style="{ padding: '0px' }"
    :width="400"
  >
    <DefectSync />
  </MsDrawer>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-菜单管理
   */
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import {
    postTabletList,
    postUpdateMenu,
    getConfigByMenuItem,
    getPoolOptions,
    getAuditorOptions,
  } from '@/api/modules/project-management/menuManagement';
  import { useAppStore } from '@/store';
  import { MenuTableConfigItem, MenuTableListItem, PoolOption } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';
  import { Message, TableData } from '@arco-design/web-vue';
  import MsTimeSelectorVue from '@/components/pure/ms-time-selector/MsTimeSelector.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import DefectSync from './components/defectSync.vue';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();
  const defectDrawerVisible = ref(false);
  const apiPoolOption = ref<PoolOption[]>([]);
  const uiPoolOption = ref<PoolOption[]>([]);
  const apiAuditorOption = ref<PoolOption[]>([]);
  const performanceAuditorOption = ref<PoolOption[]>([]);

  const expandedKeys = ref<string[]>([]);

  // 需要显示开关的配置项
  const showEnableConfigList = [
    'WORKSTATION_SYNC_RULE',
    'BUG_SYNC',
    'CASE_PUBLIC',
    'CASE_RE_REVIEW',
    'CASE_RELATED',
    'API_URL_REPEATABLE',
    'API_SYNC_CASE',
    'PERFORMANCE_TEST_SCRIPT_REVIEWER',
  ];
  // 默认勾选的配置项
  const defaultChecked = [
    'WORKSTATION_SYNC_RULE',
    'CASE_RELATED',
    'CASE_RE_REVIEW',
    'PERFORMANCE_TEST_SCRIPT_REVIEWER',
  ];
  // 默认初始值的配置项
  const defaultValueMap = {
    TEST_PLAN_CLEAN_REPORT: '30D',
    TEST_PLAN_SHARE_REPORT: '30D',
    API_CLEAN_REPORT: '30D',
    API_SHARE_REPORT: '30D',
    UI_CLEAN_REPORT: '30D',
    UI_SHARE_REPORT: '30D',
    PERFORMANCE_TEST_CLEAN_REPORT: '30D',
    PERFORMANCE_TEST_SHARE_REPORT: '30D',
  };

  const allValueMap = ref<MenuTableConfigItem>(defaultValueMap);

  const columns: MsTableColumn = [
    {
      title: 'project.menu.name',
      dataIndex: 'module',
      slotName: 'module',
      width: 221,
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
      slotName: 'moduleEnable',
      dataIndex: 'moduleEnable',
      width: 58,
    },
  ];

  const getChildren = (record: MenuTableListItem) => {
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
      columns,
      selectable: false,
      scroll: { x: '100%' },
      noDisable: true,
      rowKey: 'module',
      showExpand: true,
      emptyDataShowLine: false,
    },
    (item) => {
      const children = getChildren(item);
      return { ...item, children };
    }
  );

  const expandChange = async (record: TableData) => {
    try {
      if (expandedKeys.value.includes(record.module)) {
        // 收起
        expandedKeys.value = expandedKeys.value.filter((item) => item !== record.module);
        return;
      }
      expandedKeys.value = [...expandedKeys.value, record.module];
      const resultObj = await getConfigByMenuItem({
        projectId: currentProjectId.value,
        type: record.module as MenuEnum,
      });
      allValueMap.value = { ...allValueMap.value, ...resultObj };
      if (record.module === MenuEnum.apiTest && !apiPoolOption.value.length) {
        apiPoolOption.value = await getPoolOptions(currentProjectId.value, record.module);
      }
      if (record.module === MenuEnum.uiTest && !uiPoolOption.value.length) {
        uiPoolOption.value = await getPoolOptions(currentProjectId.value, record.module);
      }
      if (record.module === MenuEnum.apiTest && !apiAuditorOption.value.length) {
        apiAuditorOption.value = await getAuditorOptions(currentProjectId.value, record.module);
      }
      if (record.module === MenuEnum.loadTest && !performanceAuditorOption.value.length) {
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

  const handleMenuStatusChange = async (record: MenuTableListItem) => {
    try {
      await postUpdateMenu({
        projectId: currentProjectId.value,
        type: record.module as MenuEnum,
        typeValue: record.moduleEnable,
      });
      Message.success(t('common.operationSuccess'));
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };
  const fetchData = async () => {
    await loadList();
  };

  const showDefectDrawer = () => {
    defectDrawerVisible.value = true;
  };

  onMounted(() => {
    setLoadListParams({ projectId: currentProjectId.value });
    fetchData();
  });
</script>

<style scoped></style>
