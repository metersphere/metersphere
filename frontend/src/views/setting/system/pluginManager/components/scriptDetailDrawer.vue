<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    width="680px"
    :mask="false"
    :title="t('system.plugin.showScriptTitle', { name: props.config.title })"
  >
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
  import type { DrawerConfig } from '@/models/setting/plugin';

  const props = defineProps<{
    visible: boolean;
    value: string;
    config: DrawerConfig;
  }>();

  const emit = defineEmits(['update:value', 'update:visible']);

  const { t } = useI18n();
  const showScriptDrawer = ref(props.visible);
  const jobDefinition = ref(props.value);

  watch(
    () => props.visible,
    (val) => {
      showScriptDrawer.value = val;
    }
  );

  watch(
    () => showScriptDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );
</script>

<style lang="less" scoped></style>
