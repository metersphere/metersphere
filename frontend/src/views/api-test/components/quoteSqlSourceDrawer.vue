<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="960"
    :title="t('apiTestDebug.quoteSource')"
    :ok-disabled="selectedKey === ''"
    :ok-text="t('common.apply')"
    @confirm="handleConfirm"
  >
    <div class="mb-[16px] flex items-center justify-between">
      <div class="text-[var(--color-text-1)]">{{ t('apiTestDebug.sourceList') }}</div>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('apiTestDebug.searchByDataBaseName')"
        class="w-[230px]"
        allow-clear
        @search="searchDataSource"
        @press-enter="searchDataSource"
        @clear="searchDataSource"
      />
    </div>
    <MsBaseTable v-bind="propsRes" v-model:selected-key="selectedKey" v-on="propsEvent">
      <template #timeout="{ record }">
        <a-tooltip :content="record.timeout?.toLocaleString()">
          <div class="one-line-text">{{ record.timeout?.toLocaleString() }}</div>
        </a-tooltip>
      </template>
    </MsBaseTable>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';
  import { cloneDeep } from 'lodash-es';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { getEnvironment } from '@/api/modules/api-test/common';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  const props = defineProps<{
    visible: boolean;
    selectedKey?: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void;
    (e: 'apply', value: any): void;
  }>();

  const appStore = useAppStore();
  const { t } = useI18n();

  /** 接收祖先组件提供的属性 */
  const innerVisible = useVModel(props, 'visible', emit);
  const keyword = ref('');
  const selectedKey = ref(props.selectedKey || '');

  const columns: MsTableColumn = [
    {
      title: 'apiTestDebug.sqlSourceName',
      dataIndex: 'dataSource',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.driver',
      dataIndex: 'driver',
      showTooltip: true,
    },
    {
      title: 'URL',
      dataIndex: 'dbUrl',
      showTooltip: true,
    },
    {
      title: 'apiTestDebug.username',
      dataIndex: 'username',
      showTooltip: true,
      width: 150,
    },
    {
      title: 'apiTestDebug.maxConnection',
      dataIndex: 'poolMax',
      width: 140,
    },
    {
      title: 'apiTestDebug.timeout',
      dataIndex: 'timeout',
      slotName: 'timeout',
      align: 'right',
      width: 120,
    },
  ];

  const { propsRes, propsEvent } = useTable(undefined, {
    columns,
    scroll: { x: '100%' },
    heightUsed: 300,
    selectable: true,
    showSelectorAll: false,
    selectorType: 'radio',
    firstColumnWidth: 44,
    showPagination: false,
  });

  async function initEnvironment(envId: string) {
    try {
      const res = await getEnvironment(envId);
      propsRes.value.data = cloneDeep(res.dataSources) as any[];
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch(
    () => appStore.currentEnvConfig,
    (config) => {
      if (config && config.id) {
        initEnvironment(config.id);
      }
    },
    {
      immediate: true,
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      if (val && appStore.currentEnvConfig?.id) {
        selectedKey.value = props.selectedKey || '';
        initEnvironment(appStore.currentEnvConfig?.id);
      }
    }
  );

  function searchDataSource() {
    propsRes.value.data = cloneDeep(appStore.currentEnvConfig?.dataSources) as any[];
    if (keyword.value.trim() !== '') {
      propsRes.value.data = appStore.currentEnvConfig?.dataSources.filter((e) =>
        e.dataSource.includes(keyword.value)
      ) as any[];
    } else {
      propsRes.value.data = cloneDeep(appStore.currentEnvConfig?.dataSources) as any[];
    }
  }

  function handleConfirm() {
    innerVisible.value = false;
    emit(
      'apply',
      propsRes.value.data.find((item) => item.id === selectedKey.value)
    );
  }
</script>

<style lang="less" scoped>
  :deep(.arco-table-cell-align-left) {
    padding-left: 9px;
  }
</style>
