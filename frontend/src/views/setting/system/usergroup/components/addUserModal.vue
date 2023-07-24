<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
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
          <a-select
            v-model="form.name"
            multiple
            :virtual-list-props="{ height: 200 }"
            :placeholder="t('system.userGroup.pleaseSelectUser')"
            :options="userOptions"
            :field-names="fieldNames"
            @search="handleSearch"
          >
            <template #label="{ data }">
              <span class="option-name"> {{ data.name }} </span>
            </template>
            <template #option="{ data }">
              <span class="option-name"> {{ data.name }} </span>
              <span class="option-email"> {{ `(${data.email})` }} </span>
            </template>
          </a-select>
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

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const store = useUserGroupStore();

  const emit = defineEmits<{
    (e: 'cancel'): void;
    (e: 'submit', value: string[]): void;
  }>();

  const fieldNames = { value: 'id', label: 'name' };

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

  const handleSearch = (value: string) => {
    if (value) {
      loading.value = true;
      window.setTimeout(() => {
        userOptions.value = userOptions.value.filter((item) => item.name.includes(value));
        loading.value = false;
      }, 60);
    } else {
      userOptions.value = allOption.value;
    }
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
