<template>
  <a-modal
    v-model:visible="currentVisible"
    class="ms-modal-form ms-modal-medium"
    title-align="start"
    :ok-text="t('system.userGroup.add')"
    unmount-on-close
    @cancel="handleCancel(false)"
  >
    <template #title> {{ t('system.userGroup.addUser') }} </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          :label="t('system.userGroup.user')"
          :rules="[{ required: true, message: t('system.userGroup.pleaseSelectUser') }]"
        >
          <MsUserSelector v-model="form.name" v-bind="userSelectorProps" />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <a-button type="secondary" :loading="loading" @click="handleCancel(false)">
        {{ t('common.cancel') }}
      </a-button>
      <a-button type="primary" :loading="loading" :disabled="form.name.length === 0" @click="handleBeforeOk">
        {{ t('common.add') }}
      </a-button>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, inject, reactive, ref, watchEffect } from 'vue';
  import { type FormInstance, Message, type ValidatedError } from '@arco-design/web-vue';

  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { addOrgUserToUserGroup, addUserToUserGroup } from '@/api/modules/setting/usergroup';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { AuthScopeEnum } from '@/enums/commonEnum';

  const { t } = useI18n();
  const systemType = inject<AuthScopeEnum>('systemType');
  const props = defineProps<{
    visible: boolean;
    currentId: string;
  }>();

  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
  const userSelectorProps = computed(() => {
    if (systemType === AuthScopeEnum.SYSTEM) {
      return {
        type: UserRequestTypeEnum.SYSTEM_USER_GROUP,
        loadOptionParams: {
          roleId: props.currentId,
        },
        disabledKey: 'exclude',
      };
    }
    return {
      type: UserRequestTypeEnum.ORGANIZATION_USER_GROUP,
      loadOptionParams: {
        roleId: props.currentId,
        organizationId: currentOrgId.value,
      },
      disabledKey: 'checkRoleFlag',
    };
  });

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
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

  const handleCancel = (shouldSearch = false) => {
    labelCache.clear();
    form.name = [];
    emit('cancel', shouldSearch);
  };

  const handleBeforeOk = () => {
    formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        if (systemType === AuthScopeEnum.SYSTEM) {
          await addUserToUserGroup({ roleId: props.currentId, userIds: form.name });
        }
        if (systemType === AuthScopeEnum.ORGANIZATION) {
          await addOrgUserToUserGroup({
            userRoleId: props.currentId,
            userIds: form.name,
            organizationId: currentOrgId.value,
          });
        }
        handleCancel(true);
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
