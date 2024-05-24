<template>
  <MsDrawer
    v-model:visible="visible"
    :width="680"
    :title="props.isSingle ? t('apiScenario.replaceCsv') : t('apiScenario.quoteCsv')"
    :ok-text="props.isSingle ? t('common.replace') : t('common.quote')"
    :ok-disabled="propsRes.selectedKeys.size === 0 && !selectedKey"
    @confirm="handleConfirm"
    @close="handleClose"
  >
    <MsBaseTable v-bind="propsRes" v-model:selected-key="selectedKey" v-on="propsEvent">
      <template #scope="{ record }">
        {{ record.scope === 'SCENARIO' ? t('apiScenario.scenario') : t('apiScenario.step') }}
      </template>
      <template #file="{ record }">
        <a-tooltip :content="record.file?.fileAlias">
          <div>{{ record.file?.fileAlias || '-' }}</div>
        </a-tooltip>
      </template>
    </MsBaseTable>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';

  import { CsvVariable } from '@/models/apiTest/scenario';

  import { defaultCsvParamItem } from '../config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const props = defineProps<{
    csvVariables: CsvVariable[];
    excludeKeys?: string[];
    isSingle?: boolean;
  }>();
  const emit = defineEmits<{
    (e: 'confirm', selectedKeys: string[]): void;
  }>();

  const { t } = useI18n();

  const visible = defineModel<boolean>('visible', {
    required: true,
  });

  const selectedKey = ref('');

  const columns: MsTableColumn = [
    {
      title: 'apiScenario.params.csvName',
      dataIndex: 'name',
      showTooltip: true,
    },
    {
      title: 'apiScenario.params.csvScoped',
      dataIndex: 'scope',
      slotName: 'scope',
      width: 80,
    },
    {
      title: 'apiScenario.params.file',
      dataIndex: 'file',
      slotName: 'file',
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    scroll: { x: '100%' },
    selectable: true,
    showSelectorAll: false,
    selectorType: props.isSingle ? 'radio' : 'checkbox',
    firstColumnWidth: 44,
    heightUsed: 122,
    showPagination: false,
    excludeKeys: new Set(props.excludeKeys || []),
  });

  watchEffect(() => {
    propsRes.value.data = filterKeyValParams(props.csvVariables, defaultCsvParamItem).validParams.filter(
      (e) => e.enable && !props.excludeKeys?.includes(e.id)
    );
    selectedKey.value = '';
    propsRes.value.selectorType = props.isSingle ? 'radio' : 'checkbox';
  });

  function handleConfirm() {
    emit('confirm', props.isSingle ? [selectedKey.value] : Array.from(propsRes.value.selectedKeys));
    visible.value = false;
  }

  function handleClose() {
    selectedKey.value = '';
    propsRes.value.selectedKeys.clear();
  }
</script>

<style lang="less" scoped></style>
