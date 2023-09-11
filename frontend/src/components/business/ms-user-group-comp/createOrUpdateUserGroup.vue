<template>
  <a-popover :popup-visible="currentVisible" position="bl" trigger="click" class="w-[276px]">
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
            <div class="text-[14px] text-[var(--color-text-1)]">{{
              props.id ? t('system.userGroup.rename') : t('system.userGroup.createUserGroup')
            }}</div>
          </a-form-item>
          <a-form-item field="name" :rules="[{ validator: validateName }]">
            <a-input
              v-model="form.name"
              class="w-[228px]"
              :placeholder="t('system.userGroup.pleaseInputUserGroupName')"
              @press-enter="handleBeforeOk"
              @keyup.esc="handleCancel"
            />
          </a-form-item>
        </a-form>
      </div>
      <div class="flex flex-row flex-nowrap justify-end gap-2">
        <a-button type="secondary" size="mini" :disabled="loading" @click="handleCancel">
          {{ t('common.cancel') }}
        </a-button>
        <a-button
          type="primary"
          size="mini"
          :loading="loading"
          :disabled="form.name.length === 0"
          @click="handleBeforeOk"
        >
          {{ props.id ? t('common.rename') : t('common.create') }}
        </a-button>
      </div>
    </template>
    <slot></slot>
  </a-popover>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, inject } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import { updateOrAddOrgUserGroup, updateOrAddUserGroup } from '@/api/modules/setting/usergroup';
  import { UserGroupItem } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';
  import { useAppStore } from '@/store';

  const { t } = useI18n();
  const systemType = inject<AuthScopeEnum>('systemType');
  const props = defineProps<{
    id?: string;
    list: UserGroupItem[];
    visible: boolean;
    defaultName?: string;
    // 权限范围
    authScope: AuthScopeEnum;
  }>();
  const emit = defineEmits<{
    (e: 'cancel', value: boolean): void;
    (e: 'submit', currentId: string): void;
  }>();

  const formRef = ref<FormInstance>();
  const currentVisible = ref(props.visible);

  const form = reactive({
    name: '',
  });

  const appStore = useAppStore();

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
    loading.value = false;
    emit('cancel', false);
  };

  const handleBeforeOk = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return false;
      }
      try {
        loading.value = true;
        let res: UserGroupItem | undefined;
        if (systemType === AuthScopeEnum.SYSTEM) {
          res = await updateOrAddUserGroup({ id: props.id, name: form.name, type: props.authScope });
        } else if (systemType === AuthScopeEnum.ORGANIZATION) {
          debugger;
          // 组织用户组
          res = await updateOrAddOrgUserGroup({
            id: props.id,
            name: form.name,
            type: props.authScope,
            scopeId: appStore.currentOrgId,
          });
        } else {
          res = await updateOrAddUserGroup({ id: props.id, name: form.name, type: props.authScope });
        }
        if (res) {
          Message.success(
            props.id ? t('system.userGroup.updateUserGroupSuccess') : t('system.userGroup.addUserGroupSuccess')
          );
          emit('submit', res.id);
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
