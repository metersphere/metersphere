<template>
  <MsBaseTable v-bind="propsRes" ref="tableRef" v-on="propsEvent">
    <template #index="{ rowIndex }">
      <div class="circle text-xs font-medium"> {{ rowIndex + 1 }}</div>
    </template>
    <template #caseStep="{ record }">
      <a-textarea
        v-if="record.showStep"
        :ref="(el: refItem) => setStepRefMap(el, record)"
        v-model="record.step"
        size="mini"
        :auto-size="true"
        class="w-max-[267px]"
        :placeholder="t('system.orgTemplate.stepTip')"
        @blur="blurHandler(record, 'step')"
      />
      <div v-else-if="record.step && !record.showStep" class="w-full cursor-pointer" @click="edit(record, 'step')">{{
        record.step
      }}</div>
      <div
        v-else-if="!record.caseStep && !record.showStep"
        class="placeholder w-full cursor-pointer text-[var(--color-text-brand)]"
        @click="edit(record, 'step')"
        >{{ t('system.orgTemplate.stepTip') }}</div
      >
    </template>
    <template #expectedResult="{ record }">
      <a-textarea
        v-if="record.showExpected"
        :ref="(el: refItem) => setExpectedRefMap(el, record)"
        v-model="record.expected"
        :max-length="1000"
        size="mini"
        :auto-size="true"
        class="w-max-[267px]"
        :placeholder="t('system.orgTemplate.expectationTip')"
        @blur="blurHandler(record, 'expected')"
      />
      <div
        v-else-if="record.expected && !record.showExpected"
        class="w-full cursor-pointer"
        @click="edit(record, 'expected')"
        >{{ record.expected }}</div
      >
      <div
        v-else-if="!record.expected && !record.showExpected"
        class="placeholder w-full cursor-pointer text-[var(--color-text-brand)]"
        @click="edit(record, 'expected')"
        >{{ t('system.orgTemplate.expectationTip') }}</div
      >
    </template>
    <template #operation="{ record }">
      <MsTableMoreAction
        v-if="!record.internal"
        :list="moreActions"
        @select="(item:ActionsItem) => handleMoreActionSelect(item,record)"
      />
    </template>
  </MsBaseTable>
  <a-button v-if="!props.isDisabled" class="mt-2 px-0" type="text" @click="addStep">
    <template #icon>
      <icon-plus class="text-[14px]" />
    </template>
    {{ t('system.orgTemplate.addStep') }}
  </a-button>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  import type { StepList } from '@/models/caseManagement/featureCase';
  import { TableKeyEnum } from '@/enums/tableEnum';

  type refItem = Element | ComponentPublicInstance | null;
  const { t } = useI18n();

  const props = withDefaults(
    defineProps<{
      stepList: any;
      isDisabled?: boolean;
    }>(),
    {
      isDisabled: false,
    }
  );

  const emit = defineEmits(['update:stepList']);

  const templateFieldColumns = ref<MsTableColumn>([
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
  ]);

  const moreActions: ActionsItem[] = [
    {
      label: 'caseManagement.featureCase.copyStep',
      eventTag: 'copyStep',
    },
    {
      label: 'caseManagement.featureCase.InsertStepsBefore',
      eventTag: 'InsertStepsBefore',
    },
    {
      label: 'caseManagement.featureCase.afterInsertingSteps',
      eventTag: 'afterInsertingSteps',
    },
    {
      isDivider: true,
    },
    {
      label: 'common.delete',
      danger: true,
      eventTag: 'delete',
    },
  ];

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_DETAIL_TABLE,
    columns: templateFieldColumns.value,
    scroll: { x: '100%', y: 400 },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
    showPagination: false,
    enableDrag: true,
  });

  // 步骤描述
  const stepData = ref<StepList[]>([
    {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    },
  ]);

  // 复制步骤
  function copyStep(record: StepList) {
    stepData.value.push({
      ...record,
      id: getGenerateId(),
    });
  }

  // 删除步骤
  function deleteStep(record: StepList) {
    stepData.value = stepData.value.filter((item: any) => item.id !== record.id);
    setProps({ data: stepData.value });
  }

  // 步骤之前插入步骤
  function insertStepsBefore(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index, 0, insertItem);
  }

  // 步骤之后插入步骤
  function afterInsertingSteps(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    };
    stepData.value.splice(index + 1, 0, insertItem);
  }

  // 更多操作
  const handleMoreActionSelect = (item: ActionsItem, record: StepList) => {
    switch (item.eventTag) {
      case 'copyStep':
        copyStep(record);
        break;
      case 'InsertStepsBefore':
        insertStepsBefore(record);
        break;
      case 'afterInsertingSteps':
        afterInsertingSteps(record);
        break;
      default:
        deleteStep(record);
        break;
    }
  };

  // 添加步骤
  const addStep = () => {
    stepData.value.push({
      id: getGenerateId(),
      step: '',
      expected: '',
      showStep: false,
      showExpected: false,
    });
  };

  const refStepMap: Record<string, any> = {};
  function setStepRefMap(el: refItem, record: StepList) {
    if (el) {
      refStepMap[`${record.id}`] = el;
    }
  }
  const expectedRefMap: Record<string, any> = {};

  function setExpectedRefMap(el: refItem, record: StepList) {
    if (el) {
      expectedRefMap[`${record.id}`] = el;
    }
  }
  // 编辑步骤
  function edit(record: StepList, type: string) {
    if (props.isDisabled) return;
    if (type === 'step') {
      record.showStep = true;
      nextTick(() => {
        refStepMap[record.id]?.focus();
      });
    } else {
      record.showExpected = true;
      nextTick(() => {
        expectedRefMap[record.id]?.focus();
      });
    }
  }

  // 失去焦点回调
  function blurHandler(record: StepList, type: string) {
    if (props.isDisabled) return;
    if (type === 'step') {
      record.showStep = false;
    } else {
      record.showExpected = false;
    }
  }

  const tableRef = ref<InstanceType<typeof MsBaseTable> | null>(null);

  watchEffect(() => {
    if (props.isDisabled) {
      tableRef.value?.initColumn(templateFieldColumns.value.slice(0, templateFieldColumns.value.length - 1));
    } else {
      tableRef.value?.initColumn(templateFieldColumns.value);
    }
  });

  watch(
    () => stepData.value,
    (val) => {
      emit('update:stepList', val);
      setProps({ data: stepData.value });
    },
    { deep: true }
  );

  watch(
    () => props.stepList,
    () => {
      stepData.value = props.stepList;
    },
    {
      immediate: true,
    }
  );

  onBeforeMount(() => {
    setProps({ data: stepData.value });
  });
</script>

<style scoped lang="less">
  .circle {
    width: 16px;
    height: 16px;
    line-height: 16px;
    border-radius: 50%;
    text-align: center;
    color: var(--color-text-4);
    background: var(--color-text-n8);
  }
</style>
