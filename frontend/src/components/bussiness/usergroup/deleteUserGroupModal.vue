<template>
  <a-modal v-model:visible="currentVisible" modal-class="ug-delete">
    <template #title>
      <div class="flex w-full items-center">
        <icon-exclamation-circle-fill class="danger" />
        <span class="n1"> {{ t('system.userGroup.isDeleteUserGroup', { name: store.currentName }) }} </span>
      </div>
    </template>
    <div>{{ t('system.userGroup.beforeDeleteUserGroup') }}</div>
    <template #footer>
      <a-button @click="emit('cancel')">{{ t('cancel') }}</a-button>
      <a-button type="primary" @click="emit('ok')">{{ t('confirmDelete') }}</a-button>
    </template>
  </a-modal>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { ref, watchEffect } from 'vue';
  import useUserGroupStore from '@/store/modules/system/usergroup';

  const { t } = useI18n();
  const store = useUserGroupStore();

  const props = defineProps<{
    visible: boolean;
  }>();

  const currentVisible = ref(props.visible);

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'ok'): void;
  }>();

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
</script>

<style scoped lang="less">
  .ug-delete {
    .danger {
      font-size: 20px;
      color: rgb(var(--danger-6));
    }
    .n1 {
      color: var(--color-text-1);
    }
    .arco-modal-header {
      border-bottom: none;
    }
    :deep(.arco-modal-title-align-center) {
      justify-content: flex-start;
      border: 1px solid red;
    }
  }
</style>
