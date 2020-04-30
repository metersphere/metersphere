<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true">
      <el-tab-pane v-for="(item, key) in logContent" :key="key" :label="key" class="logging-content">
        {{item}}
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
    height: calc(100vh - 400px);
  }

</style>
