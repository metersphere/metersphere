<template>
  <div class="menu-container">
    <expand />
    <selection />
    <insert-box />
    <move-box :move-enable="props.moveEnable" />
    <edit-del :del-confirm="props.delConfirm" />
    <sequence-box
      v-if="props.sequenceEnable"
      :priority-prefix="props.priorityPrefix"
      :priority-count="props.priorityCount"
      :priority-disable-check="props.priorityDisableCheck"
      :priority-start-with-zero="props.priorityStartWithZero"
    />
    <progress-box v-if="props.progressEnable" />
    <tag-box
      v-if="props.tagEnable"
      :tags="props.tags"
      :tag-disable-check="props.tagDisableCheck"
      :tag-edit-check="props.tagEditCheck"
      :distinct-tags="props.distinctTags"
    />
  </div>
</template>

<script lang="ts" name="editMenu" setup>
  import insertBox from './insertBox.vue';
  import moveBox from './moveBox.vue';
  import editDel from './editDel.vue';
  import sequenceBox from './sequenceBox.vue';
  import progressBox from './progressBox.vue';
  import expand from './expand.vue';
  import selection from './selection.vue';
  import TagBox from './tagBox.vue';
  import { editMenuProps, priorityProps, tagProps, delProps } from '../../props';

  const props = defineProps({ ...editMenuProps, ...priorityProps, ...tagProps, ...delProps });
</script>

<style lang="less" scoped>
  .menu-container {
    height: 60px;
    i {
      @apply inline-block;

      width: 20px;
      height: 20px;
    }
  }
</style>
