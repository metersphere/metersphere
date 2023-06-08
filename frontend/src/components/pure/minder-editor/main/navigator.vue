<template>
  <div class="navigator">
    <div class="nav-bar">
      <div
        class="nav-btn zoom-in"
        :title="t('minder.main.navigator.amplification')"
        :class="{ active: zoomRadioIn }"
        @click="zoomIn"
      >
        <div class="icon" />
      </div>
      <div ref="zoomPan" class="zoom-pan">
        <div class="origin" :style="{ transform: 'translate(0, ' + getHeight(100) + 'px)' }" @click="RestoreSize" />
        <div
          class="indicator"
          :style="{
            transform: 'translate(0, ' + getHeight(zoom) + 'px)',
            transition: 'transform 200ms',
          }"
        />
      </div>
      <div
        class="nav-btn zoom-out"
        :title="t('minder.main.navigator.narrow')"
        :class="{ active: zoomRadioOut }"
        @click="zoomOut"
      >
        <div class="icon" />
      </div>
      <div class="nav-btn hand" :title="t('minder.main.navigator.drag')" :class="{ active: enableHand }" @click="hand">
        <div class="icon" />
      </div>
      <div class="nav-btn camera" :title="t('minder.main.navigator.locating_root')" @click="locateToOrigin">
        <div class="icon" />
      </div>
      <div
        class="nav-btn nav-trigger"
        :class="{ active: isNavOpen }"
        :title="t('minder.main.navigator.navigator')"
        @click="toggleNavOpen"
      >
        <div class="icon" />
      </div>
    </div>
    <div v-show="isNavOpen" ref="navPreviewer" class="nav-previewer" />
  </div>
</template>

<script lang="ts" name="navigator" setup>
  import { computed, nextTick, onMounted, reactive, ref } from 'vue';
  import type { Ref } from 'vue';
  import { getLocalStorage, setLocalStorage } from '../script/store';
  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  const zoomPan: Ref<HTMLDivElement | null> = ref(null);
  const navPreviewer: Ref<HTMLDivElement | null> = ref(null);

  const zoom = ref(100);
  const isNavOpen = ref(true);
  const previewNavigator: Ref<HTMLDivElement | null> = ref(null);
  const contentView = ref('');

  let visibleView = reactive<any>({});
  let visibleRect = reactive<any>({});
  let nodeThumb = reactive<any>({});
  let connectionThumb = reactive<any>({});
  let paper = reactive<any>({});
  let minder = reactive<any>({});

  const config = reactive({
    // 右侧面板最小宽度
    ctrlPanelMin: 250,
    // 右侧面板宽度
    ctrlPanelWidth: parseInt(window.localStorage.getItem('__dev_minder_ctrlPanelWidth') || '', 10) || 250,
    // 分割线宽度
    dividerWidth: 3,
    // 默认语言
    defaultLang: 'zh-cn',
    // 放大缩小比例
    zoom: [10, 20, 30, 50, 80, 100, 120, 150, 200],
  });

  const enableHand = ref(minder && minder.queryCommandState && minder.queryCommandState('hand') === 1);
  // 避免缓存
  function getNavOpenState() {
    return getLocalStorage('navigator-hidden');
  }
  function zoomIn() {
    if (minder && minder.execCommand) {
      minder.execCommand('zoomIn');
    }
  }
  function RestoreSize() {
    if (minder && minder.execCommand) {
      minder.execCommand('zoom', 100);
    }
  }
  function zoomOut() {
    if (minder && minder.execCommand) {
      minder.execCommand('zoomOut');
    }
  }
  function hand() {
    if (minder && minder.execCommand) {
      minder.execCommand('hand');
      enableHand.value = minder.queryCommandState && minder.queryCommandState('hand') === 1;
    }
  }
  function getZoomRadio(value: number) {
    try {
      if (!minder) return 2;
    } catch (e) {
      // 如果window的还没挂载minder，先捕捉undefined异常
      return 2;
    }
    const zoomStack = minder && minder.getOption && minder.getOption('zoom');
    if (!zoomStack) {
      return 2;
    }
    const minValue = zoomStack[0];
    const maxValue = zoomStack[zoomStack.length - 1];
    const valueRange = maxValue - minValue;

    return 1 - (value - minValue) / valueRange;
  }
  const zoomRadioIn = computed(() => getZoomRadio(zoom.value) === 0);
  const zoomRadioOut = computed(() => getZoomRadio(zoom.value) === 1);

  function getHeight(value: number) {
    const totalHeight = Number(zoomPan.value?.style.height);
    return getZoomRadio(value) * totalHeight;
  }
  function locateToOrigin() {
    if (minder && minder.execCommand && minder.getRoot) {
      minder.execCommand('camera', minder.getRoot(), 600);
    }
  }

  function getPathHandler(theme?: string) {
    switch (theme) {
      case 'tianpan':
      case 'tianpan-compact':
        return (nodePathData: any[], x: number, y: number, width: number) => {
          // eslint-disable-next-line no-bitwise
          const r = width >> 1;
          nodePathData.push('M', x, y + r, 'a', r, r, 0, 1, 1, 0, 0.01, 'z');
        };
      default: {
        return (nodePathData: any[], x: number, y: number, width: number, height: number) => {
          nodePathData.push('M', x, y, 'h', width, 'v', height, 'h', -width, 'z');
        };
      }
    }
  }

  let pathHandler = getPathHandler(minder.getTheme ? minder.getTheme() : '');

  function updateVisibleView() {
    if (minder.getViewDragger && visibleRect.setBox) {
      visibleView = minder.getViewDragger().getView();
      visibleRect.setBox(visibleView.intersect && visibleView.intersect(contentView.value));
    }
  }

  function updateContentView() {
    if (!minder.getRenderContainer || !paper.setViewBox || !minder.getRoot) return;
    const view = minder.getRenderContainer().getBoundaryBox();
    contentView.value = view;
    const padding = 30;
    paper.setViewBox(
      view.x - padding - 0.5,
      view.y - padding - 0.5,
      view.width + padding * 2 + 1,
      view.height + padding * 2 + 1
    );
    const nodePathData: any[] = [];
    const connectionThumbData: any[] = [];
    minder.getRoot().traverse((node: any) => {
      const box = node.getLayoutBox();
      pathHandler(nodePathData, box.x, box.y, box.width, box.height);
      if (node.getConnection() && node.parent && node.parent.isExpanded()) {
        connectionThumbData.push(node.getConnection().getPathData());
      }
    });
    if (!paper.setStyle || !minder.getStyle || !nodeThumb.fill || !nodeThumb.setPathData) return;
    paper.setStyle('background', minder.getStyle('background'));
    if (nodePathData.length) {
      nodeThumb.fill(minder.getStyle('root-background')).setPathData(nodePathData);
    } else {
      nodeThumb.setPathData(null);
    }
    if (connectionThumbData.length && connectionThumb.stroke) {
      connectionThumb.stroke(minder.getStyle('connect-color'), '0.5%').setPathData(connectionThumbData);
    } else if (connectionThumb.setPathData) {
      connectionThumb.setPathData(null);
    }
    updateVisibleView();
  }

  function bind() {
    if (minder && minder.on) {
      minder.on('layout layoutallfinish', updateContentView);
      minder.on('viewchange', updateVisibleView);
    }
  }

  function unbind() {
    if (minder && minder.off) {
      minder.off('layout layoutallfinish', updateContentView);
      minder.off('viewchange', updateVisibleView);
    }
  }

  function toggleNavOpen() {
    let isNavOpenState = false;
    isNavOpenState = !JSON.parse(getNavOpenState());
    isNavOpen.value = isNavOpenState;
    setLocalStorage('navigator-hidden', isNavOpen.value);

    nextTick(() => {
      if (isNavOpenState) {
        bind();
        updateContentView();
        updateVisibleView();
      } else {
        unbind();
      }
    });
  }
  function navigate() {
    function moveView(center: Record<string, any>, duration?: number) {
      if (!minder.getPaper || !visibleView.width || !visibleView.height) return;
      let box = visibleView;
      // eslint-disable-next-line no-param-reassign
      center.x = -center.x;
      // eslint-disable-next-line no-param-reassign
      center.y = -center.y;
      const viewMatrix = minder.getPaper().getViewPortMatrix();
      box = viewMatrix.transformBox(box);
      if (!box.width || !box.height) return;
      const targetPosition = center.offset(box.width / 2, box.height / 2);
      if (minder.getViewDragger) minder.getViewDragger().moveTo(targetPosition, duration);
    }
    if (!paper.on) return;
    let dragging = false;
    paper.on('mousedown', (e: any) => {
      dragging = true;
      moveView(e.getPosition('top'), 200);
      previewNavigator.value?.classList.add('grab');
    });
    paper.on('mousemove', (e: any) => {
      if (dragging) {
        moveView(e.getPosition('top'));
      }
    });

    paper.on('mouseup', () => {
      dragging = false;
      previewNavigator.value?.classList.remove('grab');
    });
  }

  onMounted(() => {
    nextTick(() => {
      minder = window.minder;
      const { kity } = window;
      // 以下部分是缩略图导航器
      previewNavigator.value = navPreviewer.value;

      // 画布，渲染缩略图
      paper = new kity.Paper(previewNavigator.value);

      // 用两个路径来绘制节点和连线的缩略图
      if (!paper.put || !minder || !minder.on) return;
      nodeThumb = paper.put(new kity.Path());
      connectionThumb = paper.put(new kity.Path());
      // 表示可视区域的矩形
      visibleRect = paper.put(new kity.Rect(100, 100).stroke('red', '1%'));

      contentView.value = new kity.Box();
      visibleView = new kity.Box();

      pathHandler = getPathHandler(minder.getTheme ? minder.getTheme() : '');
      if (minder.setDefaultOptions) {
        minder.setDefaultOptions({
          zoom: config.zoom,
        });
      }

      minder.on('zoom', (e: any) => {
        zoom.value = e.zoom;
      });
      if (isNavOpen.value) {
        bind();
        updateContentView();
        updateVisibleView();
      } else {
        unbind();
      }
      // 主题切换事件
      minder.on('themechange', (e: any) => {
        pathHandler = getPathHandler(e.theme);
      });

      navigate();
    });
  });
</script>

<style lang="less" scoped>
  .nav-btn .icon {
    background-image: url('@/assets/images/minder/icons.png');
  }
</style>
