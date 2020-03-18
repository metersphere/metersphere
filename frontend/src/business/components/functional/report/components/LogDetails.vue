<template>
  <div>
    <div>
      <el-tabs type="border-card">
        <el-tab-pane label="seleniumLog">
          <el-input v-model="seleniumLog" rows="15" type="textarea"></el-input>
        </el-tab-pane>
        <el-tab-pane label="browserDriverLog">
          <el-input v-model="browserDriverLog" rows="15" type="textarea"></el-input>
        </el-tab-pane>
      </el-tabs>
    </div>
    <div>

    </div>
  </div>
</template>

<script>
  export default {
    name: "LogDetails",
    data() {
      return {
        seleniumLog: '',
        browserDriverLog: ''
      }
    },
    props: {
      reportId: {
        type: String
      }
    },
    mounted() {
      this.getLogDetails(this.reportId);
    },
    watch: {
      reportId: function (newVal) {
        this.getLogDetails(newVal);
      }
    },
    methods: {
      getLogDetails(reportId) {
        if(reportId){
          let url = '/functional/report/test/log/' + reportId;
          this.$get(url, (response) => {
            this.seleniumLog = response.data.seleniumLog;
            this.browserDriverLog = response.data.browserDriverLog;
          });
        }
      }
    }
  }
</script>

<style scoped>

</style>
