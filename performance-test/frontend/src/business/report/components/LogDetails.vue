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
        <div class="logging-content" v-loading="loading">
          <ul class="infinite-list">
            <li class="infinite-list-item" v-for="(log, index) in logContent"
                :key="currentInstance+index">
              {{ log.content }}
            </li>
          </ul>
          <el-link type="primary" @click="downloadLogFile(currentInstance)">{{
              $t('load_test.download_log_file')
            }}
          </el-link>
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
} from "@/api/load-test";
import {downloadLogFile} from "@/api/report";

export default {
  name: "LogDetails",
  data() {
    return {
      resource: [],
      logContent: [],
      result: {},
      id: '',
      page: 1,
      pageCount: 5,
      loading: false,
      currentInstance: ''
    };
  },
  props: ['report', 'export', 'isShare', 'shareId', 'planReportTemplate'],
  methods: {
    getResource() {
      if (this.planReportTemplate) {
        this.handleGetLogResource(this.planReportTemplate.reportLogResource);
      } else if (this.isShare) {
        getSharePerformanceReportLogResource(this.shareId, this.id)
          .then(({data}) => {
            this.handleGetLogResource(data);
          });
      } else {
        getPerformanceReportLogResource(this.id)
          .then(({data}) => {
            this.handleGetLogResource(data);
          });
      }
    },
    handleGetLogResource(data) {
      this.resource = data;
      if (!this.currentInstance) {
        this.currentInstance = this.resource[0]?.resourceId;
      }

      //
      if (this.currentInstance) {
        this.changeInstance(this.currentInstance);
      }
    },
    load(resourceId) {
      if (this.loading || this.page > this.pageCount) {
        return;
      }
      this.loading = true;
      if (this.planReportTemplate) {
        // this.handleGetLogResourceDetail(this.planReportTemplate.logResourceDetail, resourceId);
      } else if (this.isShare) {
        getSharePerformanceReportLogResourceDetail(this.shareId, this.id, resourceId, this.page || 1)
          .then(({data}) => {
            this.handleGetLogResourceDetail(data, resourceId);
          });
      } else {
        getPerformanceReportLogResourceDetail(this.id, resourceId, this.page || 1)
          .then(({data}) => {
            this.handleGetLogResourceDetail(data, resourceId);
          });
      }
    },
    handleGetLogResourceDetail(data, resourceId) {
      data.listObject.forEach(log => {
        if (this.logContent) {
          this.logContent.push(log);
        }
      });
      this.page++;
      this.loading = false;
    },
    changeInstance(instance) {
      this.currentInstance = instance;
      this.loading = false;
      this.page = 1;
      this.logContent = [];
      this.load(instance);
    },
    downloadLogFile(resourceId) {
      this.result = downloadLogFile(this.id, resourceId, this.isShare)
        .then(response => {
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
  watch: {
    '$route'(to) {
      if (to.name === "perReportView") {
        this.id = to.path.split('/')[4];
        this.getResource();
      }
    },
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Completed" || status === "Running") {
          this.getResource();
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
  height: calc(100vh - 205px);
  padding: 0;
  margin: 0;
  list-style: none;
  overflow: auto
}

.infinite-list-item {
  overflow: hidden;
}

</style>
