<template>
  <div class="flex flex-row items-center">
    <div class="text-[var(--color-text-1)]"> {{ t('project.menu.management') }}</div>
    <a-tooltip :content="t('project.menu.manageTip')">
      <MsIcon class="ml-[4px] text-[rgb(var(--primary-5))]" type="icon-icon-maybe_outlined" />
    </a-tooltip>
  </div>
  <MsBaseTable class="mt-[16px]" v-bind="propsRes" v-on="propsEvent">
    <template #module="{ record }">
      <MsIcon :type="getMenuIcon(record.module)" />
      <span class="ml-[4px]">{{ record.module }}</span>
    </template>
    <template #moduleEnable="{ record }">
      <a-switch v-model="record.moduleEnable" @change="handleMenuStatusChange(record)" />
    </template>
  </MsBaseTable>
</template>

<script setup lang="ts">
  /**
   * @description 项目管理-项目与权限-菜单管理
   */
  import { onMounted, computed } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import { postTabletList, postUpdateMenu } from '@/api/modules/project-management/menuManagement';
  import { useAppStore } from '@/store';
  import { MenuTableListItem } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';

  import { Message } from '@arco-design/web-vue';

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'project.menu.name',
      dataIndex: 'module',
      slotName: 'module',
    },
    {
      title: 'project.menu.description',
      slotName: 'description',
      dataIndex: 'description',
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'moduleEnable',
      dataIndex: 'moduleEnable',
      fixed: 'right',
      width: 150,
    },
  ];
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(postTabletList, {
    showPagination: false,
    columns,
    selectable: false,
    scroll: { x: '100%' },
    noDisable: true,
  });

  const getMenuIcon = (type: MenuEnum) => {
    switch (type) {
      case MenuEnum.workstation:
        return 'icon-icon_pc_filled';
      case MenuEnum.testPlan:
        return 'icon-icon_test_plan_filled';
      case MenuEnum.bugManagement:
        return 'icon-icon_defect';
      case MenuEnum.caseManagement:
        return 'icon_functional_testing';
      case MenuEnum.apiTest:
        return 'icon-icon_api-test-filled';
      case MenuEnum.uiTest:
        return 'icon-icon_ui-test-filled';
      default:
        return 'icon_performance-test-filled';
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

  onMounted(() => {
    setLoadListParams({ projectId: currentProjectId.value });
    fetchData();
  });
</script>

<style scoped></style>
