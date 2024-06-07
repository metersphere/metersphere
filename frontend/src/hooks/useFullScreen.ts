import { mergeStyles } from '@/utils/dom';

export interface UseFullScreen {
  isFullScreen: Ref<boolean>;
  toggleFullScreen: () => void;
  exitFullscreen: () => void;
}

/**
 * 全屏 hook
 * @param domRef dom ref
 */
export default function useFullScreen(
  domRef: Ref<HTMLElement | Element | null | undefined> | HTMLElement | Element | null | undefined
): UseFullScreen {
  const isFullScreen = ref(false);
  const originalStyle = ref('');

  function enterFullScreen() {
    const dom = unref(domRef);
    if (dom) {
      originalStyle.value = dom.getAttribute('style') || '';
      mergeStyles(
        dom,
        'position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 1000; width: 100%; height: 100%;'
      );
      isFullScreen.value = true;
    }
  }

  function exitFullscreen() {
    const dom = unref(domRef);
    if (dom) {
      dom.setAttribute('style', originalStyle.value);
      isFullScreen.value = false;
    }
  }

  function toggleFullScreen() {
    if (isFullScreen.value) {
      exitFullscreen();
    } else {
      enterFullScreen();
    }
  }

  return {
    isFullScreen,
    toggleFullScreen,
    exitFullscreen,
  };
}
