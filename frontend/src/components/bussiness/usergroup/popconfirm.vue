<template>
  <a-popconfirm
    :popup-visible="renameVisible"
    :ok-text="t('system.userGroup.confirm')"
    :cancel-text="t('system.userGroup.cancel')"
    :blur-to-close="false"
    :click-to-close="false"
    :click-outside-to-close="false"
    @ok="handleSubmit('rename')"
    @cancel="handleRenameCancel"
    @popup-visible-change="() => (form.name = '')"
  >
    <template #icon>{{ null }}</template>
    <template #content>
      <a-form :model="form" :label-col-props="{ span: 0 }" :wrapper-col-props="{ span: 24 }">
        <a-form-item>
          <div class="title">{{ message.title }}</div>
        </a-form-item>
        <a-form-item field="name" :rules="[{ required: true, message: message.rule }]">
          <a-input v-model="form.name" />
        </a-form-item>
      </a-form>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { watchEffect, reactive, ref, computed } from 'vue';
  import { CustomMoreActionItem, RenameType } from './type';

  const { t } = useI18n();
  const form = reactive({
    name: '',
  });

  const props = defineProps<{
    visible: boolean;
    defaultName: string;
    type: RenameType;
  }>();

  const message = computed(() => {
    if (props.type === 'rename') {
      return {
        rule: t('system.userGroup.userGroupNameIsNotNone'),
        title: t('system.userGroup.rename'),
      };
    }
    return {
      rule: t('system.userGroup.userGroupAuthScopeIsNotNone'),
      title: t('system.userGroup.changeAuthScope'),
    };
  });

  const emit = defineEmits<{
    (e: 'submit', value: CustomMoreActionItem): void;
  }>();

  const renameVisible = ref(props.visible);

  const handleSubmit = (type: string) => {
    // eslint-disable-next-line no-console
    emit('submit', { eventKey: type, name: form.name });
  };
  const handleRenameCancel = () => {
    form.name = '';
  };
  watchEffect(() => {
    renameVisible.value = props.visible;
    form.name = props.defaultName;
  });
</script>

<style lang="less" scoped>
  .title {
    color: var(--color-text-1);
  }
  .error-6 {
    color: rgb(var(--danger-6));
    &:hover {
      color: rgb(var(--danger-6));
    }
  }
  :deep(.arco-form-item) {
    margin-bottom: 8px;
  }
  .button-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 24px;
    height: 24px;
    color: rgb(var(--primary-5));
    background-color: rgb(var(--primary-9));
  }
</style>
