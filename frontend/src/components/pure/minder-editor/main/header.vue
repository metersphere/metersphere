<template>
  <header>
    <a-tabs v-model="activeName" class="mind_tab-content">
      <a-tab-pane key="editMenu" :title="t('minder.main.header.minder')">
        <div class="mind-tab-panel">
          <edit-menu
            :minder="minder"
            :move-enable="props.moveEnable"
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
      </a-tab-pane>
      <a-tab-pane key="viewMenu" :title="t('minder.main.header.style')">
        <div class="mind-tab-panel">
          <view-menu
            v-if="props.viewMenuEnable"
            :minder="minder"
            :default-mold="props.defaultMold"
            :arrange-enable="props.arrangeEnable"
            :mold-enable="props.moldEnable"
            :font-enable="props.fontEnable"
            :style-enable="props.styleEnable"
            @mold-change="handleMoldChange"
          />
        </div>
      </a-tab-pane>
    </a-tabs>
  </header>
</template>

<script lang="ts" name="headerVue" setup>
  import { ref } from 'vue';
  import editMenu from '../menu/edit/editMenu.vue';
  import viewMenu from '../menu/view/viewMenu.vue';
  import { useI18n } from '@/hooks/useI18n';
  import { editMenuProps, moleProps, priorityProps, tagProps, delProps, viewMenuProps } from '../props';

  const { t } = useI18n();

  const props = defineProps({
    ...editMenuProps,
    ...moleProps,
    ...priorityProps,
    ...tagProps,
    ...delProps,
    ...viewMenuProps,
    minder: null,
  });

  const emit = defineEmits<{
    (e: 'moldChange', data: number): void;
  }>();
  const activeName = ref('editMenu');

  function handleMoldChange(data: number) {
    emit('moldChange', data);
  }
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

<style lang="less" scoped>
  header {
    @apply bg-white;

    font-size: 12px;
    & > ul {
      @apply m-0 flex items-center p-0;

      height: 30px;
      background-color: #e1e1e1;
      li {
        @apply inline-flex h-full  list-none;

        width: 80px;
        line-height: 30px;
        a {
          @apply text-center no-underline;

          font-size: 14px;
          color: #337ab7;
        }
        a:hover,
        a:focus {
          color: #23527c;
        }
      }
      li.selected {
        @apply bg-white;
        a {
          @apply text-black;
        }
      }
    }
  }
  .arco-tabs-content {
    padding-top: 10px;
  }
</style>
