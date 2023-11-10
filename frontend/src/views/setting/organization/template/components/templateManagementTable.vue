<template>
  <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
    <template #name="{ record }">
      <MsIcon v-if="!record.internal" :type="getIconType(record.type)?.iconName || ''" size="16" />
      <span class="ml-2">{{ record.name }}</span>
      <MsTag v-if="record.internal" size="small" class="ml-2">{{ t('system.orgTemplate.isSystem') }}</MsTag>
    </template>
    <template #apiFieldId="{ record }">
      <a-input
        v-model="record.apiFieldId"
        class="min-w-[200px] max-w-[300px]"
        :placeholder="t('system.orgTemplate.apiInputPlaceholder')"
        allow-clear
      />
    </template>
    <template #defaultValue="{ record }">
      <div class="form-create-wrapper w-full">
        <MsFormCreate v-model:api="record.fApi" :rule="record.formRules" :option="configOptions" />
      </div>
    </template>
    <template #required="{ record }">
      <a-checkbox v-model="record.required"></a-checkbox>
    </template>
    <template #operation="{ record }">
      <div class="flex flex-row flex-nowrap">
        <MsButton class="!mr-0" @click="editField(record)">{{ t('system.orgTemplate.edit') }}</MsButton>
        <a-divider v-if="!record.internal" direction="vertical" />
        <MsButton v-if="!record.internal" class="!mr-0" @click="deleteSelectedField(record)">{{
          t('system.orgTemplate.delete')
        }}</MsButton>
      </div>
    </template>
  </MsBaseTable>
  <!-- 添加字段到模版抽屉 -->
  <AddFieldToTemplateDrawer
    ref="fieldSelectRef"
    v-model:visible="showDrawer"
    :total-data="(totalData as DefinedFieldItem[])"
    :table-select-data="(selectList as DefinedFieldItem[])"
    :mode="props.mode"
    @confirm="confirmHandler"
    @update-data="updateFieldHandler"
  />
  <a-button class="mt-3 px-0" type="text" @click="addField">
    <template #icon>
      <icon-plus class="text-[14px]" />
    </template>
    {{ t('system.orgTemplate.createField') }}
  </a-button>
  <EditFieldDrawer
    ref="fieldDrawerRef"
    v-model:visible="showFieldDrawer"
    :mode="props.mode"
    @success="updateFieldHandler"
  />
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-创建模板-非缺陷模板自定义字段表格
   */
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import AddFieldToTemplateDrawer from './addFieldToTemplateDrawer.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { useI18n } from '@/hooks/useI18n';

  import type { DefinedFieldItem } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getIconType } from './fieldSetting';

  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      mode: 'organization' | 'project';
      enableThirdPart: boolean; // 是否对接第三方平台
      data: DefinedFieldItem[]; // 总字段数据
      selectData: Record<string, any>[]; // 选择数据
    }>(),
    {
      enableThirdPart: false,
    }
  );

  const emit = defineEmits(['update:select-data', 'update']);

  const route = useRoute();

  const columns: MsTableColumn = [
    {
      title: 'system.orgTemplate.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      fixed: 'left',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
    },
    {
      title: 'system.orgTemplate.defaultValue',
      dataIndex: 'defaultValue',
      slotName: 'defaultValue',
      width: 300,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.required',
      dataIndex: 'required',
      slotName: 'required',
      width: 180,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.description',
      dataIndex: 'remark',
      showDrag: true,
      showInTable: true,
      showTooltip: true,
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

  function getApiColumns() {
    return {
      title: 'system.orgTemplate.apiFieldId',
      dataIndex: 'apiFieldId',
      slotName: 'apiFieldId',
      showDrag: true,
      showInTable: true,
      width: 300,
    };
  }

  const tableRef = ref();

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_FIELD,
    columns,
    scroll: { x: '1800px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
    showPagination: false,
    enableDrag: true,
  });

  const configOptions = ref({
    resetBtn: false,
    submitBtn: false,
    on: false,
    form: {
      layout: 'vertical',
      labelAlign: 'left',
    },
    row: {
      gutter: 0,
    },
    wrap: {
      'asterisk-position': 'end',
      'validate-trigger': ['change'],
      'hide-label': true,
      'hide-asterisk': true,
    },
  });
  const showDrawer = ref<boolean>(false);
  const totalData = ref<DefinedFieldItem[]>([]);

  watchEffect(() => {
    totalData.value = props.data;
  });

  const selectList = ref<DefinedFieldItem[]>([]);

  // 确定处理字段表单数据
  const confirmHandler = (dataList: DefinedFieldItem[]) => {
    selectList.value = dataList;
  };

  const showFieldDrawer = ref<boolean>(false);
  const fieldDrawerRef = ref();

  // 编辑字段
  const editField = (record: DefinedFieldItem) => {
    showFieldDrawer.value = true;
    fieldDrawerRef.value.editHandler(record);
  };

  // 添加字段
  const addField = async () => {
    showDrawer.value = true;
  };

  // 编辑更新已选择字段
  const updateFieldHandler = async (isEdit: boolean) => {
    emit('update', isEdit);
  };

  // 删除已选择字段
  const deleteSelectedField = (record: DefinedFieldItem) => {
    selectList.value = selectList.value.filter((item) => item.id !== record.id);
  };

  watch(
    () => selectList.value,
    (val) => {
      if (val) {
        emit('update:select-data', val);
      }
    },
    { deep: true }
  );

  watch(
    () => props.selectData,
    (val) => {
      if (val) {
        selectList.value = val as DefinedFieldItem[];
        setProps({ data: selectList.value });
      }
    },
    { immediate: true }
  );

  watch(
    () => props.data,
    (val) => {
      totalData.value = val;
    }
  );

  // 是否开启三方API
  watch(
    () => props.enableThirdPart,
    (val) => {
      if (val && route.query.type === 'BUG') {
        const result = [...columns.slice(0, 1), getApiColumns(), ...columns.slice(1)];
        tableRef.value.initColumn(result);
      } else {
        tableRef.value.initColumn(columns);
      }
    }
  );

  // 获取拖拽数据
  const dragTableData = computed(() => {
    return propsRes.value.data;
  });

  watch(
    () => dragTableData.value,
    (val) => {
      selectList.value = dragTableData.value as DefinedFieldItem[];
      emit('update:select-data', val);
    }
  );
</script>

<style scoped lang="less">
  .form-create-wrapper {
    :deep(.arco-form-item) {
      margin-bottom: 0 !important;
    }
    :deep(.arco-picker) {
      width: 100% !important;
    }
  }
  .system-flag {
    background: var(--color-text-n8);
    @apply ml-2 rounded p-1 text-xs;
  }
</style>
