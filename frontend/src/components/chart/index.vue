<template>
  <VCharts v-if="renderChart" :option="options" :autoresize="autoResize" :style="{ width, height }" />
</template>

<script lang="ts" setup>
  import { ref, nextTick } from 'vue';
  import VCharts from 'vue-echarts';
  // import { useAppStore } from '@/store';

  defineProps({
    options: {
      type: Object,
      default() {
        return {};
      },
    },
    autoResize: {
      type: Boolean,
      default: true,
    },
    width: {
      type: String,
      default: '100%',
    },
    height: {
      type: String,
      default: '100%',
    },
  });
  // const appStore = useAppStore();
  // const theme = computed(() => {
  //   if (appStore.theme === 'dark') return 'dark';
  //   return '';
  // });
  const renderChart = ref(false);
  // wait container expand
  nextTick(() => {
    renderChart.value = true;
  });
</script>
