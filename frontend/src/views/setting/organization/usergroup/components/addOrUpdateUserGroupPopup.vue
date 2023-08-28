<template>
  <a-popconfirm
    :popup-visible="currentVisible"
    :ok-text="props.id ? t('common.rename') : t('common.create')"
    unmount-on-close
    :on-before-ok="handleBeforeOk"
    :ok-loading="loading"
    :cancel-button-props="{ disabled: loading }"
    position="bl"
    class="w-[276px]"
    @cancel="handleCancel"
  >
    <template #icon>{{ null }}</template>
    <template #content>
      <div class="form">
        <a-form
          ref="formRef"
          :model="form"
          size="large"
          layout="vertical"
          :label-col-props="{ span: 0 }"
          :wrapper-col-props="{ span: 24 }"
        >
          <a-form-item>
            <div class="text-[var(color-text-1)]">{{
              props.id ? t('system.userGroup.rename') : t('system.userGroup.createUserGroup')
            }}</div>
          </a-form-item>
          <a-form-item field="name" :rules="[{ validator: validateName }]">
            <a-input
              v-model="form.name"
              class="w-[228px]"
              :placeholder="t('system.userGroup.pleaseInputUserGroupName')"
            />
          </a-form-item>
        </a-form>
      </div>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, computed, watchEffect } from 'vue';
  import { UserGroupItem } from '@/models/setting/usergroup';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import { updateOrAddOrgUserGroup } from '@/api/modules/setting/usergroup';
  import { useAppStore } from '@/store';

  const { t } = useI18n();
  const props = defineProps<{
    id?: string;
    list: UserGroupItem[];
    visible: boolean;
    defaultName?: string;
  }>();
  const emit = defineEmits<{
    (e: 'cancel', value: boolean): void;
    (e: 'search'): void;
  }>();

  const formRef = ref<FormInstance>();
  const currentVisible = ref(props.visible);

  const form = reactive({
    name: '',
  });

  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const loading = ref(false);

  const validateName = (value: string | undefined, callback: (error?: string) => void) => {
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
  };

  const handleCancel = () => {
    form.name = '';
    emit('cancel', false);
  };

  const handleBeforeOk = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return false;
      }
      try {
        loading.value = true;
        const res = await updateOrAddOrgUserGroup({ id: props.id, name: form.name, scopeId: currentOrgId.value });
        if (res) {
          Message.success(
            props.id ? t('system.userGroup.updateUserGroupSuccess') : t('system.userGroup.addUserGroupSuccess')
          );
          emit('search');
          handleCancel();
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
  watchEffect(() => {
    currentVisible.value = props.visible;
    form.name = props.defaultName || '';
  });
</script>
