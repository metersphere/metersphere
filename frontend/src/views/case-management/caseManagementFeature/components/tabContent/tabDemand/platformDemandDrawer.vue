<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.associatedFile')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-loading="drawerLoading"
    :width="960"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="flex items-center justify-between">
      <div>XXXXXX <span>(101)</span></div>
      <a-input-search
        v-model="keyword"
        :max-length="250"
        :placeholder="t('project.member.searchMember')"
        allow-clear
        @search="searchHandler"
        @press-enter="searchHandler"
      ></a-input-search>
    </div>
    <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
      <template #demandName="{ record }">
        <span class="ml-1 text-[rgb(var(--primary-5))]">
          {{ record.demandName }}
          <span>({{ (record.children || []).length || 0 }})</span></span
        >
      </template>
    </ms-base-table>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getDemandList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      slotName: 'demandId',
      dataIndex: 'demandId',
      showInTable: true,
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'demandName',
      dataIndex: 'demandName',
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.platformDemandState',
      width: 300,
      dataIndex: 'status',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.platformDemandHandler',
      width: 300,
      dataIndex: 'handler',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.IterationPlan',
      width: 300,
      dataIndex: 'iterationPlan',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(getDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_DEMAND,
    columns,
    rowKey: 'id',
    scroll: { x: '100%' },
    selectable: false,
    showSetting: false,
  });

  const showDrawer = ref<boolean>(false);

  const drawerLoading = ref<boolean>(false);

  function handleDrawerConfirm() {
    // const selectedIds = [...propsRes.value.selectedKeys];
    // tableSelected.value = propsRes.value.data.filter((item: any) => selectedIds.indexOf(item.id) > -1);
    // emit('save', tableSelected.value);
    // showDrawer.value = false;
    // propsRes.value.selectedKeys.clear();
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
  }

  const keyword = ref<string>('');

  const initData = async () => {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  };

  const searchHandler = () => {
    initData();
    resetSelector();
  };

  onMounted(() => {
    resetSelector();
  });
</script>

<style scoped></style>
