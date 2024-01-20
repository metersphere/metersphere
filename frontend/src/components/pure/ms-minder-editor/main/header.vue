<template>
  <header>
    <div class="mind-tab-panel">
      <edit-menu
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
      />
    </div>
  </header>
</template>

<script lang="ts" name="headerVue" setup>
  import editMenu from '../menu/edit/editMenu.vue';

  import { delProps, editMenuProps, moleProps, priorityProps, tagProps, viewMenuProps } from '../props';

  const props = defineProps({
    ...editMenuProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
    minder: null,
  });
</script>

<style lang="less">
  @import '../style/header.less';
  .mind_tab-content {
    .tab-icons {
      background-image: url('@/assets/images/minder/icons.png');
      background-repeat: no-repeat;
    }
  }
</style>
