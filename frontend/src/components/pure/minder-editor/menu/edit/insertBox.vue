<template>
  <div class="insert-group">
    <div class="insert-child-box menu-btn" :disabled="appendChildNodeDisabled" @click="execCommand('AppendChildNode')">
      <i class="tab-icons" />
      <span>{{ t('minder.menu.insert.down') }}</span>
    </div>
    <div
      class="insert-parent-box menu-btn"
      :disabled="appendParentNodeDisabled"
      @click="execCommand('AppendParentNode')"
    >
      <i class="tab-icons" />
      <span>{{ t('minder.menu.insert.up') }}</span>
    </div>
    <div
      class="insert-sibling-box menu-btn"
      :disabled="appendSiblingNodeDisabled"
      @click="execCommand('AppendSiblingNode')"
    >
      <i class="tab-icons" />
      <span>{{ t('minder.menu.insert.same') }}</span>
    </div>
  </div>
</template>

<script lang="ts" name="insertBox" setup>
  import { nextTick, onMounted, ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import { isDisableNode } from '../../script/tool/utils';

  const { t } = useI18n();

  const minder = ref<any>({});
  const appendChildNodeDisabled = ref(false);
  const appendParentNodeDisabled = ref(false);
  const appendSiblingNodeDisabled = ref(false);

  function checkDisabled() {
    try {
      if (Object.keys(minder.value).length === 0) return false;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return false;
    }
    appendChildNodeDisabled.value = minder.value.queryCommandState('AppendChildNode') === -1;
    const node = minder.value.getSelectedNode();
    appendSiblingNodeDisabled.value = !node || !!isDisableNode(minder.value) || node.parent === null;
    appendParentNodeDisabled.value = !node || node.parent === null;
  }

  onMounted(() => {
    nextTick(() => {
      minder.value = window.minder;
      minder.value.on('selectionchange', () => {
        checkDisabled();
      });
    });
  });

  function execCommand(command: string) {
    if (minder.value.queryCommandState(command) !== -1) {
      minder.value.execCommand(command);
    }
  }
</script>
