<template>
  <div class="flex flex-row items-center">
    <div class="text-[var(--color-text-1)]"> {{ t('project.menu.management') }}</div>
    <a-tooltip :content="t('project.menu.manageTip')" position="bl">
      <div>
        <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
      </div>
    </a-tooltip>
  </div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent" @expand="expandChange">
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
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'TEST_PLAN_SHARE_REPORT'">
        <!-- 测试计划 报告链接有效期 -->
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'ISSUE_SYNC'">
        <!-- 缺陷同步 -->
        <span>{{ t('project.menu.row2') }}</span>
        <div class="ml-[8px] text-[rgb(var(--primary-7))]" @click="showDefectDrawer">{{ t('project.menu.sd') }}</div>
      </div>
      <div v-if="record.type === 'CASE_PUBLIC'">
        <!-- 用例 公共用例库 -->
        {{ t('project.menu.row3') }}
      </div>
      <div v-if="record.type === 'CASE_RE_REVIEW'" class="flex flex-row">
        <!-- 用例 关联需求 -->
        <div>{{ t('project.menu.row4') }}</div>
        <div class="ml-[8px] text-[rgb(var(--primary-7))]" @click="showDefectDrawer">{{ t('project.menu.rr') }}</div>
      </div>
      <div v-if="record.type === 'CASE_RELATED'">
        <!-- 用例 重新提审 -->
        {{ t('project.menu.row5') }}
      </div>
      <div v-if="record.type === 'API_URL_REPEATABLE'">
        <!-- 接口  -->
        {{ t('project.menu.row6') }}
      </div>
      <div v-if="record.type === 'API_CLEAN_REPORT'">
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'API_SHARE_REPORT'">
        <!-- 报告链接有效期 -->
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'API_RESOURCE_POOL'" class="flex flex-row items-center">
        <!-- 执行资源池 -->
        <a-select v-model="record.typeValue" />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!-- 脚本审核 -->
        <a-select v-model="record.typeValue" />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'API_ERROR_REPORT_RULE'" class="flex w-[100%] flex-row items-center">
        <!-- 误报规则 -->
        <a-select v-model="record.typeValue" class="w-[290px]" />
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
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'UI_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'UI_RESOURCE_POOL'" class="flex flex-row items-center">
        <!--UI 执行资源池 -->
        <a-select v-model="record.typeValue" />
        <a-tooltip :content="t('project.menu.manageTip')" position="bl">
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_CLEAN_REPORT'">
        <!--性能测试 报告保留时间范围 -->
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SHARE_REPORT'">
        <!--UI 报告链接有效期 -->
        <a-input v-model="record.typeValue" />
      </div>
      <div v-if="record.type === 'PERFORMANCE_TEST_SCRIPT_REVIEWER'" class="flex flex-row items-center">
        <!--UI 脚本审核 -->
        <a-select v-model="record.typeValue" />
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
    title="缺陷同步"
    :width="600"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="false"
    :footer="null"
    :get-container="false"
    :body-style="{ padding: '0px' }"
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
  import { postTabletList, postUpdateMenu, getConfigByMenuItem } from '@/api/modules/project-management/menuManagement';
  import { useAppStore } from '@/store';
  import { MenuTableListItem } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';
  import { Message, TableData } from '@arco-design/web-vue';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();
  const defectDrawerVisible = ref(false);

  const showEnableConfigList = [
    'WORKSTATION_SYNC_RULE',
    'ISSUE_SYNC',
    'CASE_PUBLIC',
    'CASE_RE_REVIEW',
    'CASE_RELATED',
    'API_URL_REPEATABLE',
    'API_SYNC_CASE',
    'PERFORMANCE_TEST_SCRIPT_REVIEWER',
  ];

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
    (item) => ({
      ...item,
      children: [],
    })
  );

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

  const expandChange = async (record: TableData) => {
    if (record.children && record.children.length > 0) {
      // 有子项
      return false;
    }
    record.children =
      (await getConfigByMenuItem({ projectId: currentProjectId.value, type: record.module as MenuEnum })) || [];
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
