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
        {{ t('system.project.updateProject') }}
        <span class="text-[var(--color-text-4)]">({{ props.currentProject?.name }})</span>
      </span>
      <span v-else>
        {{ t('system.project.createProject') }}
      </span>
    </template>
    <div class="form">
      <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
        <a-form-item
          field="name"
          required
          :label="t('system.project.name')"
          :rules="[{ required: true, message: t('system.project.projectNameRequired') }]"
        >
          <a-input v-model="form.name" allow-clear :placeholder="t('system.project.projectNamePlaceholder')" />
        </a-form-item>
        <a-form-item field="organizationId" :label="t('system.project.affiliatedOrg')">
          <a-select
            v-model="form.organizationId"
            disabled
            allow-search
            allow-clear
            :options="affiliatedOrgOption"
            :default-value="isXpack ? '' : '100001'"
            :placeholder="t('system.project.affiliatedOrgPlaceholder')"
            :field-names="{ label: 'name', value: 'id' }"
          >
          </a-select>
        </a-form-item>
        <a-form-item field="userIds" :label="t('system.project.projectAdmin')">
          <MsUserSelector
            v-model:value="form.userIds"
            :type="UserRequestTypeEnum.ORGANIZATION_PROJECT_ADMIN"
            placeholder="system.project.projectAdminPlaceholder"
            :load-option-params="{
              organizationId: currentOrgId,
            }"
          />
        </a-form-item>
        <a-form-item field="module" :label="t('system.project.moduleSetting')">
          <a-checkbox-group v-model="form.moduleIds" :options="moduleOption">
            <template #label="{ data }">
              <span>{{ t(data.label) }}</span>
            </template>
          </a-checkbox-group>
        </a-form-item>
        <a-form-item field="resourcePool" :label="t('system.project.resourcePool')">
          <MsSystemPool v-model:modelValue="form.resourcePoolIds" :organization-id="currentOrgId" />
        </a-form-item>
        <a-form-item field="description" :label="t('system.organization.description')">
          <a-textarea
            v-model="form.description"
            :placeholder="t('system.organization.descriptionPlaceholder')"
            allow-clear
            :auto-size="{ minRows: 1 }"
          />
        </a-form-item>
      </a-form>
    </div>
    <template #footer>
      <div class="flex flex-row justify-between">
        <div class="flex flex-row items-center gap-[4px]">
          <a-switch v-model="form.enable" size="small" />
          <span>{{ t('system.organization.status') }}</span>
          <a-tooltip :content="t('system.project.createTip')" position="top">
            <MsIcon type="icon-icon-maybe_outlined" class="text-[var(--color-text-4)]" />
          </a-tooltip>
        </div>
        <div class="flex flex-row gap-[14px]">
          <a-button type="secondary" :loading="loading" @click="handleCancel(false)">
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
  import { computed, reactive, ref, watchEffect } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsSystemPool from '@/components/business/ms-system-pool/MsSystemPool.vue';
  import MsUserSelector from '@/components/business/ms-user-selector/index.vue';
  import { UserRequestTypeEnum } from '@/components/business/ms-user-selector/utils';

  import { createOrUpdateProjectByOrg, getSystemOrgOption } from '@/api/modules/setting/organizationAndProject';
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
  const affiliatedOrgOption = ref<SystemOrgOption[]>([]);
  const appStore = useAppStore();
  const currentOrgId = computed(() => appStore.currentOrgId);
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
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const form = reactive<CreateOrUpdateSystemProjectParams>({
    name: '',
    userIds: [],
    organizationId: currentOrgId.value,
    description: '',
    resourcePoolIds: [],
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

  const formReset = () => {
    form.name = '';
    form.userIds = [];
    form.organizationId = currentOrgId.value;
    form.description = '';
    form.enable = true;
    form.moduleIds = [];
    form.resourcePoolIds = [];
  };
  const handleCancel = (shouldSearch: boolean) => {
    formReset();
    emit('cancel', shouldSearch);
  };

  const handleBeforeOk = async () => {
    await formRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (errors) {
        return;
      }
      try {
        loading.value = true;
        await createOrUpdateProjectByOrg({ id: props.currentProject?.id, ...form });
        Message.success(
          isEdit.value ? t('system.project.updateProjectSuccess') : t('system.project.createProjectSuccess')
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
    if (isEdit.value && props.currentProject) {
      form.id = props.currentProject.id;
      form.name = props.currentProject.name;
      form.description = props.currentProject.description;
      form.enable = props.currentProject.enable;
      form.userIds = props.currentProject.userIds;
      form.organizationId = props.currentProject.organizationId;
      form.moduleIds = props.currentProject.moduleIds;
      form.resourcePoolIds = props.currentProject.resourcePoolIds;
    }
  });
</script>
