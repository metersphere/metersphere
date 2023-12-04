<template>
  <div class="flex h-full flex-col gap-[16px]">
    <div class="flex items-center">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.apiKey') }}</div>
      <a-tooltip :content="t('ms.personal.apiKeyTip')" position="right">
        <icon-question-circle
          class="ml-[4px] text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
          size="16"
        />
      </a-tooltip>
    </div>
    <a-tooltip :content="t('ms.personal.maxTip')" position="right" :disabled="apiKeyList.length < 5">
      <a-button type="outline" class="w-[60px]" :disabled="apiKeyList.length >= 5">{{ t('common.new') }}</a-button>
    </a-tooltip>
    <a-spin class="api-list-content" :loading="loading">
      <div v-for="item of apiKeyList" :key="item.id" class="api-item">
        <div class="mb-[8px] border-b border-solid border-[var(--color-text-n8)]">
          <div class="px-[16px]">
            <div class="api-item-label">Access Key</div>
            <div class="api-item-value-strong">
              {{ item.accessKey }}
              <MsTag v-if="item.isExpire" type="warning" theme="light" size="small" class="mx-[4px] px-[4px]">
                {{ t('ms.personal.expired') }}
              </MsTag>
              <MsIcon type="icon-icon_copy_outlined" class="copy-icon" @click="handleCopy(item.accessKey)" />
            </div>
            <div class="api-item-label">Secret Key</div>
            <div class="api-item-value-strong">
              {{ item.desensitization ? item.secretKey.replace(/./g, '*') : item.secretKey }}
              <MsIcon
                :type="item.desensitization ? 'icon-icon_preview_close_one' : 'icon-icon_visible_outlined'"
                class="eye-icon"
                @click="desensitization(item)"
              />
              <MsIcon type="icon-icon_copy_outlined" class="copy-icon" @click="handleCopy(item.secretKey)" />
            </div>
          </div>
        </div>
        <div class="px-[16px]">
          <div class="api-item-label">{{ t('ms.personal.desc') }}</div>
          <a-tooltip :content="item.desc">
            <div class="api-item-value one-line-text">{{ item.desc }}</div>
          </a-tooltip>
          <div class="api-item-label">{{ t('ms.personal.createTime') }}</div>
          <div class="api-item-value">{{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}</div>
          <div class="api-item-label">{{ t('ms.personal.expireTime') }}</div>
          <div class="api-item-value">
            {{ dayjs(item.expireTime).format('YYYY-MM-DD HH:mm:ss') }}
            <a-tooltip v-if="item.isExpire" :content="t('ms.personal.expiredTip')">
              <MsIcon type="icon-icon_warning_filled" class="ml-[4px] text-[rgb(var(--warning-6))]" />
            </a-tooltip>
          </div>
        </div>
        <div class="flex items-center justify-between px-[16px]">
          <MsTableMoreAction :list="actions" trigger="click" @select="handleMoreActionSelect($event, item)">
            <a-button size="mini" type="outline" class="arco-btn-outline--secondary">
              {{ t('common.setting') }}
            </a-button>
          </MsTableMoreAction>
          <a-switch
            v-model:model-value="item.enable"
            size="small"
            :before-change="() => handleBeforeEnableChange(item)"
          ></a-switch>
        </div>
      </div>
    </a-spin>
  </div>
  <a-modal
    v-model:visible="timeModalVisible"
    :title="t('ms.personal.changeAvatar')"
    title-align="start"
    :ok-text="t('common.save')"
    class="ms-usemodal"
    :width="680"
    unmount-on-close
    @before-ok="handleTimeConfirm"
    @close="handleTimeClose"
  >
    <a-form ref="timeFormRef" :model="timeForm" layout="vertical">
      <a-form-item :label="t('ms.personal.timeSetting')">
        <a-radio-group v-model:model-value="timeForm.activeTimeType" type="button">
          <a-radio value="forever" class="show-type-icon">{{ t('ms.personal.forever') }}</a-radio>
          <a-radio value="custom" class="show-type-icon">{{ t('ms.personal.custom') }}</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item
        v-if="timeForm.activeTimeType === 'custom'"
        field="time"
        :label="t('ms.personal.timeSetting')"
        :rules="[{ required: true, message: t('ms.personal.expiredTimeRequired') }]"
        asterisk-position="end"
      >
        <a-date-picker
          v-model:model-value="timeForm.time"
          show-time
          :time-picker-props="{ defaultValue: '00:00:00' }"
          format="YYYY-MM-DD HH:mm:ss"
          class="w-[240px]"
        />
      </a-form-item>
      <a-form-item field="desc" :label="t('ms.personal.accessKeyDesc')">
        <a-input
          v-model:model-value="timeForm.desc"
          :max-length="64"
          :placeholder="t('ms.personal.accessKeyDescPlaceholder')"
          show-word-limit
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
  import { useClipboard } from '@vueuse/core';
  import { FormInstance, Message } from '@arco-design/web-vue';
  import dayjs from 'dayjs';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';

  const { copy } = useClipboard();
  const { t } = useI18n();
  const { openModal } = useModal();

  interface ApiKeyItem {
    id: string;
    accessKey: string;
    secretKey: string;
    desc: string;
    createTime: number;
    expireTime: number;
    enable: boolean;
    desensitization: boolean;
    isExpire: boolean;
  }

  const loading = ref(false);
  const apiKeyList = ref<ApiKeyItem[]>([
    {
      id: '238dy23d2',
      accessKey: 'dueiouhwded',
      secretKey: 'asdasdas',
      desc: '92387yd9283d2',
      createTime: 1629782400000,
      expireTime: 1629982400000,
      enable: true,
      desensitization: true,
      isExpire: false,
    },
    {
      id: 'ih02i3d23',
      accessKey: 'sdshsd',
      secretKey: 'poj4f',
      desc: '92387yd9283d2',
      createTime: 1629782400000,
      expireTime: 1629982400000,
      enable: false,
      desensitization: true,
      isExpire: true,
    },
    {
      id: '34hy34h3',
      accessKey: 'sdshsd',
      secretKey: 'poj4f',
      desc: '92387yd9283d2',
      createTime: 1629782400000,
      expireTime: 1629982400000,
      enable: false,
      desensitization: true,
      isExpire: true,
    },
    {
      id: 'f23',
      accessKey: 'sdshsd',
      secretKey: 'poj4f',
      desc: '92387yd9283d2',
      createTime: 1629782400000,
      expireTime: 1629982400000,
      enable: false,
      desensitization: true,
      isExpire: true,
    },
    {
      id: 'ih02i3ed23',
      accessKey: 'sdshsd',
      secretKey: 'poj4f',
      desc: '92387yd9283d2',
      createTime: 1629782400000,
      expireTime: 1629982400000,
      enable: false,
      desensitization: true,
      isExpire: true,
    },
  ]);
  const actions: ActionsItem[] = [
    {
      label: t('ms.personal.validTime'),
      eventTag: 'time',
    },
    {
      isDivider: true,
    },
    {
      label: t('common.delete'),
      danger: true,
      eventTag: 'delete',
    },
  ];

  async function handleCopy(val: string) {
    await copy(val);
    Message.success(t('ms.personal.copySuccess'));
  }

  function desensitization(item: ApiKeyItem) {
    item.desensitization = !item.desensitization;
  }

  async function handleBeforeEnableChange(item: ApiKeyItem) {
    if (item.enable) {
      openModal({
        type: 'error',
        title: t('ms.personal.confirmClose'),
        content: t('ms.personal.closeTip'),
        okText: t('common.confirmClose'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'danger',
        },
        onBeforeOk: async () => {
          try {
            Message.success(t('ms.personal.closeSuccess'));
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          }
        },
        hideCancel: false,
      });
      return false;
    }
    try {
      Message.success(t('ms.personal.openSuccess'));
      return true;
    } catch (error) {
      console.log(error);
      return false;
    }
  }

  function deleteApiKey(item: ApiKeyItem) {
    openModal({
      type: 'error',
      title: t('ms.personal.confirmDelete'),
      content: t('ms.personal.deleteTip'),
      okText: t('common.confirmDelete'),
      cancelText: t('common.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      onBeforeOk: async () => {
        try {
          Message.success(t('common.deleteSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
    return false;
  }

  const timeModalVisible = ref(false);
  const defaultTimeForm = {
    activeTimeType: 'forever',
    time: '',
    desc: '',
  };
  const timeForm = ref({ ...defaultTimeForm });
  const timeFormRef = ref<FormInstance>();

  function handleTimeConfirm(done: (closed: boolean) => void) {
    timeFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          Message.success(t('common.updateSuccess'));
          done(true);
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          done(false);
        }
      } else {
        done(false);
      }
    });
  }

  function handleTimeClose() {
    timeFormRef.value?.resetFields();
    timeForm.value = { ...defaultTimeForm };
  }

  function handleMoreActionSelect(item: ActionsItem, apiKey: ApiKeyItem) {
    if (item.eventTag === 'time') {
      timeForm.value = {
        activeTimeType: 'forever',
        time: apiKey.expireTime ? dayjs(apiKey.expireTime).format('YYYY-MM-DD HH:mm:ss') : '',
        desc: apiKey.desc,
      };
      timeModalVisible.value = true;
    } else if (item.eventTag === 'delete') {
      deleteApiKey(apiKey);
    }
  }
</script>

<style lang="less" scoped>
  .api-list-content {
    @apply grid flex-1 overflow-auto;
    .ms-scroll-bar();

    gap: 16px;
    grid-template-columns: repeat(auto-fill, minmax(318px, 2fr));
    padding: 16px;
    border-radius: var(--border-radius-small);
    background-color: var(--color-text-n9);
  }
  .api-item {
    @apply bg-white;

    padding: 16px 0;
    height: 335px;
    border-radius: var(--border-radius-small);
    .api-item-label {
      font-size: 12px;
      line-height: 16px;
      color: var(--color-text-4);
    }
    .api-item-value {
      @apply flex items-center;

      margin-bottom: 16px;
      font-size: 14px;
      color: var(--color-text-1);
    }
    .api-item-value-strong {
      @apply flex items-center;

      margin-bottom: 8px;
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-1);
      &:hover {
        .copy-icon {
          @apply visible;
        }
      }
      .copy-icon,
      .eye-icon {
        @apply cursor-pointer;

        margin-left: 4px;
        color: var(--color-text-brand);
        &:hover {
          color: rgb(var(--primary-6));
        }
      }
      .copy-icon {
        @apply invisible;
      }
    }
  }
</style>
