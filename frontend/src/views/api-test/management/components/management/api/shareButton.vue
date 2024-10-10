<template>
  <a-trigger trigger="click" :unmount-on-close="false" position="br" @popup-visible-change="visibleChange">
    <a-button type="outline" :class="`${isSelectedShare ? '' : 'arco-btn-outline--secondary'} p-[0_8px]`">
      <template #icon>
        <MsIcon
          type="icon-icon_share1"
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
          :key="item.value"
          :class="[`share-option-item ${item.value === currentShare ? 'share-option-item-active' : ''} w-full`]"
          @click="changeShare(item)"
        >
          <div class="flex w-full items-center justify-between">
            <a-tooltip :content="item.label">
              <div class="one-line-text max-w-[100px]">
                {{ item.label }}
              </div>
            </a-tooltip>
            <MsIcon
              type="icon-icon_copy_outlined"
              class="cursor-pointer text-[var(--color-text-4)] hover:text-[rgb(var(--primary-5))]"
              :size="16"
              @click="copyShareLink(item.value)"
            />
          </div>
        </div>
      </div>
    </template>
  </a-trigger>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { SelectOptionData } from '@arco-design/web-vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const emit = defineEmits<{
    (e: 'create'): void;
    (e: 'showShareList'): void;
  }>();

  const isSelectedShare = ref(false);
  function visibleChange(val: boolean) {
    isSelectedShare.value = val;
  }

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

  const shareList = ref([
    {
      label: '001',
      value: '001',
    },
    {
      label: '002',
      value: '002',
    },
  ]);

  const currentShare = ref<string>('');
  // 创建分享
  function createShare() {
    emit('create');
  }

  // 分享列表
  function showShareList() {
    emit('showShareList');
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
  function copyShareLink(value: string) {}
</script>

<style scoped lang="less">
  .share-trigger-content {
    position: relative;
    padding: 8px;
    width: 154px;
    max-height: 300px;
    border-radius: var(--border-radius-medium);
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    @apply overflow-y-auto bg-white;
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
    padding: 3px 8px;
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
