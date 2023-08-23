<template>
  <a-modal
    v-model:visible="currentVisible"
    width="680px"
    class="ms-modal-form ms-modal-medium"
    :ok-text="isEdit ? t('common.update') : t('common.create')"
    unmount-on-close
    @cancel="handleCancel"
  >
    <template #title>
      <span v-if="isEdit">
        {{ t('system.project.updateProject') }}
        <span class="text-[var(--color-text-4)]">({{ props.currentProject?.name }})</span>
      </span>
      <span v-else>
        {{ t('system.project.createProject') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" :model="form" size="large" :style="{ width: '600px' }" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('system.project.name')"
          :rules="[{ required: true, message: t('system.project.projectNameRequired') }]"
        >
          <a-input v-model="form.name" :placeholder="t('system.project.projectNamePlaceholder')" />
        </a-form-item>
        <a-form-item
          required
          field="organizationId"
          :label="t('system.project.affiliatedOrg')"
          :rules="[{ required: true, message: t('system.project.affiliatedOrgRequired') }]"
        >
          <a-select
            v-model="form.organizationId"
            :disabled="!isXpack"
            allow-search
            :options="affiliatedOrgOption"
            :default-value="isXpack ? '' : 'default_organization'"
            :placeholder="t('system.project.affiliatedOrgPlaceholder')"
            :field-names="{ label: 'name', value: 'id' }"
          >
          </a-select>
        </a-form-item>
        <a-form-item field="userIds" :label="t('system.project.projectAdmin')">
          <MsUserSelector v-model:value="form.userIds" placeholder="system.project.projectAdminPlaceholder" />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-input v-model="form.description" :placeholder="t('system.organization.descriptionPlaceholder')" />
        </a-form-item>
        <a-form-item field="module" :label="t('system.organization.description')">
          <a-checkbox-group v-model="form.moduleIds" :options="moduleOption">
            <template #label="{ data }">
              <span>{{ t(data.label) }}</span>
            </template>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row justify-between">
        <div class="flex flex-row items-center gap-[4px]">
          <a-switch v-model="form.enable" />
          <span>{{ t('system.organization.status') }}</span>
          <a-tooltip :content="t('system.project.createTip')" position="top">
            <MsIcon type="icon-icon-maybe_outlined" class="text-[var(--color-text-4)]" />
          </a-tooltip>
        </div>
        <div class="flex flex-row gap-[14px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button type="primary" :loading="loading" @click="handleBeforeOk">
            {{ isEdit ? t('common.confirm') : t('common.create') }}
          </a-button>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<script lang="ts" setup>
  import { useI18n } from '@/hooks/useI18n';
  import { reactive, ref, watchEffect, computed } from 'vue';
  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { createOrUpdateProject, getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
  import { Message } from '@arco-design/web-vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import { CreateOrUpdateSystemProjectParams, SystemOrgOption } from '@/models/setting/system/orgAndProject';
  import useLicenseStore from '@/store/modules/setting/license';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
    currentProject?: CreateOrUpdateSystemProjectParams;
  }>();

  const formRef = ref<FormInstance>();

  const loading = ref(false);
  const isEdit = computed(() => !!props.currentProject?.id);
  const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
  const licenseStore = useLicenseStore();
  const moduleOption = [
    { label: 'menu.workbench', value: 'workstation' },
    { label: 'menu.testPlan', value: 'testPlan' },
    { label: 'menu.bugManagement', value: 'bugManagement' },
    { label: 'menu.featureTest', value: 'caseManagement' },
    { label: 'menu.apiTest', value: 'apiTest' },
    { label: 'menu.uiTest', value: 'uiTest' },
    { label: 'menu.performanceTest', value: 'loadTest' },
  ];

  const emit = defineEmits<{
    (e: 'cancel'): void;
  }>();

  const form = reactive<CreateOrUpdateSystemProjectParams>({
    name: '',
    userIds: [],
    organizationId: '',
    description: '',
    enable: true,
    moduleIds: [],
  });

  const currentVisible = ref(props.visible);

  const isXpack = computed(() => {
    return licenseStore.hasLicense();
  });

  watchEffect(() => {
    currentVisible.value = props.visible;
  });
  const handleCancel = () => {
    formRef.value?.resetFields();
    emit('cancel');
  };

  const handleBeforeOk = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        await createOrUpdateProject({ id: props.currentProject?.id, ...form });
        Message.success(
          isEdit.value
            ? t('system.organization.updateOrganizationSuccess')
            : t('system.organization.createOrganizationSuccess')
        );
        handleCancel();
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        loading.value = false;
      }
    });
  };
  const initAffiliatedOrgOption = async () => {
    try {
      const res = await getSystemOrgOption();
      affiliatedOrgOption.value = res;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error(error);
    }
  };
  watchEffect(() => {
    initAffiliatedOrgOption();
    if (props.currentProject) {
      form.id = props.currentProject.id;
      form.name = props.currentProject.name;
      form.description = props.currentProject.description;
      form.enable = props.currentProject.enable;
      form.userIds = props.currentProject.userIds;
      form.organizationId = props.currentProject.organizationId;
      form.moduleIds = props.currentProject.moduleIds;
    }
  });
</script>
@/api/modules/setting/organizationAndProject
