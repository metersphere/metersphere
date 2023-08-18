import { defineStore } from 'pinia';

const LicenseKey = 'License_Key';

const useLicenseStore = defineStore('userGroup', {
  state: (): { status: string | null } => ({
    status: '',
  }),
  actions: {
    setLicenseStatus(status: string) {
      this.$state.status = status;
      localStorage.setItem(LicenseKey, status);
    },
    removeLicenseStatus() {
      localStorage.removeItem(LicenseKey);
    },
    getLicenseStatus() {
      this.status = localStorage.getItem(LicenseKey);
      return this.status;
    },
    hasLicense() {
      this.getLicenseStatus();
      return this.status && this.status === 'valid';
    },
  },
});

export default useLicenseStore;
