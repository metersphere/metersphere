<template>
  <div class="menu-item">
    <a-button
      class="arco-btn-outline--secondary mb-[4px]"
      :disabled="removeNodeDisabled"
      type="outline"
      size="small"
      @click="del"
    >
      <template #icon>
        <icon-minus />
      </template>
    </a-button>
    {{ t('minder.commons.delete') }}
  </div>
</template>

<script lang="ts" name="edit_del" setup>
  import { nextTick, onMounted, reactive, ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';
  import useMinderStore from '@/store/modules/components/minder-editor';
  import { MinderNodePosition } from '@/store/modules/components/minder-editor/types';

  import { MinderEventName } from '@/enums/minderEnum';

  import { delProps, MinderJsonNode } from '../../props';
  import { isDeleteDisableNode } from '../../script/tool/utils';

  const minderStore = useMinderStore();
  const { t } = useI18n();

  const props = defineProps(delProps);

  let minder = reactive<any>({});
  const removeNodeDisabled = ref(true);

  function checkDisabled() {
    try {
      if (!minder) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    const node = minder.getSelectedNode();
    removeNodeDisabled.value = !node || !!isDeleteDisableNode(minder) || node.parent === null;
  }

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
      minder.on('selectionchange', () => {
        checkDisabled();
      });
    });
  });

  function del() {
    if (removeNodeDisabled.value || !minder.queryCommandState || !minder.execCommand) {
      return;
    }
    const nodes: MinderJsonNode[] = minder.getSelectedNodes();
    if (nodes.length > 0) {
      if (props.delConfirm) {
        props.delConfirm(nodes);
        return;
      }
      const box = nodes[0].getRenderBox();
      minderStore.dispatchEvent(MinderEventName.DELETE_NODE, undefined, box, nodes[0].rc.node, nodes);
    }
    minder.forceRemoveNode();
  }
</script>
