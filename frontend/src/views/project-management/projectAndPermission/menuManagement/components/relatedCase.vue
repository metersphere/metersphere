<template>
  <MsDrawer
    v-model:visible="currentVisible"
    :title="t('project.menu.CASE_RELATED')"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="false"
    :get-container="false"
    :body-style="{ padding: '0px' }"
    :width="680"
    :ok-loading="okLoading"
    :ok-disabled="okDisabled"
    @cancel="handleCancel(false)"
    @confirm="handleConfirm"
  >
    <a-form ref="formRef" class="rounded-[4px]" :model="form" layout="vertical">
      <a-form-item field="platformKey" :label="t('project.menu.platformLabel')">
        <a-select
          v-model="form.PLATFORM_KEY"
          allow-clear
          :disabled="platformDisabled"
          :options="platformOption"
          :placeholder="platformDisabled ? t('project.menu.platformPlaceholder') : ''"
          :field-names="{ label: 'name', value: 'id' }"
          @change="handlePlatformChange"
        >
        </a-select>
      </a-form-item>
      <!-- form-create -->
      <MsFormCreate
        v-if="platformRules && platformRules.length"
        v-model:api="fApi"
        v-model:form-item="platformItem"
        :form-rule="platformRules"
      />
    </a-form>
    <template v-if="platformOption.length" #footerLeft>
      <div class="flex flex-row items-center gap-[4px]">
        <a-tooltip v-if="okDisabled" :content="t('project.menu.defect.enableAfterConfig')">
          <a-switch size="small" disabled />
        </a-tooltip>
        <a-switch v-else v-model="form.SYNC_ENABLE" size="small" />
        <span class="text-[var(--color-text-1)]">
          {{ t('project.menu.status') }}
        </span>
        <a-tooltip position="tl" :content-style="{ maxWidth: '500px' }">
          <template #content>
            <div class="flex flex-col">
              <div>{{ t('project.menu.defect.enableTip') }}</div>
              <div class="flex flex-nowrap">{{ t('project.menu.defect.closeTip') }}</div>
            </div>
          </template>
          <div>
            <MsIcon class="ml-[4px] text-[var(--color-text-4)]" type="icon-icon-maybe_outlined" />
          </div>
        </a-tooltip>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';

  import {
    getPlatformInfo,
    getPlatformOptions,
    postSaveRelatedCase,
  } from '@/api/modules/project-management/menuManagement';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import { PoolOption, SelectValue } from '@/models/projectManagement/menuManagement';
  import { MenuEnum } from '@/enums/commonEnum';

  const { t } = useI18n();
  const props = defineProps<{
    visible: boolean;
  }>();
  const currentVisible = ref<boolean>(props.visible);
  const platformOption = ref<PoolOption[]>([]);
  const fApi = ref<any>({});

  const appStore = useAppStore();
  const currentProjectId = computed(() => appStore.currentProjectId);
  const currentOrgId = computed(() => appStore.currentOrgId);
  const platformDisabled = computed(() => platformOption.value.length === 0);
  const okLoading = ref(false);

  const formRef = ref<FormInstance>();
  const platformRules = ref<FormItem[]>([]);
  const platformItem = ref<FormRuleItem[]>([]);

  const form = reactive({
    PLATFORM_KEY: '',
    SYNC_ENABLE: 'false', // 同步开关
  });

  const okDisabled = computed(() => !form.PLATFORM_KEY);

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
  }>();

  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
    sessionStorage.removeItem('platformKey');
    fApi.value.clearValidateState();
  };
  const handlePlatformChange = async (value: SelectValue) => {
    try {
      if (value) {
        const res = await getPlatformInfo(value as string, MenuEnum.caseManagement);
        platformRules.value = res.formItems;
      } else {
        platformRules.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  const handleConfirm = async () => {
    await fApi.value?.submit(async (formData: FormData) => {
      try {
        okLoading.value = true;
        await postSaveRelatedCase(
          { ...form, DEMAND_PLATFORM_CONFIG: JSON.stringify(formData) },
          currentProjectId.value
        );
        Message.success(t('common.createSuccess'));
        handleCancel(true);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error(error);
      } finally {
        okLoading.value = false;
      }
    });
  };

  const initPlatformOption = async () => {
    try {
      const res = await getPlatformOptions(currentOrgId.value, MenuEnum.caseManagement);
      if (res) {
        platformOption.value = res;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (val) {
        initPlatformOption();
      } else {
        formRef.value?.resetFields();
        platformRules.value = [];
      }
    }
  );
</script>
