<template>
  <div class="outerWrapper p-[3px]">
    <div class="innerWrapper">
      <div class="content">
        <div class="logo-img p-[4px]">
          <svg-icon width="36px" height="36px" :name="props.cardItem.value"></svg-icon>
        </div>
        <div class="template-operation">
          <div class="flex items-center">
            <span class="font-medium">{{ props.cardItem.name }}</span>
            <span v-if="isEnableProject" class="enable">{{ t('system.orgTemplate.enabledTemplates') }}</span>
          </div>
          <div class="flex min-w-[300px] flex-nowrap items-center">
            <!-- 字段设置 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="fieldSetting">{{ t('system.orgTemplate.fieldSetting') }}</span>
              <a-divider direction="vertical" />
            </span>
            <!-- 模板列表 -->
            <span class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="templateManagement">{{ t('system.orgTemplate.TemplateManagementList') }}</span>
              <a-divider v-if="isShow" direction="vertical" />
            </span>
            <!-- 工作流 -->
            <span v-if="props.cardItem.key === 'BUG'" class="operation hover:text-[rgb(var(--primary-5))]">
              <span @click="workflowSetup">{{ t('system.orgTemplate.workflowSetup') }}</span>
              <a-divider
                v-if="
                  hasEnablePermission &&
                  props.mode === 'organization' &&
                  !isEnableProject &&
                  props.cardItem.key === 'BUG'
                "
                v-permission="['ORGANIZATION_TEMPLATE:READ+ENABLE']"
                direction="vertical"
              />
            </span>
            <!-- 启用项目模板 只有组织可以启用 -->
            <span
              v-if="hasEnablePermission && props.mode === 'organization' && !isEnableProject"
              class="rounded p-[2px] hover:bg-[rgb(var(--primary-9))]"
            >
              <MsTableMoreAction :list="moreActions" @select="handleMoreActionSelect" />
            </span>
          </div>
        </div>
      </div>
    </div>
    <a-modal
      v-model:visible="showEnableVisible"
      class="ms-modal-form ms-modal-small"
      unmount-on-close
      title-align="start"
      :mask="true"
      :mask-closable="false"
      @close="cancelHandler"
    >
      <template #title>
        <div class="flex items-center justify-start">
          <icon-exclamation-circle-fill size="20" class="mr-[8px] text-[rgb(var(--danger-6))]" />
          <div class="text-[var(--color-text-1)]">
            {{ t('system.orgTemplate.enableTip') }}
          </div>
        </div>
      </template>

      <span class="text-[rgb(var(--warning-6))]">{{ t('system.orgTemplate.enableWarningTip') }}</span>
      <a-input
        v-model="validateKeyWord"
        :placeholder="t('system.orgTemplate.searchOrgPlaceholder')"
        allow-clear
        class="mb-4 mt-[8px]"
        :max-length="255"
      />

      <template #footer>
        <div class="flex justify-end">
          <a-button type="secondary" @click="cancelHandler">
            {{ t('common.cancel') }}
          </a-button>
          <a-button
            class="ml-3"
            type="primary"
            :loading="confirmLoading"
            :disabled="!validateKeyWord.trim().length"
            status="danger"
            @click="okHandler"
          >
            {{ t('common.confirmEnable') }}
          </a-button>
        </div>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  /**
   * @description 模板-模板管理小卡片
   */
  import { ref } from 'vue';
  import { Message } from '@arco-design/web-vue';

  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';

  import { enableOrOffTemplate } from '@/api/modules/setting/template';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';
  import useLicenseStore from '@/store/modules/setting/license';
  import useTemplateStore from '@/store/modules/setting/template';
  import { hasAnyPermission } from '@/utils/permission';

  const { t } = useI18n();
  const appStore = useAppStore();
  const templateStore = useTemplateStore();
  const licenseStore = useLicenseStore();
  const currentOrgId = computed(() => appStore.currentOrgId);

  const props = defineProps<{
    cardItem: Record<string, any>;
    mode: 'organization' | 'project';
  }>();

  const emit = defineEmits<{
    (e: 'fieldSetting', key: string): void;
    (e: 'templateManagement', key: string): void;
    (e: 'workflowSetup', key: string): void;
    (e: 'updateState'): void;
  }>();

  // 先判断项目是否是开启
  const isEnableProject = computed(() => {
    return templateStore.projectStatus[props.cardItem.key];
  });

  const moreActions = ref<ActionsItem[]>([
    {
      label: t('system.orgTemplate.enable'),
      eventTag: 'enable',
      danger: true,
      permission: ['ORGANIZATION_TEMPLATE:READ+ENABLE'],
    },
  ]);
  const showEnableVisible = ref<boolean>(false);
  const validateKeyWord = ref<string>('');
  const confirmLoading = ref<boolean>(false);

  const orgName = computed(() => {
    if (licenseStore.hasLicense()) {
      return appStore.orgList.find((item: any) => item.id === appStore.currentOrgId)?.name;
    }
    return '默认组织';
  });

  async function okHandler() {
    if (validateKeyWord.value.trim() !== '' && validateKeyWord.value !== orgName.value) {
      Message.success(t('system.orgTemplate.orgNameTip'));
      return false;
    }
    try {
      confirmLoading.value = true;
      if (props.mode) {
        await enableOrOffTemplate(currentOrgId.value, props.cardItem.key);
        Message.success(t('system.orgTemplate.enabledSuccessfully'));
        await templateStore.getStatus();
        emit('updateState');
        confirmLoading.value = false;
        showEnableVisible.value = false;
      }
    } catch (error) {
      console.log(error);
    } finally {
      confirmLoading.value = false;
    }
  }
  // 启用模板
  const enableHandler = async () => {
    try {
      showEnableVisible.value = true;
    } catch (error) {
      console.log(error);
    }
  };

  const handleMoreActionSelect = () => {
    enableHandler();
  };

  // 字段设置
  const fieldSetting = () => {
    emit('fieldSetting', props.cardItem.key);
  };

  const templateManagement = () => {
    emit('templateManagement', props.cardItem.key);
  };

  const workflowSetup = () => {
    emit('workflowSetup', props.cardItem.key);
  };

  const templateCardInfo = ref<Record<string, any>>({});

  watch(
    () => props.cardItem,
    (val) => {
      if (val) {
        templateCardInfo.value = { ...props.cardItem };
      }
    },
    { deep: true }
  );

  const hasEnablePermission = computed(() => hasAnyPermission(['ORGANIZATION_TEMPLATE:READ+ENABLE']));

  const isShow = computed(() => {
    if (props.cardItem.key === 'BUG') {
      return true;
    }
    return !hasEnablePermission.value ? false : !isEnableProject.value;
  });

  function cancelHandler() {
    showEnableVisible.value = false;
    validateKeyWord.value = '';
  }
</script>

<style scoped lang="less">
  :deep(.arco-divider-vertical) {
    margin: 0 8px;
  }
  .outerWrapper {
    background-color: var(--color-text-fff);
    box-shadow: 0 6px 15px rgba(120 56 135/ 5%);
    @apply rounded;
    .innerWrapper {
      background: var(--color-bg-3);
      @apply rounded p-6;
      .content {
        @apply flex;
        .logo-img {
          border-radius: var(--border-radius-small);
          @apply mr-3 flex items-center justify-center;

          background-color: var(--color-text-fff);
        }
        .template-operation {
          .operation {
            cursor: pointer;
          }
          .enable {
            height: 20px;
            font-size: 12px;
            color: var(--color-text-4);
            background: var(--color-text-n8);
            line-height: 14px;
            @apply ml-4 rounded p-1;
          }
          @apply flex flex-col justify-between;
        }
      }
    }
  }
</style>
