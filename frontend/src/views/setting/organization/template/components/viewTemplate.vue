<template>
  <div class="wrapper-preview">
    <div class="preview-left pr-4">
      <a-form ref="viewFormRef" class="rounded-[4px]" :model="viewForm" layout="vertical">
        <a-form-item
          field="caseName"
          label="用例名称"
          :rules="[{ required: true, message: '请输入用例名称' }]"
          required
          asterisk-position="end"
        >
          <a-input
            v-model="viewForm.name"
            :max-length="255"
            placeholder="请输入用例名称"
            show-word-limit
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item field="precondition" label="前置条件" asterisk-position="end">
          <MsRichText v-model="viewForm.precondition" />
        </a-form-item>
        <a-form-item field="step" label="步骤描述" class="relative">
          <div class="absolute left-16 top-0">
            <a-divider direction="vertical" />
            <a-dropdown :popup-max-height="false" @select="handleSelectType">
              <span class="text-[14px] text-[var(--color-text-4)]">更改类型 <icon-down /></span>
              <template #content>
                <a-doption> 步骤描述</a-doption>
                <a-doption>文本描述</a-doption>
              </template>
            </a-dropdown>
          </div>
          <!-- 步骤描述 -->
          <div>
            <MsBaseTable v-bind="propsRes" ref="stepTableRef" v-on="propsEvent">
              <template #index="{ rowIndex }">
                {{ rowIndex + 1 }}
              </template>
              <template #caseStep="{ record }">
                <a-input v-if="record.showStep" v-model="record.caseStep" class="w-max-[267px]" />
                <span v-else-if="record.caseStep && !record.showStep">{{ record.caseStep }}</span>
                <span
                  v-else-if="!record.caseStep && !record.showStep"
                  class="placeholder text-[var(--color-text-brand)]"
                  >请输入步骤</span
                >
              </template>
              <template #expectedResult="{ record }">
                <a-input v-if="record.showExpected" v-model="record.expectedResult" class="w-max-[267px]" />
                <span v-else-if="record.expectedResult && !record.showExpected">{{ record.caseStep }}</span>
                <span
                  v-else-if="!record.expectedResult && !record.showExpected"
                  class="placeholder text-[var(--color-text-brand)]"
                  >请输入预期</span
                >
              </template>
              <template #operation="{ record }">
                <MsTableMoreAction
                  v-if="!record.internal"
                  :list="moreActions"
                  @select="(item) => handleMoreActionSelect(item)"
                />
              </template>
            </MsBaseTable>
          </div>
          <a-button class="mt-2 px-0" type="text" @click="addStep">
            <template #icon>
              <icon-plus class="text-[14px]" />
            </template>
            {{ t('system.orgTemplate.addStep') }}
          </a-button>
        </a-form-item>
        <a-form-item field="remark" label="备注"> <MsRichText v-model="viewForm.remark" /> </a-form-item>
        <a-form-item field="attachment" label="添加附件">
          <div class="flex flex-col">
            <div class="mb-1"
              ><a-button type="outline">
                <template #icon> <icon-plus class="text-[14px]" /> </template>添加附件</a-button
              >
            </div>
            <div class="text-[var(--color-text-4)]">支持任意类型文件，文件大小不超过 500MB</div>
          </div>
        </a-form-item>
      </a-form>
    </div>
    <div class="preview-right">
      <div v-for="item of props.selectField" :key="item.id">
        <MsFormCreate v-model:api="item.fApi" :rule="item.formRules" :option="options"
      /></div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';
  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';

  import type { DefinedFieldItem } from '@/models/setting/template';
  import { TableKeyEnum } from '@/enums/tableEnum';

  const { t } = useI18n();
  const tableStore = useTableStore();

  const props = defineProps<{
    selectField: DefinedFieldItem[];
  }>();

  const templateFieldColumns: MsTableColumn = [
    {
      title: 'system.orgTemplate.numberIndex',
      dataIndex: 'index',
      slotName: 'index',
      width: 100,
      showDrag: false,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.useCaseStep',
      slotName: 'caseStep',
      dataIndex: 'caseStep',
      showDrag: true,
      showInTable: true,
    },
    {
      title: 'system.orgTemplate.expectedResult',
      dataIndex: 'expectedResult',
      slotName: 'expectedResult',
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
  tableStore.initColumn(TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_STEP, templateFieldColumns, 'drawer');
  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_STEP,
    scroll: { x: '800px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    enableDrag: false,
  });

  const viewForm = ref({
    name: '',
    precondition: '',
    value: '',
    remark: '',
  });
  const options = ref({
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

  const handleSelectType = () => {};

  const stepTableRef = ref();

  const moreActions: ActionsItem[] = [
    {
      label: 'system.orgTemplate.copy',
      danger: true,
      eventTag: 'copy',
    },
    {
      label: 'system.orgTemplate.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const handlerDelete = () => {};
  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem) => {
    if (item.eventTag === 'delete') {
      handlerDelete();
    }
  };

  const addStep = () => {};

  onMounted(() => {
    setProps({ data: [{ id: 1, showStep: false, showExpected: false }] });
    console.log(props.selectField, '右侧表单');
  });
</script>

<style scoped lang="less">
  .wrapper-preview {
    display: flex;
    // height: calc(100vh - 100px);
    .preview-left {
      width: 100%;
      // height: 100%;
      border-right: 1px solid var(--color-text-n8);
    }
    .preview-right {
      width: 428px;
      // height: 100%;
    }
  }
</style>
