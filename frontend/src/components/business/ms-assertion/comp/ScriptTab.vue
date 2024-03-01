<template>
  <conditionContent v-model:data="innerParams" :height-used="600" is-build-in class="mt-[16px]" />
</template>

<script lang="ts" setup>
  import conditionContent from '@/views/api-test/components/condition/content.vue';

  import { RequestConditionProcessor, RequestConditionScriptLanguage } from '@/enums/apiEnum';

  interface ScriptItem {
    [key: string]: any;
  }

  interface ScriptTabProps {
    value: ScriptItem;
  }

  const defaultScriptItem: ScriptItem = {
    id: new Date().getTime(),
    processorType: RequestConditionProcessor.SCRIPT,
    scriptName: '断言脚本名称',
    enableCommonScript: false,
    params: [],
    scriptId: '',
    scriptLanguage: RequestConditionScriptLanguage.JAVASCRIPT,
    script: '',
  };

  const props = defineProps<ScriptTabProps>();

  const emit = defineEmits<{
    (e: 'change', val: ScriptItem): void; //  数据发生变化
  }>();

  const innerParams = ref<any>(props.value || defaultScriptItem);
  watchEffect(() => {
    emit('change', innerParams.value);
  });
</script>

<style lang="less" scoped></style>
