import useLicenseStore from '@/store/modules/setting/license';

/**
 * 权限指令,TODO:校验license
 * @param el dom 节点
 */

function checkHasLicenseExpiration(el: HTMLElement) {
  const licenseStore = useLicenseStore();
  const isValid = licenseStore.expiredDuring;

  if (!isValid && el.parentNode) {
    el.parentNode.removeChild(el);
  }
}

export default {
  mounted(el: HTMLElement) {
    checkHasLicenseExpiration(el);
  },
  updated(el: HTMLElement) {
    checkHasLicenseExpiration(el);
  },
};
