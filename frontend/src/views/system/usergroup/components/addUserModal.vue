<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    :ok-text="t('system.userGroup.add')"
    @ok="handelOk"
    @cancel="handleCancel"
  >
    <template #title> {{ t('system.userGroup.addUser') }} </template>
    <div class="form">
      <a-form :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
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
  import { reactive, ref, watchEffect, onUnmounted } from 'vue';
  import { UserOption } from '@/models/system/usergroup';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();

  const fieldNames = { value: 'id', label: 'name' };

  const currentVisible = ref(props.visible);
  const loading = ref(false);

  const form = reactive({
    name: [],
  });

  const labelCache = new Map();

  const allOption: UserOption[] = [
    { id: 1, name: 'llb', email: 'name@163.com' },
    {
      id: 2,
      name: 'rubyliu',
      email: 'rubyliu@163.com',
    },
    {
      id: 3,
      name: 'jack',
      email: 'jack@163.com',
    },
  ];

  const userOptions = ref<UserOption[]>(allOption);

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const handleCancel = () => {
    emit('cancel');
  };

  const handelOk = () => {
    // eslint-disable-next-line no-console
    console.log('ok');
    handleCancel();
  };
  const handleSearch = (value: string) => {
    if (value) {
      loading.value = true;
      window.setTimeout(() => {
        userOptions.value = userOptions.value.filter((item) => item.name.includes(value));
        loading.value = false;
      }, 60);
    } else {
      userOptions.value = allOption;
    }
  };
  onUnmounted(() => {
    labelCache.clear();
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
