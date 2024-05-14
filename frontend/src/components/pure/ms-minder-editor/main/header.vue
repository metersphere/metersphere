<template>
  <!-- <div class="ms-minder-editor-header">
    <a-tooltip v-for="item of props.iconButtons" :key="item.eventTag" :content="t(item.tooltip)">
      <MsButton type="icon" class="ms-minder-editor-header-icon-button" @click="emit('click', item.eventTag)">
        <MsIcon :type="item.icon" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
    <a-divider v-if="props.iconButtons?.length" direction="vertical" :margin="8"></a-divider>
    <a-tooltip :content="isFullScreen ? t('common.offFullScreen') : t('common.fullScreen')">
      <MsButton v-if="isFullScreen" type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_off_screen" class="text-[var(--color-text-4)]" />
      </MsButton>
      <MsButton v-else type="icon" class="ms-minder-editor-header-icon-button" @click="toggleFullScreen">
        <MsIcon type="icon-icon_full_screen_one" class="text-[var(--color-text-4)]" />
      </MsButton>
    </a-tooltip>
  </div> -->
  <div class="mind-tab-panel">
    <editMenu
      :minder="minder"
      :move-enable="props.moveEnable"
      :move-confirm="props.moveConfirm"
      :sequence-enable="props.sequenceEnable"
      :tag-enable="props.tagEnable"
      :progress-enable="props.progressEnable"
      :priority-count="props.priorityCount"
      :priority-prefix="props.priorityPrefix"
      :tag-edit-check="props.tagEditCheck"
      :tag-disable-check="props.tagDisableCheck"
      :priority-disable-check="props.priorityDisableCheck"
      :priority-start-with-zero="props.priorityStartWithZero"
      :tags="props.tags"
      :distinct-tags="props.distinctTags"
      :del-confirm="props.delConfirm"
      :replaceable-tags="props.replaceableTags"
      :single-tag="props.singleTag"
      :insert-node="props.insertNode"
      :after-tag-edit="props.afterTagEdit"
    />
  </div>
</template>

<script lang="ts" setup>
  // import MsButton from '@/components/pure/ms-button/index.vue';
  // import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import editMenu from '../menu/edit/editMenu.vue';

  import { delProps, editMenuProps, insertProps, moleProps, priorityProps, tagProps, viewMenuProps } from '../props';

  const props = defineProps({
    ...editMenuProps,
    ...insertProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
    minder: null,
  });
  // import useFullScreen from '@/hooks/useFullScreen';
  // import { useI18n } from '@/hooks/useI18n';

  // import { headerProps } from '../props';

  // const props = defineProps({
  //   ...headerProps,
  // });
  // const emit = defineEmits<{
  //   (e: 'click', eventTag: string): void;
  // }>();

  // const { t } = useI18n();

  // const { toggleFullScreen, isFullScreen } = useFullScreen(document.querySelector('.ms-minder-editor-container'));
</script>

<style lang="less">
  @import '../style/header.less';
  .mind_tab-content {
    .tab-icons {
      background-image: url('@/assets/images/minder/icons.png');
      background-repeat: no-repeat;
    }
  }
  // .ms-minder-editor-header {
  //   @apply absolute z-10 flex items-center bg-white;

  //   top: 24px;
  //   right: 0;
  //   padding: 4px 8px;
  //   border-radius: var(--border-radius-small);
  //   box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
  //   .ms-minder-editor-header-icon-button {
  //     &:hover {
  //       background-color: rgb(var(--primary-1)) !important;
  //       .arco-icon {
  //         color: rgb(var(--primary-4)) !important;
  //       }
  //     }
  //   }
  // }
</style>
