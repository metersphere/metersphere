<template>
  <MsDrawer
    v-model:visible="showScriptDrawer"
    :width="680"
    :mask="false"
    :footer="false"
    :title="t('system.plugin.showScriptTitle', { name: props.config.title })"
    unmount-on-close
    @close="handleClose"
  >
    <div class="w-full">
      <MsCodeEditor
        v-model:model-value="pluginScript"
        title="JSON"
        height="calc(100vh - 155px)"
        theme="MS-text"
        :read-only="props.readOnly"
        :show-theme-change="false"
        :show-title-line="true"
      />
    </div>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { ref, watch } from 'vue';

  import MsCodeEditor from '@/components/pure/ms-code-editor/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import { useI18n } from '@/hooks/useI18n';

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
    showScriptDrawer.value = false;
  }
</script>

<style lang="less" scoped></style>
