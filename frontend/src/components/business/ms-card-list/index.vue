<template>
  <div ref="msCardListContainerRef" :class="['ms-card-list-container', containerStatusClass]">
    <div
      ref="msCardListRef"
      class="ms-card-list"
      :style="{
        'grid-template-columns': `repeat(auto-fill, minmax(${props.cardMinWidth}px, 1fr))`,
        'gap': `${props.gap}px` || '24px',
        'padding-bottom': props.paddingBottomSpace || 0,
      }"
    >
      <div v-if="topLoading" class="ms-card-list-loading">
        <a-spin :loading="topLoading"></a-spin>
      </div>
      <div
        v-for="(item, index) in props.mode === 'remote' ? remoteList : props.list"
        :key="item.key"
        class="ms-card-list-item"
        :style="{ 'aspect-ratio': props.isProportional ? 1 / 1 : 'none' }"
      >
        <slot name="item" :item="item" :index="index"></slot>
      </div>
      <div v-if="bottomLoading" class="ms-card-list-loading">
        <a-spin :loading="bottomLoading"></a-spin>
      </div>
    </div>
    <slot v-if="isShow" name="empty">
      <a-empty :description="t('common.noData')" class="h-[200px] justify-center" />
    </slot>
  </div>
</template>

<script setup lang="ts">
  import { nextTick, onMounted, type Ref, ref, watch, watchEffect } from 'vue';
  import { useResizeObserver } from '@vueuse/core';
  import { debounce } from 'lodash-es';

  import useContainerShadow from '@/hooks/useContainerShadow';
  import { useI18n } from '@/hooks/useI18n';

  import type { CommonList } from '@/models/common';

  const props = withDefaults(
    defineProps<{
      mode?: 'static' | 'remote'; // 模式，静态数据模式或者远程数据模式，默认静态数据模式，静态数据模式下，需要传入list；远程数据模式下，需要传入api请求函数
      list?: any[];
      cardMinWidth: number; // 卡片最小宽度px
      shadowLimit: number; // 滚动距离高度，用于计算顶部底部阴影
      remoteParams?: Record<string, any>; // 远程数据模式下，请求数据的参数
      gap?: number; // 卡片之间的间距
      isProportional?: boolean; // 是否等比正方形
      paddingBottomSpace?: string; // 是否存在底部的间距
      remoteFunc?: (v: any) => Promise<CommonList<any>>; // 远程数据模式下，请求数据的函数
    }>(),
    {
      mode: 'static',
      gap: 24,
      isProportional: true,
    }
  );

  const { t } = useI18n();

  const msCardListRef: Ref<HTMLElement | null> = ref(null);
  const msCardListContainerRef: Ref<HTMLElement | null> = ref(null);

  const { isArrivedTop, isArrivedBottom, isInitListener, containerStatusClass, setContainer, initScrollListener } =
    useContainerShadow({
      overHeight: props.shadowLimit,
      containerClassName: 'ms-card-list-container',
    });

  /**
   * 初始化列表滚动监听
   * @param arr 列表数组
   */
  function initListListener(arr?: any[]) {
    if (arr && arr.length > 0 && !isInitListener.value) {
      nextTick(() => {
        if (msCardListRef.value) {
          setContainer(msCardListRef.value);
          initScrollListener();
        }
      });
    }
  }

  watch(
    () => props.list,
    (val) => {
      if (props.mode === 'static') {
        initListListener(val);
      }
    },
    {
      immediate: true,
    }
  );

  const listSize = ref(0);
  const listTotal = ref(0);
  const remoteList = ref<any[]>([]);
  const noMore = ref(false);
  const isInit = ref(false);
  const endPage = ref(0); // 列表数据已加载的底部页码
  const topLoading = ref(false);
  const bottomLoading = ref(false);
  const isLoadError = ref(false); // 是否加载失败 TODO:加载失败重试

  /**
   * 加载上一页
   */
  // async function loadPrevList() {
  //   try {
  //     if (props.mode === 'remote' && typeof props.remoteFunc === 'function') {
  //       topLoading.value = true;
  //       endPage.value -= 1;
  //       const res = await props.remoteFunc({
  //         current: startPage.value,
  //         pageSize: listSize.value,
  //         ...(props.remoteParams || {}),
  //       });
  //       remoteList.value = isApartMax.value
  //         ? [...res.list, remoteList.value.slice(0, listSize.value * endPage.value)]
  //         : remoteList.value.concat(res.list);
  //       listTotal.value = res.total;
  //     }
  //   } catch (error) {
  //     // eslint-disable-next-line no-console
  //     console.log(error);
  //   } finally {
  //     topLoading.value = false;
  //   }
  // }

  /**
   * 加载下一页
   * @param isReload 是否重新加载列表
   */
  async function loadNextList(isReload = false) {
    try {
      if (props.mode === 'remote' && typeof props.remoteFunc === 'function') {
        bottomLoading.value = true;
        endPage.value += 1;
        if (isReload) {
          endPage.value = 1;
        }
        const res = await props.remoteFunc({
          current: endPage.value,
          pageSize: listSize.value,
          ...(props.remoteParams || {}),
        });
        if (isReload) {
          remoteList.value = res.list;
        } else {
          remoteList.value = remoteList.value.concat(res.list);
        }
        listTotal.value = res.total;
        bottomLoading.value = false;
        noMore.value = listSize.value * endPage.value >= listTotal.value;
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      isLoadError.value = true;
    } finally {
      bottomLoading.value = false;
    }
  }

  /**
   * 计算列表每页显示数量
   * @param width 容器宽度
   * @param height 容器高度
   */
  async function computedListSize(width?: number, height?: number) {
    let clientWidth = 0;
    let clientHeight = 0;
    if (width !== undefined && height !== undefined) {
      clientWidth = width;
      clientHeight = height;
    } else if (msCardListContainerRef.value && msCardListRef.value) {
      clientWidth = msCardListRef.value.clientWidth;
      clientHeight = msCardListContainerRef.value.clientHeight;
    }
    // 最大列数 = （容器宽度 + gap） / （最小卡片宽度 + gap）
    const maxCols = Math.floor((clientWidth + props.gap) / (props.cardMinWidth + props.gap));
    // 最大行数 =（容器高度 + gap） / （最小卡片宽度 + gap）。因为卡片宽高是 1:1，所以行数和列数相同。这里+2是为了在视图区域外多加载两行，以避免滚动条不出现
    const maxRows = Math.round((clientHeight + props.gap) / (props.cardMinWidth + props.gap)) + 2;
    // 确保最小为5
    listSize.value = Math.max(5, maxCols * maxRows);
    setTimeout(() => {
      // 设置 400ms 后初始化完成，避免 useResizeObserver 一开始就触发，因为useResizeObserver使用了debounce-300ms，所以会有延迟
      isInit.value = true;
    }, 400);
    // 列表显示数量发生变化，重置列表页码，重新加载列表
    endPage.value = 0;
    await loadNextList(true);
  }

  /**
   * 容器大小变化监听，列表的每页显示数量会根据容器大小变化而变化，计算完成后会重新加载列表
   */
  useResizeObserver(
    msCardListContainerRef,
    debounce((entries) => {
      const entry = entries[0];
      const { width, height } = entry.contentRect;
      if (isInit.value && !isLoadError.value && !noMore.value) {
        computedListSize(width, height);
      }
    }, 300)
  );

  const isShow = computed(() => {
    if (props.mode === 'static') {
      return props.list?.length === 0;
    }
    return !topLoading.value && !bottomLoading.value && (remoteList.value.length === 0 || props.list?.length === 0);
  });

  onMounted(async () => {
    if (props.mode === 'remote') {
      await computedListSize();
      initListListener(remoteList.value);
    }
  });

  watchEffect(async () => {
    // 远程数据模式下，滚动到顶部或者底部时，加载上一页或者下一页
    if (props.mode === 'remote' && typeof props.remoteFunc === 'function') {
      if (isArrivedBottom.value && !isArrivedTop.value && !noMore.value && !bottomLoading.value && !isLoadError.value) {
        // 滚动到底部且未滚动到顶部（也就是数据量大于 1 页），且不是正在加载下一页，则加载下一页
        loadNextList();
      }
    }
  });

  function reload() {
    loadNextList(true);
  }

  defineExpose({
    reload,
  });
</script>

<style lang="less" scoped>
  .ms-card-list-container {
    @apply h-full overflow-hidden;
    .ms-container--shadow-y();
    .ms-card-list {
      @apply grid max-h-full overflow-auto;

      .ms-scroll-bar();

      grid-template-columns: repeat(auto-fill, minmax(102px, 1fr));
      .ms-card-list-item {
        @apply relative w-full;
      }
      .ms-card-list-loading {
        @apply col-span-full flex items-center justify-center;

        padding: 8px;
      }
    }
  }
  :deep(.arco-icon-empty) {
    font-size: 48px;
  }
</style>
