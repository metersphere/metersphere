<template>
  <condition
    v-model:list="innerParams"
    condition-type="preOperation"
    :condition-types="[RequestConditionProcessor.SCRIPT, RequestConditionProcessor.SQL]"
    add-text="apiTestDebug.precondition"
    response=""
    :show-associated-scene="props.showAssociatedScene"
    :show-pre-post-request="props.showPrePostRequest"
    :request-radio-text-props="props.requestRadioTextProps"
    show-quick-copy
  >
  </condition>
</template>

<script lang="ts" setup>
  import condition from '@/views/api-test/components/condition/index.vue';

  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';

  import { RequestConditionProcessor } from '@/enums/apiEnum';

  const props = defineProps<{
    showAssociatedScene?: boolean;
    showPrePostRequest?: boolean;
    requestRadioTextProps?: Record<string, any>;
    activeTab: string;
  }>();

  const store = useProjectEnvStore();
  const innerParams = computed({
    set: (value: any) => {
      if (props.activeTab === 'scenarioProcessorConfig') {
        store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig.scenarioProcessorConfig.processors =
          value || [];
      } else {
        store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig.requestProcessorConfig.processors =
          value || [];
      }
    },
    get: () => {
      if (props.activeTab === 'scenarioProcessorConfig') {
        return (
          store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig.scenarioProcessorConfig.processors ||
          []
        );
      }
      return (
        store.currentEnvDetailInfo.config.preProcessorConfig.apiProcessorConfig.requestProcessorConfig.processors || []
      );
    },
  });
</script>

<style lang="less" scoped></style>
