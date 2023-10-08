<template>
  <div class="quota-container" v-loading="loading">
    <div class="quota-top">
      <default-quota :title="$t('quota.default.workspace')"
                        :quota="defaultQuota" :resources="resources" :modules="modules"
                        @confirm="saveDefaultQuota" :quota-type="quotaType"/>
    </div>
    <div class="quota-bottom">
      <quota-list :resources="resources" :modules="modules" :default-quota="defaultQuota" :quota-type="quotaType" ref="quotaList"/>
    </div>
  </div>
</template>

<script>
import DefaultQuota from "./DefaultQuota";
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperators from "metersphere-frontend/src/components/MsTableOperators";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import QuotaList from "./QuotaList";
import {getAllValidResourcePool, getWorkspaceDefaultQuota, saveWorkspaceDefaultQuota} from "../../../api/quota";
import {QUOTA_TYPE} from "../../../common/constants";

export default {
  name: "MxWorkspaceQuota",
  components: {
    MsTableOperators,
    MsTablePagination,
    MsTableHeader,
    DefaultQuota,
    QuotaList,
  },
  data() {
    return {
      loading: false,
      defaultQuota: {},
      resources: [],
      modules:[
        {
          name: this.$t('commons.my_workstation'),
          id: 'workstation',
        },
        {
          name: this.$t('test_track.test_track'),
          id: 'track',
        },
        {
          name: this.$t('commons.api'),
          id: 'api',
        },
        {
          name: this.$t('commons.ui'),
          id: 'ui',
        },
        {
          name: this.$t('commons.performance') ,
          id: 'performance',
        },
        {
          name: this.$t('commons.report_statistics.title') ,
          id: 'report',
        },
      ],
      QUOTA_TYPE,
      quotaType: QUOTA_TYPE.WORKSPACE
    }
  },
  methods: {
    getResourcePool() {
      getAllValidResourcePool().then(res => {
        this.resources = res.data;
      });
    },
    getDefaultQuota(func) {
      this.defaultQuota = {};
      this.loading = getWorkspaceDefaultQuota()
        .then(res => {
          this.defaultQuota = res.data;
          if (func && typeof func === 'function') {
            func();
          }
        });
      let module = localStorage.getItem('modules');
      module = module ? JSON.parse(module) : [];
      this.modules = this.modules.filter(item => module[item.id] === "ENABLE");

    },
    saveDefaultQuota(obj) {
      this.loading = saveWorkspaceDefaultQuota(obj)
        .then(() => {
          this.$success(this.$t("commons.save_success"));
          this.getDefaultQuota();
        })
        .catch(() => {
          this.getDefaultQuota();
        });
    },
  },
  mounted() {
    this.getResourcePool();
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
