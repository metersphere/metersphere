<template>
  <MsDrawer v-model:visible="showJobDrawer" width="680px" :title="t('system.resourcePool.customJobTemplate')">
    <MsCodeEditor
      v-model:model-value="jobDefinition"
      title="YAML"
      width="100%"
      height="calc(100vh - 205px)"
      theme="MS-text"
    />
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';

  const props = defineProps<{
    visible: boolean;
    value: string;
  }>();

  const emit = defineEmits(['update:value', 'update:visible']);

  const { t } = useI18n();
  const showJobDrawer = ref(props.visible);
  const jobDefinition = ref(props.value);

  watch(
    () => props.visible,
    (val) => {
      showJobDrawer.value = val;
    }
  );

  watch(
    () => showJobDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );
</script>

<style lang="less" scoped></style>
