<template>
  <a-popconfirm
    ref="popoverRef"
    :popup-visible="currentVisible"
    position="bottom"
    class="ms-pop-confirm--hidden-icon"
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
    :cancel-button-props="{ disabled: loading }"
    @popup-visible-change="reset"
  >
    <template #content>
      <div class="mb-[8px] text-[14px] font-medium text-[var(--color-text-1)]">{{
        props.id ? t('system.userGroup.rename') : t('system.userGroup.createUserGroup')
      }}</div>
      <div v-outer="handleOutsideClick">
        <a-form ref="formRef" :model="form" layout="vertical">
          <a-form-item class="hidden-item" field="name" :rules="[{ validator: validateName }]">
            <a-input
              v-model="form.name"
              class="w-[245px]"
              :placeholder="t('system.userGroup.searchHolder')"
              allow-clear
              :max-length="255"
              @press-enter="handleBeforeOk(undefined)"
              @keyup.esc="handleCancel"
            />
          </a-form-item>
        </a-form>
      </div>
    </template>
    <slot></slot>
  </a-popconfirm>
</template>

<script lang="ts" setup>
  import { reactive, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { getGroupDetailEnv, groupUpdateEnv, updateOrAddEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore, { NEW_ENV_GROUP } from '@/store/modules/setting/useProjectEnvStore';

  import { EnvListItem } from '@/models/projectManagement/environmental';
  import { EnvAuthScopeEnum } from '@/enums/envEnum';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const store = useProjectEnvStore();
  const props = defineProps<{
    id?: string;
    list: EnvListItem[];
    visible?: boolean;
    defaultName?: string;
    description?: string;
    type: EnvAuthScopeEnum;
  }>();

  const emit = defineEmits<{
    (e: 'cancel', value: boolean): void;
    (e: 'success'): void;
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

  const handleBeforeOk = (done?: (closed: boolean) => void) => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        try {
          loading.value = true;

          if (props.type === EnvAuthScopeEnum.PROJECT) {
            await updateOrAddEnv({ fileList: [], request: { ...store.currentEnvDetailInfo, name: form.name } });
          } else {
            const id = store.currentGroupId === NEW_ENV_GROUP ? undefined : store.currentGroupId;
            if (id) {
              const detail: Record<string, any> = await getGroupDetailEnv(id);
              const envGroupProject = detail?.environmentGroupInfo.filter(
                (item: any) => item.projectId && item.environmentId
              );
              const params = {
                id,
                name: form.name,
                description: detail.description,
                projectId: appStore.currentProjectId,
                envGroupProject,
              };

              await groupUpdateEnv(params);
            }
          }
          Message.success(t('project.fileManagement.renameSuccess'));
          emit('success');
          handleCancel();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        } finally {
          loading.value = false;
        }
      } else if (done) {
        done(false);
      }
    });
  };

  watchEffect(() => {
    currentVisible.value = props.visible;
    form.name = props.defaultName || '';
  });

  const handleOutsideClick = () => {
    if (currentVisible.value) {
      handleCancel();
    }
  };
  function reset(val: boolean) {
    if (!val) {
      form.name = '';
      formRef.value?.resetFields();
    }
  }
</script>

<style lang="less">
  .move-left {
    position: relative;
    right: 22px;
  }
  :deep(.arco-trigger-content) {
    padding: 16px;
  }
</style>
