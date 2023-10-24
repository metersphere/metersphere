<template>
  <MsCard :has-breadcrumb="true" simple>
    <a-alert class="mb-6" :type="isEnable ? 'warning' : 'info'">{{
      isEnable ? t('system.orgTemplate.enableDescription') : t('system.orgTemplate.fieldLimit')
    }}</a-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnable" class="font-medium">{{ t('system.orgTemplate.fieldList') }}</span>
      <a-button v-else type="primary" :disabled="totalData.length > 20" @click="fieldHandler('add')">
        {{ t('system.orgTemplate.addField') }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.orgTemplate.searchTip')"
        class="w-[230px]"
        allow-clear
        @search="searchFiled"
        @press-enter="searchFiled"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
      <template #name="{ record }">
        <MsIcon v-if="!record.internal" :type="getIconType(record.type)?.iconName || ''" size="16" />
        <span class="ml-2">{{ record.name }}</span>
        <span v-if="record.internal" class="system-flag">{{ t('system.orgTemplate.isSystem') }}</span>
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <MsPopConfirm
            type="error"
            :title="t('system.orgTemplate.updateTip', { name: characterLimit(record.name) })"
            :sub-title-tip="t('system.orgTemplate.updateDescription')"
            :ok-text="t('system.orgTemplate.confirm')"
            @confirm="handleOk(record)"
          >
            <MsButton class="!mr-0">{{ t('system.orgTemplate.edit') }}</MsButton></MsPopConfirm
          >

          <a-divider v-if="!record.internal" direction="vertical" />
          <MsTableMoreAction
            v-if="!record.internal"
            :list="moreActions"
            @select="(item) => handleMoreActionSelect(item, record)"
          />
        </div>
      </template>
      <template #fieldType="{ record }">
        <span>{{ getIconType(record.type)?.label }}</span>
      </template>
    </MsBaseTable>
    <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showDrawer" @success="successHandler" />
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模版-字段列表
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { deleteOrdField, getFieldList } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';

  import type { AddOrUpdateField, SeneType } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { cardList, getIconType } from './fieldSetting';

  const templateStore = useTemplateStore();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const route = useRoute();
  const { openModal } = useModal();

  const currentOrd = appStore.currentOrgId;

  const fieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.columnFieldType',
      dataIndex: 'type',
      slotName: 'fieldType',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.columnFieldDescription',
      dataIndex: 'remark',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.columnFieldUpdatedTime',
      dataIndex: 'updateTime',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.operation',
      slotName: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
      showDrag: false,
    },
  ];
  tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING, fieldColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getFieldList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING,
    scroll: { x: '1000px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    heightUsed: 380,
  });

  const keyword = ref('');
  const totalData = ref([]);

  // 查询模板字段
  const searchFiled = async () => {
    try {
      totalData.value = await getFieldList({ organizationId: currentOrd, scene: route.query.type });
      const filterData = totalData.value.filter((item: AddOrUpdateField) => item.name.includes(keyword.value));
      setProps({ data: filterData });
    } catch (error) {
      console.log(error);
    }
  };
  const scene = ref<SeneType>('');
  const fetchData = async () => {
    scene.value = route.query.type;
    setLoadListParams({ organizationId: currentOrd, scene });
    await loadList();
  };

  const tableRef = ref();
  const isEnable = ref<boolean>(templateStore.templateStatus[scene.value as string]); // 开始默认未启用

  // 切换模版是否启用展示操作列
  const isEnableOperation = () => {
    if (isEnable.value) {
      const noOperationColumn = fieldColumns.slice(0, -1);
      tableStore.setColumns(TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING, noOperationColumn, 'drawer');
      tableRef.value.initColumn();
    } else {
      tableStore.setColumns(TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING, fieldColumns, 'drawer');
      tableRef.value.initColumn();
    }
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  // 删除字段
  const handlerDelete = (record: AddOrUpdateField) => {
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteTitle', { name: characterLimit(record.name) }),
      content: t('system.userGroup.beforeDeleteUserGroup'),
      okText: t('system.userGroup.confirmDelete'),
      cancelText: t('system.userGroup.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteOrdField(record.id);
          Message.success(t('system.user.deleteUserSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: AddOrUpdateField) => {
    if (item.eventTag === 'delete') {
      handlerDelete(record);
    }
  };

  const showDrawer = ref<boolean>(false);

  const fieldDrawerRef = ref();
  const fieldHandler = (type: string, record?: AddOrUpdateField) => {
    showDrawer.value = true;
    if (type === 'edit' && record) fieldDrawerRef.value.editHandler(record);
  };

  const handleOk = (record: AddOrUpdateField) => {
    fieldHandler('edit', record);
  };

  const successHandler = () => {
    loadList();
  };

  // 更新面包屑根据不同的模版
  const updateBreadcrumbList = () => {
    const { breadcrumbList } = appStore;
    const breadTitle = cardList.find((item) => item.key === route.query.type);
    if (breadTitle) {
      breadcrumbList[0].locale = breadTitle.name;
      appStore.setBreadcrumbList(breadcrumbList);
    }
  };

  onMounted(() => {
    fetchData();
    isEnableOperation();
    updateBreadcrumbList();
  });
</script>

<style scoped lang="less">
  .system-flag {
    background: var(--color-text-n8);
    @apply ml-2 rounded p-1 text-xs;
  }
</style>
