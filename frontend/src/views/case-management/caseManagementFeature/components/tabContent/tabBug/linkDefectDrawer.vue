<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="false"
    :title="t('caseManagement.featureCase.linkDefect')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-loading="drawerLoading"
    :width="960"
    unmount-on-close
    :show-continue="false"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.defectList') }}</div>
      <div>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
          allow-clear
          class="mx-[8px] w-[240px]"
        ></a-input-search
      ></div>
    </div>
    <div>
      <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
        <template #defectName="{ record }">
          <span class="one-line-text max-w[300px]"> {{ record.name }}</span
          ><span class="ml-1 text-[rgb(var(--primary-5))]">{{ t('caseManagement.featureCase.preview') }}</span>
        </template>
      </ms-base-table>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getRecycleListRequest } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const props = defineProps<{
    visible: boolean;
  }>();

  const emit = defineEmits(['update:visible']);
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'id',
      width: 200,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'defectName',
      dataIndex: 'defectName',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'name',
      dataIndex: 'updateUser',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'defectState',
      dataIndex: 'defectState',
      showInTable: true,
      showTooltip: true,
      width: 300,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.IterationPlan',
      dataIndex: 'level',
      showInTable: true,
      width: 200,
      showTooltip: true,
      ellipsis: true,
      showDrag: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getRecycleListRequest, {
    columns,
    tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEFECT,
    selectable: true,
    scroll: { x: 1000 },
    heightUsed: 340,
    enableDrag: true,
  });

  const drawerLoading = ref<boolean>(false);

  function handleDrawerConfirm() {}
  function handleDrawerCancel() {}

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  const keyword = ref<string>('');

  function getFetch() {
    setLoadListParams({ keyword: keyword.value });
    loadList();
  }

  onMounted(() => {
    getFetch();
  });
</script>

<style scoped></style>
