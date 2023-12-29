<template>
  <a-form ref="formRef" :model="formModel" layout="vertical">
    <div class="w-full overflow-y-auto bg-[var(--color-text-n9)] px-[12px] py-[12px]">
      <header class="flex flex-row items-center justify-between">
        <div class="text-[var(--color-text-2)]">{{ t('advanceFilter.setFilterCondition') }}</div>
        <div class="flex flex-row items-center text-[var(--color-text-2)]">
          <div class="text-[var(--color-text-2)]">{{ t('advanceFilter.accordBelow') }}</div>
          <div class="ml-[16px]">
            <a-select v-model:model-value="accordBelow" size="small">
              <a-option value="AND">{{ t('advanceFilter.all') }}</a-option>
              <a-option value="OR">{{ t('advanceFilter.any') }}</a-option>
            </a-select>
          </div>
          <div class="ml-[8px] text-[var(--color-text-2)]">{{ t('advanceFilter.condition') }}</div>
        </div>
      </header>
      <a-scrollbar :style="{ 'max-height': '300px', 'overflow': 'auto' }">
        <article class="mt-[12px] flex max-h-[300px] flex-col gap-[8px]">
          <section
            v-for="(item, idx) in formModel.list"
            :key="item.dataIndex || `filter_item_${idx}`"
            class="flex flex-row items-center gap-[8px]"
          >
            <div class="flex-1 grow">
              <a-form-item
                :field="`list[${idx}].dataIndex`"
                hide-asterisk
                class="hidden-item"
                :rules="[{ required: true, message: t('advanceFilter.plaseSelectFilterDataIndex') }]"
              >
                <a-select v-model="item.dataIndex" allow-search @change="(v) => dataIndexChange(v, idx)">
                  <div v-for="(option, i) in currentOptionArr" :key="option.dataIndex">
                    <a-option :value="option.dataIndex" :disabled="option.disabled">
                      {{ t(option.title as string) }}
                    </a-option>
                    <a-divider
                      v-if="
                        props?.customList &&
                        (props.customList || []).length &&
                        (props.configList || []).length - 1 === i
                      "
                      class="!my-1"
                    />
                  </div>
                </a-select>
              </a-form-item>
            </div>
            <div class="grow-0">
              <a-form-item
                :field="`list[${idx}].operator`"
                hide-asterisk
                class="hidden-item"
                :rules="[{ required: true, message: t('advanceFilter.plaseSelectOperator') }]"
              >
                <a-select
                  v-model="item.operator"
                  class="w-[120px]"
                  :disabled="!item.dataIndex"
                  @change="(v) => operationChange(v, item.dataIndex as string, idx)"
                >
                  <a-option
                    v-for="option in getOperationOption(item.type as FilterType, item.dataIndex as string)"
                    :key="option.value"
                    :value="option.value"
                  >
                    {{ t(option.label as string) }}
                  </a-option>
                </a-select>
              </a-form-item>
            </div>
            <div class="flex-1 grow">
              <a-form-item
                :field="`list[${idx}].value`"
                :rules="[{ required: true, message: t('advanceFilter.plaseInputFilterContent') }]"
                hide-asterisk
                class="hidden-item"
              >
                <a-input
                  v-if="item.type === FilterType.INPUT"
                  v-model:model-value="item.value"
                  class="w-full"
                  allow-clear
                  :disabled="!item.dataIndex"
                  :max-length="60"
                />
                <MsTagsInput
                  v-else-if="item.type === FilterType.TAGS_INPUT"
                  v-model:model-value="item.value"
                  :disabled="!item.dataIndex"
                  allow-clear
                  unique-value
                  retain-input-value
                />
                <a-input-number
                  v-else-if="item.type === FilterType.NUMBER"
                  v-model:model-value="item.value"
                  class="w-full"
                  allow-clear
                  :disabled="!item.dataIndex"
                  :max-length="60"
                />
                <MsSelect
                  v-else-if="item.type === FilterType.SELECT"
                  v-model:model-value="item.value"
                  class="w-full"
                  allow-clear
                  allow-search
                  :placeholder="t('common.pleaseSelect')"
                  :disabled="!item.dataIndex"
                  :options="item.selectProps?.options || []"
                  v-bind="item.selectProps"
                />
                <a-tree-select
                  v-else-if="item.type === FilterType.TREE_SELECT"
                  v-model:model-value="item.value"
                  :data="item.treeSelectData"
                  :disabled="!item.dataIndex"
                  v-bind="(item.treeSelectProps as any)"
                />
                <a-date-picker
                  v-else-if="item.type === FilterType.DATE_PICKER && item.operator !== 'between'"
                  v-model:model-value="item.value"
                  class="w-full"
                  show-time
                  format="YYYY-MM-DD hh:mm"
                  :disabled="!item.dataIndex"
                />
                <a-range-picker
                  v-else-if="item.type === FilterType.DATE_PICKER && item.operator === 'between'"
                  v-model:model-value="item.value"
                  class="w-full"
                  show-time
                  format="YYYY-MM-DD HH:mm"
                  :disabled="!item.dataIndex"
                />
                <MsCascader
                  v-else-if="item.type === FilterType.CASCADER"
                  v-model:model-value="item.value"
                  :options="item.cascaderOptions || []"
                  :disabled="!item.dataIndex"
                  v-bind="item.cascaderProps"
                />
                <a-textarea
                  v-else-if="item.type === FilterType.TEXTAREA"
                  v-model:model-value="item.value"
                  class="w-full"
                  allow-clear
                  :disabled="!item.dataIndex"
                  :auto-size="{
                    minRows: 1,
                    maxRows: 1,
                  }"
                  show-word-limit
                  :max-length="512"
                />
                <a-radio-group
                  v-else-if="item.type === FilterType.RADIO"
                  v-model:model-value="item.value"
                  v-bind="(item.radioProps as any)"
                >
                  <a-radio
                    v-for="it of item.radioProps?.options || []"
                    :key="it[item.radioProps?.valueKey || 'value']"
                    :value="it[item.radioProps?.valueKey || 'value']"
                    >{{ it[item.radioProps?.labelKey || 'label'] }}</a-radio
                  >
                </a-radio-group>
                <a-checkbox-group v-else-if="item.type === FilterType.CHECKBOX" v-model:model-value="item.value">
                  <a-checkbox
                    v-for="it of item.checkProps?.options || []"
                    :key="it[item.checkProps?.valueKey || 'value']"
                    :value="it[item.checkProps?.valueKey || 'value']"
                    >{{ it[item.checkProps?.labelKey || 'label'] }}</a-checkbox
                  >
                </a-checkbox-group>
              </a-form-item>
            </div>
            <div class="delete-btn" :class="{ 'delete-btn:disabled': idx === 0 }" @click="handleDeleteItem(idx)">
              <icon-minus-circle />
            </div>
          </section>
        </article>
      </a-scrollbar>
      <footer
        class="mt-[12px] flex flex-row items-center justify-between"
        :class="{ '!justify-end': !showAddCondition }"
      >
        <div
          v-if="showAddCondition"
          class="flex cursor-pointer items-center gap-[4px] text-[rgb(var(--primary-7))]"
          @click="handleAddItem"
        >
          <icon-plus />
          <span>{{ t('advanceFilter.addCondition') }}</span>
        </div>
        <div>
          <a-button class="mr-[8px]" @click="handleReset">{{ t('advanceFilter.reset') }}</a-button>
          <a-button type="primary" @click="handleFilter">{{ t('advanceFilter.filter') }}</a-button>
        </div>
      </footer>
    </div>
  </a-form>
</template>

<script lang="ts" setup>
  import { FormInstance } from '@arco-design/web-vue';

  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { OptionsItem } from '@/models/setting/log';

  import { OPERATOR_MAP } from './index';
  import { AccordBelowType, BackEndEnum, CombineItem, FilterFormItem, FilterResult, FilterType } from './type';

  const { t } = useI18n();
  const accordBelow = ref<AccordBelowType>('AND');
  const formRef = ref<FormInstance | null>(null);
  const formModel = reactive<{ list: FilterFormItem[] }>({
    list: [],
  });
  const props = defineProps<{
    configList: FilterFormItem[]; // 系统字段
    customList?: FilterFormItem[]; // 自定义字段
    visible: boolean;
    count: number;
    rowCount: number;
  }>();
  const emit = defineEmits<{
    (e: 'onSearch', value: FilterResult): void;
    (e: 'dataIndexChange', value: string): void;
    (e: 'update:count', value: number): void; // 用于展示 FilterIcon 的数量
    (e: 'update:rowCount', value: number): void; // 用于展示 MsBaseTable 的总行数
  }>();

  const isMultipleSelect = (dataIndex: string) => {
    const tmpObj = [...props.configList, ...(props.customList || [])].find((item) => item.dataIndex === dataIndex);
    if (tmpObj) {
      return tmpObj.selectProps?.multiple || tmpObj.type === FilterType.TAGS_INPUT;
    }
    return false;
  };

  const getOperationOption = (type: FilterType, dataIndex: string) => {
    let result: { label: string; value: string }[] = [];
    switch (type) {
      case FilterType.NUMBER:
        result = OPERATOR_MAP.number;
        break;
      case FilterType.DATE_PICKER:
        result = OPERATOR_MAP.date;
        break;
      case FilterType.SELECT:
        result = isMultipleSelect(dataIndex) ? OPERATOR_MAP.array : OPERATOR_MAP.string;
        break;
      default:
        result = OPERATOR_MAP.string;
    }
    return result;
  };

  // 获取当前可选的选项
  const getCurrentOptionArr = () => {
    const arr1 = [...props.configList, ...(props?.customList || [])];
    const arr2 = formModel.list.map((item) => item.dataIndex);
    const intersection = arr1.map((item1) => ({
      ...item1,
      disabled: arr2.includes(item1.dataIndex),
    }));
    return intersection;
  };

  const currentOptionArr = computed(() => getCurrentOptionArr());

  // 是否显示添加条件按钮
  const showAddCondition = computed(() => {
    return currentOptionArr.value.some((item) => !item.disabled);
  });

  const getInitItem = () => {
    return {
      dataIndex: '',
      type: FilterType.INPUT,
      operator: '',
      value: '',
      backendType: BackEndEnum.STRING,
    };
  };

  /**
   * @description 添加条件
   */
  const handleAddItem = async () => {
    formRef.value?.validate((errors) => {
      if (!errors) {
        formModel.list.push(getInitItem());
      }
    });
  };
  /**
   * @description 删除条件
   */
  const handleDeleteItem = (index: number) => {
    if (index === 0) {
      return;
    }
    formModel.list.splice(index, 1);
  };
  /**
   * @description 重置
   */
  const handleReset = () => {
    formRef.value?.resetFields();
    formModel.list = [getInitItem()];
  };
  /**
   * @description 筛选
   */
  const handleFilter = () => {
    formRef.value?.validate((errors) => {
      if (!errors) {
        const tmpObj: FilterResult = { accordBelow: 'AND', combine: {} };
        const combine: CombineItem = {};
        formModel.list.forEach((item) => {
          combine[item.dataIndex as string] = {
            operator: item.operator,
            value: item.value,
            backendType: item.backendType,
          };
        });
        tmpObj.accordBelow = accordBelow.value;
        tmpObj.combine = combine;
        emit('onSearch', tmpObj);
      }
    });
  };

  const getAttributeByDataIndex = (dataIndex: string) => {
    return [...props.configList, ...(props.customList || [])].find((item) => item.dataIndex === dataIndex);
  };

  /**
   * @description 筛选项变化
   */
  const dataIndexChange = (dataIndex: SelectValue, idx: number) => {
    if (!dataIndex) {
      return;
    }
    const tmpObj = getAttributeByDataIndex(dataIndex as string);
    if (!tmpObj) {
      return;
    }
    formModel.list[idx] = { ...tmpObj };
    formModel.list[idx].value = isMultipleSelect(dataIndex as string) ? [] : '';

    emit('dataIndexChange', dataIndex as string);
  };

  const operationChange = (v: SelectValue, dataIndex: string, idx: number) => {
    if (v === 'between') {
      formModel.list[idx].value = [];
    } else {
      formModel.list[idx].value = isMultipleSelect(dataIndex) ? [] : '';
    }
  };

  onBeforeMount(() => {
    formModel.list = [getInitItem()];
  });
  watch(
    () => props.visible,
    (val) => {
      if (!val) {
        emit('update:count', formModel.list.filter((item) => !!item.dataIndex).length);
      }
    }
  );
</script>

<style lang="less" scoped>
  .delete-btn {
    padding: 8px;
    width: 32px;
    height: 32px;
    border-radius: 4px;
    color: var(--color-text-4);
    cursor: pointer;
  }
  .delete-btn:hover {
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-9));
  }
  .delete-btn:disabled {
    @apply cursor-not-allowed hover:bg-transparent hover:text-[var(--color-text-4)];
  }
  :deep(.arco-form-item-layout-vertical > .arco-form-item-label-col) {
    margin-bottom: 0;
  }
  :deep(.arco-form-item.arco-form-item-error, .arco-form-item.arco-form-item-has-help) {
    position: relative;
    top: 10px;
  }
  :deep(.arco-scrollbar-track-direction-vertical .arco-scrollbar-thumb-bar) {
    margin-left: 8px;
  }
</style>
