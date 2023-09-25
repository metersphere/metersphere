<template>
  <div class="filter-panel">
    <div class="mb-4 flex items-center justify-between">
      <div class="condition-text">{{ t('featureTest.featureCase.setFilterCondition') }}</div>
      <div>
        <span class="condition-text">{{ t('featureTest.featureCase.followingCondition') }}</span>
        <a-select v-model="filterConditions.unit" class="mx-4 w-[68px]" size="small">
          <a-option v-for="version of conditionOptions" :key="version.id" :value="version.value">{{
            version.name
          }}</a-option>
        </a-select>
        <span class="condition-text">{{ t('featureTest.featureCase.condition') }}</span>
      </div>
    </div>
    <div v-for="(formItem, index) in formModels" :key="index" class="mb-[8px]">
      <a-scrollbar class="overflow-y-auto" :style="{ 'max-height': props.maxHeight || '300px' }">
        <QueryFromItem
          :form-item="formItem"
          :form-list="formModels"
          :select-group-list="selectGroupList"
          :index="index"
          @data-updated="handleDataUpdated"
        >
          <div
            v-show="formModels.length > 1"
            :class="[
              'flex',
              'h-[32px]',
              'w-[32px]',
              'p-[2px]',
              'cursor-pointer',
              'items-center',
              'justify-center',
              'text-[var(--color-text-4)]',
              'hover:text-[rgb(var(--primary-5))]',
              'hover:bg-[rgb(var(--primary-9))]',
              'rounded',
              'ml-[8px]',
            ]"
            @click="removeField(index)"
          >
            <icon-minus-circle />
          </div>
        </QueryFromItem>
      </a-scrollbar>
    </div>
    <div class="flex w-full items-center justify-between">
      <a-button class="px-0" type="text" :disabled="isDisabledAdd" @click="addField">
        <template #icon>
          <icon-plus class="text-[14px]" />
        </template>
        {{ t('searchPanel.addCondition') }}
      </a-button>
      <div>
        <a-button type="secondary" @click="resetField">{{ t('searchPanel.reset') }}</a-button>
        <a-button class="ml-3" type="primary">{{ t('searchPanel.filter') }}</a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';
  import { computed, ref } from 'vue';
  import QueryFromItem from './query-form-item.vue';
  import { useI18n } from '@/hooks/useI18n';
  import type { ConditionOptions, QueryTemplate } from './type';

  const { t } = useI18n();
  const props = defineProps<{
    maxHeight?: string; // 查询区域高度 默认300px
  }>();

  const filterConditions = ref({
    unit: '',
  });

  const conditionOptions = ref<ConditionOptions[]>([
    {
      id: '1001',
      name: t('condition.all'),
      value: '',
    },
    {
      id: '1002',
      name: t('condition.oneOf'),
      value: 'oneOf',
    },
  ]);

  // 查询字段列表
  const selectGroupList = ref([
    {
      label: '系统字段',
      options: [
        {
          value: 'name',
          label: '名称',
        },
        {
          value: 'updateTime',
          label: '更新时间',
        },
      ],
    },
    {
      label: '模版字段',
      options: [
        {
          value: 'tags',
          label: '标签',
        },
        {
          value: 'module',
          label: '模块',
        },
      ],
    },
  ]);

  // 默认模版
  const deaultTemplate: QueryTemplate = {
    // 查询关键字段
    searchKey: {
      label: '',
      type: 'a-select',
      value: '',
      field: 'nameKey',
      props: {
        placeholder: '请选择字段',
      },
      options: [
        {
          label: '系统字段',
          options: [
            {
              value: 'name',
              label: '名称',
            },
            {
              value: 'updateTime',
              label: '更新时间',
            },
          ],
        },
        {
          label: '模版字段',
          options: [
            {
              value: 'tags',
              label: '标签',
            },
            {
              value: 'module',
              label: '模块',
            },
          ],
        },
      ],
    },
    // 查询运算符条件
    operatorCondition: {
      label: '',
      type: 'a-select',
      value: '',
      field: 'operator',
      props: {
        placeholder: '请选择条件',
      },
      options: [],
    },
    // 查询内容
    queryContent: {
      label: '',
      type: 'a-input',
      value: '',
      field: 'condition',
    },
  };

  const formModels = ref<QueryTemplate[]>([{ ...deaultTemplate }]);

  // 移除查询条件项
  const removeField = (index: number) => {
    formModels.value.splice(index, 1);
  };

  // 添加查询条件项
  const addField = () => {
    const ishasCondition = formModels.value.some((item) => !item.searchKey.value);
    if (ishasCondition) {
      Message.warning(t('searchPanel.selectTip'));
      return;
    }
    formModels.value.push(deaultTemplate);
  };

  // 处理更新后的数据
  const handleDataUpdated = (newFromData: any, index: number) => {
    formModels.value.splice(index, 1, newFromData);
  };

  // 计算所有条件都选中不能再添加条件
  const isDisabledAdd = computed(() => {
    const isSelectValueKeys = formModels.value.map((item) => item.searchKey.value).filter((item) => item);
    const allOptions = selectGroupList.value.flatMap((group) => group.options).map((item) => item.value);
    const isAllExistValue = allOptions.every((item) => isSelectValueKeys.indexOf(item) > -1);
    return isAllExistValue;
  });

  // 重置三级查询条件
  const resetField = () => {
    formModels.value = formModels.value.map((item) => ({
      ...item,
      queryContent: {
        ...item.queryContent,
        value: '',
      },
    }));
  };
</script>

<style scoped lang="less">
  .filter-panel {
    background: var(--color-text-n9);
    @apply mt-1 rounded-md p-3;
    .condition-text {
      color: var(--color-text-2);
    }
  }
</style>
