<template>
  <div v-loading="result.loading" class="report-view-container">
    <div class="main-content">
      <el-card>
        <el-row>
          <el-col :span="16">
            <el-row>
              <el-breadcrumb separator-class="el-icon-arrow-right">
                <el-breadcrumb-item :to="{ path: '/' }">{{projectName}}</el-breadcrumb-item>
                <el-breadcrumb-item>{{testName}}</el-breadcrumb-item>
                <el-breadcrumb-item>{{reportName}}</el-breadcrumb-item>
              </el-breadcrumb>
            </el-row>
            <el-row class="ms-report-view-btns">
              <el-button type="primary" plain size="mini">立即停止</el-button>
              <el-button type="success" plain size="mini">再次执行</el-button>
              <el-button type="info" plain size="mini">导出</el-button>
              <el-button type="warning" plain size="mini">比较</el-button>
            </el-row>
          </el-col>
          <el-col :span="8">
            <span class="ms-report-time-desc">
              持续时间：  30 分钟
            </span>
            <span class="ms-report-time-desc">
              开始时间：  2020-3-10 12:00:00
            </span>
            <span class="ms-report-time-desc">
              结束时间：  2020-3-10 12:30:00
            </span>
          </el-col>
        </el-row>

        <el-divider></el-divider>

        <el-tabs v-model="active" type="border-card" :stretch="true">

          <el-tab-pane :label="$t('report.test_log_details')">
            <ms-report-log-details />
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </div>
  </div>
</template>

<script>
  import MsReportLogDetails from './components/LogDetails';

  export default {
    name: "FunctionalReportView",
    components: {
      MsReportLogDetails
    },
    data() {
      return {
        result: {},
        active: '0',
        reportId: '',
        reportName: '',
        testName: '',
        projectName: ''
      }
    },
    methods: {
      initBreadcrumb() {
        if(this.reportId){
          this.result = this.$get("/functional/report/test/pro/info/" + this.reportId, res => {
            let data = res.data;
            if(data){
              this.reportName = data.name;
              this.testName = data.testName;
              this.projectName = data.projectName;
            }
          })
        }
      }
    },
    created() {
      this.reportId = this.$route.path.split('/')[4];
      this.initBreadcrumb();
    },
    watch: {
      '$route'(to) {
        let reportId = to.path.split('/')[4];
        if(reportId){
          this.$get("/functional/report/test/pro/info/" + reportId, response => {
            let data = response.data;
            if(data){
              this.reportName = data.name;
              this.testName = data.testName;
              this.projectName = data.projectName;
            }
          });
        }
      }
    }
  }
</script>

<style scoped>
  .report-view-container {
    float: none;
    text-align: center;
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .report-view-container .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .ms-report-view-btns {
    margin-top: 15px;
  }

  .ms-report-time-desc {
    text-align: left;
    display: block;
    color: #5C7878;
  }
</style>
