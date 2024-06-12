<template>
  <MsDrawer
    v-model:visible="showDrawer"
    :mask="true"
    :title="t('caseManagement.featureCase.linkDefect')"
    :ok-text="t('caseManagement.featureCase.associated')"
    :ok-disabled="propsRes.selectedKeys.size === 0"
    :width="1200"
    :mask-closable="true"
    unmount-on-close
    :show-continue="false"
    :ok-loading="props.drawerLoading"
    @confirm="handleDrawerConfirm"
    @cancel="handleDrawerCancel"
  >
    <div class="mb-4 flex items-center justify-between">
      <div class="font-medium">{{ t('caseManagement.featureCase.defectList') }}</div>
      <div>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('caseManagement.featureCase.searchByNameAndId')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        ></a-input-search>
      </div>
    </div>
    <div>
      <ms-base-table
        ref="tableRef"
        v-bind="propsRes"
        :action-config="{
          baseAction: [],
          moreAction: [],
        }"
        v-on="propsEvent"
      >
        <template #name="{ record }">
          <BugNamePopover :name="record.name" :content="record.content" />
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
  import BugNamePopover from '@/views/case-management/caseManagementFeature/components/tabContent/tabBug/bugNamePopover.vue';

  import { getDrawerDebugPage } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { TableKeyEnum } from '@/enums/tableEnum';

  import debounce from 'lodash-es/debounce';

  const { t } = useI18n();
  const appStore = useAppStore();

  const currentProjectId = computed(() => appStore.currentProjectId);

  const props = withDefaults(
    defineProps<{
      visible: boolean;
      caseId: string;
      drawerLoading: boolean;
      showSelectorAll?: boolean;
    }>(),
    {
      showSelectorAll: true,
    }
  );

  const emit = defineEmits(['update:visible', 'save']);
  const columns: MsTableColumn = [
    {
      title: 'caseManagement.featureCase.tableColumnID',
      dataIndex: 'num',
      width: 150,
      showInTable: true,
      showTooltip: true,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.defectName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showDrag: false,
    },

    {
      title: 'caseManagement.featureCase.defectState',
      slotName: 'statusName',
      dataIndex: 'statusName',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnTag',
      slotName: 'tags',
      dataIndex: 'tags',
      showInTable: true,
      isTag: true,
      width: 300,
      showDrag: true,
    },
    {
      title: 'caseManagement.featureCase.updateUser',
      slotName: 'handleUserName',
      dataIndex: 'handleUserName',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
      showDrag: false,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateUser',
      slotName: 'createUser',
      dataIndex: 'createUser',
      showInTable: true,
      showTooltip: true,
      width: 200,
      ellipsis: true,
    },
    {
      title: 'caseManagement.featureCase.tableColumnCreateTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showInTable: true,
      width: 200,
      showDrag: true,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, resetSelector } = useTable(
    getDrawerDebugPage,
    {
      scroll: { x: 'auto' },
      columns,
      tableKey: TableKeyEnum.CASE_MANAGEMENT_TAB_DEFECT,
      selectable: true,
      showSelectorAll: props.showSelectorAll,
      heightUsed: 340,
      enableDrag: false,
    },
    (record) => {
      return {
        ...record,
        tags: (record.tags || []).map((item: string, i: number) => {
          return {
            id: `${record.id}-${i}`,
            name: item,
          };
        }),
      };
    }
  );

  const keyword = ref<string>('');

  const showDrawer = computed({
    get() {
      return props.visible;
    },
    set(value) {
      emit('update:visible', value);
    },
  });

  function handleDrawerConfirm() {
    const { excludeKeys, selectedKeys, selectorStatus } = propsRes.value;
    const params = {
      excludeIds: [...excludeKeys],
      selectIds: selectorStatus === 'all' ? [] : [...selectedKeys],
      selectAll: selectorStatus === 'all',
      projectId: currentProjectId.value,
      keyword: keyword.value,
      searchMode: 'AND',
      combine: {},
      caseId: props.caseId,
      condition: { keyword: keyword.value },
    };
    showDrawer.value = false;
    emit('save', params);
  }

  function handleDrawerCancel() {
    resetSelector();
  }

  function getFetch() {
    setLoadListParams({ keyword: keyword.value, projectId: currentProjectId.value, sourceId: props.caseId });
    loadList();
  }

  const searchList = debounce(() => {
    getFetch();
  }, 100);

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        resetSelector();
        getFetch();
      }
    }
  );
</script>

<style lang="less">
  .bug-content-popover {
    .arco-popover-content {
      overflow: auto;
      max-height: 400px;
      .ms-scroll-bar();
    }
  }
</style>
