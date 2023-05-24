<template>
  <div class="progress-group">
    <ul>
      <ul :disabled="commandDisabled">
        <li
          v-for="(item, index) in items"
          :key="item.text"
          class="menu-btn"
          :class="classArray(index)"
          :title="title(index)"
          @click="execCommand(index)"
        />
      </ul>
    </ul>
  </div>
</template>

<script lang="ts" name="progressBox" setup>
  import { computed, nextTick, onMounted, reactive, ref } from 'vue';
  import { isDisableNode } from '../../script/tool/utils';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  let minder = reactive<any>({});
  const commandValue = ref('');

  const items = [
    { text: '0' },
    { text: '1' },
    { text: '2' },
    { text: '3' },
    { text: '4' },
    { text: '5' },
    { text: '6' },
    { text: '7' },
    { text: '8' },
    { text: '9' },
  ];

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
    });
  });

  const commandDisabled = computed(() => {
    if (Object.keys(minder).length === 0 || !minder.on) return true;
    minder.on('interactchange', () => {
      commandValue.value = minder.queryCommandValue && minder.queryCommandValue('progress');
    });
    if (isDisableNode(minder)) {
      return true;
    }
    return minder.queryCommandState && minder.queryCommandState('progress') === -1;
  });

  function execCommand(index: number) {
    if (!commandDisabled.value && minder.execCommand) {
      minder.execCommand('progress', index);
    }
  }

  function classArray(index: number) {
    const isActive = minder.queryCommandValue && minder.queryCommandValue('progress') === index;
    const sequence = `progress-${index}`;

    // 用数组返回多个class
    const arr = [
      {
        active: isActive,
      },
      sequence,
    ];
    return arr;
  }
  function title(index: number) {
    switch (index) {
      case 0:
        return t('minder.menu.progress.remove_progress');
      case 1:
        return t('minder.menu.progress.prepare');
      case 9:
        return t('minder.menu.progress.complete_all');
      default:
        return `${t('minder.menu.progress.complete') + (index - 1)}/8`;
    }
  }
</script>

<style lang="less" scoped>
  .progress-group li {
    background-image: url('@/assets/images/minder/iconprogress.png');
  }
</style>
