<template>
  <MsFormCreate
    v-model:api="fApi"
    :rule="currentPluginScript"
    :option="currentPluginOptions"
    @change="handlePluginFormChange"
  />
</template>

<script lang="ts" setup>
  import { debounce } from 'lodash-es';

  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';

  import { EnvPluginScript } from '@/models/projectManagement/environmental';

  import type { Api } from '@form-create/arco-design';

  const props = defineProps<{
    script: EnvPluginScript;
  }>();
  const fApi = ref<Api>();

  const currentPluginScript = computed(() => props.script?.script || {});
  const currentPluginOptions = computed(() => props.script?.options || {});

  const innerParams = defineModel<Record<string, any>>({ default: () => ({}) });

  // 处理插件表单输入框变化
  const handlePluginFormChange = debounce(() => {
    innerParams.value = fApi.value?.formData() || {};
  }, 300);
</script>

<style lang="less" scoped></style>
