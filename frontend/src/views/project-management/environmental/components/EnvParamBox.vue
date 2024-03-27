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
        <a-form-item
          class="mb-[16px]"
          asterisk-position="end"
          field="description"
          :label="t('project.environmental.desc')"
        >
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

      <a-tabs v-if="contentTabList.length" v-model:active-key="activeKey" class="no-content" @change="handleTabChange">
        <a-tab-pane v-for="item of contentTabList" :key="item.value" :title="t(item.label)" />
        <a-tab-pane key="displaySetting" :title="t('project.environmental.displaySetting')" />
      </a-tabs>
    </div>
    <a-divider
      :margin="0"
      :class="{
        '!mb-[16px]': !(activeKey === 'pre' || activeKey === 'post'),
      }"
    />
    <div class="content h-full">
      <EnvParamsTab v-if="activeKey === 'envParams'" />
      <HttpTab v-else-if="activeKey === 'http'" />
      <DataBaseTab v-else-if="activeKey === 'database'" />
      <HostTab v-else-if="activeKey === 'host'" />
      <!-- <PreTab v-else-if="activeKey === 'pre'" />
      <PostTab v-else-if="activeKey === 'post'" /> -->
      <div v-else-if="activeKey === 'pre' || activeKey === 'post'" class="h-full">
        <PreAndPostTab :active-type="activeKey" />
      </div>

      <AssertTab v-else-if="activeKey === 'assert'" />
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
  import { Message } from '@arco-design/web-vue';

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
  import useProjectEnvStore from '@/store/modules/setting/useProjectEnvStore';
  import { hasAnyPermission } from '@/utils/permission';

  import { ContentTabItem, EnvPluginListItem } from '@/models/projectManagement/environmental';

  const { setState } = useLeaveUnSaveTip();
  setState(false);
  const emit = defineEmits<{
    (e: 'ok', envId: string | undefined): void;
    (e: 'resetEnv'): void;
  }>();

  const activeKey = ref('envParams');
  const envForm = ref();
  const { t } = useI18n();
  const loading = ref(false);
  const tabSettingVisible = ref(false);
  const appStore = useAppStore();

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

  const contentTabList = ref<ContentTabItem[]>([]);
  const isDisabled = computed(() => !hasAnyPermission(['PROJECT_ENVIRONMENT:READ+UPDATE']));

  const sourceTabList = [
    {
      value: 'envParams',
      label: 'project.environmental.envParams',
      canHide: false,
      isShow: true,
    },
    {
      value: 'http',
      label: 'project.environmental.HTTP',
      canHide: true,
      isShow: true,
    },
    {
      value: 'database',
      label: 'project.environmental.database',
      canHide: true,
      isShow: true,
    },
    {
      value: 'host',
      label: 'project.environmental.HOST',
      canHide: true,
      isShow: true,
    },
    {
      value: 'pre',
      label: 'project.environmental.pre',
      canHide: true,
      isShow: true,
    },
    {
      value: 'post',
      label: 'project.environmental.post',
      canHide: true,
      isShow: true,
    },
    {
      value: 'assert',
      label: 'project.environmental.assert',
      canHide: true,
      isShow: true,
    },
  ];

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
  await store.initContentTabList([...sourceTabList, ...pluginTabList.value]);
  contentTabList.value = ((await store.getContentTabList()) || []).filter((item) => item.isShow);
  // 插件状态存储

  const handleReset = () => {
    envForm.value?.resetFields();
    emit('resetEnv');
  };

  const handleSave = async () => {
    await envForm.value?.validate(async (valid) => {
      if (!valid) {
        try {
          loading.value = true;
          store.currentEnvDetailInfo.mock = true;
          await updateOrAddEnv({ fileList: [], request: store.currentEnvDetailInfo });
          setState(true);

          Message.success(store.currentEnvDetailInfo.id ? t('common.updateSuccess') : t('common.saveSuccess'));
          emit('ok', store.currentEnvDetailInfo.id);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          loading.value = false;
        }
      }
    });
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
    if (key === 'displaySetting') {
      tabSettingVisible.value = true;
    }
  };
</script>

<style lang="less" scoped>
  .page {
    position: relative;
    transform: scale3d(1, 1, 1);
    height: 100%;
    .header {
      padding: 24px 24px 0;
    }
    .content {
      overflow-y: auto;
      padding: 0 24px;
      height: 100%;
      max-height: calc(100% - 320px);
      background-color: #ffffff;
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
      background-color: #ffffff;
      box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
      gap: 16px;
    }
  }
</style>
