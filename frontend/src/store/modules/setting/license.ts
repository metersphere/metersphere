import { defineStore } from 'pinia';
import dayjs from 'dayjs';

import { getLicenseInfo } from '@/api/modules/setting/authorizedManagement';

const useLicenseStore = defineStore('license', {
  persist: true,
  state: (): { status: string | null; expiredDuring: boolean; expiredDays: number } => ({
    status: '',
    expiredDuring: false,
    expiredDays: 0,
  }),
  actions: {
    setLicenseStatus(status: string) {
      this.status = status;
    },
    removeLicenseStatus() {
      this.status = null;
    },
    hasLicense() {
      return this.status === 'valid';
    },
    getExpirationTime(resTime: string) {
      const today = Date.now();
      const startDate = dayjs(today).format('YYYY-MM-DD');
      const endDate = dayjs(resTime);

      const daysDifference = endDate.diff(startDate, 'day');
      this.expiredDays = daysDifference;
      if (daysDifference <= 30 && daysDifference >= 0) {
        this.expiredDuring = true;
      } else if (daysDifference <= 0 && daysDifference >= -30) {
        this.expiredDuring = true;
      } else {
        this.expiredDuring = false;
      }
    },
    // license校验
    async getValidateLicense() {
      try {
        const result = await getLicenseInfo();
        if (!result || !result.status || !result.license || !result.license.count) {
          return;
        }
        this.setLicenseStatus(result.status);
        // 计算license时间
        this.getExpirationTime(result.license.expired);
      } catch (error) {
        console.log(error);
      }
    },
  },
});

export default useLicenseStore;
