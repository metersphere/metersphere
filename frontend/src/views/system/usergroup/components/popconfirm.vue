<template>
  <a-popconfirm
    :popup-visible="renameVisible"
    :ok-text="t('system.userGroup.confirm')"
    :cancel-text="t('system.userGroup.cancel')"
    @before-ok="handleSubmit"
    @cancel="handleCancel"
    @popup-visible-change="() => (form.name = '')"
  >
    <template #icon>{{ null }}</template>
    <template #content>
      <a-form ref="formRef" :model="form" :label-col-props="{ span: 0 }" :wrapper-col-props="{ span: 24 }">
        <a-form-item>
          <div class="title">{{ message.title }}</div>
        </a-form-item>
        <a-form-item field="name" :rules="[{ validator: validateName }]">
          <a-input v-if="props.type === 'rename'" v-model="form.name" />
          <a-select v-else v-model="form.name" class="w-[176px]">
            <a-option value="SYSTEM">{{ t('system.userGroup.SYSTEM') }}</a-option>
            <a-option value="ORGANIZATION">{{ t('system.userGroup.ORGANIZATION') }}</a-option>
            <a-option value="PROJECT">{{ t('system.userGroup.PROJECT') }}</a-option>
          </a-select>
        </a-form-item>
      </a-form>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { watchEffect, ref, computed, onUnmounted } from 'vue';
  import { CustomMoreActionItem, RenameType, UserGroupItem } from '@/models/system/usergroup';
  import { ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const formRef = ref();
  const form = ref({
    name: '',
  });

  const props = defineProps<{
    visible: boolean;
    defaultName: string;
    type: RenameType;
    list: UserGroupItem[];
  }>();

  const validateName = (value: string | undefined, callback: (error?: string) => void) => {
    if (props.type === 'rename') {
      if (value === undefined || value === '') {
        callback(t('system.userGroup.userGroupNameIsNotNone'));
      } else {
        if (value === props.defaultName) {
          callback();
        } else {
          const isExist = props.list.some((item) => item.name === value);
          if (isExist) {
            callback(t('system.userGroup.userGroupNameIsExist', { name: value }));
          }
        }
        callback();
      }
    } else if (value === '') {
      callback(t('system.userGroup.userGroupAuthScopeIsNotNone'));
    } else {
      callback();
    }
  };

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
    (e: 'submit', value: CustomMoreActionItem): Promise<void>;
    (e: 'cancel'): void;
  }>();

  const renameVisible = ref(props.visible);

  const handleSubmit = async () => {
    await formRef.value.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        emit('submit', { eventKey: props.type, name: form.value.name });
        return true;
      }
    });
    return false;
  };
  const handleCancel = () => {
    form.value.name = '';
    emit('cancel');
  };
  watchEffect(() => {
    renameVisible.value = props.visible;
    form.value.name = props.defaultName;
  });

  onUnmounted(() => {
    handleCancel();
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
