<template>
  <div>
    <!-- special-height的119: 上面卡片高度103 +上面卡片高度mb的16 -->
    <MsCard :min-width="1060" :special-height="119" simple>
      <a-alert v-if="!getIsVisited()" :show-icon="false" class="mb-[16px]" closable @close="addVisited">
        {{ t('project.messageManagement.botListTips') }}
        <template #close-element>
          <span class="text-[14px]">{{ t('project.messageManagement.notRemind') }}</span>
        </template>
      </a-alert>
      <a-button v-permission="['PROJECT_MESSAGE:READ+ADD']" type="primary" class="mb-[16px]" @click="handleCreateClick">
        {{ t('project.messageManagement.createBot') }}
      </a-button>
      <div
        ref="robotListContainerRef"
        :class="['robot-list-container', containerStatusClass]"
        :style="{ height: `calc(100% - ${getIsVisited() ? 48 : 104}px)` }"
      >
        <div ref="robotListRef" class="robot-list">
          <div v-for="robot of botList" :key="robot.id" class="robot-card">
            <div class="flex">
              <MsIcon
                :type="IconMap[robot.platform]"
                class="mr-[8px] h-[40px] w-[40px] bg-[var(--color-text-n9)] p-[8px] text-[rgb(var(--primary-5))]"
              />
              <div class="flex flex-1 flex-col overflow-hidden">
                <a-tooltip position="tl" :content="robot.name">
                  <div class="one-line-text font-medium text-[var(--color-text-1)]">{{ robot.name }}</div>
                </a-tooltip>
                <div
                  v-if="['IN_SITE', 'MAIL'].includes(robot.platform)"
                  class="text-[12px] leading-[16px] text-[var(--color-text-4)]"
                >
                  {{ robot.description }}
                </div>
                <div v-else class="flex flex-wrap items-center text-[12px] leading-[16px] text-[var(--color-text-4)]">
                  <a-tooltip
                    v-if="translateTextToPX(robot.createUser) > 200"
                    position="tl"
                    mini
                    :content="robot.createUser"
                  >
                    <span class="one-line-text" style="max-width: 200px">{{ robot.createUser }}</span>
                  </a-tooltip>
                  <span v-else class="one-line-text" style="max-width: 200px">{{ robot.createUser }}</span>
                  <span class="mr-[16px]">
                    {{
                      `${t('project.messageManagement.createAt')} ${dayjs(robot.createTime).format(
                        'YYYY-MM-DD HH:mm:ss'
                      )}`
                    }}
                  </span>
                  <a-tooltip
                    v-if="translateTextToPX(robot.updateUser) > 200"
                    position="tl"
                    mini
                    :content="robot.updateUser"
                  >
                    <span class="one-line-text" style="max-width: 200px">{{ robot.updateUser }}</span>
                  </a-tooltip>
                  <span v-else class="one-line-text" style="max-width: 200px">{{ robot.updateUser }}</span>
                  {{
                    ` ${t('project.messageManagement.updateAt')} ${dayjs(robot.updateTime).format(
                      'YYYY-MM-DD HH:mm:ss'
                    )}`
                  }}
                </div>
              </div>
            </div>
            <div class="flex items-center justify-between leading-[24px]">
              <div v-if="!['IN_SITE', 'MAIL'].includes(robot.platform)">
                <a-button
                  v-permission="['PROJECT_MESSAGE:READ+UPDATE']"
                  type="outline"
                  size="mini"
                  class="arco-btn-outline--secondary mr-[8px]"
                  @click="editRobot(robot)"
                >
                  {{ t('common.edit') }}
                </a-button>
                <a-button
                  v-permission="['PROJECT_MESSAGE:READ+DELETE']"
                  type="outline"
                  size="mini"
                  class="arco-btn-outline--secondary"
                  @click="delRobot(robot)"
                >
                  {{ t('common.delete') }}
                </a-button>
              </div>
              <a-switch
                v-model:model-value="robot.enable"
                v-permission="['PROJECT_MESSAGE:READ']"
                size="small"
                class="ml-auto"
                type="line"
                :disabled="!hasAnyPermission(['PROJECT_MESSAGE:READ+UPDATE'])"
                @change="handleEnableIntercept(robot)"
              />
            </div>
          </div>
        </div>
      </div>
    </MsCard>
    <MsDrawer
      v-model:visible="showDetailDrawer"
      :width="960"
      :title="isEdit ? t('project.messageManagement.updateBot') : t('project.messageManagement.createBot')"
      :ok-loading="drawerLoading"
      :show-continue="!isEdit"
      :ok-text="isEdit ? t('common.update') : t('common.create')"
      :save-continue-text="t('project.messageManagement.saveContinueText')"
      @confirm="handleDrawerConfirm"
      @continue="handleDrawerConfirm(true)"
      @cancel="handleDrawerCancel"
    >
      <a-form ref="robotFormRef" :model="robotForm" layout="vertical">
        <a-form-item :label="t('project.messageManagement.choosePlatform')" field="platform">
          <div class="grid w-full grid-cols-4 gap-[16px]">
            <div
              v-for="platform of editPlatformList"
              :key="platform.key"
              :class="['platform-card', robotForm.platform === platform.key ? 'platform-card--active' : '']"
              @click="robotForm.platform = platform.key"
            >
              <MsIcon
                :type="IconMap[platform.key]"
                class="h-[32px] w-[32px] rounded-[var(--border-radius-small)] bg-[var(--color-text-n9)] p-[4px]"
              />
              {{ platform.name }}
            </div>
            <div
              v-xpack
              :class="['platform-card-custom', robotForm.platform === 'CUSTOM' ? 'platform-card--active' : '']"
              @click="robotForm.platform = 'CUSTOM'"
            >
              <MsIcon type="icon-icon_add_outlined" size="14" class="text-[rgb(var(--primary-6))]" />
              <div class="ml-[8px] text-[rgb(var(--primary-6))]">{{ t('project.messageManagement.CUSTOM') }}</div>
              <div class="text-[var(--color-text-n4)]">{{ t('project.messageManagement.business') }}</div>
            </div>
          </div>
        </a-form-item>
        <a-form-item
          :label="t('project.messageManagement.name')"
          field="name"
          asterisk-position="end"
          :rules="[{ required: true, message: t('project.messageManagement.nameRequired') }]"
          required
        >
          <a-input
            v-model:model-value="robotForm.name"
            :max-length="255"
            :placeholder="t('project.messageManagement.namePlaceholder')"
            allow-clear
            class="w-[732px]"
          ></a-input>
        </a-form-item>
        <a-form-item
          v-if="robotForm.platform === 'DING_TALK'"
          :label="t('project.messageManagement.dingTalkType')"
          field="type"
        >
          <a-radio-group v-model:model-value="robotForm.type" type="button">
            <a-radio value="CUSTOM">
              {{ t('project.messageManagement.CUSTOM') }}
            </a-radio>
            <a-radio value="ENTERPRISE">
              {{ t('project.messageManagement.ENTERPRISE') }}
            </a-radio>
          </a-radio-group>
          <template v-if="robotForm.type === 'CUSTOM'">
            <MsFormItemSub
              :text="t('project.messageManagement.dingTalkCustomTip')"
              :show-fill-icon="false"
              class="mb-[16px]"
            >
              <MsButton
                type="text"
                class="ml-[8px] !text-[12px] leading-[16px]"
                @click="openExternalLink('https://open.dingtalk.com/document/orgapp/custom-robot-access')"
              >
                <MsIcon type="icon-icon_share" size="12" class="mr-[4px]" />
                {{ t('project.messageManagement.noticeDetail') }}
              </MsButton>
            </MsFormItemSub>
            <a-alert :title="t('project.messageManagement.dingTalkCustomTitle')" class="w-[732px]">
              <div class="text-[var(--color-text-2)]">{{ t('project.messageManagement.dingTalkCustomContent1') }}</div>
              <div class="text-[var(--color-text-2)]">
                {{ t('project.messageManagement.dingTalkCustomContent2', { at: '@' }) }}
              </div>
              <div class="text-[var(--color-text-2)]">{{ t('project.messageManagement.dingTalkCustomContent3') }}</div>
            </a-alert>
          </template>
          <template v-else>
            <MsFormItemSub
              :text="t('project.messageManagement.dingTalkEnterpriseTip')"
              :show-fill-icon="false"
              class="mb-[16px]"
            >
              <MsButton
                type="text"
                class="ml-[8px] !text-[12px] leading-[16px]"
                @click="
                  openExternalLink(
                    'https://open.dingtalk.com/document/orgapp/the-creation-and-installation-of-the-application-robot-in-the'
                  )
                "
              >
                <MsIcon type="icon-icon_share" size="12" class="mr-[4px]" />
                {{ t('project.messageManagement.helpDoc') }}
              </MsButton>
            </MsFormItemSub>
            <a-alert :title="t('project.messageManagement.dingTalkEnterpriseTitle')" class="w-[732px]">
              <div class="text-[var(--color-text-2)]">
                {{ t('project.messageManagement.dingTalkEnterpriseContent1', { at: '@' }) }}
              </div>
              <div class="text-[var(--color-text-2)]">
                {{ t('project.messageManagement.dingTalkEnterpriseContent2') }}
              </div>
            </a-alert>
          </template>
        </a-form-item>
        <template v-if="robotForm.platform === 'DING_TALK' && robotForm.type === 'ENTERPRISE'">
          <a-form-item
            :label="t('project.messageManagement.appKey')"
            field="appKey"
            asterisk-position="end"
            :rules="[{ required: true, message: t('project.messageManagement.appKeyRequired') }]"
            required
          >
            <a-input
              v-model:model-value="robotForm.appKey"
              :max-length="255"
              :placeholder="t('project.messageManagement.appKeyPlaceholder')"
              allow-clear
              class="w-[732px]"
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('project.messageManagement.appSecret')"
            field="appSecret"
            asterisk-position="end"
            :rules="[{ required: true, message: t('project.messageManagement.appSecretRequired') }]"
            required
          >
            <a-input
              v-model:model-value="robotForm.appSecret"
              :max-length="255"
              :placeholder="t('project.messageManagement.appSecretPlaceholder')"
              allow-clear
              class="w-[732px]"
            ></a-input>
          </a-form-item>
        </template>

        <a-form-item
          :label="t('project.messageManagement.webhook')"
          field="webhook"
          asterisk-position="end"
          :rules="[
            {
              required: true,
              message: t('project.messageManagement.webhookRequired'),
            },
          ]"
          required
        >
          <a-input
            v-model:model-value="robotForm.webhook"
            :max-length="255"
            :placeholder="
              t(
                robotForm.platform === 'CUSTOM'
                  ? 'project.messageManagement.webhookCustomPlaceholder'
                  : 'project.messageManagement.webhookPlaceholder',
                {
                  type: t(`project.messageManagement.${robotForm.platform}`),
                }
              )
            "
            class="w-[732px]"
            allow-clear
          ></a-input>
        </a-form-item>
      </a-form>
      <template #footerLeft>
        <a-switch v-model:model-value="robotForm.enable" size="small" class="mr-[4px]" type="line"></a-switch>
        {{ t('project.messageManagement.status') }}
        <a-tooltip position="tl" mini>
          <template #content>
            <div>{{ t('project.messageManagement.statusTipOn') }}</div>
            <div>{{ t('project.messageManagement.statusTipOff') }}</div>
          </template>
          <icon-question-circle class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-6))]" />
        </a-tooltip>
      </template>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, ref, watch } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import {
    addRobot,
    deleteRobot,
    getRobotList,
    toggleRobot,
    updateRobot,
  } from '@/api/modules/project-management/messageManagement';
  import useContainerShadow from '@/hooks/useContainerShadow';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import useVisit from '@/hooks/useVisit';
  import useAppStore from '@/store/modules/app';
  import { characterLimit } from '@/utils';
  import { translateTextToPX } from '@/utils/css';
  import { hasAnyPermission } from '@/utils/permission';

  import type {
    ProjectRobotPlatform,
    ProjectRobotPlatformCanEdit,
    RobotAddParams,
    RobotEditParams,
    RobotItem,
  } from '@/models/projectManagement/message';

  const props = defineProps<{
    activeTab: string;
  }>();

  const { t } = useI18n();
  const appStore = useAppStore();
  const { openModal } = useModal();

  const botList = ref<RobotItem[]>([]);

  const IconMap: Record<ProjectRobotPlatform, string> = {
    IN_SITE: 'icon-icon_bot1',
    MAIL: 'icon-icon_mail',
    LARK: 'icon-logo_lark',
    DING_TALK: 'icon-logo_dingtalk',
    WE_COM: 'icon-logo_wechat-work',
    CUSTOM: 'icon-icon_bot1',
  };

  const robotListContainerRef = ref<HTMLDivElement | null>(null);
  const robotListRef = ref<HTMLDivElement | null>(null);

  const { containerStatusClass, setContainer, initScrollListener } = useContainerShadow({
    overHeight: 20,
    containerClassName: 'robot-list-container',
  });

  async function initRobotList() {
    const res = await getRobotList(appStore.currentProjectId);
    botList.value = res as RobotItem[];
    nextTick(() => {
      if (robotListRef.value) {
        setContainer(robotListRef.value);
        initScrollListener();
      }
    });
  }

  watch(
    () => props.activeTab,
    (value) => {
      if (value === 'botList') {
        initRobotList();
      }
    },
    { immediate: true }
  );

  const visitedKey = 'messageManagementRobotListTip';
  const { addVisited, getIsVisited } = useVisit(visitedKey);

  const showDetailDrawer = ref(false);
  const drawerLoading = ref(false);
  const defaultRobot = {
    id: '',
    projectId: appStore.currentProjectId,
    name: '',
    platform: 'WE_COM',
    enable: true,
    webhook: '',
    type: 'CUSTOM',
    appKey: '',
    appSecret: '',
  } as RobotAddParams | RobotEditParams | RobotItem;
  const robotForm = ref({ ...defaultRobot });
  const robotFormRef = ref<FormInstance | null>(null);
  const isEdit = ref(false);
  const editPlatformList = ref<{ name: string; key: ProjectRobotPlatformCanEdit }[]>([
    {
      name: t('project.messageManagement.WE_COM'),
      key: 'WE_COM',
    },
    {
      name: t('project.messageManagement.DING_TALK'),
      key: 'DING_TALK',
    },
    {
      name: t('project.messageManagement.LARK'),
      key: 'LARK',
    },
  ]);

  function handleCreateClick() {
    isEdit.value = false;
    showDetailDrawer.value = true;
    robotForm.value = { ...defaultRobot };
  }

  function editRobot(robot: RobotItem) {
    isEdit.value = true;
    robotForm.value = { ...robot, type: robot.type || 'CUSTOM' };
    showDetailDrawer.value = true;
  }

  function openExternalLink(url: string) {
    window.open(url);
  }

  /**
   * 启用/禁用机器人
   * @param robot 机器人信息
   */
  function handleEnableIntercept(robot: RobotItem) {
    robot.enable = !robot.enable;
    openModal({
      type: robot.enable ? 'warning' : 'info',
      title: t(robot.enable ? 'project.messageManagement.disableTitle' : 'project.messageManagement.enableTitle', {
        name: characterLimit(robot.name),
      }),
      content: () =>
        h('div', {
          innerHTML: `<div>${t(
            robot.enable ? 'project.messageManagement.disableContent' : 'project.messageManagement.enableContent',
            { robot: robot.name }
          )}</div><div>${
            robot.platform === 'MAIL' && !robot.enable ? t('project.messageManagement.enableEmailContentTip') : ''
          }</div>`,
        }),
      okText: t(robot.enable ? 'project.messageManagement.disableConfirm' : 'project.messageManagement.enableConfirm', {
        robot: robot.name,
      }),
      cancelText: t('common.cancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await toggleRobot(robot.id);
          Message.success(
            t(robot.enable ? 'project.messageManagement.disableSuccess' : 'project.messageManagement.enableSuccess')
          );
          initRobotList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 删除机器人
   * @param robot 机器人信息
   */
  function delRobot(robot: RobotItem) {
    openModal({
      type: 'error',
      title: t('project.messageManagement.deleteTitle', { name: characterLimit(robot.name) }),
      content: t('project.messageManagement.deleteContent'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteRobot(robot.id);
          Message.success(t('common.deleteSuccess'));
          initRobotList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 保存机器人
   * @param isContinue 是否继续添加
   */
  async function saveRobot(isContinue: boolean) {
    try {
      drawerLoading.value = true;
      const params = { ...robotForm.value };
      switch (robotForm.value.platform) {
        case 'WE_COM':
        case 'LARK':
        case 'CUSTOM':
          params.type = undefined;
          params.appKey = undefined;
          params.appSecret = undefined;
          break;
        default:
          break;
      }
      if (isEdit.value) {
        await updateRobot(params as RobotEditParams);
        Message.success(t('common.updateSuccess'));
        showDetailDrawer.value = false;
      } else {
        await addRobot(params as RobotAddParams);
        Message.success(t('common.addSuccess'));
        robotFormRef.value?.resetFields();
        robotForm.value = { ...defaultRobot };
        if (!isContinue) {
          showDetailDrawer.value = false;
        }
      }
      initRobotList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  /**
   * 处理抽屉确认
   * @param isContinue 是否继续添加
   */
  function handleDrawerConfirm(isContinue: boolean) {
    robotFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        saveRobot(isContinue);
      }
    });
  }

  function handleDrawerCancel() {
    showDetailDrawer.value = false;
    robotFormRef.value?.resetFields();
    robotForm.value = { ...defaultRobot };
  }

  defineExpose({
    createRobot: handleCreateClick,
  });
</script>

<style lang="less" scoped>
  .robot-list-container {
    @apply relative;

    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
    .ms-container--shadow-y();
    .robot-list {
      @apply grid  max-h-full overflow-y-auto;
      .ms-scroll-bar();

      padding: 16px;
      grid-template-columns: repeat(2, minmax(128px, 1fr));
      border-radius: var(--border-radius-small);
      gap: 16px;
      .robot-card {
        @apply flex flex-col;

        padding: 24px;
        max-height: 128px;
        border-radius: var(--border-radius-small);
        background-color: var(--color-text-fff);
        gap: 16px;
      }
    }
  }
  .platform-card,
  .platform-card-custom {
    @apply flex cursor-pointer items-center;

    padding: 12px;
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    gap: 8px;
    &:hover {
      box-shadow: 0 4px 10px rgb(100 100 102 / 15%);
    }
  }
  .platform-card--active {
    border-color: rgb(var(--primary-5));
  }
  .platform-card-custom {
    @apply border-dashed;

    border-width: 2px;
    gap: 0;
  }
</style>
