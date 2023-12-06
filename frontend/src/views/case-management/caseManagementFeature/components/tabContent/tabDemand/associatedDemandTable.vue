<template>
  <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
    <template #demandId="{ record }">
      <span class="ml-2"> {{ record.demandId }}</span>
    </template>
    <template #demandName="{ record }">
      <span class="ml-1" :class="[props.highlightName ? 'text-[rgb(var(--primary-5))]' : '']">
        {{ record.demandName }}
        <span v-if="record.children && (record.children || []).length"
          >（{{ (record.children || []).length }}）</span
        ></span
      >
    </template>
    <template #operation="{ record }">
      <MsButton v-if="record.demandPlatform === 'LOCAL'" @click="emit('update', record)">{{
        t('caseManagement.featureCase.cancelAssociation')
      }}</MsButton>
      <MsButton v-if="record.children && (record.children || []).length" @click="emit('update', record)">{{
        t('common.edit')
      }}</MsButton>
    </template>
  </ms-base-table>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getDemandList } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';

  import type { DemandItem } from '@/models/caseManagement/featureCase';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      funParams: Record<string, any>; // 列表查询参数
      isShowOperation?: boolean; // 是否显示操作列
      highlightName?: boolean; // 是否高亮名称
    }>(),
    {
      isShowOperation: true,
      highlightName: true,
    }
  );

  const emit = defineEmits<{
    (e: 'update', record: DemandItem): void;
  }>();

  const expandedKeys = ref<string[]>([]);
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      slotName: 'demandId',
      showInTable: true,
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.tableColumnName',
      slotName: 'demandName',
      width: 300,
    },
    {
      title: 'caseManagement.featureCase.demandPlatform',
      width: 300,
      dataIndex: 'demandPlatform',
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 300,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getDemandList, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_DEMAND,
    columns,
    rowKey: 'id',
    scroll: { x: '100%' },
    selectable: false,
    showSetting: false,
  });

  const initData = async () => {
    const { keyword, caseId } = props.funParams;
    setLoadListParams({ keyword, caseId });
    await loadList();
  };

  onMounted(() => {
    initData();
  });

  defineExpose({
    initData,
  });
  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  watch(
    () => props.isShowOperation,
    (val) => {
      if (!val) {
        tableRef.value?.initColumn(columns.slice(0, columns.length - 1));
      }
    }
  );
</script>

<style scoped lang="less"></style>
