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
        <span class="text-[var(--color-text-4)]">({{ props.currentProject?.name }})</span>
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
          <a-input v-model="form.name" allow-clear :placeholder="t('project.environmental.database.namePlaceholder')" />
        </a-form-item>
        <a-form-item field="driver" asterisk-position="end" :label="t('project.environmental.database.driver')">
          <a-select v-model="form.driver">
            <a-option value="mysql">MySQL</a-option>
          </a-select>
        </a-form-item>
        <a-form-item
          field="url"
          required
          :label="t('project.environmental.database.url')"
          asterisk-position="end"
          :extra="t('project.environmental.database.urlExtra')"
          :rules="[{ required: true, message: t('project.environmental.database.urlIsRequire') }]"
        >
          <a-input v-model="form.url" allow-clear :placeholder="t('common.pleaseInput')" />
        </a-form-item>
        <a-form-item
          field="username"
          required
          :label="t('project.environmental.database.username')"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.environmental.database.usernameIsRequire') }]"
        >
          <a-input v-model="form.username" allow-clear :placeholder="t('common.pleaseInput')" />
        </a-form-item>
        <a-form-item field="password" :label="t('project.environmental.database.password')">
          <a-input
            v-model="form.password"
            :placeholder="t('common.pleaseInput')"
            allow-clear
            :auto-size="{ minRows: 1 }"
          />
        </a-form-item>
        <a-form-item field="poolMax" :label="t('project.environmental.database.poolMax')">
          <a-input-number v-model:model-value="form.poolMax" :min="1" :default-value="1" />
        </a-form-item>
        <a-form-item field="timeout" :label="t('project.environmental.database.timeout')">
          <a-input-number v-model:model-value="form.timeout" :default-value="1000" />
        </a-form-item>
        <a-button type="outline" class="w-[88px]">
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
  import { computed, reactive, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';

  import { CreateOrUpdateSystemProjectParams, SystemOrgOption } from '@/models/setting/system/orgAndProject';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentProject?: CreateOrUpdateSystemProjectParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const isEdit = computed(() => props.currentProject && props.currentProject.id);
  const driverOption = ref<SystemOrgOption[]>([]);
  const appStore = useAppStore();
  const licenseStore = useLicenseStore();

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const form = reactive({
    id: '',
    name: '',
    driver: 'mysql',
    url: '',
    username: '',
    password: '',
    poolMax: 1,
    timeout: 1000,
    enable: true,
  });

  const currentVisible = ref(props.visible);

  const isXpack = computed(() => {
    return licenseStore.hasLicense();
  });

  watchEffect(() => {
    currentVisible.value = props.visible;
  });

  const formReset = () => {
    form.name = '';
  };
  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
  };

  const handleBeforeOk = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        Message.success(
          isEdit.value
            ? t('project.environmental.database.updateProjectSuccess')
            : t('project.environmental.database.createProjectSuccess')
        );
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
  const initDriverOption = async () => {
    try {
      const res = [];
      driverOption.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  watchEffect(() => {
    if (isEdit.value && props.currentProject) {
      form.name = props.currentProject.name;
    }
  });
  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (!val) {
        formReset();
      } else {
        initDriverOption();
      }
    }
  );
</script>
