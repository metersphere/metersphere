<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :width="680"
    :mask="false"
    :footer="false"
    :title="t('system.plugin.showScriptTitle', { name: props.config.title })"
    @close="handleClose"
  >
    <MsCodeEditor
      v-model:model-value="pluginScript"
      title="JSON"
      width="100%"
      height="calc(100vh - 155px)"
      theme="MS-text"
      :read-only="props.readOnly"
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
    defaultVal?: string | null;
    readOnly?: boolean;
  }>();

  const emit = defineEmits(['update:value', 'update:visible']);

  const { t } = useI18n();
  const showScriptDrawer = ref(props.visible);
  const pluginScript = ref(props.value);

  watch(
    () => props.visible,
    (val) => {
      showScriptDrawer.value = val;
    }
  );
  watch(
    () => props.value,
    (val) => {
      if (val) {
        pluginScript.value = val;
      }
    }
  );

  watch(
    () => showScriptDrawer.value,
    (val) => {
      emit('update:visible', val);
    }
  );
  function handleClose() {
    emit('update:value', pluginScript.value);
  }
</script>

<style lang="less" scoped></style>
