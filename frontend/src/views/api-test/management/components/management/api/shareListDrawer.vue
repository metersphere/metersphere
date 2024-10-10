<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :title="t('apiTestManagement.shareList')"
    :width="800"
    :footer="false"
    no-content-padding
    unmount-on-close
  >
    <div class="p-[16px]">
      <div class="mb-4 flex items-center justify-between">
        <a-button class="w-[84px]" type="primary" @click="emit('editOrCreate')">
          {{ t('apiTestManagement.newCreateShare') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('apiScenario.params.searchPlaceholder')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        />
      </div>
      <MsBaseTable v-bind="propsRes" no-disable :row-class="getRowClass" v-on="propsEvent">
        <template #accessRestriction="{ record }">
          {{ record.accessRestriction ? t('apiTestManagement.passwordView') : t('apiTestManagement.publicityView') }}
        </template>
        <template #operation="{ record }">
          <MsButton class="!mx-0" @click="viewLink(record)">
            {{ t('apiTestManagement.viewLink') }}
          </MsButton>
          <a-divider direction="vertical" :margin="8" />
          <MsButton class="!mx-0" @click="editShare(record.id)">
            {{ t('common.edit') }}
          </MsButton>
          <a-divider direction="vertical" :margin="8" />
          <MsButton class="!mx-0" @click="deleteShare(record)">
            {{ t('common.delete') }}
          </MsButton>
        </template>
      </MsBaseTable>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';

  import { ApiTestRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { openNewPage } = useOpenNewPage();

  const { openModal } = useModal();

  const { t } = useI18n();

  const tableStore = useTableStore();

  const emit = defineEmits<{
    (e: 'editOrCreate', id?: string): void;
  }>();

  const innerVisible = defineModel<boolean>('visible', {
    required: true,
  });

  const columns: MsTableColumn = [
    {
      title: 'common.name',
      slotName: 'name',
      dataIndex: 'name',
      showTooltip: true,
      width: 200,
      columnSelectorDisabled: true,
    },
    {
      title: 'apiTestManagement.accessRestriction',
      slotName: 'accessRestriction',
      dataIndex: 'accessRestriction',
      showDrag: true,
    },
    {
      title: 'common.createTime',
      slotName: 'createTime',
      dataIndex: 'createTime',
      showTooltip: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 150,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.deadline',
      dataIndex: 'deadline',
      slotName: 'deadline',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 180,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setKeyword } = useTable(undefined, {
    tableKey: TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY,
    scroll: { y: 'auto' },
    selectable: false,
    showSetting: true,
    heightUsed: 310,
    showSelectAll: false,
  });

  // 查看链接
  function viewLink(record: any) {
    openNewPage(ApiTestRouteEnum.API_TEST_MANAGEMENT, {
      dId: record.id,
      pId: record.projectId,
    });
  }

  // 编辑
  function editShare(id: string) {
    emit('editOrCreate', id);
  }

  // 删除
  function deleteShare(record: any) {
    openModal({
      type: 'error',
      title: t('common.deleteConfirmTitle', { name: characterLimit(record.name) }),
      content: t('apiTestManagement.deleteShareTip'),
      okText: t('common.confirmDelete'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          Message.success(t('caseManagement.featureCase.deleteSuccess'));
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  const keyword = ref<string>('');

  function searchList() {}

  function getRowClass(record: any) {
    return record.expired ? 'grey-row-class' : '';
  }

  onMounted(() => {
    searchList();
  });

  await tableStore.initColumn(TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY, columns, 'drawer');
</script>

<style scoped lang="less">
  :deep(.grey-row-class.arco-table-tr) {
    color: var(--color-text-4) !important;
    .arco-table-td {
      color: var(--color-text-4) !important;
    }
  }
</style>
