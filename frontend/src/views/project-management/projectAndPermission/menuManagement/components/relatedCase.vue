<template>
  <MsDrawer
    v-model:visible="currentVisible"
    :title="t('project.menu.CASE_RELATED')"
    :destroy-on-close="true"
    :closable="true"
    :mask-closable="true"
    :get-container="false"
    :body-style="{ padding: '0px' }"
    :width="680"
    :ok-loading="okLoading"
    :ok-disabled="okDisabled"
    :ok-permission="['PROJECT_APPLICATION_CASE:UPDATE']"
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
        @mounted="handleMounted"
      />
      <!-- 同步频率 -->
      <a-form-item field="CRON_EXPRESSION" :label="t('project.menu.CRON_EXPRESSION')">
        <MsCronSelect v-model:model-value="form.CRON_EXPRESSION" />
      </a-form-item>
    </a-form>
    <template v-if="platformOption.length" #footerLeft>
      <div class="flex flex-row items-center gap-[4px]">
        <a-tooltip v-if="okDisabled" :content="t('project.menu.defect.enableAfterConfig')">
          <a-switch size="small" type="line" disabled />
        </a-tooltip>
        <a-switch
          v-else
          v-model="form.CASE_ENABLE"
          checked-value="true"
          unchecked-value="false"
          size="small"
          type="line"
        />
        <span class="text-[var(--color-text-1)]">
          {{ t('project.menu.status') }}
        </span>
        <a-tooltip position="tl" :content-style="{ maxWidth: '500px' }">
          <template #content>
            <div class="flex flex-col">
              <div>{{ t('project.menu.demand.enableTip') }}</div>
              <div class="flex flex-nowrap">{{ t('project.menu.demand.closeTip') }}</div>
            </div>
          </template>
          <div>
            <MsIcon
              class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              type="icon-icon-maybe_outlined"
            />
          </div>
        </a-tooltip>
      </div>
    </template>
  </MsDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';

  import MsCronSelect from '@/components/pure/ms-cron-select/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsFormCreate from '@/components/pure/ms-form-create/ms-form-create.vue';
  import type { FormItem, FormRuleItem } from '@/components/pure/ms-form-create/types';

  import {
    getCaseRelatedInfo,
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
    CASE_ENABLE: 'false', // 关联需求开关
    SYNC_ENABLE: 'true', // 同步开关
    CRON_EXPRESSION: '0 0 0 * * ?', // 同步频率
  });

  const formCreateValue = ref<Record<string, any>>({});

  const okDisabled = computed(() => !form.PLATFORM_KEY);

  const emit = defineEmits<{
    (e: 'cancel', shouldSearch: boolean): void;
    (e: 'ok'): void;
  }>();

  const resetForm = () => {
    formRef.value?.resetFields();
    platformItem.value = [];
    platformRules.value = [];
    formCreateValue.value = {};
    sessionStorage.removeItem('platformKey');
  };

  const handleCancel = (shouldSearch: boolean) => {
    emit('cancel', shouldSearch);
    sessionStorage.removeItem('platformKey');
    form.PLATFORM_KEY = '';
    fApi.value?.clearValidateState();
  };
  const handlePlatformChange = async (value: SelectValue) => {
    platformRules.value = [];
    try {
      if (value) {
        const res = await getPlatformInfo(value as string, MenuEnum.caseManagement);
        if (formCreateValue.value) {
          res.formItems.forEach((item) => {
            if (formCreateValue.value[item.name]) {
              item.value = formCreateValue.value[item.name];
            }
          });
        }
        resetForm();
        platformRules.value = res.formItems;
        sessionStorage.setItem('platformKey', value as string);
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
        Message.success(t('common.linkSuccess'));
        handleCancel(true);
        emit('ok');
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
  // 获取关联需求信息
  const initDetailInfo = async () => {
    try {
      await initPlatformOption();
      const res = await getCaseRelatedInfo(currentProjectId.value);
      if (res && res.platform_key) {
        formCreateValue.value = JSON.parse(res.demand_platform_config);
        // 如果平台key存在调用平台change拉取插件字段
        await handlePlatformChange(res.platform_key);
        form.CASE_ENABLE = res.case_enable;
        form.PLATFORM_KEY = res.platform_key;
        form.SYNC_ENABLE = res.sync_enable;
        form.CRON_EXPRESSION = res.cron_expression;
      }
    } catch (e) {
      // eslint-disable-next-line no-console
      console.log(e);
    }
  };

  /**
   * 初始化回显字段值
   */
  function setValue() {
    const tempObj: Record<string, any> = {};
    platformRules.value.forEach((item) => {
      tempObj[item.name] = item.value;
    });
    fApi.value?.setValue({ ...tempObj });
  }

  function handleMounted() {
    setValue();
  }

  watch(
    () => props.visible,
    (val) => {
      currentVisible.value = val;
      if (val) {
        initDetailInfo();
      } else {
        formRef.value?.resetFields();
        platformRules.value = [];
      }
    }
  );
</script>
