<template>
  <div class="ms-minder-navigator">
    <div class="ms-minder-navigator-bar">
      <a-slider
        v-model:model-value="zoomPercent"
        :min="0"
        :max="150"
        :step="1"
        :style="{ width: '100px' }"
        :format-tooltip="formatter"
        :marks="{
          50: '',
        }"
        class="ml-[8px]"
        @change="changeZoom"
      />
      <a-tooltip :content="t('minder.main.navigator.drag')">
        <MsButton
          type="icon"
          class="ms-minder-navigator-bar-icon-button"
          :class="enableHand ? 'ms-minder-navigator-bar-icon-button--focus' : ''"
          @click="hand"
        >
          <icon-drag-arrow class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-tooltip :content="t('minder.main.navigator.navigator')">
        <MsButton
          type="icon"
          class="ms-minder-navigator-bar-icon-button"
          :class="isNavOpen ? 'ms-minder-navigator-bar-icon-button--focus' : ''"
          @click="toggleNavOpen"
        >
          <MsIcon type="icon-icon_frame_select" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-tooltip :content="t('minder.main.navigator.locating_root')">
        <MsButton type="icon" class="ms-minder-navigator-bar-icon-button" @click="locateToOrigin">
          <MsIcon type="icon-icon_aiming" class="text-[var(--color-text-4)]" />
        </MsButton>
      </a-tooltip>
      <a-trigger
        :popup-translate="[5, -20]"
        position="rb"
        class="ms-minder-shortcut-trigger"
        @popup-visible-change="(val) => (shortcutTriggerVisible = val)"
      >
        <MsButton
          type="icon"
          class="ms-minder-navigator-bar-icon-button"
          :class="shortcutTriggerVisible ? 'ms-minder-navigator-bar-icon-button--focus' : ''"
          @click="locateToOrigin"
        >
          <MsIcon type="icon-icon_keyboard" class="text-[var(--color-text-4)]" />
        </MsButton>
        <template #content>
          <div class="mb-[4px] text-[14px] font-medium">{{ t('minder.shortcutTitle') }}</div>
          <div class="ms-minder-shortcut-trigger-list">
            <div class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.expand') }}</div>
              <div class="ms-minder-shortcut-trigger-listitem-icon">/</div>
            </div>
            <div v-if="props.shortcutList.includes('copy')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('common.copy') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <MsCtrlOrCommand />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">C</div>
              </div>
            </div>
            <div v-if="props.shortcutList.includes('addSibling')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.hotboxMenu.insetBrother') }}</div>
              <div class="ms-minder-shortcut-trigger-listitem-icon">
                <MsIcon type="icon-icon_carriage_return2" />
              </div>
            </div>
            <div v-if="props.shortcutList.includes('paste')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.hotboxMenu.paste') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <MsCtrlOrCommand />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">V</div>
              </div>
            </div>
            <div v-if="props.shortcutList.includes('addChild')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.hotboxMenu.insetSon') }}</div>
              <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto">
                Tab
              </div>
            </div>
            <div v-if="props.shortcutList.includes('cut')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.hotboxMenu.cut') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <MsCtrlOrCommand />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">X</div>
              </div>
            </div>
            <div v-if="props.shortcutList.includes('enter')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.hotboxMenu.enterNode') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <MsCtrlOrCommand />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <MsIcon type="icon-icon_carriage_return2" />
                </div>
              </div>
            </div>
            <!-- <div class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.main.history.undo') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <icon-command :size="14" />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">Z</div>
              </div>
            </div> -->
            <div v-if="props.shortcutList.includes('delete')" class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('common.delete') }}</div>
              <div class="ms-minder-shortcut-trigger-listitem-icon">
                <MsIcon type="icon-icon_carriage_return1" />
              </div>
            </div>
            <!-- <div class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.main.history.redo') }}</div>
              <div class="flex items-center gap-[4px]">
                <div class="ms-minder-shortcut-trigger-listitem-icon">
                  <icon-command :size="14" />
                </div>
                <div class="ms-minder-shortcut-trigger-listitem-icon">Y</div>
              </div>
            </div> -->
            <div class="ms-minder-shortcut-trigger-listitem">
              <div>{{ t('minder.editNodeText') }}</div>
              <div class="ms-minder-shortcut-trigger-listitem-icon ms-minder-shortcut-trigger-listitem-icon-auto">
                Space
              </div>
            </div>
            <slot name="shortCutList"></slot>
          </div>
        </template>
      </a-trigger>
    </div>
    <div v-show="isNavOpen" ref="navPreviewer" class="ms-minder-navigator-previewer" />
  </div>
</template>

<script lang="ts" setup>
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCtrlOrCommand from '@/components/pure/ms-ctrl-or-command';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  import { navigatorProps } from '../props';
  import { setLocalStorage } from '../script/store';
  import type { Ref } from 'vue';

  const props = defineProps(navigatorProps);

  const { t } = useI18n();

  const navPreviewer: Ref<HTMLDivElement | null> = ref(null);

  const isNavOpen = ref(window.localStorage.getItem('navigator-hidden') || false);
  const previewNavigator: Ref<HTMLDivElement | null> = ref(null);
  const contentView = ref('');

  let visibleView = reactive<any>({});
  let visibleRect = reactive<any>({});
  let nodeThumb = reactive<any>({});
  let connectionThumb = reactive<any>({});
  let paper = reactive<any>({});
  let minder = reactive<any>({});

  const zoomPercent = ref(50); // 默认 100%缩放（滑动条是从 50% 开始，所以减 50）
  /**
   * 缩放
   * @param value 缩放值
   */
  function changeZoom(value: number | [number, number]) {
    if (minder && minder.execCommand) {
      minder.execCommand('zoom', (value as number) + 50);
    }
  }

  function formatter(value: number) {
    // 滑动条 0 开始就是 50%，所以+ 50
    return `${value + 50}%`;
  }

  const enableHand = ref(false);
  /**
   * 开启拖拽模式
   */
  function hand() {
    if (minder && minder.execCommand) {
      minder.execCommand('hand');
      enableHand.value = minder.queryCommandState && minder.queryCommandState('hand') === 1;
    }
  }

  /**
   * 回到根节点
   */
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

  /**
   * 更新导航器视图
   */
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
    isNavOpen.value = !isNavOpen.value;
    setLocalStorage('navigator-hidden', isNavOpen.value);

    nextTick(() => {
      if (isNavOpen.value) {
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

  const shortcutTriggerVisible = ref(false);

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

      minder.on('zoom', (e: any) => {
        zoomPercent.value = e.zoom - 50;
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

<style lang="less">
  .ms-minder-shortcut-trigger {
    .arco-trigger-content {
      @apply w-auto;

      padding: 16px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-fff);
      box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
      .ms-minder-shortcut-trigger-list {
        @apply grid grid-cols-2;

        gap: 8px 12px;
        .ms-minder-shortcut-trigger-listitem {
          @apply flex items-center justify-between;

          padding: 4px 8px;
          width: 190px;
          font-size: 12px;
          border-radius: var(--border-radius-small);
          background-color: var(--color-text-n9);
          line-height: 16px;
          .ms-minder-shortcut-trigger-listitem-icon {
            @apply flex items-center justify-center font-medium;

            width: 22px;
            height: 22px;
            font-size: 12px;
            border: 1px solid var(--color-text-n8);
            border-radius: var(--border-radius-small);
            color: var(--color-text-4);
            line-height: 16px;
          }
          .ms-minder-shortcut-trigger-listitem-icon-auto {
            padding: 2px 4px;
            width: auto;
          }
        }
      }
    }
  }
</style>

<style lang="less" scoped>
  .ms-minder-navigator {
    @apply absolute;

    bottom: 6px;
    left: 6px;
    box-shadow: 0 4px 10px -1px rgb(100 100 102 / 15%);
    .ms-minder-navigator-bar {
      @apply flex w-auto items-center;

      padding: 4px 8px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-fff);
      gap: 8px;
      .ms-minder-navigator-bar-icon-button {
        @apply !mr-0;
        &:hover {
          background-color: rgb(var(--primary-1)) !important;
          .arco-icon {
            color: rgb(var(--primary-4)) !important;
          }
        }
      }
      .ms-minder-navigator-bar-icon-button--focus {
        background-color: rgb(var(--primary-1)) !important;
        .arco-icon {
          color: rgb(var(--primary-5)) !important;
        }
      }
      :deep(.arco-slider-with-marks) {
        @apply mb-0 p-0;
      }
    }
    .ms-minder-navigator-previewer {
      @apply absolute cursor-crosshair;

      bottom: 36px;
      left: 45px;
      z-index: 9;
      padding: 8px;
      width: 240px;
      height: 160px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-fff);
      box-shadow: 0 5px 5px -3px rgb(0 0 0 / 10%), 0 8px 10px 1px rgb(0 0 0 / 6%), 0 3px 14px 2px rgb(0 0 0 / 5%);
      transition: -webkit-transform 0.7s 0.1s ease;
      transition: transform 0.7s 0.1s ease;
      .grab {
        @apply cursor-grabbing;
      }
      :deep(svg) {
        background-color: var(--color-text-fff) !important;
      }
    }
  }
</style>
