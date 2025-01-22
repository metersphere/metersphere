<template>
  <div class="page">
    <div class="header">
      <a-form ref="envForm" layout="vertical" :model="form" :disabled="isDisabled">
        <a-form-item
          class="mb-[16px]"
          asterisk-position="end"
          field="name"
          :label="t('project.environmental.envName')"
          :rules="[{ required: true, message: t('project.environmental.envNameRequired') }]"
        >
          <a-input
            v-model="form.name"
            :max-length="255"
            :placeholder="t('project.environmental.envNamePlaceholder')"
            @blur="store.currentEnvDetailInfo.name = form.name"
          />
        </a-form-item>
        <a-form-item class="mb-[16px]" asterisk-position="end" field="description" :label="t('common.desc')">
          <a-textarea
            v-model="form.description"
            :placeholder="t('project.environmental.envDescPlaceholder')"
            allow-clear
            :auto-size="{ minRows: 1 }"
            :max-length="1000"
            @blur="store.currentEnvDetailInfo.description = form.description"
          />
        </a-form-item>
      </a-form>
      <MsTab
        v-model:active-key="activeKey"
        :content-tab-list="contentTabList"
        :get-text-func="getTabBadge"
        class="no-content relative mb-[16px] border-b"
        @change="handleTabChange"
      />
    </div>
    <div class="content w-full">
      <div v-show="activeKey === EnvTabTypeEnum.ENVIRONMENT_PARAM">
        <EnvParamsTab v-model:keyword="envKeyword" />
      </div>
      <HttpTab v-if="activeKey === EnvTabTypeEnum.ENVIRONMENT_HTTP" />
      <DataBaseTab v-else-if="activeKey === EnvTabTypeEnum.ENVIRONMENT_DATABASE" />
      <HostTab v-else-if="activeKey === EnvTabTypeEnum.ENVIRONMENT_HOST" ref="hostTabRef" />
      <div
        v-else-if="activeKey === EnvTabTypeEnum.ENVIRONMENT_PRE || activeKey === EnvTabTypeEnum.ENVIRONMENT_POST"
        class="h-full"
      >
        <PreAndPostTab :active-type="activeKey" />
      </div>

      <AssertTab v-else-if="activeKey === EnvTabTypeEnum.ENVIRONMENT_ASSERT" />
      <template v-for="item in envPluginList" :key="item.pluginId">
        <PluginTab
          v-if="activeKey === item.pluginId"
          :model-value="store.currentEnvDetailInfo.config.pluginConfigMap[item.pluginId]"
          :plugin-id="item.pluginId"
          :script="item.script"
          :fields="item.script.fields"
          @update:model-value="store.currentEnvDetailInfo.config.pluginConfigMap[item.pluginId] = $event"
        />
      </template>
    </div>

    <div v-permission="['PROJECT_ENVIRONMENT:READ+UPDATE']" class="footer" :style="{ width: '100%' }">
      <a-button :disabled="loading" @click="handleReset">{{ t('common.cancel') }}</a-button>
      <a-button type="primary" :loading="loading" @click="handleSave">{{ t('common.save') }}</a-button>
    </div>
  </div>
  <TabSettingDrawer v-model:visible="tabSettingVisible" @init-data="initTab" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import { cloneDeep, isEqual } from 'lodash-es';

  import MsTab from '@/components/pure/ms-tab/index.vue';
  import TabSettingDrawer from './common/TabSettingDrawer.vue';
  import AssertTab from './envParams/AssertTab.vue';
  import DataBaseTab from './envParams/DatabaseTab.vue';
  import EnvParamsTab from './envParams/EnvParamsTab.vue';
  import HostTab from './envParams/HostTab.vue';
  import HttpTab from './envParams/HttpTab.vue';
  import PluginTab from './envParams/PluginTab.vue';
  import PreAndPostTab from './envParams/preAndPost.vue';

  import { getEnvPlugin, updateOrAddEnv } from '@/api/modules/project-management/envManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useLeaveUnSaveTip from '@/hooks/useLeaveUnSaveTip';
  import { useAppStore } from '@/store';
  import useProjectEnvStore, { ALL_PARAM } from '@/store/modules/setting/useProjectEnvStore';
  import { hasAnyPermission } from '@/utils/permission';

  import { ContentTabItem, EnvPluginListItem } from '@/models/projectManagement/environmental';
  import { EnvTabTypeEnum } from '@/enums/envEnum';

  import { defaultHeaderParamsItem } from '@/views/api-test/components/config';
  import { filterKeyValParams } from '@/views/api-test/components/utils';

  const leaveTitle = 'common.tip';
  const leaveContent = 'apiTestDebug.unsavedLeave';

  const { setIsSave } = useLeaveUnSaveTip({
    leaveTitle,
    leaveContent,
    tipType: 'warning',
  });

  const emit = defineEmits<{
    (e: 'ok', envId: string | undefined): void;
    (e: 'resetEnv'): void;
  }>();

  const activeKey = ref<string>(EnvTabTypeEnum.ENVIRONMENT_PARAM);
  const envForm = ref<FormInstance>();
  const { t } = useI18n();
  const loading = ref(false);
  const tabSettingVisible = ref(false);
  const appStore = useAppStore();
  const hostTabRef = ref();

  const store = useProjectEnvStore();
  const envPluginList = ref<EnvPluginListItem[]>([]);
  const pluginTabList = computed(() =>
    envPluginList.value.map((item) => ({
      label: item.script.tabName,
      value: item.pluginId,
      canHide: true,
      isShow: true,
    }))
  );

  const form = reactive({
    name: '',
    description: '',
  });

  const envKeyword = ref('');

  const contentTabList = ref<ContentTabItem[]>([]);
  const isDisabled = computed(() => !hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']));

  const settingList = [
    {
      value: EnvTabTypeEnum.ENVIRONMENT_SETTING,
      label: t('project.environmental.displaySetting'),
      canHide: true,
      isShow: true,
    },
  ];

  const sourceTabList = [
    {
      value: EnvTabTypeEnum.ENVIRONMENT_PARAM,
      label: t('project.environmental.envParams'),
      canHide: false,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_HTTP,
      label: t('project.environmental.HTTP'),
      canHide: true,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_DATABASE,
      label: t('project.environmental.database'),
      canHide: true,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_HOST,
      label: t('project.environmental.HOST'),
      canHide: true,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_PRE,
      label: t('project.environmental.pre'),
      canHide: true,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_POST,
      label: t('project.environmental.post'),
      canHide: true,
      isShow: true,
    },
    {
      value: EnvTabTypeEnum.ENVIRONMENT_ASSERT,
      label: t('project.environmental.assert'),
      canHide: true,
      isShow: true,
    },
  ];

  const isChangeEnvValue = computed(() => {
    return store.currentId === ALL_PARAM
      ? isEqual(store.allParamDetailInfo, store.backupAllParamDetailInfo)
      : isEqual(store.currentEnvDetailInfo, store.backupEnvDetailInfo);
  });

  function getParameters(isNew = false) {
    const paramsConfig = cloneDeep(store.currentEnvDetailInfo.config);

    const httpConfigList = paramsConfig.httpConfig.map((e) => {
      return {
        ...e,
        headers: filterKeyValParams(e.headers, defaultHeaderParamsItem, true).validParams,
      };
    });
    if (isNew) {
      store.currentEnvDetailInfo.name = store.currentEnvDetailInfo.name
        ? store.currentEnvDetailInfo.name
        : t('project.environmental.newEnv');
    }

    return {
      ...cloneDeep(store.currentEnvDetailInfo),
      config: {
        ...paramsConfig,
        httpConfig: httpConfigList,
      },
    };
  }

  const saveCallBack = async (isNew = false) => {
    // 校验通过回调保存
    loading.value = true;
    store.currentEnvDetailInfo.mock = true;
    await updateOrAddEnv({
      fileList: [],
      request: getParameters(isNew),
    });
    setIsSave(true);
    loading.value = false;
    Message.success(store.currentEnvDetailInfo.id ? t('common.updateSuccess') : t('common.saveSuccess'));
    emit('ok', store.currentEnvDetailInfo.id);
  };

  const handleSave = async () => {
    await envForm.value?.validate(async (valid) => {
      if (!valid) {
        try {
          // 保存Host-Tab的数据(目前只加了Host)
          if (activeKey.value === EnvTabTypeEnum.ENVIRONMENT_HOST) {
            hostTabRef.value?.validateForm(saveCallBack);
          } else {
            envKeyword.value = '';
            await nextTick();
            await saveCallBack();
          }
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
  };

  defineExpose({
    saveCallBack,
  });

  // 初始化插件
  const initPlugin = async () => {
    try {
      envPluginList.value = await getEnvPlugin(appStore.currentProjectId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };
  await initPlugin();
  await store.initContentTabList([...sourceTabList, ...pluginTabList.value, ...settingList]);
  contentTabList.value = ((await store.getContentTabList()) || []).filter((item) => item.isShow);
  // 插件状态存储

  const handleReset = () => {
    envForm.value?.resetFields();
    emit('resetEnv');
  };

  watchEffect(() => {
    if (store.currentId) {
      store.initEnvDetail();
      // initPlugin();
    }
  });

  watchEffect(() => {
    if (store.currentEnvDetailInfo) {
      const { currentEnvDetailInfo } = store;
      form.name = currentEnvDetailInfo.name;
      form.description = currentEnvDetailInfo.description as string;
    }
  });

  const initTab = async () => {
    tabSettingVisible.value = false;
    const tmpArr = (await store.getContentTabList()) || [];
    contentTabList.value = tmpArr.filter((item) => item.isShow);
    if (contentTabList.value.length) {
      activeKey.value = contentTabList.value[0].value;
    }
  };

  const handleTabChange = (key: string | number) => {
    if (key === 'SETTING') {
      tabSettingVisible.value = true;
    }
    if (key === EnvTabTypeEnum.ENVIRONMENT_PARAM) {
      envKeyword.value = '';
    }
  };

  function getTabBadge(tabKey: string) {
    switch (tabKey) {
      case EnvTabTypeEnum.ENVIRONMENT_ASSERT:
        const assertLength = store.currentEnvDetailInfo.config.assertionConfig.assertions.length;
        return `${assertLength > 99 ? '99+' : assertLength || ''}`;
      default:
        return '';
    }
  }

  watchEffect(() => {
    if (isChangeEnvValue.value) {
      setIsSave(true);
    } else {
      setIsSave(false);
    }
  });
</script>

<style lang="less" scoped>
  .page {
    position: relative;
    transform: scale3d(1, 1, 1);
    height: 100%;
    .header {
      padding: 16px 16px 0;
    }
    .content {
      .ms-scroll-bar();

      overflow-y: auto;
      padding: 0 16px;
      height: 100%;
      max-height: calc(100% - 320px);
      background-color: var(--color-text-fff);
    }
    .no-content {
      :deep(.arco-tabs-content) {
        padding-top: 0;
      }
    }
    .footer {
      position: fixed;
      right: 0;
      bottom: 0;
      z-index: 999;
      display: flex;
      justify-content: flex-end;
      padding: 24px;
      background-color: var(--color-text-fff);
      box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
      gap: 16px;
    }
  }
</style>
