<template>
  <MsCard has-breadcrumb simple>
    <a-alert v-if="isEnableOrdTemplate" class="mb-6" type="warning">{{
      t('system.orgTemplate.enableTemplateTip')
    }}</a-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnableOrdTemplate" class="font-medium">{{ t('system.orgTemplate.templateList') }}</span>
      <a-button
        v-else
        v-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
        type="primary"
        :disabled="false"
        @click="createTemplate"
      >
        {{ t('system.orgTemplate.createTemplate') }}
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
        <div class="flex items-center">
          <span class="ml-2">{{ record.name }}</span>
          <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isSystem') }}</MsTag>
        </div>
      </template>
      <template #enableThirdPart="{ record }">
        {{ record.enableThirdPart ? t('system.orgTemplate.yes') : t('system.orgTemplate.no') }}
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap items-center">
          <MsButton v-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']" @click="editTemplate(record.id)">{{
            t('system.orgTemplate.edit')
          }}</MsButton>
          <MsButton v-permission="['ORGANIZATION_TEMPLATE:READ+ADD']" class="!mr-0" @click="copyTemplate(record.id)">{{
            t('system.orgTemplate.copy')
          }}</MsButton>
          <a-divider
            v-if="!record.internal"
            v-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
            class="h-[12px]"
            direction="vertical"
          />
          <MsTableMoreAction
            v-if="!record.internal"
            v-permission="['ORGANIZATION_TEMPLATE:READ+DELETE']"
            :list="moreActions"
            @select="(item) => handleMoreActionSelect(item, record)"
          />
        </div>
      </template>
    </MsBaseTable>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板-模板管理列表
   */
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
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { deleteOrdTemplate, getOrganizeTemplateList } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';

  import type { OrdTemplateManagement } from '@/models/setting/template';
  import { SettingRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getCardList } from './fieldSetting';

  const route = useRoute();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const currentOrd = computed(() => appStore.currentOrgId);

  const fieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.columnTemplateName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'system.orgTemplate.description',
      dataIndex: 'remark',
      showDrag: true,
      showInTable: true,
      width: 300,
      showTooltip: true,
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
      dataIndex: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getOrganizeTemplateList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT,
    scroll: { x: '100%' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    heightUsed: 380,
  });
  const scene = route.query.type;

  const isEnableOrdTemplate = computed(() => {
    return templateStore.projectStatus[scene as string];
  });

  const totalList = ref<OrdTemplateManagement[]>([]);

  // 查询字段
  const searchFiled = async () => {
    try {
      totalList.value = await getOrganizeTemplateList({ organizationId: currentOrd.value, scene });
      const filterData = totalList.value.filter((item: OrdTemplateManagement) => item.name.includes(keyword.value));
      setProps({ data: filterData });
    } catch (error) {
      console.log(error);
    }
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  // 删除模板
  const handlerDelete = (record: any) => {
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteTemplateTitle', { name: characterLimit(record.name) }),
      content: t('system.userGroup.beforeDeleteUserGroup'),
      okText: t('system.userGroup.confirmDelete'),
      cancelText: t('system.userGroup.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteOrdTemplate(record.id);
          Message.success(t('common.deleteSuccess'));
          loadList();
        } catch (error) {
          console.log(error);
        }
      },
      hideCancel: false,
    });
  };

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: any) => {
    if (item.eventTag === 'delete') {
      handlerDelete(record);
    }
  };

  const fetchData = async () => {
    setLoadListParams({ organizationId: currentOrd.value, scene: route.query.type });
    await loadList();
  };

  // 创建模板
  const createTemplate = () => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        type: route.query.type,
      },
      params: {
        mode: 'create',
      },
    });
  };

  // 编辑模板
  const editTemplate = (id: string) => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        id,
        type: route.query.type,
      },
      params: {
        mode: 'edit',
      },
    });
  };

  // 复制模板
  const copyTemplate = (id: string) => {
    router.push({
      name: SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_DETAIL,
      query: {
        id,
        type: route.query.type,
      },
      params: {
        mode: 'copy',
      },
    });
  };

  // 更新面包屑根据不同的模板
  const updateBreadcrumbList = () => {
    debugger;
    const { breadcrumbList } = appStore;
    const breadTitle = getCardList('organization').find((item: any) => item.key === route.query.type);
    if (breadTitle) {
      breadcrumbList[0].locale = breadTitle.name;
      appStore.setBreadcrumbList(breadcrumbList);
    }
  };

  const tableRef = ref();

  const sceneType = computed(() => route.query.type);

  const isThirdParty = {
    title: 'system.orgTemplate.isThirdParty',
    dataIndex: 'enableThirdPart',
    slotName: 'enableThirdPart',
    showDrag: true,
    showInTable: true,
  };

  function updateColumns() {
    const columns =
      sceneType.value === 'BUG' ? fieldColumns.slice(0, 1).concat(isThirdParty, fieldColumns.slice(1)) : fieldColumns;
    if (isEnableOrdTemplate.value) {
      const result = columns.slice(0, columns.length - 1);
      tableRef.value.initColumn(result);
    } else {
      tableRef.value.initColumn(columns);
    }
  }

  onMounted(() => {
    updateBreadcrumbList();
    fetchData();
    updateColumns();
  });
  tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT, fieldColumns, 'drawer');
</script>

<style scoped lang="less">
  .system-flag {
    background: var(--color-text-n8);
    @apply ml-2 rounded p-1 text-xs;
  }
</style>
