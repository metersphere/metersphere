<template>
  <MsCard simple class="mb-[16px]" auto-height :loading="loading">
    <div class="outer-wrapper">
      <div class="mb-[16px] flex justify-between">
        <div class="font-medium text-[var(--color-text-1)]">{{ t('organization.service.integrationList') }}</div>
      </div>
      <div class="ms-card-wrap">
        <a-scrollbar
          :style="{
            overflow: 'auto',
            height: `calc(100vh - 196px)`,
          }"
        >
          <div v-if="filterList.length" class="list">
            <div v-for="item of filterList" :key="item.key" class="item">
              <div class="flex">
                <span class="float-left mr-2 h-[40px] w-[40px] rounded">
                  <MsIcon :type="item.logo" size="40"></MsIcon>
                </span>
                <div class="flex flex-col justify-start">
                  <p>
                    <span class="mr-4 font-semibold">{{ item.title }}</span>
                    <span v-if="!item.hasConfig" class="ms-enable">{{ t('organization.service.unconfigured') }}</span>
                    <span
                      v-else
                      class="ms-enable active"
                      :style="{
                        background: 'rgb(var(--success-1))',
                        color: 'rgb(var(--success-6))',
                      }"
                      >{{ t('organization.service.configured') }}</span
                    >
                  </p>
                  <p class="mt-2 text-sm text-[var(--color-text-4)]">{{ item.description }}</p>
                </div>
              </div>
              <div class="flex justify-between">
                <a-space>
                  <a-tooltip v-if="!item.hasConfig" :content="t('organization.service.unconfiguredTip')" position="tl">
                    <span>
                      <a-button
                        v-if="!item.hasConfig"
                        type="outline"
                        class="arco-btn-outline--secondary"
                        size="mini"
                        :disabled="
                          !item.hasConfig || !hasAnyPermission(['SYSTEM_PARAMETER_SETTING_QRCODE:READ+UPDATE'])
                        "
                        @click="getValidateHandler(item.key)"
                        >{{ t('organization.service.testLink') }}</a-button
                      ></span
                    >
                  </a-tooltip>
                  <a-button
                    v-else
                    :disabled="!item.hasConfig || !hasAnyPermission(['SYSTEM_PARAMETER_SETTING_QRCODE:READ+UPDATE'])"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="getValidateHandler(item.key)"
                    >{{ t('organization.service.testLink') }}
                  </a-button>
                  <a-button
                    v-if="item.edit"
                    v-permission="['SYSTEM_PARAMETER_SETTING_QRCODE:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="editHandler(item.key)"
                    >{{ t('organization.service.edit') }}
                  </a-button>
                  <a-button
                    v-else
                    v-permission="['SYSTEM_PARAMETER_SETTING_QRCODE:READ+UPDATE']"
                    type="outline"
                    class="arco-btn-outline--secondary"
                    size="mini"
                    @click="editHandler(item.key)"
                    >{{ t('common.add') }}
                  </a-button>
                </a-space>
                <span>
                  <a-tooltip v-if="!item.valid" :content="t('organization.service.unconfiguredTip')" position="br">
                    <span
                      ><a-switch
                        v-model="item.enable"
                        size="small"
                        :disabled="true"
                        @change="(v) => changeStatus(v, item.key)"
                    /></span>
                  </a-tooltip>
                  <a-switch
                    v-else
                    v-model="item.enable"
                    size="small"
                    :disabled="!hasAnyPermission(['SYSTEM_PARAMETER_SETTING_QRCODE:READ+UPDATE'])"
                    @change="(v) => changeStatus(v, item.key)"
                  />
                </span>
              </div>
            </div>
          </div>
          <a-empty v-if="!filterList.length" class="mt-20"></a-empty>
        </a-scrollbar>
      </div>
    </div>
  </MsCard>
  <we-com-modal v-model:visible="showWeComModal" @success="loadList()" />
  <ding-talk-modal v-model:visible="showDingTalkModal" @success="loadList()" />
  <lark-modal v-model:visible="showLarkModal" @success="loadList()" />
  <lark-suite-modal v-model:visible="showLarkSuiteModal" @success="loadList()" />
</template>

<script setup lang="ts">
  import { onBeforeMount, ref } from 'vue';

  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import DingTalkModal from '@/views/setting/system/config/components/dingTalkModal.vue';
  import LarkModal from '@/views/setting/system/config/components/larkModal.vue';
  import LarkSuiteModal from '@/views/setting/system/config/components/larkSuiteModal.vue';
  import WeComModal from '@/views/setting/system/config/components/weComModal.vue';

  import {
    closeValidateDingTalk,
    closeValidateLark,
    closeValidateLarkSuite,
    closeValidateWeCom,
    enableDingTalk,
    enableLark,
    enableLarkSuite,
    enableWeCom,
    getPlatformSourceList,
    validateDingTalkConfig,
    validateLarkConfig,
    validateLarkSuiteConfig,
    validateWeComConfig,
  } from '@/api/modules/setting/qrCode';
  import { useI18n } from '@/hooks/useI18n';
  import { hasAnyPermission } from '@/utils/permission';

  import {
    DingTalkInfo,
    EnableEditorRequest,
    LarkInfo,
    PlatformConfigItem,
    PlatformConfigList,
    PlatformSource,
    PlatformSourceList,
    WeComInfo,
  } from '@/models/setting/qrCode';

  import Message from '@arco-design/web-vue/es/message';

  const { t } = useI18n();

  const filterList = ref<PlatformConfigList>([
    {
      key: 'WE_COM',
      title: t('project.messageManagement.WE_COM'),
      description: '为企业打造的专业办公管理工具',
      enable: false,
      valid: false,
      logo: 'icon-logo_wechat-work',
      edit: false,
      hasConfig: false,
    },
    {
      key: 'DING_TALK',
      title: t('project.messageManagement.DING_TALK'),
      description: '企业级智能移动办公平台',
      enable: false,
      valid: false,
      logo: 'icon-logo_dingtalk',
      edit: false,
      hasConfig: false,
    },
    {
      key: 'LARK',
      title: t('project.messageManagement.LARK'),
      description: '先进企业合作与管理平台',
      enable: false,
      valid: false,
      logo: 'icon-logo_lark',
      edit: false,
      hasConfig: false,
    },
    {
      key: 'LARK_SUITE',
      title: t('project.messageManagement.LARK_SUITE'),
      description: '先进企业合作与管理平台',
      enable: false,
      valid: false,
      logo: 'icon-logo_lark',
      edit: false,
      hasConfig: false,
    },
  ]);
  const data = ref<PlatformSourceList>([]);
  const loading = ref<boolean>(false);
  const showWeComModal = ref<boolean>(false);
  const showDingTalkModal = ref<boolean>(false);
  const showLarkModal = ref<boolean>(false);
  const showLarkSuiteModal = ref<boolean>(false);

  const weComInfo = ref<WeComInfo>({
    corpId: '',
    agentId: '',
    appSecret: '',
    callBack: '',
    enable: false,
    valid: false,
  });

  const dingTalkInfo = ref<DingTalkInfo>({
    agentId: '',
    appKey: '',
    appSecret: '',
    callBack: '',
    enable: false,
    valid: false,
  });

  const larkInfo = ref<LarkInfo>({
    agentId: '',
    appSecret: '',
    callBack: '',
    enable: false,
    valid: false,
  });

  // 集成列表
  const loadList = async () => {
    loading.value = true;
    try {
      data.value = await getPlatformSourceList();
      filterList.value.forEach((filterKey: PlatformConfigItem) => {
        data.value.forEach((dataKey: PlatformSource) => {
          if (filterKey.key === dataKey.platform) {
            filterKey.enable = dataKey.enable;
            filterKey.valid = dataKey.valid;
            filterKey.edit = true;
            filterKey.hasConfig = dataKey.hasConfig;
          }
        });
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  // 添加或配置
  const editHandler = (key: string) => {
    if (key === 'WE_COM') {
      showWeComModal.value = true;
      showDingTalkModal.value = false;
      showLarkModal.value = false;
      showLarkSuiteModal.value = false;
    } else if (key === 'DING_TALK') {
      showWeComModal.value = false;
      showDingTalkModal.value = true;
      showLarkModal.value = false;
      showLarkSuiteModal.value = false;
    } else if (key === 'LARK') {
      showWeComModal.value = false;
      showDingTalkModal.value = false;
      showLarkModal.value = true;
      showLarkSuiteModal.value = false;
    } else if (key === 'LARK_SUITE') {
      showWeComModal.value = false;
      showDingTalkModal.value = false;
      showLarkModal.value = false;
      showLarkSuiteModal.value = true;
    }
  };

  // 外部测试连接
  const getValidateHandler = async (key: string) => {
    loading.value = true;
    try {
      if (key === 'WE_COM') {
        await validateWeComConfig(weComInfo.value);
      } else if (key === 'DING_TALK') {
        await validateDingTalkConfig(dingTalkInfo.value);
      } else if (key === 'LARK') {
        await validateLarkConfig(larkInfo.value);
      } else if (key === 'LARK_SUITE') {
        await validateLarkSuiteConfig(larkInfo.value);
      }
      Message.success(t('organization.service.testLinkStatusTip'));
    } catch (error) {
      if (key === 'WE_COM') {
        await closeValidateWeCom();
      } else if (key === 'DING_TALK') {
        await closeValidateDingTalk();
      } else if (key === 'LARK') {
        await closeValidateLark();
      } else if (key === 'LARK_SUITE') {
        await closeValidateLarkSuite();
      }

      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loadList();
      loading.value = false;
    }
  };

  // 切换状态
  const changeStatus = async (value: string | number | boolean, key: string | undefined) => {
    loading.value = true;
    const message = value ? 'organization.service.enableSuccess' : 'organization.service.closeSuccess';
    const params: EnableEditorRequest = {
      enable: typeof value === 'boolean' ? value : false,
    };
    try {
      if (key === 'WE_COM') {
        await enableWeCom(params);
      } else if (key === 'DING_TALK') {
        await enableDingTalk(params);
      } else if (key === 'LARK') {
        await enableLark(params);
      } else if (key === 'LARK_SUITE') {
        await enableLarkSuite(params);
      }
      Message.success(t(message));
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  onBeforeMount(() => {
    loadList();
  });
</script>

<style scoped lang="less">
  .ms-card-wrap {
    overflow: hidden;
    padding: 8px;
    height: calc(100% - 58px) !important;
    min-height: 300px;
    border-radius: var(--border-radius-small);
    background: var(--color-text-n9);
    .list {
      display: flex;
      flex-wrap: wrap;
      justify-content: flex-start;
      align-content: flex-start;
      width: 100%;
      .item {
        margin: 8px;
        padding: 24px;
        height: 144px;
        border-radius: 4px;
        background: var(--color-text-fff);
        @apply flex flex-col justify-between;
        .ms-enable {
          font-size: 12px;
          border-radius: var(--border-radius-small);
          background: var(--color-text-n9);
          @apply px-2 py-1;
        }
      }
    }

    @media screen and (min-width: 800px) {
      .item {
        flex-basis: calc(50% - 16px);
      }
    }
    @media screen and (min-width: 1160px) {
      .item {
        flex-basis: calc(33.3% - 16px);
      }
    }
    @media screen and (min-width: 1800px) {
      .item {
        flex-basis: calc(25% - 16px);
      }
    }
  }
</style>
