<template>
  <MsCard has-breadcrumb simple>
    <div class="mb-4 flex items-center justify-between">
      <!-- <span v-if="isEnableOrdTemplate" class="font-medium">{{
        t('system.orgTemplate.templateList', { type: getTemplateName('project', route.query.type as string) })
      }}</span> -->
      <span v-if="isShowList" class="font-medium">{{
        t('system.orgTemplate.templateList', { type: getTemplateName('project', route.query.type as string) })
      }}</span>
      <!--TODO 这个版本不允许修改默认模版也不允许创建用例模版  -->
      <a-button
        v-if="!isEnableOrdTemplate && route.query.type === 'BUG'"
        v-permission="['PROJECT_TEMPLATE:READ+ADD']"
        type="primary"
        :disabled="false"
        @click="createTemplate"
      >
        {{
          t('system.orgTemplate.createTemplateType', {
            type: getTemplateName('project', route.query.type as string),
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
      <template #defaultTemplate="{ record }">
        <div v-if="record.enableDefault">
          <a-tooltip :content="t('system.orgTemplate.must.open.one')" :mouse-enter-delay="500">
            <a-switch
              v-model="record.enableDefault"
              :disabled="true"
              size="small"
              type="line"
              @change="(value) => changeDefault(value, record)"
            />
          </a-tooltip>
        </div>
        <a-switch
          v-else
          v-model="record.enableDefault"
          :disabled="record.enableDefault"
          size="small"
          type="line"
          @change="(value) => changeDefault(value, record)"
        />
      </template>
      <template #enableThirdPart="{ record }">
        {{ record.enableThirdPart ? t('system.orgTemplate.yes') : t('system.orgTemplate.no') }}
      </template>
      <template #name="{ record }">
        <div class="flex items-center">
          <span v-if="record.enablePlatformDefault" class="one-line-text ml-2">{{ record.name }}</span>
          <span
            v-else
            class="one-line-text ml-2 cursor-pointer text-[rgb(var(--primary-5))]"
            @click="previewDetail(record.id)"
            >{{ record.name }}</span
          >
          <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isBuiltIn') }}</MsTag>
        </div>
      </template>
      <template #operation="{ record }">
        <div v-if="!record.enablePlatformDefault" class="flex flex-row flex-nowrap items-center">
          <MsButton v-permission="['PROJECT_TEMPLATE:READ+UPDATE']" @click="editTemplate(record.id)">{{
            t('system.orgTemplate.edit')
          }}</MsButton>
          <MsButton
            v-if="route.query.type === 'BUG' && hasAnyPermission(['PROJECT_TEMPLATE:READ+ADD'])"
            class="!mr-0"
            @click="copyTemplate(record.id)"
            >{{ t('system.orgTemplate.copy') }}</MsButton
          >
          <a-divider
            v-if="!record.internal && hasAnyPermission(['PROJECT_TEMPLATE:READ+ADD'])"
            class="h-[16px]"
            direction="vertical"
          />
          <MsTableMoreAction
            v-if="!record.internal"
            v-permission="['PROJECT_TEMPLATE:READ+DELETE']"
            :list="moreActions"
            @select="(item) => handleMoreActionSelect(item, record)"
          />
        </div>
      </template>
    </MsBaseTable>
    <MsDrawer
      v-model:visible="showDetailVisible"
      :title="titleDetail"
      :width="1200"
      :footer="false"
      unmount-on-close
      @cancel="handleCancel"
    >
      <PreviewTemplate
        :select-field="(selectData as DefinedFieldItem[])"
        :template-type="route.query.type"
        :system-fields="systemFields"
      />
    </MsDrawer>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-项目-模板-模板管理列表
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import PreviewTemplate from '@/views/setting/organization/template/components/viewTemplate.vue';

  import {
    deleteProjectTemplate,
    getProjectFieldList,
    getProjectTemplateInfo,
    getProjectTemplateList,
    setDefaultTemplate,
  } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import router from '@/router';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';
  import { hasAnyPermission } from '@/utils/permission';

  import type { CustomField, DefinedFieldItem, OrdTemplateManagement } from '@/models/setting/template';
  import { ProjectManagementRouteEnum } from '@/enums/routeEnum';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import {
    getCustomDetailFields,
    getTemplateName,
    getTotalFieldOptionList,
  } from '@/views/setting/organization/template/components/fieldSetting';

  const route = useRoute();
  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const { openModal } = useModal();

  const keyword = ref('');
  const currentProjectId = computed(() => appStore.currentProjectId);

  const sceneType = computed(() => route.query.type);

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['PROJECT_TEMPLATE:READ+ADD', 'PROJECT_TEMPLATE:READ+UPDATE', 'PROJECT_TEMPLATE:READ+DELETE'])
  );

  const fieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.columnTemplateName',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      fixed: 'left',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'system.orgTemplate.defaultTemplate',
      dataIndex: 'remark',
      slotName: 'defaultTemplate',
      showDrag: true,
      showInTable: true,
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

  const isThirdParty = {
    title: 'system.orgTemplate.isThirdParty',
    dataIndex: 'enableThirdPart',
    slotName: 'enableThirdPart',
    showDrag: true,
    showInTable: true,
  };

  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT, fieldColumns, 'drawer');
  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getProjectTemplateList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT,
    scroll: { x: '1400px' },
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
    return !templateStore.projectStatus[scene as string];
  });

  const totalList = ref<OrdTemplateManagement[]>([]);

  // 查询字段
  const searchFiled = async () => {
    try {
      totalList.value = await getProjectTemplateList({ projectId: currentProjectId.value, scene });
      const filterData: OrdTemplateManagement[] = totalList.value.filter((item: OrdTemplateManagement) =>
        item.name.includes(keyword.value)
      );
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
      content: t('system.orgTemplate.deleteProjectTemplateTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteProjectTemplate(record.id);
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
    setLoadListParams({ projectId: currentProjectId.value, scene: route.query.type });
    await loadList();
  };

  // 设置默认模板
  const changeDefault = async (value: any, record: OrdTemplateManagement) => {
    if (value) {
      try {
        await setDefaultTemplate(currentProjectId.value, record.id);
        Message.success(t('system.orgTemplate.setSuccessfully'));
        fetchData();
      } catch (error) {
        console.log(error);
      }
    }
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

  const showDetailVisible = ref<boolean>(false);
  const selectData = ref<DefinedFieldItem[]>([]);
  const totalData = ref<DefinedFieldItem[]>([]);

  // 处理自定义字段列表
  const getFieldOptionList = () => {
    totalData.value = getTotalFieldOptionList(totalData.value as DefinedFieldItem[]);
  };

  const initDetailForm = {
    name: '',
    description: '',
  };
  const titleDetail = ref<string>();
  const defectForm = ref<Record<string, any>>({ ...initDetailForm });
  const systemFields = ref<CustomField[]>([]);

  // 预览详情
  const previewDetail = async (id: string) => {
    showDetailVisible.value = true;

    try {
      totalData.value = await getProjectFieldList({ scopedId: currentProjectId.value, scene: route.query.type });
      getFieldOptionList();
      const res = await getProjectTemplateInfo(id);
      titleDetail.value = res.name;
      selectData.value = getCustomDetailFields(totalData.value as DefinedFieldItem[], res.customFields);
      systemFields.value = res.systemFields;
    } catch (error) {
      console.log(error);
    }
  };

  const handleCancel = () => {
    showDetailVisible.value = false;
    defectForm.value = { ...initDetailForm };
  };

  const tableRef = ref();

  function updateColumns() {
    const columns =
      sceneType.value === 'BUG' ? fieldColumns.slice(0, 2).concat(isThirdParty, fieldColumns.slice(2)) : fieldColumns;
    if (isEnableOrdTemplate.value) {
      const result = columns.slice(0, fieldColumns.length - 1);
      tableRef.value.initColumn(result);
    } else {
      tableRef.value.initColumn(columns);
    }
  }

  const isShowList = computed(() => {
    if (!hasAnyPermission(['PROJECT_TEMPLATE:READ+ADD'])) {
      return true;
    }
    if (isEnableOrdTemplate.value && route.query.type === 'BUG') {
      return true;
    }
    return route.query.type !== 'BUG';
  });

  onMounted(() => {
    if (route.query.id) {
      previewDetail(route.query.id as string);
    }
  });

  onBeforeUnmount(() => {
    showDetailVisible.value = false;
  });

  onMounted(() => {
    fetchData();
    updateColumns();
    if (route.query.type === 'FUNCTIONAL') {
      routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_CASE_DETAIL;
    } else if (route.query.type === 'API') {
      routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_API_DETAIL;
    } else {
      routeName.value = ProjectManagementRouteEnum.PROJECT_MANAGEMENT_TEMPLATE_MANAGEMENT_BUG_DETAIL;
    }
  });
</script>

<style scoped lang="less">
  .system-flag {
    font-size: 12px;
    background: var(--color-text-n8);
    line-height: 20px;
    @apply ml-2 rounded p-1;
  }
</style>
