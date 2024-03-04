<template>
  <ms-base-table ref="tableRef" v-bind="propsRes" v-on="propsEvent">
    <template #demandName="{ record }">
      <span :class="[props.highlightName ? 'text-[rgb(var(--primary-5))]' : '']" @click="emit('open', record)">
        {{ characterLimit(record.demandName) }} </span
      ><span class="one-text-line text-[rgb(var(--primary-5))]">({{ (record.children || []).length || 0 }})</span>
    </template>
    <template #operation="{ record }">
      <MsButton @click="emit('cancel', record)">
        {{ t('caseManagement.featureCase.cancelAssociation') }}
      </MsButton>
      <MsButton v-if="record.demandPlatform === pageConfig.platformName" @click="emit('update', record)">
        {{ t('common.edit') }}
      </MsButton>
    </template>
    <template v-if="(props.funParams.keyword || '').trim() === '' && props.showEmpty" #empty>
      <div class="flex w-full items-center justify-center">
        {{ t('caseManagement.caseReview.tableNoData') }}
        <MsButton class="ml-[8px]" @click="emit('associate')">
          {{ t('caseManagement.featureCase.associatedDemand') }}
        </MsButton>
        {{ t('caseManagement.featureCase.or') }}
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
  import { characterLimit } from '@/utils';

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
    (e: 'associate'): void;
    (e: 'cancel', record: DemandItem): void;
    (e: 'open', record: DemandItem): void;
  }>();

  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      slotName: 'demandId',
      dataIndex: 'demandId',
      width: 200,
    },
    {
      title: 'caseManagement.featureCase.demandName',
      slotName: 'demandName',
      dataIndex: 'demandName',
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.demandPlatform',
      width: 300,
      dataIndex: 'demandPlatform',
      showTooltip: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnActions',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 150,
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
    heightUsed: 360,
  });

  const initData = async () => {
    setLoadListParams({ ...props.funParams });
    await loadList();
    featureCaseStore.getCaseCounts(props.funParams.caseId);
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
