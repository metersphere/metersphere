<template>
  <div :disabled="commandDisabled">
    <a-tag
      v-for="item in props.tags"
      :key="item"
      size="small"
      :color="getResourceColor(item)"
      @click="editResource(item)"
      >{{ item }}</a-tag
    >
  </div>
</template>

<script lang="ts" name="TagBox" setup>
  import { nextTick, onMounted, reactive, ref } from 'vue';
  import { isDisableNode, isTagEnable } from '../../script/tool/utils';
  import { tagProps } from '../../props';

  const props = defineProps(tagProps);

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

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
      minder.on('selectionchange', () => {
        commandDisabled.value = isDisable();
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
      if (!props.tagEditCheck(resourceName)) {
        return;
      }
    }
    if (!resourceName || !/\S/.test(resourceName)) {
      return;
    }
    const origin = window.minder.queryCommandValue('resource');
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
    window.minder.execCommand('resource', origin);
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
  .add-btn {
    padding: 0 !important;
    width: 36px;
    height: 24px;
    border-style: dashed !important;
  }
</style>
