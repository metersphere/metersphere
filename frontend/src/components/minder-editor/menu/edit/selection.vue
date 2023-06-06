<template>
  <div class="selection-group">
    <a-button type="text" class="tab-icons selection" @click="selectAll" />
    <a-dropdown :popup-max-height="false" @select="handleCommand">
      <span class="dropdown-link">
        {{ t('minder.menu.selection.all') }}
        <icon-caret-down />
      </span>
      <template #content>
        <a-doption value="1">{{ t('minder.menu.selection.invert') }}</a-doption>
        <a-doption value="2">{{ t('minder.menu.selection.sibling') }}</a-doption>
        <a-doption value="3">{{ t('minder.menu.selection.same') }}</a-doption>
        <a-doption value="4">{{ t('minder.menu.selection.path') }}</a-doption>
        <a-doption value="5">{{ t('minder.menu.selection.subtree') }}</a-doption>
      </template>
    </a-dropdown>
  </div>
</template>

<script lang="ts" name="selection" setup>
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  function selectAll() {
    const selection: any[] = [];
    window.minder.getRoot().traverse((node: any) => {
      selection.push(node);
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function selectRevert() {
    const selected = window.minder.getSelectedNodes();
    const selection: any[] = [];
    window.minder.getRoot().traverse((node: any) => {
      if (selected.indexOf(node) === -1) {
        selection.push(node);
      }
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function selectSiblings() {
    const selected = window.minder.getSelectedNodes();
    const selection: any[] = [];
    selected.forEach((node: any) => {
      if (!node.parent) return;
      node.parent.children.forEach((sibling: string) => {
        if (selection.indexOf(sibling) === -1) selection.push(sibling);
      });
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function selectLevel() {
    const selectedLevel = window.minder.getSelectedNodes().map((node: any) => node.getLevel());
    const selection: any[] = [];
    window.minder.getRoot().traverse((node: any) => {
      if (selectedLevel.indexOf(node.getLevel()) !== -1) {
        selection.push(node);
      }
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function selectPath() {
    const selected = window.minder.getSelectedNodes();
    const selection: any[] = [];
    selected.forEach((node: any) => {
      let tempNode = node;
      while (tempNode && selection.indexOf(tempNode) === -1) {
        selection.push(tempNode);
        tempNode = node.parent;
      }
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function selectTree() {
    const selected = window.minder.getSelectedNodes();
    const selection: any[] = [];
    selected.forEach((parent: any) => {
      parent.traverse((node: any) => {
        if (selection.indexOf(node) === -1) selection.push(node);
      });
    });
    window.minder.select(selection, true);
    window.minder.fire('receiverfocus');
  }

  function handleCommand(value: string | number | Record<string, any> | undefined) {
    switch (value) {
      case 1:
        selectRevert();
        break;
      case 2:
        selectSiblings();
        break;
      case 3:
        selectLevel();
        break;
      case 4:
        selectPath();
        break;
      case 5:
        selectTree();
        break;
      default:
    }
  }
</script>

<style lang="less" scoped>
  .dropdown-link {
    @apply cursor-pointer;
  }
</style>
