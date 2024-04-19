import { useAppStore } from '@/store';
import useLicenseStore from '@/store/modules/setting/license';

/**
 * 权限指令,TODO:校验license
 * @param el dom 节点
 */

function checkHasLicenseExpiration(el: HTMLElement) {
  const licenseStore = useLicenseStore();
  const appStore = useAppStore();
  const isValid = licenseStore.expiredDuring && appStore.packageType === 'enterprise';

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
