<template>
  <a-form ref="formRef" :model="formModel" layout="vertical">
    <div class="w-full overflow-y-auto bg-[var(--color-text-n9)] px-[12px] py-[12px]">
      <header class="flex flex-row items-center justify-between">
        <div>{{ t('advanceFilter.setFilterCondition') }}</div>
        <div class="flex flex-row items-center text-[var(--color-text-2)]">
          <div>{{ t('advanceFilter.accordBelow') }}</div>
          <div class="ml-[16px]">
            <a-select v-model:model-value="accordBelow" size="small">
              <a-option value="all">{{ t('advanceFilter.all') }}</a-option>
              <a-option value="any">{{ t('advanceFilter.any') }}</a-option>
            </a-select>
          </div>
          <div class="ml-[8px]">{{ t('advanceFilter.condition') }}</div>
        </div>
      </header>
      <article class="overflow-auto-y mt-[12px] flex max-h-[300px] flex-col gap-[8px]">
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
                <a-option
                  v-for="option in currentOptionArr"
                  :key="option.dataIndex"
                  :value="option.dataIndex"
                  :disabled="option.disabled"
                >
                  {{ t(option.title as string) }}
                </a-option>
              </a-select>
            </a-form-item>
          </div>
          <div class="flex-1 grow-0">
            <a-form-item
              :field="`list[${idx}].operator`"
              hide-asterisk
              class="hidden-item"
              :rules="[{ required: true, message: t('advanceFilter.plaseSelectOperator') }]"
            >
              <a-select v-model="item.operator" class="w-[100px]" :disabled="!item.dataIndex">
                <a-option value="equal">{{ t('advanceFilter.operator.equal') }}</a-option>
                <a-option value="notEqual">{{ t('advanceFilter.operator.notEqual') }}</a-option>
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
              <a-select
                v-else-if="item.type === FilterType.SELECT"
                v-model:model-value="item.value"
                class="w-full"
                allow-clear
                allow-search
                :option="item.options"
                :placeholder="t('advanceFilter.pleaseSelect')"
                :disabled="!item.dataIndex"
              ></a-select>
              <a-date-picker
                v-else-if="item.type === FilterType.DATE_PICKER"
                v-model:model-value="item.value"
                class="w-full"
                show-time
                format="YYYY-MM-DD hh:mm"
                :disabled="!item.dataIndex"
              />
              <a-range-picker
                v-else-if="item.type === FilterType.RANGE_PICKER"
                v-model:model-value="item.value"
                class="w-full"
                show-time
                format="YYYY-MM-DD HH:mm"
                :disabled="!item.dataIndex"
              />
            </a-form-item>
          </div>
          <div class="delete-btn" :class="{ 'delete-btn:disabled': idx === 0 }" @click="handleDeleteItem(idx)">
            <icon-minus-circle />
          </div>
        </section>
      </article>
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

  import { useI18n } from '@/hooks/useI18n';

  import { SelectValue } from '@/models/projectManagement/menuManagement';

  import { AccordBelowType, BackEndEnum, FilterFormItem, FilterResult, FilterType } from './type';

  const { t } = useI18n();
  const accordBelow = ref<AccordBelowType>('all');
  const formRef = ref<FormInstance | null>(null);
  const formModel = reactive<{ list: FilterFormItem[] }>({
    list: [],
  });
  const props = defineProps<{ configList: FilterFormItem[]; visible: boolean; count: number }>();
  const emit = defineEmits<{
    (e: 'onSearch', value: FilterResult): void;
    (e: 'dataIndexChange', value: string): void;
    (e: 'update:count', value: number): void;
  }>();

  // 获取当前可选的选项
  const getCurrentOptionArr = () => {
    const arr1 = props.configList;
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
        const tmpObj: FilterResult = {};
        formModel.list.forEach((item) => {
          tmpObj[item.dataIndex as string] = {
            operator: item.operator,
            value: item.value,
            backendType: item.backendType,
          };
        });
        tmpObj.accordBelow = accordBelow.value;
        emit('onSearch', tmpObj);
      }
    });
  };

  const getAttributeByDataIndex = (dataIndex: string) => {
    return props.configList.find((item) => item.dataIndex === dataIndex);
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
    const { type, backendType } = tmpObj;
    formModel.list[idx].operator = '';
    formModel.list[idx].backendType = backendType;
    formModel.list[idx].type = type;
    if (
      formModel.list[idx].type === FilterType.RANGE_PICKER ||
      formModel.list[idx].type === FilterType.MUTIPLE_SELECT
    ) {
      formModel.list[idx].value = [];
    } else {
      formModel.list[idx].value = '';
    }
    emit('dataIndexChange', dataIndex as string);
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
    color: var(--color-text-4);
    cursor: pointer;
  }
  .delete-btn:hover {
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-2));
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
</style>
