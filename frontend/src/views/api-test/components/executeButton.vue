<template>
  <a-dropdown-button
    v-if="hasLocalExec && !props.executeLoading"
    class="exec-btn"
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
  <a-button v-else-if="!hasLocalExec && !props.executeLoading" type="primary" @click="() => execute('serverExec')">
    {{ t('apiTestDebug.serverExec') }}
  </a-button>
  <a-button v-else type="primary" @click="emit('stopDebug')">
    {{ t('common.stop') }}
  </a-button>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  import { getLocalConfig } from '@/api/modules/user/index';

  const props = defineProps<{
    executeLoading?: boolean;
  }>();

  const emit = defineEmits<{
    (e: 'execute', executeType?: 'localExec' | 'serverExec', localExecuteUrl?: string): void;
    (e: 'stopDebug'): void;
  }>();

  const { t } = useI18n();

  const hasLocalExec = ref(false); // 是否配置了api本地执行
  const isPriorityLocalExec = ref(false); // 是否优先本地执行
  const localExecuteUrl = ref('');

  async function initLocalConfig() {
    if (hasLocalExec.value) {
      return;
    }
    try {
      const res = await getLocalConfig();
      const apiLocalExec = res.find((e) => e.type === 'API');
      if (apiLocalExec) {
        hasLocalExec.value = true;
        isPriorityLocalExec.value = apiLocalExec.enable || false;
        localExecuteUrl.value = apiLocalExec.userUrl || '';
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }
  onBeforeMount(() => {
    initLocalConfig();
  });

  async function execute(executeType?: 'localExec' | 'serverExec') {
    emit('execute', executeType, localExecuteUrl.value);
  }

  defineExpose({
    isPriorityLocalExec,
    execute,
    localExecuteUrl,
  });
</script>

<style lang="less" scoped>
  .exec-btn :deep(.arco-btn) {
    color: white !important;
    background-color: rgb(var(--primary-5)) !important;
    .btn-base-primary-hover();
    .btn-base-primary-active();
    .btn-base-primary-disabled();
  }
</style>
