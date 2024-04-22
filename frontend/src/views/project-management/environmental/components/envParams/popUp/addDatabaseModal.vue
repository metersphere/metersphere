<template>
  <a-modal
    v-model:visible="currentVisible"
    title-align="start"
    class="ms-modal-form ms-modal-medium"
    unmount-on-close
    :modal-style="{
      top: '0 !important',
    }"
    @cancel="handleCancel(false)"
  >
    <template #title>
      <span v-if="props.currentId && !props.isCopy">
        {{ t('project.environmental.database.updateDatabase') }}
      </span>
      <span v-else>
        {{ t('project.environmental.database.addDatabase') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="dataSource"
          required
          :label="t('project.environmental.database.name')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.environmental.database.nameIsRequire') }]"
        >
          <a-input
            v-model="form.dataSource"
            :max-length="255"
            allow-clear
            :placeholder="t('project.environmental.database.namePlaceholder')"
          />
        </a-form-item>
        <a-form-item
          field="driverId"
          required
          asterisk-position="end"
          :label="t('project.environmental.database.driver')"
          :rules="[{ required: true, message: t('project.environmental.database.driverIsRequire') }]"
        >
          <a-select v-model="form.driverId" :options="driverOption" />
        </a-form-item>
        <a-form-item
          field="dbUrl"
          required
          :label="t('project.environmental.database.url')"
          asterisk-position="end"
          :extra="
            form.driverId === 'system&com.mysql.cj.jdbc.Driver' ? t('project.environmental.database.urlExtra') : ''
          "
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
            :precision="0"
            :max="10000"
            :default-value="1"
          />
        </a-form-item>
        <a-form-item field="timeout" :label="t('project.environmental.database.timeout')">
          <a-input-number
            v-model:model-value="form.timeout"
            class="w-[152px]"
            mode="button"
            :step="100"
            :precision="0"
            :min="0"
            :default-value="1000"
            :max="600000"
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
            {{ props.currentId && !props.isCopy ? t('common.confirm') : t('common.add') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { Message } from '@arco-design/web-vue';

  import { driverOptionFun, validateDatabaseEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { getGenerateId } from '@/utils';

  import { DataSourceItem } from '@/models/projectManagement/environmental';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const store = useProjectEnvStore();
  const props = defineProps<{
    currentId: string;
    visible: boolean;
    isCopy: boolean;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const driverOption = ref<{ label: string; value: string }[]>([]);
  const appStore = useAppStore();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
    (e: 'addOrUpdate', data: DataSourceItem, cb: (v: boolean) => void): void;
  }>();

  const currentVisible = defineModel('visible', {
    default: false,
    type: Boolean,
  });

  const initForm = {
    id: '',
    dataSource: '',
    driverId: '',
    dbUrl: '',
    username: '',
    password: '',
    poolMax: 1,
    timeout: 1000,
  };

  const form = ref<DataSourceItem>({
    ...initForm,
  });

  const getDriverOption = async () => {
    try {
      const res = (await driverOptionFun(appStore.currentOrgId)) || [];
      driverOption.value = res.map((item) => ({
        label: item.name,
        value: item.id,
      }));
      if (res.length && !props.currentId) {
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
            ...form.value,
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

  const formReset = () => {
    form.value = {
      id: '',
      dataSource: '',
      driverId: '',
      dbUrl: '',
      username: '',
      password: '',
      poolMax: 1,
      timeout: 1000,
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
      const isExist = store.currentEnvDetailInfo.config.dataSources.some(
        (item) => item.dataSource === form.value.dataSource && item.id !== form.value.id
      );
      if (isExist) {
        Message.error(t('project.environmental.database.nameIsExist'));
        return;
      }
      const { driverId } = form.value;
      try {
        const index = store.currentEnvDetailInfo.config.dataSources.findIndex((item: any) => item.id === form.value.id);
        if (index > -1 && !props.isCopy) {
          store.currentEnvDetailInfo.config.dataSources.splice(index, 1, form.value);
        } else if (index > -1 && props.isCopy) {
          const insertItem = {
            ...form.value,
            id: getGenerateId(),
            driver: driverOption.value.find((item) => item.value === driverId)?.label,
          };
          store.currentEnvDetailInfo.config.dataSources.splice(index + 1, 0, insertItem);
        } else {
          const dataSourceItem = {
            ...form.value,
            id: getGenerateId(),
            driver: driverOption.value.find((item) => item.value === driverId)?.label,
          };
          store.currentEnvDetailInfo.config.dataSources.push(dataSourceItem);
        }
        formReset();
        currentVisible.value = false;
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };

  watch(
    () => props.visible,
    (val) => {
      if (val) {
        if (props.currentId) {
          const currentItem = store.currentEnvDetailInfo.config.dataSources.find(
            (item) => item.id === props.currentId
          ) as DataSourceItem;
          if (currentItem) {
            if (props.isCopy) {
              form.value = {
                ...currentItem,
                id: '',
                dataSource: `copy_${currentItem.dataSource}`.substring(0, 255),
              };
            } else {
              form.value = {
                ...currentItem,
              };
            }
          } else {
            formReset();
          }
        }
      }
    }
  );

  onMounted(() => {
    getDriverOption();
  });
</script>

<style lang="less" scoped></style>
