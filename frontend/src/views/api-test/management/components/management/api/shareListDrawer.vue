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
        <a-button
          v-permission="['PROJECT_API_DEFINITION:READ+SHARE']"
          class="w-[84px]"
          type="primary"
          @click="emit('editOrCreate')"
        >
          {{ t('apiTestManagement.newCreateShare') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('common.searchByName')"
          allow-clear
          class="mx-[8px] w-[240px]"
          @search="searchList"
          @press-enter="searchList"
          @clear="searchList"
        />
      </div>
      <MsBaseTable v-bind="propsRes" no-disable :row-class="getRowClass" v-on="propsEvent">
        <template #isPrivate="{ record }">
          {{ record.isPrivate ? t('apiTestManagement.passwordView') : t('apiTestManagement.publicityView') }}
        </template>
        <template #updateUserName="{ record }">
          <a-tooltip :content="`${record.updateUserName}`" position="tl">
            <div class="one-line-text">{{ record.updateUserName || '-' }}</div>
          </a-tooltip>
        </template>
        <template #invalidTime="{ record }">
          {{ record.invalidTime === 0 ? t('apiTestManagement.permanent') : record.invalidTime }}
        </template>
        <template #operation="{ record }">
          <div v-permission="['PROJECT_API_DEFINITION:READ+SHARE']" class="flex items-center">
            <a-tooltip :disabled="!!record.apiShareNum" :content="t('apiTestManagement.apiShareNumberTip')">
              <MsButton class="!mx-0" :disabled="!record.apiShareNum" @click="viewLink(record)">
                {{ t('apiTestManagement.viewLink') }}
              </MsButton>
            </a-tooltip>
            <a-divider direction="vertical" :margin="8" />
            <MsButton class="!mx-0" @click="editShare(record)">
              {{ t('common.edit') }}
            </MsButton>
            <a-divider direction="vertical" :margin="8" />
            <MsButton class="!mx-0" @click="deleteHandler(record)">
              {{ t('common.delete') }}
            </MsButton>
          </div>
        </template>
      </MsBaseTable>
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';

  import { deleteShare, getSharePage } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { useAppStore, useTableStore } from '@/store';
  import { characterLimit, operationWidth } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { ShareDetail, shareItem } from '@/models/apiTest/management';
  import { ShareEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

  const { openNewPage } = useOpenNewPage();

  const { openModal } = useModal();

  const { t } = useI18n();

  const tableStore = useTableStore();
  const appStore = useAppStore();

  const emit = defineEmits<{
    (e: 'editOrCreate', record?: ShareDetail): void;
    (e: 'loadList'): void;
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
      slotName: 'isPrivate',
      dataIndex: 'isPrivate',
      showDrag: true,
    },
    {
      title: 'apiTestManagement.apiShareNum',
      slotName: 'apiShareNum',
      dataIndex: 'apiShareNum',
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
      width: 200,
      showDrag: true,
    },
    {
      title: 'apiTestManagement.deadline',
      dataIndex: 'invalidTime',
      slotName: 'invalidTime',
      showInTable: true,
      sortable: {
        sortDirections: ['ascend', 'descend'],
        sorter: true,
      },
      width: 200,
      showDrag: true,
    },
    {
      title: 'common.operator',
      dataIndex: 'updateUser',
      slotName: 'updateUserName',
      showInTable: true,
      width: 200,
      showDrag: true,
      filterConfig: {
        mode: 'remote',
        loadOptionParams: {
          projectId: appStore.currentProjectId,
        },
        remoteMethod: FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER,
      },
    },
    {
      title: hasAnyPermission(['PROJECT_API_DEFINITION:READ+SHARE']) ? 'common.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: operationWidth(215, hasAnyPermission(['PROJECT_API_DEFINITION:READ+SHARE']) ? 180 : 50),
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(
    getSharePage,
    {
      tableKey: TableKeyEnum.SYSTEM_RESOURCE_POOL_CAPACITY,
      scroll: { x: '100%' },
      selectable: false,
      showSetting: true,
      heightUsed: 272,
      showSelectAll: false,
    },
    (item) => ({
      ...item,
      invalidTime: item.invalidTime ? dayjs(item.invalidTime).format('YYYY-MM-DD HH:mm:ss') : 0,
    })
  );

  // 查看链接
  function viewLink(record: shareItem) {
    openNewPage(ShareEnum.SHARE_DEFINITION_API, {
      docShareId: record.id,
    });
  }

  // 编辑
  function editShare(record: ShareDetail) {
    emit('editOrCreate', record);
  }

  const keyword = ref<string>('');
  function searchList() {
    setLoadListParams({
      keyword: keyword.value,
      projectId: appStore.currentProjectId,
    });
    loadList();
  }

  // 删除
  function deleteHandler(record: shareItem) {
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
          if (record.id) {
            await deleteShare(record.id);
            Message.success(t('caseManagement.featureCase.deleteSuccess'));
            emit('loadList');
            searchList();
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  function getRowClass(record: shareItem) {
    return record.invalid ? 'grey-row-class' : '';
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        searchList();
      }
    }
  );

  defineExpose({
    searchList,
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
