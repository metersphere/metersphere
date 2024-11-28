<template>
  <a-trigger trigger="click" :unmount-on-close="false" position="br" @popup-visible-change="visibleChange">
    <a-button type="outline" :class="`${isSelectedShare ? '' : 'arco-btn-outline--secondary'} p-[0_8px]`">
      <template #icon>
        <MsIcon
          type="icon-icon_link-copy_outlined"
          :class="`${isSelectedShare ? 'text-[rgb(var(--primary-5))]' : 'text-[var(--color-text-4)]'}`"
          :size="16"
        />
      </template>
      {{ t('common.share') }}
    </a-button>
    <template #content>
      <div class="share-trigger-content">
        <div
          v-for="item in internalShare"
          :key="item.value"
          :class="[`share-option-item ${item.value === currentShare ? 'share-option-item-active' : ''} w-full`]"
          @click="changeShare(item)"
        >
          <a-tooltip :content="item.label">
            <div class="one-line-text">
              {{ item.label }}
            </div>
          </a-tooltip>
        </div>
        <div class="share-option-title">
          <span>{{ t('apiTestManagement.newCreatedList') }}</span>
          <a-divider />
        </div>
        <div
          v-for="item in shareList"
          :key="item.id"
          :class="[`share-option-item ${item.id === currentShare ? 'share-option-item-active' : ''} w-full`]"
          @click="changeShare(item)"
        >
          <div class="flex w-full items-center justify-between">
            <a-tooltip :content="item.name">
              <div class="one-line-text max-w-[100px]">
                {{ item.name }}
              </div>
            </a-tooltip>
            <a-tooltip :disabled="!!item.apiShareNum" position="tr" :content="t('apiTestManagement.apiShareNumberTip')">
              <MsIcon
                type="icon-icon_copy_outlined"
                :class="`text-[var(--color-text-4)] ${
                  item.apiShareNum ? 'cursor-pointer hover:text-[rgb(var(--primary-5))]' : ''
                }`"
                :size="16"
                @click="copyShareLink(item)"
              />
            </a-tooltip>
          </div>
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useClipboard } from '@vueuse/core';
  import { Message, SelectOptionData } from '@arco-design/web-vue';

  import { getSharePage } from '@/api/modules/api-test/management';
  import { useI18n } from '@/hooks/useI18n';
  import { useAppStore } from '@/store';

  import type { shareItem } from '@/models/apiTest/management';
  import { RouteEnum } from '@/enums/routeEnum';

  const appStore = useAppStore();

  const { copy, isSupported } = useClipboard({ legacy: true });
  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'create'): void;
    (e: 'showShareList'): void;
  }>();

  const internalShare = ref([
    {
      label: t('apiTestManagement.shareList'),
      value: 'shareList',
    },
    {
      label: t('apiTestManagement.newCreateShare'),
      value: 'newShare',
    },
  ]);

  const shareList = ref<shareItem[]>();

  const currentShare = ref<string>('');
  // 创建分享
  function createShare() {
    emit('create');
  }

  // 分享列表
  function showShareList() {
    emit('showShareList');
  }

  const isSelectedShare = ref(false);
  function visibleChange(val: boolean) {
    isSelectedShare.value = val;
    if (!val) {
      currentShare.value = '';
    }
  }

  function changeShare(item: SelectOptionData) {
    currentShare.value = item.value as string;
    switch (item.value) {
      case 'newShare':
        createShare();
        break;
      case 'shareList':
        showShareList();
        break;

      default:
        break;
    }
  }

  // 快捷复制分享
  function copyShareLink(item: shareItem) {
    if (!item.apiShareNum) {
      return;
    }
    if (isSupported) {
      // 判断路由中是不是有dId参数，有的话只是修改当前的值就可以了
      const shareLinkUrl = `${origin}/#/${RouteEnum.SHARE}/${RouteEnum.SHARE_DEFINITION_API}`;
      const dIdParam = `?orgId=${appStore.currentOrgId}&pId=${appStore.currentProjectId}&docShareId=${item.id}`;
      copy(`${shareLinkUrl}${dIdParam}`);
      Message.success(t('apiTestManagement.shareUrlCopied'));
    } else {
      Message.error(t('common.copyNotSupport'));
    }
  }

  async function initShareList() {
    try {
      const res = await getSharePage({
        current: 1,
        pageSize: 10,
        sort: {},
        combineSearch: {
          searchMode: 'AND',
          conditions: [],
        },
        projectId: appStore.currentProjectId,
        filter: {},
      });

      shareList.value = res.list;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  }

  onBeforeMount(() => {
    initShareList();
  });

  defineExpose({
    initShareList,
  });
</script>

<style scoped lang="less">
  .share-trigger-content {
    position: relative;
    padding: 8px;
    width: 154px;
    max-height: 300px;
    border-radius: var(--border-radius-medium);
    background-color: var(--color-text-fff);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    @apply overflow-y-auto;
    .ms-scroll-bar();
  }
  .share-option-title {
    display: flex;
    align-items: center;
    margin: 0 2px;
    padding: 0 8px;
    font-size: 12px;
    color: var(--color-text-brand);
    line-height: 20px;
    .arco-divider-horizontal {
      margin: 4px 0 4px 8px;
      min-width: 0;
      border-bottom-color: var(--color-text-n8);
      flex: 1;
    }
  }
  .share-option-item {
    padding: 4px 8px;
    border-radius: 4px;
    cursor: pointer;
    @apply flex w-full items-center justify-between;
    &:hover {
      background-color: rgb(var(--primary-1));
      .select-extra {
        visibility: visible;
      }
    }
    &-active {
      color: rgb(var(--primary-5)) !important;
      background-color: rgb(var(--primary-1)) !important;
    }
  }
</style>
