<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true">
      <el-tab-pane v-for="item in logContent" :key="item.id" :label="item.resourceName" class="logging-content">
        {{item.content}}...
        <el-link type="primary" @click="downloadLogFile(item)">{{$t('load_test.download_log_file')}}</el-link>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  export default {
    name: "LogDetails",
    props: ['id', 'status'],
    data() {
      return {
        logContent: null,
        result: {},
      }
    },
    methods: {
      initTableData() {
        this.result = this.$get("/performance/report/log/" + this.id, res => {
          this.logContent = res.data;
        })
      },
      downloadLogFile(item) {
        let config = {
          url: '/performance/report/log/download/' + item.id,
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
      status() {
        if ("Completed" === this.status) {
          this.initTableData()
        }
      }
    },
  }
</script>

<style scoped>
  .logging-content {
    white-space: pre-line;
    overflow: auto;
  }

</style>
