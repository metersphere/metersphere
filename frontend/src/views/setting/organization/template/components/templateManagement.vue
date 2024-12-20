<template>
  <MsCard has-breadcrumb simple>
    <a-alert v-if="isEnableOrdTemplate" class="mb-6" type="warning">
      {{ t('system.orgTemplate.enableTemplateTip') }}
    </a-alert>
    <div class="mb-4 flex items-center justify-between">
      <!-- <span v-if="isEnableOrdTemplate || route.query.type === 'BUG'" class="font-medium">{{
        t('system.orgTemplate.templateList', { type: getTemplateName('organization', route.query.type as string) })
      }}</span> -->
      <span v-if="isShowListTip" class="font-medium">
        {{
          t('system.orgTemplate.templateList', { type: getTemplateName('organization', route.query.type as string) })
        }}
      </span>
      <a-button
        v-if="!isEnableOrdTemplate && route.query.type === 'BUG'"
        v-permission="['ORGANIZATION_TEMPLATE:READ+ADD']"
        type="primary"
        :disabled="false"
        @click="createTemplate"
      >
        {{
          t('system.orgTemplate.createTemplateType', {
            type: getTemplateName('organization', route.query.type as string),
          })
        }}
      </a-button>
      <a-input-search
        v-model:model-value="keyword"
        :placeholder="t('system.orgTemplate.searchTip')"
        class="w-[230px]"
        allow-clear
        @search="searchFiled"
        @press-enter="searchFiled"
        @clear="searchFiled"
      ></a-input-search>
    </div>
    <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
      <template #name="{ record }">
        <div class="flex items-center">
          <span class="ml-2">{{ record.name }}</span>
          <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isBuiltIn') }}</MsTag>
        </div>
      </template>
      <template #enableThirdPart="{ record }">
        {{ record.enableThirdPart ? t('system.orgTemplate.yes') : t('system.orgTemplate.no') }}
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap items-center">
          <MsButton v-permission="['ORGANIZATION_TEMPLATE:READ+UPDATE']" @click="editTemplate(record.id)">
            {{ t('system.orgTemplate.edit') }}
          </MsButton>
          <MsButton
            v-if="route.query.type === 'BUG' && hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ADD'])"
            class="!mr-0"
            @click="copyTemplate(record.id)"
          >
            {{ t('system.orgTemplate.copy') }}
          </MsButton>
          <a-divider
            v-if="hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ADD']) && !record.internal"
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
  import { hasAnyPermission } from '@/utils/permission';

  import type { OrdTemplateManagement } from '@/models/setting/template';
  import { SettingRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getTemplateName } from '@/views/setting/organization/template/components/fieldSetting';

  const route = useRoute();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const currentOrd = computed(() => appStore.currentOrgId);

  const hasOperationPermission = computed(() =>
    hasAnyPermission([
      'ORGANIZATION_TEMPLATE:READ+ADD',
      'ORGANIZATION_TEMPLATE:READ+UPDATE',
      'ORGANIZATION_TEMPLATE:READ+DELETE',
    ])
  );

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
      title: 'common.desc',
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
      title: hasOperationPermission.value ? 'system.orgTemplate.operation' : '',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: hasOperationPermission.value ? 200 : 50,
      showInTable: true,
      showDrag: false,
    },
  ];

  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getOrganizeTemplateList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT,
    scroll: { x: '100%' },
    columns: fieldColumns,
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
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

  const routeName = ref<string>('');

  // 创建模板
  const createTemplate = () => {
    router.push({
      name: routeName.value,
      query: {
        ...route.query,
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
      name: routeName.value,
      query: {
        ...route.query,
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
      name: routeName.value,
      query: {
        ...route.query,
        id,
        type: route.query.type,
      },
      params: {
        mode: 'copy',
      },
    });
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

  const isShowListTip = computed(() => {
    if (!hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ADD'])) {
      return true;
    }
    if (isEnableOrdTemplate.value && route.query.type === 'BUG') {
      return true;
    }
    return route.query.type !== 'BUG';
  });

  onMounted(() => {
    fetchData();
    updateColumns();
    if (route.query.type === 'FUNCTIONAL') {
      routeName.value = SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_CASE_DETAIL;
    } else if (route.query.type === 'API') {
      routeName.value = SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_API_DETAIL;
    } else {
      routeName.value = SettingRouteEnum.SETTING_ORGANIZATION_TEMPLATE_MANAGEMENT_BUG_DETAIL;
    }
  });
</script>

<style scoped lang="less">
  .system-flag {
    @apply ml-2 rounded p-1;

    font-size: 12px;
    line-height: 20px;
    background: var(--color-text-n8);
  }
</style>
