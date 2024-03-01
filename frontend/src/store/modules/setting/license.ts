import { defineStore } from 'pinia';

import { getLicenseInfo } from '@/api/modules/setting/authorizedManagement';

import useAppStore from '../app';

const useLicenseStore = defineStore('license', {
  persist: true,
  state: (): { status: string | null } => ({
    status: '',
  }),
  actions: {
    setLicenseStatus(status: string) {
      this.status = status;
    },
    removeLicenseStatus() {
      this.status = null;
    },
    hasLicense() {
      return this.status && this.status === 'valid';
    },
    // license校验
    async getValidateLicense() {
      const appStore = useAppStore();
      try {
        const result = await getLicenseInfo();
        if (!result || !result.status || !result.license || !result.license.count) {
          return;
        }
        this.setLicenseStatus(result.status);
      } catch (error) {
        console.log(error);
      }
    },
  },
});

export default useLicenseStore;
