<template>
  <a-modal
    v-model="currentVisible"
    class="ms-modal-form ms-modal-medium"
    width="680px"
    text-align="start"
    :ok-text="t('system.userGroup.add')"
    unmount-on-close
    :ok-loading="loading"
    :on-before-ok="handleBeforeOk"
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
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, onUnmounted, onMounted } from 'vue';
  import { UserTableItem } from '@/models/setting/usergroup';
  import { useUserGroupStore } from '@/store';
  import { getUserList, addUserToUserGroup } from '@/api/modules/setting/usergroup';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/bussiness/ms-user-selector/index.vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const store = useUserGroupStore();

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

  const allOption = ref<UserTableItem[]>([]);

  const userOptions = ref<UserTableItem[]>([]);

  const formRef = ref<FormInstance>();

  const initUserList = async () => {
    const res = await getUserList();
    allOption.value = res;
    userOptions.value = res;
  };

  watchEffect(() => {
    currentVisible.value = props.visible;
  });

  const handleCancel = () => {
    emit('cancel');
  };

  const handleBeforeOk = () => {
    loading.value = true;
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        loading.value = false;
        return false;
      }
      await addUserToUserGroup({ roleId: store.currentId, userIds: form.name });
      return true;
    });
  };

  onMounted(() => {
    initUserList();
  });

  onUnmounted(() => {
    labelCache.clear();
    form.name = [];
    loading.value = false;
  });
</script>

<style lang="less" scoped>
  .option-name {
    color: var(--color-text-1);
  }
  .option-email {
    color: var(--color-text-4);
  }
</style>
