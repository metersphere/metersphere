<template>
  <div>
    <el-tabs @tab-click="selectTab" v-model="active">
      <el-tab-pane v-for="item in resource"
                   :key="item.resourceId"
                   :label="item.resourceName"
                   v-loading="result.loading"
                   class="logging-content">
        <ul class="infinite-list" v-infinite-scroll="load(item.resourceId)">
          <li class="infinite-list-item" v-for="(log, index) in logContent[item.resourceId]"
              :key="item.resourceId+index">
            {{ log.content }}
          </li>
        </ul>
        <el-link type="primary" @click="downloadLogFile(item)">{{ $t('load_test.download_log_file') }}</el-link>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>

export default {
  name: "LogDetails",
  data() {
    return {
      active: '0',
      resource: [],
      logContent: {},
      result: {},
      id: '',
      page: {},
      pageCount: 5,
      loading: false,
      init: false,
      logStatus: {}
    };
  },
  methods: {
    getResource() {
      this.init = true;
      this.active = '0';
      this.result = this.$get("/performance/report/log/resource/" + this.id, data => {
        this.resource = data.data;
        if (!this.resource || this.resource.length === 0) {
          this.init = false;
        }
        this.page = data.data.map(item => item.resourceId).reduce((result, curr) => {
          result[curr] = 1;
          return result;
        }, {});
        this.logContent = data.data.map(item => item.resourceId).reduce((result, curr) => {
          result[curr] = [];
          return result;
        }, {});
      });
    },
    load(resourceId) {
      if (this.loading || this.page[resourceId] > this.pageCount) {
        return;
      }
      this.logStatus[resourceId] = true;
      this.loading = true;
      let url = "/performance/report/log/" + this.id + "/" + resourceId + "/" + this.page[resourceId];
      this.$get(url, res => {
        let data = res.data;
        data.listObject.forEach(log => {
          this.logContent[resourceId].push(log);
        });
        this.page[resourceId]++;
        this.loading = false;
      });
    },
    selectTab(tab) {
      let resourceId = tab.$vnode.key;
      if (this.logStatus[resourceId]) {
        return;
      }
      this.loading = false;
      this.page[resourceId] = 1;
      this.logContent[resourceId] = [];
      this.load(resourceId);
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
        if (!val.status || !val.id || !this.id) {
          return;
        }
        if (this.init) {
          return;
        }
        let status = val.status;
        if (status === "Completed" || status === "Running") {
          this.getResource();
        } else {
          this.resource = [];
        }
      },
      deep: true
    }
  },
  props: ['report']
};
</script>

<style scoped>
.logging-content {
  white-space: pre-line;
  overflow: auto;
}

.infinite-list {
  height: calc(100vh - 390px);
  padding: 0;
  margin: 0;
  list-style: none;
  overflow: auto
}

.infinite-list-item {
  overflow: hidden;
}

</style>
