<template>
  <div class="menu-item">
    <a-dropdown @select="handleCommand">
      <a-button
        class="arco-btn-outline--secondary mb-[4px]"
        :disabled="appendChildNodeDisabled && appendParentNodeDisabled && appendSiblingNodeDisabled"
        type="outline"
        size="small"
      >
        <template #icon>
          <icon-plus />
        </template>
      </a-button>
      <template #content>
        <a-doption :disabled="appendChildNodeDisabled" value="down">{{ t('minder.menu.insert.down') }}</a-doption>
        <a-doption :disabled="appendParentNodeDisabled" value="up">{{ t('minder.menu.insert.up') }}</a-doption>
        <a-doption :disabled="appendSiblingNodeDisabled" value="same">{{ t('minder.menu.insert.same') }}</a-doption>
      </template>
    </a-dropdown>
    {{ t('minder.menu.insert.insert') }}
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

  function handleCommand(val: string | number | Record<string, any> | undefined) {
    switch (val) {
      case 'down':
        execCommand('AppendChildNode');
        break;
      case 'up':
        execCommand('AppendParentNode');
        break;
      case 'same':
        execCommand('AppendSiblingNode');
        break;
      default:
        break;
    }
  }
</script>
