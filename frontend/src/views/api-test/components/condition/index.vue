<template>
  <div class="mb-[8px] flex items-center justify-between">
    <a-dropdown @select="(val) => addCondition(val as ConditionType)">
      <a-button type="outline">
        <template #icon>
          <icon-plus :size="14" />
        </template>
        {{ t(props.addText) }}
      </a-button>
      <template #content>
        <a-doption v-for="key of props.conditionTypes" :key="key" :value="key">
          {{ t(conditionTypeNameMap[key]) }}
        </a-doption>
      </template>
    </a-dropdown>
    <div v-if="$slots.titleRight" class="flex items-center">
      <slot name="titleRight"></slot>
    </div>
  </div>
  <div v-if="data.length > 0" class="flex h-[calc(100%-40px)] gap-[8px]">
    <div class="h-full w-[20%] min-w-[220px]">
      <conditionList
        v-model:list="data"
        :active-id="activeItem.id"
        @active-change="handleListActiveChange"
        @change="emit('change')"
      />
    </div>
    <conditionContent
      v-model:data="activeItem"
      :response="props.response"
      :height-used="props.heightUsed"
      @copy="copyListItem"
      @delete="deleteListItem"
      @change="emit('change')"
    />
  </div>
</template>

<script setup lang="ts">
  import { useVModel } from '@vueuse/core';

  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import conditionContent from './content.vue';
  import conditionList from './list.vue';

  import { conditionTypeNameMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';

  import { ConditionType, ExecuteConditionProcessor } from '@/models/apiTest/debug';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    list: ExecuteConditionProcessor[];
    conditionTypes: Array<ConditionType>;
    addText: string;
    heightUsed?: number;
    response?: string; // 响应内容
  }>();
  const emit = defineEmits<{
    (e: 'update:list', list: ExecuteConditionProcessor[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const data = useVModel(props, 'list', emit);
  const activeItem = ref<ExecuteConditionProcessor>(data.value[0]);

  function handleListActiveChange(item: ExecuteConditionProcessor) {
    activeItem.value = item;
  }

  /**
   * 复制列表项
   */
  function copyListItem() {
    const copyItem = {
      ...activeItem.value,
      id: new Date().getTime(),
    };
    data.value.push(copyItem as ExecuteConditionProcessor);
    activeItem.value = copyItem as ExecuteConditionProcessor;
    emit('change');
  }

  /**
   * 删除列表项
   */
  function deleteListItem(id: string | number) {
    data.value = data.value.filter((precondition) => precondition.id !== activeItem.value.id);
    if (activeItem.value.id === id) {
      [activeItem.value] = data.value;
    }
    emit('change');
  }

  /**
   * 添加条件
   */
  function addCondition(value: ConditionType) {
    const id = new Date().getTime();
    switch (value) {
      case RequestConditionProcessor.SCRIPT:
        data.value.push({
          id,
          processorType: RequestConditionProcessor.SCRIPT,
          scriptName: t('apiTestDebug.preconditionScriptName'),
          enableCommonScript: false,
          enable: true,
          script: '',
          scriptId: '',
          scriptLanguage: LanguageEnum.BEANSHELL,
          params: [],
        });
        break;
      // case RequestConditionProcessor.SQL:
      //   data.value.push({
      //     id,
      //     enableCommonScript: false,
      //     desc: '',
      //     enable: true,
      //     sqlSource: {
      //       scriptName: '',
      //       script: '',
      //       storageType: 'column',
      //       params: [],
      //     },
      //   });
      //   break;
      case RequestConditionProcessor.TIME_WAITING:
        data.value.push({
          id,
          processorType: RequestConditionProcessor.TIME_WAITING,
          enable: true,
          delay: 1000,
        });
        break;
      case RequestConditionProcessor.EXTRACT:
        data.value.push({
          id,
          processorType: RequestConditionProcessor.EXTRACT,
          enable: true,
          extractors: [],
        });
        break;
      default:
        break;
    }
    activeItem.value = data.value[data.value.length - 1];
    emit('change');
  }
</script>

<style lang="less" scoped></style>
