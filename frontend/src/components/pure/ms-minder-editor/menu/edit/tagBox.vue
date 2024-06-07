<template>
  <div v-if="tagList.length > 0" class="menu-group flex items-center">
    <a-tag
      v-for="item in tagList"
      :key="item"
      :color="getResourceColor(item)"
      :class="commandDisabled ? 'disabledTag' : ''"
      @click="editResource(item)"
    >
      {{ item }}
    </a-tag>
  </div>
</template>

<script lang="ts" name="TagBox" setup>
  import { nextTick, onMounted, reactive, ref } from 'vue';

  import useMinderStore from '@/store/modules/components/minder-editor';

  import { MinderEventName } from '@/enums/minderEnum';

  import { MinderJsonNode, tagProps } from '../../props';
  import { isDisableNode, isTagEnable } from '../../script/tool/utils';

  const props = defineProps(tagProps);
  const minderStore = useMinderStore();

  let minder = reactive<any>({});
  const commandDisabled = ref(true);

  const isDisable = (): boolean => {
    if (Object.keys(minder).length === 0 || !minder.on) return true;
    if (isDisableNode(minder) && !isTagEnable(minder)) {
      return true;
    }
    if (props.tagDisableCheck) {
      return props.tagDisableCheck();
    }
    return !!minder.queryCommandState && minder.queryCommandState('resource') === -1;
  };

  const tagList = ref(props.tags);

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
      minder.on('selectionchange', () => {
        commandDisabled.value = isDisable();
        const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
        if (commandDisabled.value) {
          tagList.value = [];
        } else if (props.replaceableTags) {
          tagList.value = props.replaceableTags(nodes);
        } else {
          tagList.value = [];
        }
      });
    });
  });

  function getResourceColor(resource: string) {
    if (minder.getResourceColor) {
      return minder.getResourceColor(resource).toHEX();
    }
  }

  function editResource(resourceName: string) {
    if (commandDisabled.value) {
      return;
    }
    if (props.tagEditCheck) {
      const nodes: MinderJsonNode[] = minder.getSelectedNodes();
      if (!props.tagEditCheck(nodes, resourceName)) {
        return;
      }
    }
    if (!resourceName || !/\S/.test(resourceName)) {
      return;
    }
    const origin = window.minder.queryCommandValue('resource');
    if (props.singleTag) {
      origin.splice(0, origin.length, resourceName);
    } else {
      const index = origin.indexOf(resourceName);
      // 先删除排他的标签
      if (props.distinctTags.indexOf(resourceName) > -1) {
        for (let i = 0; i < origin.length; i++) {
          if (props.distinctTags.indexOf(origin[i]) > -1) {
            origin.splice(i, 1);
            i--;
          }
        }
      }
      if (index !== -1) {
        origin.splice(index, 1);
      } else {
        origin.push(resourceName);
      }
    }
    window.minder.execCommand('resource', origin);
    const nodes: MinderJsonNode[] = window.minder.getSelectedNodes();
    minderStore.dispatchEvent(MinderEventName.SET_TAG, undefined, undefined, undefined, nodes);
    if (props.replaceableTags) {
      tagList.value = props.replaceableTags(nodes);
    }
    if (props.afterTagEdit) {
      props.afterTagEdit(nodes, resourceName);
    }
  }
</script>

<style lang="less" scoped>
  .arco-tag {
    @apply border-none text-black;

    margin-right: 4px;
  }
  .arco-tag:hover {
    @apply cursor-pointer;
  }
  .arco-tag:first-child {
    margin-left: 4px;
  }
  .disabledTag {
    @apply !cursor-not-allowed;

    color: var(--color-text-4);
    background-color: var(--color-secondary-disabled);
  }
</style>
