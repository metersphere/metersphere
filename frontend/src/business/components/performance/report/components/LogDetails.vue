<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true">
      <el-tab-pane v-for="item in logContent" :key="item.resourceId" :label="item.resourceName" class="logging-content">
        {{item.content}}...
        <el-link type="primary" @click="downloadLogFile(item)">{{$t('load_test.download_log_file')}}</el-link>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  export default {
    name: "LogDetails",
    data() {
      return {
        logContent: null,
        result: {},
        id: ''
      }
    },
    methods: {
      initTableData() {
        this.result = this.$get("/performance/report/log/" + this.id).then(res => {
          this.logContent = res.data.data;
        }).catch(() => {
          this.logContent = null;
        })
      },
      downloadLogFile(item) {
        let config = {
          url: '/performance/report/log/download/' + this.id + '/' + item.resourceId,
          method: 'get',
          responseType: 'blob'
        };
        this.result = this.$request(config).then(response => {
          const filename = 'jmeter.log'
          const blob = new Blob([response.data]);
          if ("download" in document.createElement("a")) {
            // 非IE下载
            //  chrome/firefox
            let aTag = document.createElement('a');
            aTag.download = filename;
            aTag.href = URL.createObjectURL(blob);
            aTag.click();
            URL.revokeObjectURL(aTag.href)
          } else {
            // IE10+下载
            navigator.msSaveBlob(blob, filename);
          }
        });
      }
    },
    watch: {
      report: {
        handler(val){
          let status = val.status;
          this.id = val.id;
          if (status === "Completed") {
            this.initTableData();
          } else {
            this.logContent = null;
          }
        },
        deep:true
      }
    },
    props: ['report']
  }
</script>

<style scoped>
  .logging-content {
    white-space: pre-line;
    overflow: auto;
  }

</style>
