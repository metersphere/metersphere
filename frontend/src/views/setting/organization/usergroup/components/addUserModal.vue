<template>
  <a-modal
    v-model:visible="currentVisible"
    class="ms-modal-form ms-modal-medium"
    width="680px"
    text-align="start"
    :ok-text="t('system.userGroup.add')"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.userGroup.addUser') }} </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.userGroup.user')"
          :rules="[{ required: true, message: t('system.userGroup.pleaseSelectUser') }]"
        >
          <MsUserSelector v-model:value="form.name" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="form.name.length === 0" @click="handleBeforeOk">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, computed } from 'vue';
  import useUserGroupStore from '@/store/modules/setting/organization/usergroup';
  import { useAppStore } from '@/store';
  import { addOrgUserToUserGroup } from '@/api/modules/setting/usergroup';
  import { Message, type FormInstance, type ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const store = useUserGroupStore();
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'submit', value: string[]): void;
  }>();

  const currentVisible = ref(props.visible);
  const loading = ref(false);

  const form = reactive({
    name: [],
  });

  const labelCache = new Map();

  const formRef = ref<FormInstance>();

  watchEffect(() => {
    currentVisible.value = props.visible;
  });

  const handleCancel = () => {
    labelCache.clear();
    form.name = [];
    emit('cancel');
  };

  const handleBeforeOk = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        await addOrgUserToUserGroup({
          userRoleId: store.currentId,
          userIds: form.name,
          organizationId: currentOrgId.value,
        });
        handleCancel();
        Message.success(t('common.addSuccess'));
      } catch (e) {
        // eslint-disable-next-line no-console
        console.log(e);
      } finally {
        loading.value = false;
      }
    });
  };
</script>

<style lang="less" scoped>
  .option-name {
    color: var(--color-text-1);
  }
  .option-email {
    color: var(--color-text-4);
  }
</style>
