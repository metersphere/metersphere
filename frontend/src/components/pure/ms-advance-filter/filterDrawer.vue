<template>
  <MsDrawer v-model:visible="visible" :width="800">
    <template #title>
      <a-input
        v-show="isShowNameInput"
        ref="nameInputRef"
        v-model:model-value="formModel.name"
        class="flex-1"
        :max-length="255"
        show-word-limit
        @blur="isShowNameInput = false"
      />
      <div v-show="!isShowNameInput" class="flex flex-1 items-center gap-[8px] overflow-hidden">
        <a-tooltip :content="formModel.name">
          <div class="one-line-text"> {{ formModel.name }}</div>
        </a-tooltip>
        <MsIcon
          type="icon-icon_edit_outlined"
          class="min-w-[16px] cursor-pointer hover:text-[rgb(var(--primary-5))]"
          @click="showNameInput"
        />
      </div>
    </template>
    <a-form ref="formRef" :model="formModel" layout="vertical">
      <a-select v-model="formModel.searchMode" :options="searchModeOptions" class="mb-[12px] w-[170px]">
        <template #prefix> {{ t('advanceFilter.meetTheFollowingConditions') }} </template>
      </a-select>
      <div
        v-for="(item, listIndex) in formModel.list"
        :key="item.dataIndex || `filter_item_${listIndex}`"
        class="flex items-start gap-[8px]"
      >
        <a-form-item
          class="flex-1 overflow-hidden"
          :field="`list[${listIndex}].dataIndex`"
          hide-asterisk
          :rules="[{ required: true, message: t('advanceFilter.conditionRequired') }]"
        >
          <a-select
            v-model="item.dataIndex"
            allow-search
            @change="(val: SelectValue) => dataIndexChange(val, listIndex)"
          >
            <div
              v-for="(option, currentOptionsIndex) in currentOptions(item.dataIndex as string)"
              :key="option.dataIndex"
            >
              <a-option :value="option.dataIndex">
                {{ t(option.title as string) }}
              </a-option>
              <a-divider
                v-if="(props?.customList || [])?.length && (props.configList || []).length - 1 === currentOptionsIndex"
                class="!my-1"
              />
            </div>
          </a-select>
        </a-form-item>
        <a-form-item :field="`list[${listIndex}].operator`" class="w-[120px]" hide-asterisk>
          <a-select v-model="item.operator" :disabled="!item.dataIndex" @change="operatorChange(item, listIndex)">
            <a-option v-for="option in operatorOptionsMap[item.type]" :key="option.value" :value="option.value">
              {{ t(option.label as string) }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          class="flex-1 overflow-hidden"
          :field="`list[${listIndex}].value`"
          hide-asterisk
          :rules="[
            {
              validator: (value, callback) => {
                validateFilterValue(item, value, callback);
              },
            },
          ]"
        >
          <a-input
            v-if="item.type === FilterType.INPUT"
            v-model:model-value="item.value"
            allow-clear
            :disabled="isValueDisabled(item)"
            :max-length="255"
            :placeholder="t('advanceFilter.inputPlaceholder')"
          />
          <a-textarea
            v-else-if="item.type === FilterType.TEXTAREA"
            v-model:model-value="item.value"
            allow-clear
            :disabled="isValueDisabled(item)"
            :auto-size="{
              minRows: 1,
              maxRows: 1,
            }"
            :placeholder="t('advanceFilter.inputPlaceholder')"
            :max-length="1000"
          />
          <MsTagsInput
            v-else-if="item.type === FilterType.TAGS_INPUT"
            v-model:model-value="item.value"
            :disabled="isValueDisabled(item)"
            allow-clear
            unique-value
            retain-input-value
          />
          <a-input-number
            v-else-if="item.type === FilterType.NUMBER"
            v-model:model-value="item.value"
            allow-clear
            :disabled="isValueDisabled(item)"
            :max-length="255"
            :placeholder="t('common.pleaseInput')"
          />
          <MsSelect
            v-else-if="item.type === FilterType.SELECT"
            v-model:model-value="item.value"
            allow-clear
            allow-search
            :placeholder="t('common.pleaseSelect')"
            :disabled="isValueDisabled(item)"
            :options="item.selectProps?.options || []"
            v-bind="item.selectProps"
          />
          <a-tree-select
            v-else-if="item.type === FilterType.TREE_SELECT"
            v-model:model-value="item.value"
            :data="item.treeSelectData"
            :disabled="isValueDisabled(item)"
            v-bind="item.treeSelectProps"
          >
            <template #tree-slot-title="node">
              <a-tooltip :content="`${node.name}`" position="tr">
                <div class="one-line-text max-w-[170px]">{{ node.name }}</div>
              </a-tooltip>
            </template>
          </a-tree-select>
          <a-date-picker
            v-else-if="item.type === FilterType.DATE_PICKER && item.operator !== OperatorEnum.BETWEEN"
            v-model:model-value="item.value"
            show-time
            format="YYYY-MM-DD hh:mm"
            :disabled="isValueDisabled(item)"
          />
          <a-range-picker
            v-else-if="item.type === FilterType.DATE_PICKER && item.operator === OperatorEnum.BETWEEN"
            v-model:model-value="item.value"
            show-time
            format="YYYY-MM-DD HH:mm"
            :separator="t('common.to')"
            :disabled="isValueDisabled(item)"
          />
          <a-radio-group
            v-else-if="item.type === FilterType.RADIO"
            v-model:model-value="item.value"
            :disabled="isValueDisabled(item)"
          >
            <a-radio
              v-for="it of item.radioProps?.options || []"
              :key="it[item.radioProps?.valueKey || 'value']"
              :value="it[item.radioProps?.valueKey || 'value']"
            >
              {{ it[item.radioProps?.labelKey || 'label'] }}
            </a-radio>
          </a-radio-group>
          <a-checkbox-group
            v-else-if="item.type === FilterType.CHECKBOX"
            v-model:model-value="item.value"
            :disabled="isValueDisabled(item)"
          >
            <a-checkbox
              v-for="it of item.checkProps?.options || []"
              :key="it[item.checkProps?.valueKey || 'value']"
              :value="it[item.checkProps?.valueKey || 'value']"
            >
              {{ it[item.checkProps?.labelKey || 'label'] }}
            </a-checkbox>
          </a-checkbox-group>
        </a-form-item>
        <a-button
          v-if="formModel.list.length > 1"
          type="outline"
          class="arco-btn-outline--secondary"
          @click="handleDeleteItem(listIndex)"
        >
          <template #icon> <MsIcon type="icon-icon_block_outlined" class="text-[var(--color-text-4)]" /> </template>
        </a-button>
      </div>
    </a-form>
    <MsButton type="text" class="mt-[5px]" @click="handleAddItem">
      <MsIcon type="icon-icon_add_outlined" class="mr-[3px]" />
      {{ t('advanceFilter.addCondition') }}
    </MsButton>
    <template #footer>
      <div v-show="!isSaveAsView" class="flex items-center gap-[8px]">
        <a-button type="primary" @click="handleFilter">{{ t('common.filter') }}</a-button>
        <a-button class="mr-[16px]" @click="handleReset">{{ t('common.reset') }}</a-button>
        <MsButton type="text" class="!text-[var(--color-text-1)]"> {{ t('common.save') }}</MsButton>
        <MsButton type="text" class="!text-[var(--color-text-1)]" @click="isSaveAsView = true">
          {{ t('advanceFilter.saveAsView') }}
        </MsButton>
      </div>
      <div v-show="isSaveAsView" class="flex items-center gap-[8px]">
        <a-input
          v-model:model-value="saveAsViewName"
          :placeholder="t('advanceFilter.viewNamePlaceholder')"
          class="w-[240px]"
          :max-length="255"
          show-word-limit
        />
        <a-button type="primary">{{ t('common.save') }}</a-button>
        <a-button @click="handleCancelSaveAsView">{{ t('common.cancel') }}</a-button>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { FormInstance, InputInstance } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { FilterType, OperatorEnum } from '@/enums/advancedFilterEnum';

  import { defaultFormModelList, operatorOptionsMap } from './index';
  import { AccordBelowType, FilterFormItem, FilterResult } from './type';

  const props = defineProps<{
    configList: FilterFormItem[]; // 系统字段
    customList?: FilterFormItem[]; // 自定义字段
  }>();
  const emit = defineEmits<{
    (e: 'handleFilter', value: FilterResult): void;
  }>();
  const visible = defineModel<boolean>('visible', { required: true });

  const { t } = useI18n();

  // TODO lmy 联调
  const formModel = ref<{ name: string; searchMode: AccordBelowType; list: FilterFormItem[] }>({
    name: '111',
    searchMode: 'AND',
    list: [...defaultFormModelList],
  });
  const savedFormModel = ref(cloneDeep(formModel.value));

  const isShowNameInput = ref(false);
  const nameInputRef = ref<InputInstance>();
  function showNameInput() {
    isShowNameInput.value = true;
    nextTick(() => {
      nameInputRef.value?.focus();
    });
  }

  const searchModeOptions = [
    { value: 'AND', label: t('advanceFilter.and') },
    { value: 'OR', label: t('advanceFilter.or') },
  ];

  const formRef = ref<FormInstance>();
  function validateFilterValue(item: FilterFormItem, value: string | undefined, callback: (error?: string) => void) {
    if (
      item.dataIndex?.length &&
      item.operator?.length &&
      !['EMPTY', 'NOT_EMPTY'].includes(item.operator as string) &&
      !value?.length
    ) {
      callback(t('advanceFilter.filterContentRequired'));
    }
  }
  function getListItemByDataIndex(dataIndex: string) {
    return [...props.configList, ...(props.customList || [])].find((item) => item.dataIndex === dataIndex);
  }
  // 第三列值是数组类型的
  function valueIsArray(listItem: FilterFormItem) {
    return (
      listItem.selectProps?.multiple ||
      [FilterType.CHECKBOX, FilterType.TAGS_INPUT].includes(listItem.type) ||
      (listItem.type === FilterType.DATE_PICKER && listItem.operator === OperatorEnum.BETWEEN)
    );
  }
  // 第一列下拉数据
  const currentOptions = computed(() => {
    return (currentDataIndex: string) => {
      const otherDataIndices = formModel.value.list
        .filter((listItem) => listItem.dataIndex !== currentDataIndex)
        .map((item: FilterFormItem) => item.dataIndex);
      return [...props.configList, ...(props.customList || [])]
        .filter(({ dataIndex }) => !otherDataIndices.includes(dataIndex))
        .map((item) => ({ ...item, label: t(item.title as string) }));
    };
  });
  // 改变第一列值
  function dataIndexChange(dataIndex: SelectValue, index: number) {
    const listItem = getListItemByDataIndex(dataIndex as string);
    if (!listItem) return;
    formModel.value.list[index] = { ...listItem };
    formModel.value.list[index].value = valueIsArray(listItem) ? [] : '';

    // 第二列默认：包含/属于/等于
    if (!formModel.value.list[index].operator?.length) {
      const optionsValueList = operatorOptionsMap[formModel.value.list[index].type].map(
        (optionItem) => optionItem.value
      );
      if (optionsValueList.includes(OperatorEnum.CONTAINS)) {
        formModel.value.list[index].operator = OperatorEnum.CONTAINS;
      } else if (optionsValueList.includes(OperatorEnum.BELONG_TO)) {
        formModel.value.list[index].operator = OperatorEnum.BELONG_TO;
      } else if (optionsValueList.includes(OperatorEnum.EQUAL)) {
        formModel.value.list[index].operator = OperatorEnum.EQUAL;
      }
    }
  }
  // 改变第二列值
  function operatorChange(item: FilterFormItem, index: number) {
    formModel.value.list[index].value = valueIsArray(item) ? [] : '';
    if (['EMPTY', 'NOT_EMPTY'].includes(formModel.value.list[index].operator as string)) {
      formRef.value?.validate();
    }
  }
  function isValueDisabled(item: FilterFormItem) {
    return !item.dataIndex || ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
  }

  function handleDeleteItem(index: number) {
    if (formModel.value.list.length === 1) return;
    formModel.value.list.splice(index, 1);
  }
  function handleAddItem() {
    const item = {
      dataIndex: '',
      type: FilterType.INPUT,
      value: '',
    };
    formModel.value.list.push(item);
  }

  function getParams() {
    const conditions = formModel.value.list.map(({ value, operator, customField, dataIndex }) => ({
      value,
      operator,
      customField: customField ?? false,
      key: dataIndex,
    }));
    return { searchMode: formModel.value.searchMode, conditions };
  }

  // TODO lmy 根据视图重置
  function handleReset() {
    formModel.value = cloneDeep(savedFormModel.value);
    emit('handleFilter', { searchMode: formModel.value.searchMode, conditions: [] });
  }

  function handleFilter() {
    formRef.value?.validate((errors) => {
      if (!errors) {
        visible.value = false;
        emit('handleFilter', getParams());
      }
    });
  }

  const isSaveAsView = ref(false);
  const saveAsViewName = ref('');
  function handleCancelSaveAsView() {
    isSaveAsView.value = false;
    saveAsViewName.value = '';
  }
</script>

<style lang="less" scoped>
  :deep(.arco-form-item) {
    .arco-form-item-label-col {
      display: none;
    }
    .arco-form-item-message {
      margin-bottom: 2px;
    }
  }
</style>
