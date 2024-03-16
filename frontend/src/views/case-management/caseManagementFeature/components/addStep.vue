<template>
  <MsBaseTable v-bind="propsRes" ref="tableRef" :hoverable="false" v-on="propsEvent" @change="changeHandler">
    <template #index="{ rowIndex }">
      <div class="circle text-xs font-medium"> {{ rowIndex + 1 }}</div>
    </template>
    <template #caseStep="{ record }">
      <!--         v-if="record.showStep" -->
      <a-textarea
        :ref="(el: refItem) => setStepRefMap(el, record)"
        v-model="record.step"
        size="mini"
        :max-length="1000"
        :auto-size="true"
        class="w-max-[267px] param-input"
        :placeholder="t('system.orgTemplate.stepTip')"
        @blur="blurHandler(record, 'step')"
      />
    </template>
    <template #expectedResult="{ record }">
      <a-textarea
        :ref="(el: refItem) => setExpectedRefMap(el, record)"
        v-model="record.expected"
        :max-length="1000"
        size="mini"
        :auto-size="true"
        class="w-max-[267px] param-input"
        :placeholder="t('system.orgTemplate.expectationTip')"
        @blur="blurHandler(record, 'expected')"
      />
    </template>
    <template #operation="{ record }">
      <MsTableMoreAction
        v-if="!record.internal"
        :list="moreActionList"
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
  import { TableChangeExtra, TableData } from '@arco-design/web-vue';

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
      isScrollY?: boolean;
    }>(),
    {
      isDisabled: false,
      isScrollY: true,
    }
  );

  const emit = defineEmits(['update:stepList']);

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

  const moreActionList = computed(() => {
    return stepData.value.length <= 1 ? moreActions.slice(0, moreActions.length - 2) : moreActions;
  });

  const { propsRes, propsEvent, setProps } = useTable(undefined, {
    tableKey: TableKeyEnum.CASE_MANAGEMENT_DETAIL_TABLE,
    columns: templateFieldColumns.value,
    scroll: { x: '100%', y: props.isScrollY ? 400 : '' },
    selectable: false,
    noDisable: true,
    size: 'default',
    showSetting: false,
    showPagination: false,
    enableDrag: true,
  });

  // 复制步骤
  function copyStep(record: StepList) {
    const index = stepData.value.map((item: any) => item.id).indexOf(record.id);
    const insertItem = {
      ...record,
      id: getGenerateId(),
    };
    stepData.value.splice(index + 1, 0, insertItem);
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

  function changeHandler(data: TableData[], extra: TableChangeExtra, currentData: TableData[]) {
    if (!currentData || currentData.length === 1) {
      return false;
    }
    stepData.value = data as StepList[];
  }

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
  :deep(.param-input:not(.arco-input-focus, .arco-select-view-focus)) {
    &:not(:hover) {
      border-color: transparent !important;
      .arco-input::placeholder {
        @apply invisible;
      }
      .arco-select-view-icon {
        @apply invisible;
      }
      .arco-select-view-value {
        color: var(--color-text-brand);
      }
    }
  }
</style>
