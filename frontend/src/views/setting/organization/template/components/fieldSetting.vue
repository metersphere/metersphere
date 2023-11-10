<template>
  <div>
    <a-alert class="mb-6" :type="isEnabledTemplate && props.mode === 'organization' ? 'warning' : 'info'">{{
      isEnabledTemplate && props.mode === 'organization'
        ? t('system.orgTemplate.enableDescription')
        : t('system.orgTemplate.fieldLimit')
    }}</a-alert>
    <div class="mb-4 flex items-center justify-between">
      <span v-if="isEnabledTemplate" class="font-medium">{{ t('system.orgTemplate.fieldList') }}</span>
      <a-button v-else type="primary" :disabled="isDisabled" @click="fieldHandler">
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
        <div class="flex items-center">
          <MsIcon
            v-if="!record.internal"
            :type="getIconType(record.type)?.iconName || ''"
            size="16"
            :class="{
              'text-[rgb(var(--primary-5))]': props.mode === 'project',
              'cursor-pointer': props.mode === 'project',
            }"
          />
          <span
            class="ml-2"
            :class="{
              'text-[rgb(var(--primary-5))]': props.mode === 'project',
              'cursor-pointer': props.mode === 'project',
            }"
            @click="showDetail(record)"
            >{{ record.name }}</span
          >
          <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isSystem') }}</MsTag></div
        >
      </template>
      <template #operation="{ record }">
        <div class="flex flex-row flex-nowrap items-center">
          <MsPopConfirm
            type="error"
            :title="t('system.orgTemplate.updateTip', { name: characterLimit(record.name) })"
            :sub-title-tip="t('system.orgTemplate.updateDescription')"
            :ok-text="t('system.orgTemplate.confirm')"
            @confirm="handleOk(record)"
          >
            <MsButton class="!mr-0">{{ t('system.orgTemplate.edit') }}</MsButton></MsPopConfirm
          >

          <a-divider v-if="!record.internal" class="h-[12px]" direction="vertical" />
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
    <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showDrawer" :mode="props.mode" @success="successHandler" />
    <MsDrawer
      ref="detailDrawerRef"
      v-model:visible="showDetailVisible"
      :width="480"
      :footer="false"
      :title="t('system.orgTemplate.filedDetail', { name: detailInfo?.name })"
    >
      <div class="p-4">
        <div class="flex">
          <span class="label">{{ t('system.orgTemplate.fieldName') }}</span>
          <span class="content">{{ detailInfo?.name }}</span>
        </div>
        <div class="flex">
          <span class="label">{{ t('system.orgTemplate.description') }}</span>
          <span class="content">{{ detailInfo?.remark || '-' }}</span>
        </div>
        <div class="flex">
          <span class="label">{{ t('system.orgTemplate.fieldType') }}</span>
          <span class="content">{{ detailInfo?.fieldType || '-' }}</span>
        </div>
        <div v-if="detailInfo?.options?.length" class="flex">
          <span class="label">{{ t('system.orgTemplate.optionContent') }}</span>
          <span class="content flex flex-col">
            <span v-for="item of detailInfo?.options" :key="item.value" class="flex">
              <MsTag class="!mr-2 mb-2">{{ item.text }}</MsTag>
              <MsTag v-if="detailInfo?.enableOptionKey" class="mb-2">{{ item.value }}</MsTag></span
            >
          </span>
        </div>
        <div v-if="detailInfo?.type === 'DATE' || detailInfo?.type === 'DATETIME'" class="flex">
          <span class="label">{{ t('system.orgTemplate.dateFormat') }}</span>
          <span class="content">
            {{
              detailInfo?.type === 'DATE' ? dayjs().format('YYYY/MM/DD') : dayjs().format('YYYY/MM/DD HH:mm:ss')
            }}</span
          >
        </div>
        <div v-if="detailInfo?.type === 'INT' || detailInfo?.type === 'FLOAT'" class="flex">
          <span class="label">{{ t('system.orgTemplate.numberFormat') }}</span>
          <span class="content">{{
            detailInfo?.type === 'INT' ? t('system.orgTemplate.int') : t('system.orgTemplate.float')
          }}</span>
        </div>
      </div>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 模版-字段列表
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsPopConfirm from '@/components/pure/ms-popconfirm/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';
  import { characterLimit } from '@/utils';

  import type { AddOrUpdateField, SeneType } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getFieldRequestApi, getIconType } from './fieldSetting';

  const templateStore = useTemplateStore();

  const { t } = useI18n();
  const tableStore = useTableStore();
  const appStore = useAppStore();
  const route = useRoute();
  const { openModal } = useModal();

  const props = defineProps<{
    mode: 'organization' | 'project';
  }>();

  const currentOrd = computed(() => appStore.currentOrgId);
  const currentProjectId = computed(() => appStore.currentProjectId);

  const fieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'system.orgTemplate.columnFieldType',
      dataIndex: 'type',
      slotName: 'fieldType',
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.columnFieldDescription',
      dataIndex: 'remark',
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.columnFieldUpdatedTime',
      dataIndex: 'updateTime',
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.operation',
      slotName: 'operation',
      dataIndex: 'operation',
      fixed: 'right',
      width: 200,
      showInTable: true,
    },
  ];

  const getList = getFieldRequestApi(props.mode).list;
  const { propsRes, propsEvent, loadList, setLoadListParams, setProps } = useTable(getList, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING,
    scroll: { x: '1000px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
    showPagination: false,
    heightUsed: 380,
  });

  const keyword = ref('');
  const totalData = ref([]);
  const scene = ref<SeneType>(route.query.type);

  const getParams = () => {
    scene.value = route.query.type;
    return {
      scene: scene.value,
      scopedId: props.mode === 'organization' ? currentOrd.value : currentProjectId.value,
    };
  };
  // 查询模板字段
  const searchFiled = async () => {
    try {
      totalData.value = await getList(getParams());
      const filterData = totalData.value.filter((item: AddOrUpdateField) => item.name.includes(keyword.value));
      setProps({ data: filterData });
    } catch (error) {
      console.log(error);
    }
  };

  // 获取字段列表数据
  const fetchData = async () => {
    setLoadListParams(getParams());
    await loadList();
    totalData.value = await getList(getParams());
  };

  const isDisabled = computed(() => {
    return totalData.value.length > 20;
  });

  const tableRef = ref();

  const isEnabledTemplate = computed(() => {
    return props.mode === 'organization'
      ? templateStore.projectStatus[scene.value as string]
      : !templateStore.projectStatus[scene.value as string];
  });

  // 切换模版是否启用展示操作列
  const isEnableOperation = () => {
    if (isEnabledTemplate.value) {
      const noOperationColumn = fieldColumns.slice(0, -1);
      tableRef.value.initColumn(noOperationColumn);
    } else {
      tableRef.value.initColumn(fieldColumns);
    }
  };

  const moreActions: ActionsItem[] = [
    {
      label: 'system.userGroup.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const deleteApi = getFieldRequestApi(props.mode).delete;
  // 删除字段
  const handlerDelete = (record: AddOrUpdateField) => {
    openModal({
      type: 'error',
      title: t('system.orgTemplate.deleteTitle', { name: characterLimit(record.name) }),
      content: t('system.orgTemplate.deleteFiledContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          if (record.id) await deleteApi(record.id);
          Message.success(t('system.orgTemplate.deleteSuccess'));
          fetchData();
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

  const showDetailVisible = ref<boolean>(false);
  const detailInfo = ref<AddOrUpdateField>();

  // 详情
  const showDetail = (record: AddOrUpdateField) => {
    if (props.mode === 'organization') return;

    showDetailVisible.value = true;
    let fieldType;
    if (record.type === 'MEMBER') {
      fieldType = getIconType(record.type)?.label;
    } else if (record.type === 'MULTIPLE_MEMBER') {
      fieldType = `${getIconType(record.type)?.label}（允许添加多个）`;
    } else {
      fieldType = getIconType(record.type)?.label;
    }

    detailInfo.value = { ...record, fieldType };
  };
  const showDrawer = ref<boolean>(false);

  const fieldDrawerRef = ref();
  const fieldHandler = () => {
    showDrawer.value = true;
  };

  const handleOk = (record: AddOrUpdateField) => {
    fieldDrawerRef.value.editHandler(record);
  };

  const successHandler = () => {
    fetchData();
  };

  onMounted(() => {
    isEnableOperation();
    fetchData();
  });
  await tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_FIELD_SETTING, fieldColumns, 'drawer');
</script>

<style scoped lang="less">
  .system-flag {
    background: var(--color-text-n8);
    @apply ml-2 inline-block p-1 align-middle text-xs;
  }
  .label {
    margin-top: 16px;
    width: 30%;
    color: var(--color-text-3);
  }
  .content {
    margin-top: 16px;
    width: 70%;
    color: var(--color-text-1);
  }
</style>
