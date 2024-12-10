<template>
  <MsDrawer v-model:visible="visible" class="filter-drawer" :mask="false" :width="600">
    <template #title>
      <ViewNameInput
        v-if="isShowNameInput"
        ref="viewNameInputRef"
        v-model:form="formModel"
        :all-names="allViewNames.filter((name) => name !== savedFormModel.name)"
        @handle-submit="isShowNameInput = false"
      />
      <div v-else class="flex flex-1 items-center gap-[8px] overflow-hidden">
        <a-tooltip :content="formModel.name">
          <div class="one-line-text"> {{ formModel.name }}</div>
        </a-tooltip>
        <MsIcon
          v-if="!isInternalViews(formModel?.id)"
          type="icon-icon_edit_outlined"
          class="min-w-[16px] cursor-pointer hover:text-[rgb(var(--primary-5))]"
          @click="showNameInput"
        />
      </div>
    </template>
    <a-alert class="mb-[12px]" closable>
      {{ t('advanceFilter.filterTip') }}
    </a-alert>
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
                v-if="(props?.customList || [])?.length && !option.customField && currentOptions(item.dataIndex as string)[currentOptionsIndex+1]?.customField"
                class="!my-1"
              />
            </div>
          </a-select>
        </a-form-item>
        <a-form-item :field="`list[${listIndex}].operator`" class="w-[105px]" hide-asterisk>
          <a-select v-model="item.operator" :disabled="!item.dataIndex" @change="operatorChange(item, listIndex)">
            <a-option v-for="option in operatorOptionsMap[item.type]" :key="option.value" :value="option.value">
              {{ t(option.label as string) }}
            </a-option>
          </a-select>
        </a-form-item>
        <a-form-item class="flex-[1.5] overflow-hidden" :field="`list[${listIndex}].value`" hide-asterisk>
          <a-textarea
            v-if="item.type === FilterType.TEXTAREA"
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
          <a-input-number
            v-else-if="
              item.type === FilterType.NUMBER ||
              (item.type === FilterType.TAGS_INPUT && [OperatorEnum.COUNT_LT, OperatorEnum.COUNT_GT].includes(item.operator as OperatorEnum))
            "
            v-model:model-value="item.value"
            allow-clear
            :disabled="isValueDisabled(item)"
            :max-length="255"
            :placeholder="t('common.pleaseInput')"
            v-bind="item.numberProps"
          >
            <template v-if="item.numberProps?.suffix" #suffix>
              {{ item.numberProps.suffix }}
            </template>
          </a-input-number>
          <MsTagsInput
            v-else-if="item.type === FilterType.TAGS_INPUT&& ![OperatorEnum.COUNT_LT, OperatorEnum.COUNT_GT].includes(item.operator as OperatorEnum)"
            v-model:model-value="item.value"
            :disabled="isValueDisabled(item)"
            allow-clear
            unique-value
            retain-input-value
          />
          <MsSelect
            v-else-if="item.type === FilterType.MEMBER"
            v-model:model-value="item.value"
            allow-clear
            allow-search
            :placeholder="t('common.pleaseSelect')"
            :disabled="isValueDisabled(item)"
            :search-keys="['label']"
            v-bind="{
              options: props.memberOptions,
              multiple: true,
            }"
          />
          <MsSelect
            v-else-if="item.type === FilterType.SELECT || item.type === FilterType.SELECT_EQUAL"
            v-model:model-value="item.value"
            allow-clear
            allow-search
            :search-keys="item.selectProps?.labelKey?.length ? [item.selectProps?.labelKey] : ['label']"
            :placeholder="t('common.pleaseSelect')"
            :disabled="isValueDisabled(item)"
            :options="item.selectProps?.options || []"
            v-bind="item.selectProps"
          />
          <MsTreeSelect
            v-else-if="item.type === FilterType.TREE_SELECT"
            v-model:model-value="item.value"
            :data="item.treeSelectData ?? []"
            :disabled="isValueDisabled(item)"
            allow-clear
            :multiple="item.treeSelectProps?.multiple"
            v-bind="item.treeSelectProps"
            :placeholder="t('common.pleaseSelect')"
            :field-names="item.treeSelectProps?.fieldNames"
          />
          <MsCascader
            v-else-if="item.type === FilterType.CASCADER"
            v-model:model-value="item.value"
            :options="item.cascaderProps?.options || []"
            mode="native"
            option-size="small"
            strictly
            label-path-mode
            v-bind="item.cascaderProps"
            :placeholder="t('common.pleaseSelect')"
            :disabled="isValueDisabled(item)"
            :virtual-list-props="{ height: 200 }"
          />
          <a-date-picker
            v-else-if="item.type === FilterType.DATE_PICKER && item.operator !== OperatorEnum.BETWEEN"
            v-model:model-value="item.value"
            show-time
            value-format="timestamp"
            :disabled="isValueDisabled(item)"
            v-bind="item.dataProps"
          />
          <a-range-picker
            v-else-if="item.type === FilterType.DATE_PICKER && item.operator === OperatorEnum.BETWEEN"
            v-model:model-value="item.value"
            show-time
            value-format="timestamp"
            :separator="t('common.to')"
            :disabled="isValueDisabled(item)"
            v-bind="item.dataProps"
          />
          <a-input
            v-else
            v-model:model-value="item.value"
            allow-clear
            :disabled="isValueDisabled(item)"
            :max-length="255"
            :placeholder="t('advanceFilter.inputPlaceholder')"
          />
        </a-form-item>
        <a-button type="outline" class="arco-btn-outline--secondary" @click="handleDeleteItem(listIndex)">
          <template #icon> <MsIcon type="icon-icon_block_outlined" class="text-[var(--color-text-4)]" /> </template>
        </a-button>
      </div>
    </a-form>
    <MsButton
      :disabled="formModel.list?.length === [...props.configList, ...(props.customList ?? [])].length"
      type="text"
      class="mt-[5px] w-[fit-content]"
      @click="handleAddItem"
    >
      <MsIcon type="icon-icon_add_outlined" class="mr-[3px]" />
      {{ t('advanceFilter.addCondition') }}
    </MsButton>
    <template #footer>
      <div v-if="!isSaveAsView" class="mb-[22px] flex items-center gap-[8px]">
        <a-button v-if="!formModel?.id" type="primary" @click="handleSaveAndFilter">{{
          t('advanceFilter.saveAndFilter')
        }}</a-button>
        <a-button v-if="formModel?.id" type="primary" @click="handleFilter">{{ t('common.filter') }}</a-button>
        <a-button class="mr-[16px]" @click="handleReset">{{ t('common.reset') }}</a-button>
        <MsButton
          v-if="!isInternalViews(formModel?.id) && formModel?.id"
          type="text"
          :loading="saveLoading"
          class="h-[32px] !text-[var(--color-text-1)]"
          @click="handleSaveView()"
        >
          {{ t('common.save') }}
        </MsButton>
        <MsButton
          v-if="(formModel?.id && !isInternalViews(formModel?.id)) || formModel?.id === 'all_data'"
          type="text"
          class="h-[32px] !text-[var(--color-text-1)]"
          @click="handleToSaveAs"
        >
          {{ t('advanceFilter.saveAsView') }}
        </MsButton>
      </div>
      <div v-else class="flex items-center gap-[8px]">
        <ViewNameInput
          ref="saveAsViewNameInputRef"
          v-model:form="saveAsViewForm"
          class="w-[240px]"
          :all-names="allViewNames"
        />
        <a-button type="primary" class="mb-[22px]" :loading="addLoading" @click="handleAddView">{{
          t('common.save')
        }}</a-button>
        <a-button @click="handleCancelSaveAsView">{{ t('common.cancel') }}</a-button>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsTagsInput from '@/components/pure/ms-tags-input/index.vue';
  import MsTreeSelect from '@/components/pure/ms-tree-select/index.vue';
  import MsCascader from '@/components/business/ms-cascader/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import ViewNameInput from './components/viewNameInput.vue';

  import { addView, getViewDetail, updateView } from '@/api/modules/user/index';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';

  import { SelectValue } from '@/models/projectManagement/menuManagement';
  import { FilterType, OperatorEnum, ViewTypeEnum } from '@/enums/advancedFilterEnum';

  import { getAllDataDefaultConditions, internalViewsHiddenConditionsMap, operatorOptionsMap } from './index';
  import { ConditionsItem, FilterForm, FilterFormItem, FilterResult, ViewItem } from './type';

  const props = defineProps<{
    configList: FilterFormItem[]; // 系统字段
    customList?: FilterFormItem[]; // 自定义字段
    viewType: ViewTypeEnum;
    currentView: string; // 当前视图
    allViewNames: string[];
    canNotAddView: boolean;
    internalViews: ViewItem[]; // 系统视图
    memberOptions: { label: string; value: string }[];
  }>();
  const emit = defineEmits<{
    (e: 'handleFilter', value: FilterResult): void;
    (e: 'refreshViewList'): void;
    (e: 'changeViewToFirstCustom'): void;
  }>();
  const visible = defineModel<boolean>('visible', { required: true });

  const { t } = useI18n();
  const appStore = useAppStore();

  const defaultFormModel: FilterForm = {
    name: '',
    searchMode: 'AND',
    list: [{ dataIndex: '', operator: undefined, value: '', type: FilterType.INPUT }],
  };
  const formModel = ref<FilterForm>(cloneDeep(defaultFormModel));
  const savedFormModel = ref(cloneDeep(formModel.value));

  const currentConfigList = computed<FilterFormItem[]>(() =>
    props.configList.filter(
      (item) => !(internalViewsHiddenConditionsMap[props.currentView] ?? []).includes(item.dataIndex as string)
    )
  );
  function isInternalViews(id?: string): boolean {
    return props.internalViews.some((item) => item.id === id);
  }

  function getListItemByDataIndex(dataIndex: string) {
    return [...currentConfigList.value, ...(props.customList || [])].find((item) => item.dataIndex === dataIndex);
  }
  async function getUserViewDetail(id?: string) {
    if (!id) {
      return;
    }
    try {
      const res = await getViewDetail(props.viewType, id);
      // 全部数据默认显示搜索条件
      if (res?.id === 'all_data') {
        res.conditions = [...getAllDataDefaultConditions(props.viewType)];
      }
      const list: FilterFormItem[] = [];
      (res.conditions ?? [])?.forEach((item: ConditionsItem) => {
        const listItem = getListItemByDataIndex(item.name ?? '') as FilterFormItem;
        let { value } = item;
        // 过滤掉已经删除的选项
        if (FilterType.SELECT === listItem?.type) {
          const valueKeyList = listItem.selectProps?.options?.map(
            (option) => option[listItem.selectProps?.valueKey ?? 'value']
          );
          value = value?.filter((valueItem: string) => valueKeyList?.includes(valueItem));
        } else if (FilterType.MEMBER === listItem?.type) {
          value = value?.filter((valueItem: string) =>
            props.memberOptions?.map((option) => option.value).includes(valueItem)
          );
        }
        if (listItem) {
          list.push({
            ...listItem,
            operator: item.operator,
            value,
          });
        }
      });
      formModel.value = { ...res, list };
      savedFormModel.value = cloneDeep(formModel.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  watch([() => props.configList, () => props.customList], ([newConfigList, newCustomList]) => {
    formModel.value.list.forEach((formModelListItem: FilterFormItem, index) => {
      const listItem = [...newConfigList, ...(newCustomList ?? [])].find(
        (item) => item.dataIndex === formModelListItem.dataIndex
      );
      if (listItem) {
        formModel.value.list[index] = {
          ...listItem,
          operator: formModelListItem.operator,
          value: formModelListItem.value,
        };
      }
    });
  });

  const isShowNameInput = ref(false);
  const viewNameInputRef = ref<InstanceType<typeof ViewNameInput>>();
  function showNameInput() {
    isShowNameInput.value = true;
    nextTick(() => {
      viewNameInputRef.value?.inputFocus();
    });
  }

  const searchModeOptions = [
    { value: 'AND', label: t('advanceFilter.and') },
    { value: 'OR', label: t('advanceFilter.or') },
  ];

  const formRef = ref<FormInstance>();
  // 第三列值是数组类型的
  function valueIsArray(listItem: FilterFormItem) {
    return (
      listItem.selectProps?.multiple ||
      listItem.cascaderProps?.multiple ||
      (listItem.type === FilterType.TAGS_INPUT &&
        ![OperatorEnum.COUNT_LT, OperatorEnum.COUNT_GT].includes(listItem.operator as OperatorEnum)) ||
      (listItem.type === FilterType.DATE_PICKER && listItem.operator === OperatorEnum.BETWEEN)
    );
  }
  // 第一列下拉数据
  const currentOptions = computed(() => {
    return (currentDataIndex: string) => {
      const otherDataIndices = formModel.value.list
        .filter((listItem) => listItem.dataIndex !== currentDataIndex)
        .map((item: FilterFormItem) => item.dataIndex);
      return [...currentConfigList.value, ...(props.customList || [])]
        .filter(({ dataIndex }) => !otherDataIndices.includes(dataIndex))
        .map((item) => ({ ...item, label: t(item.title as string) }));
    };
  });
  // 第二列默认：包含/属于/等于
  function getDefaultOperator(list: string[]) {
    if (list.includes(OperatorEnum.CONTAINS)) {
      return OperatorEnum.CONTAINS;
    }
    if (list.includes(OperatorEnum.BELONG_TO)) {
      return OperatorEnum.BELONG_TO;
    }
    if (list.includes(OperatorEnum.EQUAL)) {
      return OperatorEnum.EQUAL;
    }
    return OperatorEnum.BETWEEN; // 时间
  }
  // 改变第一列值
  function dataIndexChange(dataIndex: SelectValue, index: number) {
    const listItem = getListItemByDataIndex(dataIndex as string);
    if (!listItem) return;
    formModel.value.list[index] = { ...listItem };
    formModel.value.list[index].value = valueIsArray(listItem) ? [] : undefined;

    // 第二列默认：包含/属于/等于
    if (!formModel.value.list[index].operator?.length) {
      const optionsValueList = operatorOptionsMap[formModel.value.list[index].type].map(
        (optionItem) => optionItem.value
      );
      formModel.value.list[index].operator = getDefaultOperator(optionsValueList);
    }
  }
  // 改变第二列值
  function operatorChange(item: FilterFormItem, index: number) {
    formModel.value.list[index].value = valueIsArray(item) ? [] : undefined;
  }
  function isValueDisabled(item: FilterFormItem) {
    return !item.dataIndex || ['EMPTY', 'NOT_EMPTY'].includes(item.operator as string);
  }

  function handleDeleteItem(index: number) {
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
    const conditions = formModel.value.list.map(({ customFieldType, value, operator, customField, dataIndex }) => {
      return {
        value,
        operator,
        customField: customField ?? false,
        name: dataIndex,
        customFieldType: customFieldType ?? '',
      };
    });
    return { searchMode: formModel.value.searchMode, conditions };
  }

  // 重置
  function handleReset() {
    formModel.value = cloneDeep(savedFormModel.value);
    isShowNameInput.value = false;
  }
  // 过滤
  function handleFilter() {
    formRef.value?.validate((errors) => {
      if (!errors) {
        visible.value = false;
        emit('handleFilter', getParams());
      }
    });
  }
  watch(
    () => props.currentView,
    async (val, oldValue) => {
      await getUserViewDetail(val);
      if (!oldValue?.length) return;
      handleFilter();
    }
  );

  // 数据改为新建视图的空数据
  function resetToNewViewForm() {
    // 命名递增数字
    let name = '';
    for (let i = 1; i <= 10; i++) {
      const defaultName = `${t('advanceFilter.unnamedView')}${String(i).padStart(3, '0')}`;
      if (!props.allViewNames.includes(defaultName)) {
        name = defaultName;
        break;
      }
    }
    formModel.value = {
      ...cloneDeep(defaultFormModel),
      name,
    };
    savedFormModel.value = cloneDeep(formModel.value);
  }

  // 保存视图
  const saveLoading = ref(false);
  function realSaveView(isChangeView = false) {
    formRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          saveLoading.value = true;
          if (formModel.value.id) {
            await updateView(props.viewType, { ...getParams(), name: formModel.value.name, id: formModel.value.id });
          } else {
            await addView(props.viewType, {
              ...getParams(),
              scopeId: appStore.currentProjectId,
              name: formModel.value.name,
              id: formModel.value.id,
            });
          }
          Message.success(t('common.saveSuccess'));
          savedFormModel.value = cloneDeep(formModel.value);
          if (!isChangeView) {
            emit('refreshViewList');
          } else {
            emit('changeViewToFirstCustom');
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          saveLoading.value = false;
        }
      }
    });
  }
  function handleSaveView(isChangeView = false) {
    if (viewNameInputRef.value) {
      viewNameInputRef.value?.validateForm(realSaveView, isChangeView);
    } else {
      realSaveView(isChangeView);
    }
  }

  // 开启另存为视图模式
  const isSaveAsView = ref(false);
  const saveAsViewForm = ref({ name: '' });
  const saveAsViewNameInputRef = ref<InstanceType<typeof ViewNameInput>>();
  function handleToSaveAs() {
    if (props.canNotAddView) {
      Message.warning(t('advanceFilter.maxViewTip'));
      return;
    }
    formRef.value?.validate((errors) => {
      if (!errors) {
        isSaveAsView.value = true;
        nextTick(() => {
          saveAsViewNameInputRef.value?.inputFocus();
        });
      }
    });
  }
  // 取消另存为视图模式
  function handleCancelSaveAsView() {
    isSaveAsView.value = false;
    saveAsViewForm.value.name = '';
  }
  // 新增视图
  const addLoading = ref(false);
  async function realAddView() {
    try {
      addLoading.value = true;
      await addView(props.viewType, {
        ...getParams(),
        scopeId: appStore.currentProjectId,
        name: saveAsViewForm.value.name,
        id: formModel.value.id,
      });
      Message.success(t('common.saveSuccess'));
      emit('refreshViewList');
      handleCancelSaveAsView();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      addLoading.value = false;
    }
  }
  async function handleAddView() {
    saveAsViewNameInputRef.value?.validateForm(realAddView);
  }
  function handleSaveAndFilter() {
    handleSaveView(true);
  }

  watch(
    () => visible.value,
    async (val) => {
      // 新建视图关闭后重新获取数据
      if (!val) {
        if (isShowNameInput.value) {
          formModel.value.name = savedFormModel.value.name;
          isShowNameInput.value = false;
        }
        handleCancelSaveAsView();
        if (formModel.value?.id !== props.currentView) {
          await getUserViewDetail(props.currentView);
        }
      }
    }
  );

  defineExpose({
    resetToNewViewForm,
    handleReset,
    getUserViewDetail,
  });
</script>

<style lang="less" scoped>
  :deep(.arco-form-item) {
    .arco-form-item-label-col {
      display: none;
    }
    .arco-form-item-message {
      margin-bottom: 0;
      text-align: left;
    }
  }
</style>

<style lang="less">
  // 为了名称校验，input不移动
  .filter-drawer {
    .arco-drawer-header {
      align-items: baseline;
      padding-top: 16px;
      height: 60px;
      .arco-form-item-content,
      .arco-input-wrapper,
      .arco-form-item-content-wrapper {
        height: 26px;
        min-height: 26px;
      }
    }
    .arco-drawer-footer {
      padding-bottom: 0;
      & > div {
        align-items: start;
      }
    }
  }
</style>
