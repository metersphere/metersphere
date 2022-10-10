<template>
  <div class="license-head" v-if="validData.status === 'expired'">
    License has expired since
    {{ (validData !== undefined && validData.license !== undefined) ? validData.license.expired : '' }},
    please update license.
  </div>
</template>

<script>
import {getLicense, getSystemUserSize} from "../api/license";
import {saveLicense} from "../utils/permission";

export default {
  name: "MxLicenseMessage",
  data() {
    return {
      validData: {}
    }
  },
  created() {
    this.result = getLicense()
      .then(response => {
        let data = response.data;
        if (!data || !data.status || !data.license || !data.license.count) {
          return;
        }
        saveLicense(data.status)
        if (data.status !== 'valid') {
          localStorage.setItem('setShowLicenseCountWarning', "false");
          return;
        }
        let licenseCount = data.license.count;
        getSystemUserSize()
          .then(res => {
            let userCount = res ? res.data : 0;
            if (userCount > licenseCount) {
              localStorage.setItem('setShowLicenseCountWarning', "true");
            } else {
              localStorage.setItem('setShowLicenseCountWarning', "false");
            }
          });
      })
      .catch(e => {
      })
  },
  valid(o) {
    return getLicense();
  }
}
</script>

<style scoped>
.license-head {
  height: 30px;
  background: #BA331B;
  text-align: center;
  line-height: 30px;
  color: white;
}
</style>
