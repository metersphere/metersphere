<template>
  <div class="mb-[8px] flex items-center justify-between">
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
    <div v-if="$slots.titleRight" class="flex items-center">
      <slot name="titleRight"></slot>
    </div>
  </div>
  <div v-if="data.length > 0 && activeItem" class="flex h-[calc(100%-40px)] gap-[8px]">
    <div class="h-full w-[20%] min-w-[220px]">
      <conditionList
        v-model:list="data"
        :disabled="props.disabled"
        :active-id="activeItem.id"
        :show-associated-scene="props.showAssociatedScene"
        :show-pre-post-request="props.showPrePostRequest"
        @active-change="handleListActiveChange"
        @change="emit('change')"
      />
    </div>
    <conditionContent
      v-model:data="activeItem"
      :disabled="props.disabled"
      :total-list="data"
      :response="props.response"
      :height-used="props.heightUsed"
      :show-associated-scene="props.showAssociatedScene"
      :show-pre-post-request="props.showPrePostRequest"
      :request-radio-text-props="props.requestRadioTextProps"
      @copy="copyListItem"
      @delete="deleteListItem"
      @change="emit('change')"
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

  import { ConditionType, ExecuteConditionProcessor } from '@/models/apiTest/common';
  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = withDefaults(
    defineProps<{
      disabled?: boolean;
      list: ExecuteConditionProcessor[];
      conditionTypes: Array<ConditionType>;
      addText: string;
      requestRadioTextProps?: Record<string, any>;
      heightUsed?: number;
      response?: string; // 响应内容
      showAssociatedScene?: boolean;
      showPrePostRequest?: boolean; // 是否展示前后置请求忽略选项
    }>(),
    {
      showAssociatedScene: false,
    }
  );
  const emit = defineEmits<{
    (e: 'update:list', list: ExecuteConditionProcessor[]): void;
    (e: 'change'): void;
  }>();

  const { t } = useI18n();

  const data = defineModel<ExecuteConditionProcessor[]>('list', {
    required: true,
  });
  const activeItem = ref<ExecuteConditionProcessor>(data.value[0]);

  function handleListActiveChange(item: ExecuteConditionProcessor) {
    activeItem.value = item;
  }

  /**
   * 复制列表项
   */
  function copyListItem() {
    const copyItem = {
      ...cloneDeep(activeItem.value),
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
      // 脚本执行类型
      case RequestConditionProcessor.SCRIPT:
        let type = RequestConditionProcessor.SCRIPT;
        if (props.showAssociatedScene) {
          type = RequestConditionProcessor.SCENARIO_SCRIPT;
        } else if (props.showPrePostRequest) {
          type = RequestConditionProcessor.REQUEST_SCRIPT;
        }
        const isExistPre = data.value.filter(
          (item) => item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length;
        const isExistPost = data.value.filter(
          (item) => !item.beforeStepScript && item.processorType === RequestConditionProcessor.REQUEST_SCRIPT
        ).length;
        // 如果是场景或者是请求类型的 需要限制前后脚本类型只能为一前一后

        if (isExistPre && isExistPost && props.showPrePostRequest) {
          return;
        }
        data.value.push({
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
        const isSQL = data.value.find((item) => item.processorType === RequestConditionProcessor.SQL);

        if (props.showPrePostRequest && isSQL) {
          return;
        }
        data.value.push({
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
        data.value.push({
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
        const isEXTRACT = data.value.find((item) => item.processorType === RequestConditionProcessor.EXTRACT);
        if (isEXTRACT) {
          return;
        }

        data.value.push({
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
    activeItem.value = data.value[data.value.length - 1];
    emit('change');
  }

  watchEffect(() => {
    // 后台存储无id，渲染时需要手动添加一次
    let hasNoIdItem = false;
    const tempArr = props.list.map((item, i) => {
      if (!item.id) {
        hasNoIdItem = true;
        return {
          ...item,
          id: new Date().getTime() + i,
        };
      }
      return item;
    });
    if (hasNoIdItem) {
      data.value = tempArr.map((e) => e);
      [activeItem.value] = data.value;
    }
  });
</script>

<style lang="less" scoped></style>
