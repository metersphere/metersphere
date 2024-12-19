<template>
  <div class="mb-[8px] flex items-center justify-between">
    <div class="flex items-center gap-[8px]">
      <a-dropdown @select="(val) => addCondition(val as ConditionType)">
        <a-button type="outline" :disabled="props.disabled">
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
      <div v-if="$slots.dropdownAppend" class="flex items-center">
        <slot name="dropdownAppend"></slot>
      </div>
    </div>
    <div v-if="$slots.titleRight" class="flex items-center">
      <slot name="titleRight"></slot>
    </div>
  </div>
  <div v-if="list.length > 0 && activeItem" class="flex h-[calc(100%-40px)] w-full gap-[8px]">
    <div class="h-full w-[20%] min-w-[220px]">
      <conditionList
        v-model:list="list"
        :disabled="props.disabled"
        :active-id="activeItemId"
        :show-associated-scene="props.showAssociatedScene"
        :show-pre-post-request="props.showPrePostRequest"
        @active-change="handleListActiveChange"
        @change="emit('change')"
      />
    </div>
    <conditionContent
      v-model:data="activeItem"
      :condition-type="props.conditionType"
      :disabled="props.disabled"
      :total-list="list"
      :response="props.response"
      :show-associated-scene="props.showAssociatedScene"
      :show-pre-post-request="props.showPrePostRequest"
      :request-radio-text-props="props.requestRadioTextProps"
      :sql-code-editor-height="props.sqlCodeEditorHeight"
      :show-quick-copy="props.showQuickCopy"
      @copy="copyListItem"
      @delete="deleteListItem"
      @change="changeHandler"
    />
  </div>
</template>

<script setup lang="ts">
  import { cloneDeep } from 'lodash-es';

  import { LanguageEnum } from '@/components/pure/ms-code-editor/types';
  import conditionContent from './content.vue';
  import conditionList from './list.vue';

  import { conditionTypeNameMap } from '@/config/apiTest';
  import { useI18n } from '@/hooks/useI18n';
  import { getGenerateId } from '@/utils';

  import { ConditionType, ExecuteConditionProcessor, RegexExtract } from '@/models/apiTest/common';
  import { RequestConditionProcessor, RequestExtractScope } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      conditionType: 'preOperation' | 'postOperation' | 'assertion' | 'scenario'; // 前置、后置、断言、场景
      disabled?: boolean;
      conditionTypes: Array<ConditionType>;
      addText: string;
      requestRadioTextProps?: Record<string, any>;
      response?: string; // 响应内容
      showAssociatedScene?: boolean;
      showPrePostRequest?: boolean; // 是否展示前后置请求忽略选项
      sqlCodeEditorHeight?: string;
      showQuickCopy?: boolean; // 显示快捷复制icon
    }>(),
    {
      showAssociatedScene: false,
    }
  );
  const emit = defineEmits<{
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const list = defineModel<ExecuteConditionProcessor[]>('list', {
    required: true,
  });
  const activeItem = ref<ExecuteConditionProcessor>(list.value[0]);
  const activeItemId = computed(() => activeItem.value?.id);

  function handleListActiveChange(item: ExecuteConditionProcessor) {
    activeItem.value = item;
  }

  /**
   * 复制列表项
   */
  function copyListItem() {
    const copyItem = {
      ...cloneDeep(activeItem.value),
      id: getGenerateId(),
    };
    list.value.push(copyItem as ExecuteConditionProcessor);
    activeItem.value = list.value[list.value.length - 1];
    emit('change');
  }

  /**
   * 删除列表项
   */
  function deleteListItem(id: string | number) {
    list.value = list.value.filter((precondition) => precondition.id !== activeItemId.value);
    if (activeItemId.value === id) {
      [activeItem.value] = list.value;
    }
    emit('change');
  }

  /**
   * 添加条件
   */
  function addCondition(value: ConditionType) {
    const id = getGenerateId();
    switch (value) {
      // 脚本执行类型
      case RequestConditionProcessor.SCRIPT:
        let type = RequestConditionProcessor.SCRIPT;
        if (props.showAssociatedScene) {
          type = RequestConditionProcessor.SCENARIO_SCRIPT;
        } else if (props.showPrePostRequest) {
          type = RequestConditionProcessor.REQUEST_SCRIPT;
        }
        const isExistPre = list.value.filter(
          (item) => item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length;
        const isExistPost = list.value.filter(
          (item) => !item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length;
        // 如果是场景或者是请求类型的 需要限制前后脚本类型只能为一前一后

        if (isExistPre && isExistPost && props.showPrePostRequest) {
          return;
        }
        list.value.push({
          id,
          processorType: type,
          name: t('apiTestDebug.preconditionScriptName'),
          enableCommonScript: false,
          associateScenarioResult: false,
          ignoreProtocols: [],
          beforeStepScript: !isExistPre,
          enable: true,
          script: '',
          scriptId: '',
          scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
          commonScriptInfo: {
            id: '',
            name: '',
            script: '',
            params: [],
            scriptLanguage: LanguageEnum.BEANSHELL_JSR233,
          },
        });

        break;
      case RequestConditionProcessor.SQL:
        const isSQL = list.value.find((item) => item.processorType === RequestConditionProcessor.SQL);

        if (props.showPrePostRequest && isSQL) {
          return;
        }
        list.value.push({
          id,
          processorType: RequestConditionProcessor.SQL,
          enableCommonScript: false,
          associateScenarioResult: false,
          ignoreProtocols: [],
          beforeStepScript: true,
          name: '',
          enable: true,
          dataSourceId: '',
          queryTimeout: 0,
          resultVariable: '',
          script: '',
          variableNames: '',
          extractParams: [],
        });

        break;
      case RequestConditionProcessor.TIME_WAITING:
        list.value.push({
          id,
          processorType: RequestConditionProcessor.TIME_WAITING,
          associateScenarioResult: false,
          ignoreProtocols: [],
          beforeStepScript: true,
          enable: true,
          delay: 1000,
        });
        break;
      case RequestConditionProcessor.EXTRACT:
        const isEXTRACT = list.value.find((item) => item.processorType === RequestConditionProcessor.EXTRACT);
        if (isEXTRACT) {
          return;
        }
        list.value.push({
          id,
          processorType: RequestConditionProcessor.EXTRACT,
          enableCommonScript: false,
          associateScenarioResult: false,
          ignoreProtocols: [],
          beforeStepScript: true,
          enable: true,
          extractors: [],
        });
        break;
      default:
        break;
    }
    activeItem.value = list.value[list.value.length - 1];
    emit('change');
  }

  watchEffect(() => {
    // 后台存储无id，渲染时需要手动添加一次
    let hasNoIdItem = false;
    const tempArr = list.value.map((item, i) => {
      if (!item.id) {
        hasNoIdItem = true;
        return {
          ...item,
          extractors: item.extractors?.map((e, j) => ({
            ...e,
            extractScope: (e as RegexExtract).extractScope || RequestExtractScope.BODY,
            id: getGenerateId() + j,
          })),
          id: getGenerateId() + i,
        };
      }
      return item;
    });
    if (hasNoIdItem) {
      list.value = tempArr;
      [activeItem.value] = list.value;
    }
  });

  function changeHandler() {
    emit('change');
  }

  defineExpose({
    activeItemId,
  });
</script>

<style lang="less" scoped></style>
