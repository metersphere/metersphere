<template>
  <div class="quota-container" v-loading="loading">
    <div class="quota-top">
      <default-quota :title="this.$t('quota.default.project')"
                        :quota="defaultQuota" :resources="resources"
                        @confirm="saveDefaultQuota" :quota-type="quotaType"/>
    </div>
    <div class="quota-bottom">
      <quota-list :resources="resources"
                  :default-quota="defaultQuota"
                  :quota-type="quotaType"
                  @refresh="getDefaultQuota"
                  ref="quotaList"
      />
    </div>
  </div>
</template>

<script>


import QuotaList from "../../system/quota/QuotaList";
import DefaultQuota from "../../system/quota/DefaultQuota";
import {getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getProjectDefaultQuota, getWorkspaceValidResourcePool, saveProjectDefaultQuota} from "../../../api/quota";
import {QUOTA_TYPE} from "../../../common/constants";

export default {
  name: "MxProjectQuota",
  components: {
    DefaultQuota,
    QuotaList,
  },
  data() {
    return {
      loading: false,
      defaultQuota: {},
      resources: [],
      QUOTA_TYPE,
      quotaType: QUOTA_TYPE.PROJECT
    }
  },
  methods: {
    getDefaultQuota(func) {
      this.defaultQuota = {};
      this.loading = getProjectDefaultQuota(getCurrentWorkspaceId())
        .then(res => {
          this.defaultQuota = res.data;
          if (func && typeof func === 'function') {
            func();
          }
        });
    },
    saveDefaultQuota(obj) {
      obj.id = getCurrentWorkspaceId();
      this.loading = saveProjectDefaultQuota(obj)
        .then(() => {
          this.$success(this.$t("commons.save_success"));
          this.getDefaultQuota();
        })
        .catch(() => {
          this.getDefaultQuota();
        });
    }
  },
  mounted() {
    getWorkspaceValidResourcePool(getCurrentWorkspaceId())
      .then(res => {
        this.resources = res.data;
      });
    this.getDefaultQuota();
  }
}
</script>

<style scoped>
.quota-container {
  width: 100%;
}

.quota-top {
  width: 100%;
  margin-bottom: 8px;
}


</style>
