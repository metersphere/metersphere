<template>
  <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
    <template #name="{ record }">
      <MsIcon v-if="!record.internal" :type="getIconType(record.type)?.iconName || ''" size="16" />
      <span class="ml-2">{{ record.name }}</span>
      <span v-if="record.internal" class="system-flag">{{ t('system.orgTemplate.isSystem') }}</span>
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
        <MsButton class="!mr-0" @click="deleteSelectedField(record)">{{ t('system.orgTemplate.delete') }}</MsButton>
      </div>
    </template>
  </MsBaseTable>
  <!-- 添加字段到模版抽屉 -->
  <AddFieldToTemplateDrawer
    ref="fieldSelectRef"
    v-model:visible="showDrawer"
    :system-data="(systemData as DefinedFieldItem[])"
    :custom-data="(customData as DefinedFieldItem[])"
    :selected-data="(templateStore.previewList as DefinedFieldItem[])"
    @confirm="confirmHandler"
    @update="updateFieldHandler"
  />
  <a-button class="mt-3 px-0" type="text" @click="addField">
    <template #icon>
      <icon-plus class="text-[14px]" />
    </template>
    {{ t('system.orgTemplate.createField') }}
  </a-button>
  <EditFieldDrawer ref="fieldDrawerRef" v-model:visible="showFieldDrawer" @success="updateFieldHandler" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useRoute } from 'vue-router';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import { FieldTypeFormRules } from '@/components/pure/ms-form-create/form-create';
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import AddFieldToTemplateDrawer from './addFieldToTemplateDrawer.vue';
  import EditFieldDrawer from './editFieldDrawer.vue';

  import { getFieldList } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore, useTableStore } from '@/store';
  import useTemplateStore from '@/store/modules/setting/template';

  import type { DefinedFieldItem } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import { getIconType } from './fieldSetting';

  const tableStore = useTableStore();
  const { t } = useI18n();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const route = useRoute();

  const templateFieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 300,
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.defaultValue',
      dataIndex: 'defaultValue',
      slotName: 'defaultValue',
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

  tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_FIELD, templateFieldColumns, 'drawer');
  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_FIELD,
    scroll: { x: '1000px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    heightUsed: 560,
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

  const data = ref<DefinedFieldItem[]>([]);
  const systemData = ref<DefinedFieldItem[]>([]);
  const customData = ref<DefinedFieldItem[]>([]);
  const totalTemplateField = ref<DefinedFieldItem[]>([]);
  const currentOrd = appStore.currentOrgId;

  // 处理表单数据格式
  const getFieldOptionList = () => {
    totalTemplateField.value = totalTemplateField.value.map((item) => {
      const currentFormRules = FieldTypeFormRules[item.type];
      if (item.options && item.options.length) {
        currentFormRules.options = item.options.map((optionItem: any) => {
          return {
            label: optionItem.text,
            value: optionItem.value,
          };
        });
      }
      return {
        ...item,
        formRules: [currentFormRules],
        fApi: null,
        required: item.internal,
      };
    });
  };

  // 获取当前用例模版下所有已存在字段
  const getClassifyField = async () => {
    try {
      totalTemplateField.value = await getFieldList({ organizationId: currentOrd, scene: route.query.type });
      getFieldOptionList();
      systemData.value = totalTemplateField.value.filter((item: any) => item.internal);
      customData.value = totalTemplateField.value.filter((item: any) => !item.internal);
    } catch (error) {
      console.log(error);
    }
  };

  // 确定处理字段表单数据
  const confirmHandler = (dataList: DefinedFieldItem[]) => {
    data.value = dataList;
    setProps({ data: data.value });
  };

  const showFieldDrawer = ref<boolean>(false);
  const fieldDrawerRef = ref();

  // 编辑字段
  const editField = (record: DefinedFieldItem) => {
    showFieldDrawer.value = true;
    fieldDrawerRef.value.editHandler(record);
  };

  const fieldSelectRef = ref();
  // 添加字段
  const addField = async () => {
    showDrawer.value = true;
    await getClassifyField();
    fieldSelectRef.value.showSelectField();
  };

  // 编辑更新已选择字段
  const updateFieldHandler = async () => {
    await getClassifyField();
    data.value =
      totalTemplateField.value.filter((item) => data.value.some((dataItem) => item.id === dataItem.id)) || [];
    if (data.value.length > 0) {
      setProps({ data: data.value });
    }
  };

  // 删除已选择字段
  const deleteSelectedField = (record: DefinedFieldItem) => {
    data.value = data.value.filter((item) => item.id !== record.id);
    setProps({ data: data.value });
  };

  // 获取table内字段customFields
  const getCustomFields = () => {
    return data.value.map((item) => {
      return {
        fieldId: item.id,
        fieldName: item.name,
        required: item.required,
        apiFieldId: '',
        defaultValue: item.fApi.formData().fieldName,
      };
    });
  };
  // 外界获取表格字段进行存储
  const getSelectFiled = () => {
    return data.value;
  };
  defineExpose({
    getCustomFields,
    getSelectFiled,
  });

  watch(
    () => data.value,
    (val) => {
      templateStore.setPreviewHandler(val as DefinedFieldItem[]);
      data.value = val;
    }
  );

  onMounted(() => {
    data.value = templateStore.previewList;
    setProps({ data: data.value });
  });
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
</style>
