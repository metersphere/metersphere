<template>
  <a-dropdown-button
    v-if="hasLocalExec && !props.executeLoading"
    :size="props.size"
    type="primary"
    @click="() => execute(isPriorityLocalExec ? 'localExec' : 'serverExec')"
    @select="execute"
  >
    {{ isPriorityLocalExec ? t('apiTestDebug.localExec') : t('apiTestDebug.serverExec') }}
    <template #icon>
      <icon-down />
    </template>
    <template #content>
      <a-doption :value="isPriorityLocalExec ? 'serverExec' : 'localExec'">
        {{ isPriorityLocalExec ? t('apiTestDebug.serverExec') : t('apiTestDebug.localExec') }}
      </a-doption>
    </template>
  </a-dropdown-button>
  <a-button
    v-else-if="!hasLocalExec && !props.executeLoading"
    :size="props.size"
    type="primary"
    @click="() => execute('serverExec')"
  >
    {{ t('apiTestDebug.serverExec') }}
  </a-button>
  <a-button v-else type="primary" :size="props.size" @click="emit('stopDebug')">
    {{ t('common.stop') }}
  </a-button>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  import { useUserStore } from '@/store';

  const props = defineProps<{
    executeLoading?: boolean;
    size?: 'mini' | 'small' | 'medium' | 'large';
  }>();

  const emit = defineEmits<{
    (e: 'execute', executeType?: 'localExec' | 'serverExec', localExecuteUrl?: string): void;
    (e: 'stopDebug'): void;
  }>();

  const { t } = useI18n();
  const userStore = useUserStore();

  const hasLocalExec = computed(() => userStore.hasLocalExec); // 是否配置了api本地执行
  const isPriorityLocalExec = computed(() => userStore.isPriorityLocalExec); // 是否优先本地执行
  const localExecuteUrl = computed(() => userStore.localExecuteUrl);

  async function execute(executeType?: 'localExec' | 'serverExec') {
    emit('execute', executeType, localExecuteUrl.value);
  }

  defineExpose({
    isPriorityLocalExec,
    execute,
    localExecuteUrl,
    hasLocalExec,
  });
</script>
