<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="4">
        <el-select v-model="currentInstance" placeholder="" size="small" style="width: 100%"
                   @change="changeInstance(currentInstance)">
          <el-option
              v-for="item in resource"
              :key="item.resourceId"
              :label="item.resourceName"
              :value="item.resourceId">
          </el-option>
        </el-select>
      </el-col>
      <el-col :span="20">
        <div v-if="currentInstance" class="logging-content">
          <ul class="infinite-list" v-infinite-scroll="load(currentInstance)">
            <li class="infinite-list-item" v-for="(log, index) in logContent[currentInstance]"
                :key="currentInstance+index">
              {{ log.content }}
            </li>
          </ul>
          <el-link type="primary" @click="downloadLogFile(item)">{{ $t('load_test.download_log_file') }}</el-link>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import {
  getPerformanceReportLogResource,
  getPerformanceReportLogResourceDetail,
  getSharePerformanceReportLogResource,
  getSharePerformanceReportLogResourceDetail,
} from "@/network/load-test";

export default {
  name: "LogDetails",
  data() {
    return {
      resource: [],
      logContent: {},
      result: {},
      id: '',
      page: {},
      pageCount: 5,
      loading: false,
      init: false,
      logStatus: {},
      currentInstance: ''
    };
  },
  props: ['report', 'export', 'isShare', 'shareId', 'planReportTemplate'],
  methods: {
    getResource() {
      if (this.planReportTemplate) {
        this.handleGetLogResource(this.planReportTemplate.reportLogResource);
      } else if (this.isShare) {
        getSharePerformanceReportLogResource(this.shareId, this.id, (data) => {
          this.handleGetLogResource(data);
        });
      } else {
        getPerformanceReportLogResource(this.id, (data) => {
          this.handleGetLogResource(data);
        });
      }
    },
    handleGetLogResource(data) {
      this.resource = data;
      if (!this.resource || this.resource.length === 0) {
        this.init = false;
      }
      if (!this.currentInstance) {
        this.currentInstance = this.resource[0].resourceId;
      }
      this.page = data.map(item => item.resourceId).reduce((result, curr) => {
        result[curr] = 1;
        return result;
      }, {});
      this.logContent = data.map(item => item.resourceId).reduce((result, curr) => {
        result[curr] = [];
        return result;
      }, {});
    },
    load(resourceId) {
      if (this.loading || this.page[resourceId] > this.pageCount) {
        return;
      }
      this.logStatus[resourceId] = true;
      this.loading = true;
      if (this.planReportTemplate) {
        // this.handleGetLogResourceDetail(this.planReportTemplate.logResourceDetail, resourceId);
      } else if (this.isShare) {
        getSharePerformanceReportLogResourceDetail(this.shareId, this.id, resourceId, this.page[resourceId] || 1, data => {
          this.handleGetLogResourceDetail(data, resourceId);
        });
      } else {
        getPerformanceReportLogResourceDetail(this.id, resourceId, this.page[resourceId] || 1, data => {
          this.handleGetLogResourceDetail(data, resourceId);
        });
      }
    },
    handleGetLogResourceDetail(data, resourceId) {
      data.listObject.forEach(log => {
        this.logContent[resourceId].push(log);
      });
      this.page[resourceId]++;
      this.loading = false;
    },
    changeInstance(instance) {
      this.currentInstance = instance;
      if (this.logStatus[instance]) {
        return;
      }
      this.loading = false;
      this.page[instance] = 1;
      this.logContent[instance] = [];
      this.load(instance);
    },
    downloadLogFile(item) {
      let config = {
        url: '/performance/report/log/download/' + this.id + '/' + item.resourceId,
        method: 'get',
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
        const filename = 'jmeter.log';
        const blob = new Blob([response.data]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = filename;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href);
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, filename);
        }
      });
    },
  },
  created() {
    this.id = this.$route.path.split('/')[4];
    this.getResource();
  },
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.id = to.path.split('/')[4];
        this.init = false;
        this.getResource();
      }
    },
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        if (this.init) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Running") {
          this.getResource();
        } else {
          this.resource = [];
        }
      },
      deep: true
    },
    planReportTemplate: {
      handler() {
        if (this.planReportTemplate) {
          this.getResource();
        }
      },
      deep: true
    }
  },
};
</script>

<style scoped>
.logging-content {
  white-space: pre-line;
  overflow: auto;
}

.infinite-list {
  height: calc(100vh - 250px);
  padding: 0;
  margin: 0;
  list-style: none;
  overflow: auto
}

.infinite-list-item {
  overflow: hidden;
}

</style>
