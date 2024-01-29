/**
 * 全屏 hook
 * @param domRef dom ref
 */
export default function useFullScreen(domRef: Ref<HTMLElement | null | undefined>) {
  const isFullScreen = ref(false);

  function enterFullScreen() {
    domRef.value?.setAttribute('style', 'position: fixed; top: 0; left: 0; right: 0; bottom: 0; z-index: 100;');
    isFullScreen.value = true;
  }

  function exitFullscreen() {
    domRef.value?.setAttribute('style', '');
    isFullScreen.value = false;
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
  };
}
