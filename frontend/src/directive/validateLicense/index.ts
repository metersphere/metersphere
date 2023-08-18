import useLicenseStore from '@/store/modules/setting/license';

/**
 * 权限指令,TODO:校验license
 * @param el dom 节点
 */

function checkHasLicense(el: HTMLElement) {
  const licenseStore = useLicenseStore();
  const isValid = licenseStore.hasLicense();

  if (!isValid && el.parentNode) {
    el.parentNode.removeChild(el);
  }
}

export default {
  mounted(el: HTMLElement) {
    checkHasLicense(el);
  },
  updated(el: HTMLElement) {
    checkHasLicense(el);
  },
};
