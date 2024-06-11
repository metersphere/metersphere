<template>
  <div class="flex items-center">
    <a-button
      :disabled="commandDisabled"
      class="delete-btn !mx-[4px] !my-0 !h-[23px] !w-[23px] !p-[2px]"
      shape="circle"
      @click="execCommand()"
    >
      <template #icon>
        <icon-delete />
      </template>
    </a-button>
    <template v-for="(item, pIndex) in priorityCount + 1">
      <a-button
        v-if="pIndex != 0"
        :key="item"
        :disabled="commandDisabled"
        class="priority-btn mr-[4px] h-[22px] w-[22px] rounded-[8px] pr-[5px] text-[12px] leading-[16px]"
        :class="'priority-btn_' + pIndex"
        size="small"
        @click="execCommand(pIndex)"
      >
        {{ priorityPrefix }}{{ priorityStartWithZero ? pIndex - 1 : pIndex }}
      </a-button>
    </template>
  </div>
</template>

<script lang="ts" name="sequenceBox" setup>
  import { nextTick, onMounted, reactive, ref } from 'vue';

  import { priorityProps } from '../../props';
  import { isDisableNode, setPriorityView } from '../../script/tool/utils';

  const props = defineProps(priorityProps);

  const commandValue = ref('');
  const commandDisabled = ref(true);
  const minder = reactive<any>({});

  const isDisable = (): boolean => {
    if (Object.keys(minder).length === 0) return true;
    nextTick(() => {
      setPriorityView(props.priorityStartWithZero, props.priorityPrefix);
    });
    if (minder.on) {
      minder.on('interactchange', () => {
        commandValue.value = minder.queryCommandValue && minder.queryCommandValue('priority');
      });
    }
    const node = minder.getSelectedNode();
    if (isDisableNode(minder) || !node || node.parent === null) {
      return true;
    }
    if (props.priorityDisableCheck) {
      return props.priorityDisableCheck(node);
    }
    return !!minder.queryCommandState && minder.queryCommandState('priority') === -1;
  };

  // onMounted(() => {
  //   nextTick(() => {
  //     minder = window.minder;
  //     const freshFuc = setPriorityView;
  //     if (minder.on) {
  //       minder.on('contentchange', () => {
  //         // 异步执行，否则执行完，还会被重置
  //         setTimeout(() => {
  //           freshFuc(props.priorityStartWithZero, props.priorityPrefix);
  //         }, 0);
  //       });
  //       minder.on('selectionchange', () => {
  //         commandDisabled.value = isDisable();
  //       });
  //     }
  //   });
  // });

  function execCommand(index?: number) {
    if (index && minder.execCommand) {
      if (!commandDisabled.value) {
        minder.execCommand('priority', index);
      }
      setPriorityView(props.priorityStartWithZero, props.priorityPrefix);
    } else if (minder.execCommand && !commandDisabled.value) {
      minder.execCommand('priority');
    }
  }
</script>

<style lang="less" scoped>
  .delete-btn {
    border-color: #909399;
    background-color: #909399;
    i {
      width: 1em !important;
      height: 1em !important;
    }
  }
  .priority-btn {
    @apply border-none p-0 italic text-white;
  }
  .priority-btn_1 {
    border-bottom: 3px solid #840023;
    background-color: #ff1200;
  }
  .priority-btn_1:hover:not(:disabled) {
    border-bottom: 3px solid #840023;
    color: white;
    background-color: #ff1200;
  }
  .priority-btn_2 {
    border-bottom: 3px solid #01467f;
    background-color: #0074ff;
  }
  .priority-btn_2:hover:not(:disabled) {
    border-bottom: 3px solid #01467f;
    color: white;
    background-color: #0074ff;
  }
  .priority-btn_3 {
    border-bottom: 3px solid #006300;
    background-color: #00af00;
  }
  .priority-btn_3:hover:not(:disabled) {
    border-bottom: 3px solid #006300;
    color: white;
    background-color: #00af00;
  }
  .priority-btn_4 {
    border-bottom: 3px solid #b25000;
    background-color: #ff962e;
  }
  .priority-btn_4:hover:not(:disabled) {
    border-bottom: 3px solid #b25000;
    color: white;
    background-color: #ff962e;
  }
  .priority-btn_5 {
    border-bottom: 3px solid #4720c4;
    background-color: #a464ff;
  }
  .priority-btn_5:hover:not(:disabled) {
    border-bottom: 3px solid #4720c4;
    color: white;
    background-color: #a464ff;
  }
  .priority-btn_6 {
    border-bottom: 3px solid #515151;
    background-color: #a3a3a3;
  }
  .priority-btn_6:hover:not(:disabled) {
    border-bottom: 3px solid #515151;
    color: white;
    background-color: #a3a3a3;
  }
</style>
