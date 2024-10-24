<template>
  <MsDrawer
    v-model:visible="showJobDrawer"
    :width="680"
    :title="t('system.resourcePool.customJobTemplate')"
    :footer="!props.readOnly"
    show-full-screen
    @close="handleClose"
  >
    <MsCodeEditor
      v-model:model-value="jobDefinition"
      title="YAML"
      width="100%"
      height="calc(100vh - 205px)"
      theme="MS-text"
      :read-only="props.readOnly"
      :show-full-screen="false"
    />
    <template v-if="!props.readOnly" #footer>
      <a-button type="secondary" @click="resetTemplate">{{ t('system.resourcePool.jobTemplateReset') }}</a-button>
    </template>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { job } from '../template';

  const props = defineProps<{
    visible: boolean;
    value?: string | null;
    defaultVal?: string | null;
    readOnly?: boolean;
  }>();

  const emit = defineEmits(['update:value', 'update:visible']);

  const { t } = useI18n();
  const showJobDrawer = ref(props.visible);
  const jobDefinition = ref(props.defaultVal || props.value || '');

  watch(
    () => props.visible,
    (val) => {
      showJobDrawer.value = val;
    }
  );

  watch(
    () => props.defaultVal,
    (val) => {
      if (val) {
        jobDefinition.value = val;
      }
    }
  );

  watch(
    () => props.value,
    (val) => {
      if (val) {
        jobDefinition.value = val;
      }
    }
  );

  watch(
    () => showJobDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  function resetTemplate() {
    jobDefinition.value = job;
  }

  function handleClose() {
    emit('update:value', jobDefinition.value);
  }
</script>

<style lang="less" scoped></style>
