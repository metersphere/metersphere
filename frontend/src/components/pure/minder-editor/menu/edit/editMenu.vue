<template>
  <div class="menu-container">
    <div class="menu-group">
      <div class="menu-item">
        <a-button class="arco-btn-outline--secondary mb-[4px]" type="outline" size="small" @click="expand">
          <template #icon>
            <icon-plus />
          </template>
        </a-button>
        {{ t('minder.menu.expand.expand') }}
      </div>
      <div class="menu-item">
        <a-button class="arco-btn-outline--secondary mb-[4px]" type="outline" size="small" @click="folding">
          <template #icon>
            <icon-minus />
          </template>
        </a-button>
        {{ t('minder.menu.expand.folding') }}
      </div>
      <move-box :move-enable="props.moveEnable" />
      <insert-box />
      <edit-del :del-confirm="props.delConfirm" />
    </div>
    <div class="menu-group">
      <tag-box
        v-if="props.tagEnable"
        :tags="props.tags"
        :tag-disable-check="props.tagDisableCheck"
        :tag-edit-check="props.tagEditCheck"
        :distinct-tags="props.distinctTags"
      />
    </div>
    <div class="menu-group">
      <sequence-box
        v-if="props.sequenceEnable"
        :priority-prefix="props.priorityPrefix"
        :priority-count="props.priorityCount"
        :priority-disable-check="props.priorityDisableCheck"
        :priority-start-with-zero="props.priorityStartWithZero"
      />
    </div>
  </div>
</template>

<script lang="ts" name="editMenu" setup>
  import editDel from './editDel.vue';
  import insertBox from './insertBox.vue';
  import moveBox from './moveBox.vue';
  import sequenceBox from './sequenceBox.vue';
  import TagBox from './tagBox.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { delProps, editMenuProps, priorityProps, tagProps } from '../../props';

  const props = defineProps({ ...editMenuProps, ...priorityProps, ...tagProps, ...delProps });

  const { t } = useI18n();

  const hasSelectedNode = ref(false);
  let minder = reactive<any>({});

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
      if (minder.on) {
        minder.on('selectionchange', () => {
          hasSelectedNode.value = Object.keys(minder).length > 0 && minder.getSelectedNode();
        });
      }
    });
  });

  /**
   * 展开
   */
  function expand() {
    const state = window.minder?.queryCommandState('Expand');
    if (state === 0) {
      // 有选中的节点
      window.minder?.execCommand('Expand');
    } else {
      window.minder?.execCommand('ExpandToLevel', 999);
    }
  }

  /**
   * 收起
   */
  function folding() {
    if (hasSelectedNode.value) {
      window.minder?.execCommand('Collapse');
    } else {
      window.minder?.execCommand('ExpandToLevel', 1);
    }
  }
</script>
