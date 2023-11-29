<template>
  <ms-base-table ref="tableRef" v-bind="propsRes" :expanded-keys="expandedKeys" v-on="propsEvent">
    <template #demandId="{ record }">
      {{ record.demandId }}
    </template>
    <template #demandName="{ record }">
      <div v-if="record.children && (record.children || []).length" class="expandName absolute -ml-10 cursor-pointer">
        <span
          v-if="(record.children || []).length > 0 && !isExpand(record)"
          class="flex items-center justify-center"
          @click="expandHandler(record)"
        >
          <icon-right class="text-[var(--color-text-4)]" :style="{ 'font-size': '12px' }" />
        </span>
        <span v-if="isExpand(record)" class="expand flex items-center justify-center" @click="expandHandler(record)">
          <icon-down class="text-[rgb(var(--primary-6))]" :style="{ 'font-size': '12px' }" />
        </span>
      </div>
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

  function expandHandler(record: any) {
    const index = expandedKeys.value.findIndex((id) => id === record.id);
    if (index >= 0) {
      expandedKeys.value.splice(index, 1); // 如果已展开，则移除 ID
    } else {
      expandedKeys.value.push(record.id); // 如果未展开，则追加 ID
    }
  }
  function isExpand(record: any) {
    return expandedKeys.value.findIndex((id) => id === record.id) < 0;
  }

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

<style scoped lang="less">
  :deep(.arco-table-cell-inline-icon .arco-table-expand-btn) {
    background: transparent !important;
    .arco-icon {
      color: transparent;
    }
  }
</style>
