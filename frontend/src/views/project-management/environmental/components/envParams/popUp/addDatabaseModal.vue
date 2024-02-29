<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    unmount-on-close
    @cancel="handleCancel(false)"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('project.environmental.database.updateDatabase') }}
        <span class="text-[var(--color-text-4)]">({{ form.name }})</span>
      </span>
      <span v-else>
        {{ t('project.environmental.database.addDatabase') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('project.environmental.database.name')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.environmental.database.nameIsRequire') }]"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            allow-clear
            :placeholder="t('project.environmental.database.namePlaceholder')"
          />
        </a-form-item>
        <a-form-item field="driverId" asterisk-position="end" :label="t('project.environmental.database.driver')">
          <a-select v-model="form.driverId" :options="driverOption" />
        </a-form-item>
        <a-form-item
          field="dbUrl"
          required
          :label="t('project.environmental.database.url')"
          asterisk-position="end"
          :extra="t('project.environmental.database.urlExtra')"
          :rules="[{ required: true, message: t('project.environmental.database.urlIsRequire') }]"
        >
          <a-input v-model="form.dbUrl" :max-length="255" allow-clear :placeholder="t('common.pleaseInput')" />
        </a-form-item>
        <a-form-item
          field="username"
          required
          :label="t('project.environmental.database.username')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.environmental.database.usernameIsRequire') }]"
        >
          <a-input v-model="form.username" :max-length="255" allow-clear :placeholder="t('common.pleaseInput')" />
        </a-form-item>
        <a-form-item field="password" :label="t('project.environmental.database.password')">
          <a-input-password
            v-model="form.password"
            :placeholder="t('common.pleaseInput')"
            allow-clear
            :auto-size="{ minRows: 1 }"
          />
        </a-form-item>
        <a-form-item field="poolMax" :label="t('project.environmental.database.poolMax')">
          <a-input-number
            v-model:model-value="form.poolMax"
            class="w-[152px]"
            mode="button"
            :min="1"
            :default-value="1"
          />
        </a-form-item>
        <a-form-item field="timeout" :label="t('project.environmental.database.timeout')">
          <a-input-number
            v-model:model-value="form.timeout"
            class="w-[152px]"
            mode="button"
            :step="100"
            :min="0"
            :default-value="1000"
          />
        </a-form-item>
        <a-button type="outline" class="w-[88px]" @click="testConnection">
          {{ t('project.environmental.database.testConnection') }}
        </a-button>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row justify-end">
        <div class="flex flex-row gap-[14px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel(false)">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleBeforeOk">
            {{ isEdit ? t('common.confirm') : t('common.add') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, defineModel, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { driverOptionFun, validateDatabaseEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';

  import { DataSourceItem } from '@/models/projectManagement/environmental';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();

  const props = defineProps<{
    currentDatabase: DataSourceItem;
    visible: boolean;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const driverOption = ref<{ label: string; value: string }[]>([]);
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
    (e: 'addOrUpdate', data: DataSourceItem, cb: (v: boolean) => void): void;
  }>();

  const currentVisible = defineModel('visible', {
    default: false,
    type: Boolean,
  });

  const form = ref<DataSourceItem>({
    id: '',
    name: '',
    driverId: '',
    dbUrl: '',
    username: '',
    password: '',
    poolMax: 1,
    timeout: 1000,
    enable: true,
  });

  const isEdit = computed(() => !!props.currentDatabase.id);

  const getDriverOption = async () => {
    try {
      const res = (await driverOptionFun(appStore.currentOrgId)) || [];
      driverOption.value = res.map((item) => ({
        label: item.name,
        value: item.id,
      }));
      if (res.length && !isEdit.value) {
        // 创建模式下默认选中第一个
        form.value.driverId = res[0].id;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };

  const testConnection = async () => {
    await formRef.value?.validateField(
      ['url', 'username'],
      async (errors: undefined | Record<string, ValidatedError>) => {
        if (errors) {
          return;
        }
        try {
          loading.value = true;
          await validateDatabaseEnv({
            name: form.value.name,
            driverId: form.value.driverId,
            dbUrl: form.value.dbUrl,
            username: form.value.username,
            password: form.value.password,
            poolMax: form.value.poolMax,
            timeout: form.value.timeout,
          });
          Message.success(t('project.environmental.database.testConnectionSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.error(error);
        } finally {
          loading.value = false;
        }
      }
    );
  };

  const isXpack = computed(() => {
    return licenseStore.hasLicense();
  });

  const formReset = () => {
    form.value = {
      id: '',
      name: '',
      driverId: '',
      dbUrl: '',
      username: '',
      password: '',
      poolMax: 1,
      timeout: 1000,
      enable: true,
    };
  };
  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
    currentVisible.value = false;
    formReset();
  };

  const handleBeforeOk = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        emit('addOrUpdate', form.value, (v: boolean) => {
          Message.success(
            isEdit.value
              ? t('project.environmental.database.updateDataSourceSuccess')
              : t('project.environmental.database.createDataSourceSuccess')
          );
          handleCancel(v);
        });
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
  const initData = () => {
    getDriverOption();
  };
  watchEffect(() => {
    initData();
    if (props.currentDatabase?.id) {
      // 编辑
      if (props.currentDatabase) {
        form.value = { ...props.currentDatabase };
      }
    } else {
      // 新建
      formReset();
    }
  });
</script>
