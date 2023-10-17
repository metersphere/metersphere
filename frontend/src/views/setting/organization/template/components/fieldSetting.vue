<template>
  <MsCard :has-breadcrumb="true" simple>
    <a-alert class="mb-6" type="warning">{{ t('system.orgTemplate.enableDescription') }}</a-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnable" class="font-medium">{{ t('system.orgTemplate.fieldList') }}</span>
      <a-button v-else type="primary" :disabled="false" @click="fieldHandler('add')">
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
        <MsIcon v-if="getIconType(record.type).type !== 'system'" :type="getIconType(record.type).iconName" size="16" />
        <span class="ml-2">{{ record.name }}</span>
        <span v-if="record.internal" class="system-flag">{{ t('system.orgTemplate.isSystem') }}</span>
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap">
          <MsButton class="!mr-0" @click="fieldHandler('edit', record)">{{ t('system.orgTemplate.edit') }}</MsButton>
          <a-divider v-if="!record.internal" direction="vertical" />
          <MsTableMoreAction
            v-if="!record.internal"
            :list="moreActions"
            @select="(item) => handleMoreActionSelect(item, record)"
          />
        </div>
      </template>
      <template #fieldType="{ record }">
        <span>{{ getIconType(record.type)['type'] }}</span>
      </template>
    </MsBaseTable>
    <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showDrawer" @success="successHandler" />
  </MsCard>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
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
  import { characterLimit } from '@/utils';

  import type { AddOrUpdateField } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import { TemplateIconEnum } from '@/enums/templateEnum';

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

  tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE, fieldColumns, 'drawer');

  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getFieldList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE,
    scroll: { x: '1000px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    heightUsed: 380,
  });

  const keyword = ref('');

  // 查询字段
  const searchFiled = async () => {
    try {
      const totalData = await getFieldList({ organizationId: currentOrd, scene: route.query.type });
      const filterData = totalData.filter((item: AddOrUpdateField) => item.name.includes(keyword.value));
      setProps({ data: filterData });
    } catch (error) {
      console.log(error);
    }
  };

  const fetchData = async () => {
    const scene = route.query.type;
    setLoadListParams({ organizationId: currentOrd, scene });
    await loadList();
  };

  const tableRef = ref();
  const isEnable = ref<boolean>(false); // 开始默认未启用

  // 切换模版是否启用展示操作列
  const isEnableOperation = () => {
    if (isEnable.value) {
      const noOperationColumn = fieldColumns.slice(0, -1);
      tableStore.setColumns(TableKeyEnum.ORGANIZATION_TEMPLATE, noOperationColumn, 'drawer');
      tableRef.value.initColumn();
    } else {
      tableStore.setColumns(TableKeyEnum.ORGANIZATION_TEMPLATE, fieldColumns, 'drawer');
      tableRef.value.initColumn();
    }
  };

  // 获取当前字段类型
  const getIconType = (iconType: string) => {
    switch (iconType) {
      case 'INPUT':
        return { iconName: TemplateIconEnum.INPUT, type: t('system.orgTemplate.input') };
      case 'TEXTAREA':
        return { iconName: TemplateIconEnum.TEXTAREA, type: t('system.orgTemplate.textarea') };
      case 'SELECT':
        return { iconName: TemplateIconEnum.SELECT, type: t('system.orgTemplate.select') };
      case 'MULTIPLE_SELECT':
        return { iconName: TemplateIconEnum.MULTIPLE_SELECT, type: t('system.orgTemplate.multipleSelect') };
      case 'RADIO':
        return { iconName: TemplateIconEnum.RADIO, type: t('system.orgTemplate.radio') };
      case 'CHECKBOX':
        return { iconName: TemplateIconEnum.CHECKBOX, type: t('system.orgTemplate.checkbox') };
      case 'MEMBER':
        return { iconName: TemplateIconEnum.MEMBER, type: t('system.orgTemplate.member') };
      case 'MULTIPLE_MEMBER':
        return { iconName: TemplateIconEnum.MULTIPLE_MEMBER, type: t('system.orgTemplate.multipleMember') };
      case 'DATE':
        return { iconName: TemplateIconEnum.DATE, type: t('system.orgTemplate.date') };
      case 'DATETIME':
        return { iconName: TemplateIconEnum.DATE, type: t('system.orgTemplate.dateTime') };
      case 'INT':
        return { iconName: TemplateIconEnum.NUMBER, type: t('system.orgTemplate.number') };
      case 'FLOAT':
        return { iconName: TemplateIconEnum.NUMBER, type: t('system.orgTemplate.number') };
      case 'MULTIPLE_INPUT':
        return { iconName: TemplateIconEnum.MULTIPLE_INPUT, type: t('system.orgTemplate.multipleInput') };
      default:
        return { type: 'system', iconName: '' };
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
    if (type === 'edit' && record) fieldDrawerRef.value.isEditHandler(record);
  };

  const successHandler = () => {
    loadList();
  };

  const templateList = ref([
    {
      value: 'FUNCTIONAL',
      name: 'system.orgTemplate.caseTemplates',
    },
    { value: 'API', name: 'system.orgTemplate.APITemplates' },
    { value: 'UI', name: 'system.orgTemplate.UITemplates' },
    { value: 'TEST_PLAN', name: 'system.orgTemplate.testPlanTemplates' },
    { value: 'BUG', name: 'system.orgTemplate.defectTemplates' },
  ]);

  // 更新面包屑根据不同的模版
  const updateBreadcrumbList = () => {
    const { breadcrumbList } = appStore;
    const breadTitle = templateList.value.find((item) => item.value === route.query.type);
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
