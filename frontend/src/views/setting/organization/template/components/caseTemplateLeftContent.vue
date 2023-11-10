<template>
  <a-form ref="viewFormRef" class="rounded-[4px]" :model="viewForm" layout="vertical">
    <a-form-item
      field="caseName"
      :label="t('system.orgTemplate.caseName')"
      :rules="[{ required: true, message: t('system.orgTemplate.caseNamePlaceholder') }]"
      required
      asterisk-position="end"
    >
      <a-input
        v-model="viewForm.name"
        :max-length="255"
        :placeholder="t('system.orgTemplate.caseNamePlaceholder')"
        show-word-limit
        allow-clear
        :disabled="isDisabled"
      ></a-input>
    </a-form-item>
    <a-form-item field="precondition" :label="t('system.orgTemplate.precondition')" asterisk-position="end">
      <MsRichText v-model="viewForm.precondition" />
    </a-form-item>
    <a-form-item field="step" :label="t('system.orgTemplate.stepDescription')" class="relative">
      <div class="absolute left-16 top-0">
        <a-divider direction="vertical" />
        <a-dropdown :popup-max-height="false" @select="handleSelectType">
          <span class="text-[14px] text-[var(--color-text-4)]"
            >{{ t('system.orgTemplate.changeType') }} <icon-down
          /></span>
          <template #content>
            <a-doption> {{ t('system.orgTemplate.stepDescription') }}</a-doption>
            <a-doption>{{ t('system.orgTemplate.textDescription') }}</a-doption>
          </template>
        </a-dropdown>
      </div>
      <!-- 步骤描述 -->
      <div class="w-full">
        <MsBaseTable v-bind="propsRes" ref="stepTableRef" v-on="propsEvent">
          <template #index="{ rowIndex }">
            {{ rowIndex + 1 }}
          </template>
          <template #caseStep="{ record }">
            <a-input v-if="record.showStep" v-model="record.caseStep" class="w-max-[267px]" />
            <span v-else-if="record.caseStep && !record.showStep">{{ record.caseStep }}</span>
            <span v-else-if="!record.caseStep && !record.showStep" class="placeholder text-[var(--color-text-brand)]">{{
              t('system.orgTemplate.stepTip')
            }}</span>
          </template>
          <template #expectedResult="{ record }">
            <a-input v-if="record.showExpected" v-model="record.expectedResult" class="w-max-[267px]" />
            <span v-else-if="record.expectedResult && !record.showExpected">{{ record.caseStep }}</span>
            <span
              v-else-if="!record.expectedResult && !record.showExpected"
              class="placeholder text-[var(--color-text-brand)]"
              >{{ t('system.orgTemplate.expectationTip') }}</span
            >
          </template>
          <template #operation="{ record }">
            <MsTableMoreAction
              v-if="!record.internal"
              :list="moreActions"
              @select="(item:ActionsItem) => handleMoreActionSelect(item)"
            />
          </template>
        </MsBaseTable>
      </div>
      <a-button class="mt-2 px-0" type="text" :disabled="isDisabled" @click="addStep">
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
          ><a-button type="outline" :disabled="isDisabled">
            <template #icon> <icon-plus class="text-[14px]" /> </template
            >{{ t('system.orgTemplate.addAttachment') }}</a-button
          >
        </div>
        <div class="text-[var(--color-text-4)]">{{ t('system.orgTemplate.addAttachmentTip') }}</div>
      </div>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-组织-模板管理-用例模板左侧内容
   */
  import { ref } from 'vue';

  import MsRichText from '@/components/pure/ms-rich-text/MsRichText.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';

  import { TableKeyEnum } from '@/enums/tableEnum';

  const isDisabled = ref<boolean>(true);

  const { t } = useI18n();

  const viewForm = ref({
    name: '',
    precondition: '',
    value: '',
    remark: '',
  });

  const handleSelectType = () => {};

  const moreActions: ActionsItem[] = [
    {
      label: 'system.orgTemplate.copy',
      eventTag: 'copy',
      disabled: isDisabled.value,
    },
    {
      label: 'system.orgTemplate.delete',
      danger: true,
      eventTag: 'delete',
      disabled: isDisabled.value,
    },
  ];

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

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.ORGANIZATION_TEMPLATE_MANAGEMENT_STEP,
    columns: templateFieldColumns,
    scroll: { x: '800px' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: true,
    showPagination: false,
    enableDrag: false,
  });

  const addStep = () => {};
  const handlerDelete = () => {};

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem) => {
    if (item.eventTag === 'delete') {
      handlerDelete();
    }
  };
  onMounted(() => {
    setProps({ data: [{ id: 1, showStep: false, showExpected: false }] });
  });
</script>

<style scoped></style>
