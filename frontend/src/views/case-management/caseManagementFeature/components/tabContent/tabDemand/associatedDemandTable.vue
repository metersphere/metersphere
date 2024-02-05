<template>
  <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
    <template #demandName="{ record }">
      <span class="ml-1" :class="[props.highlightName ? 'text-[rgb(var(--primary-5))]' : '']">
        {{ record.demandName }}
        <span>({{ (record.children || []).length || 0 }})</span></span
      >
    </template>
    <template #operation="{ record }">
      <MsButton v-if="record.demandPlatform !== pageConfig.platformName" @click="emit('update', record)">
        {{ t('caseManagement.featureCase.cancelAssociation') }}
      </MsButton>
      <MsButton v-if="record.demandPlatform === pageConfig.platformName" @click="emit('update', record)">
        {{ t('common.edit') }}
      </MsButton>
    </template>
    <template v-if="(props.funParams.keyword || '').trim() === '' && props.showEmpty" #empty>
      <div class="flex w-full items-center justify-center">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]" @click="emit('create')">
          {{ t('caseManagement.featureCase.addDemand') }}
        </MsButton>
      </div>
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
  import { useAppStore } from '@/store';
  import useFeatureCaseStore from '@/store/modules/case/featureCase';

  import type { DemandItem } from '@/models/caseManagement/featureCase';

  const appStore = useAppStore();
  const pageConfig = computed(() => appStore.pageConfig);
  const featureCaseStore = useFeatureCaseStore();
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      funParams: {
        caseId: string;
        keyword: string;
        projectId: string;
      }; // 列表查询参数
      isShowOperation?: boolean; // 是否显示操作列
      highlightName?: boolean; // 是否高亮名称
      showEmpty?: boolean; // 是否显示自定义的空状态，否则显示表格的默认空状态
    }>(),
    {
      isShowOperation: true,
      highlightName: true,
    }
  );

  const emit = defineEmits<{
    (e: 'update', record: DemandItem): void;
    (e: 'create'): void;
  }>();

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
    columns,
    rowKey: 'id',
    scroll: { x: '100%' },
    selectable: false,
    showSetting: false,
  });

  const initData = async () => {
    setLoadListParams({ ...props.funParams });
    await loadList();
    const { msPagination } = propsRes.value;
    featureCaseStore.setListCount(featureCaseStore.activeTab, msPagination?.total || 0);
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
