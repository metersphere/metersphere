<template>
  <div class="p-[24px]">
    <div class="header">
      <a-button type="primary">{{ t('project.fileManagement.addFile') }}</a-button>
      <div class="header-right">
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('project.fileManagement.folderSearchPlaceholder')"
          allow-clear
          class="w-[240px]"
        ></a-input-search>
        <a-radio-group v-model:model-value="fileType" type="button" class="file-show-type" @change="changeFileType">
          <a-radio value="Module" class="show-type-icon">{{ t('project.fileManagement.module') }}</a-radio>
          <a-radio value="Storage" class="show-type-icon">{{ t('project.fileManagement.storage') }}</a-radio>
        </a-radio-group>
        <a-radio-group v-model:model-value="showType" type="button" class="file-show-type" @change="changeShowType">
          <a-radio value="list" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_view-list_outlined" /></a-radio>
          <a-radio value="card" class="show-type-icon p-[2px]"><MsIcon type="icon-icon_card_outlined" /></a-radio>
        </a-radio-group>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  const { t } = useI18n();

  const keyword = ref('');
  const fileType = ref('Module');

  function changeFileType() {
    console.log(fileType.value);
  }

  const showType = ref('list');

  function changeShowType() {
    console.log(showType.value);
  }
</script>

<style lang="less" scoped>
  .header {
    @apply flex items-center justify-between;
    .header-right {
      @apply ml-auto flex items-center justify-end;

      width: 70%;
      gap: 8px;
      .show-type-icon {
        :deep(.arco-radio-button-content) {
          @apply flex;

          padding: 4px;
          line-height: 20px;
        }
      }
    }
  }
</style>
