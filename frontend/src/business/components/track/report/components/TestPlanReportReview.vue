<template>
    <el-drawer
      :visible.sync="showDialog"
      :with-header="false"
      :modal-append-to-body="false"
      size="100%"
      ref="drawer"
      v-loading="result.loading">
      <template v-slot:default="scope">
        <el-row type="flex" class="head-bar">
          <el-col :span="12">
            <div class="name-edit">
              <el-button plain size="mini" icon="el-icon-back" @click="handleClose">{{$t('test_track.return')}}
              </el-button>&nbsp;
              <span class="title">{{plan.name}}</span>
            </div>
          </el-col>
          <el-col :span="12" class="head-right">
<!--            <el-button v-permission="['PROJECT_TRACK_REPORT:READ+EXPORT']" :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExport(report.name)">-->
<!--              {{$t('test_track.plan_view.export_report')}}-->
<!--            </el-button>-->
          </el-col>
        </el-row>
        <el-scrollbar>
          <div class="container">
            <test-plan-report-content :plan-id="plan.id"/>
          </div>
        </el-scrollbar>
      </template>
    </el-drawer>
</template>

<script>

import TestPlanReportContent from "@/business/components/track/plan/view/comonents/report/detail/TestPlanReportContent";
export default {
  name: "TestPlanReportReview",
  components: {
    TestPlanReportContent
  },
  data() {
    return {
      result: {},
      showDialog: false,
      plan: {},
      isTestManagerOrTestUser: false
    }
  },
  mounted() {
    this.isTestManagerOrTestUser = true;
  },
  methods: {
    // listenGoBack() {
    //   //监听浏览器返回操作，关闭该对话框
    //   if (window.history && window.history.pushState) {
    //     history.pushState(null, null, document.URL);
    //     window.addEventListener('popstate', this.goBack, false);
    //   }
    // },
    // goBack() {
    //   this.handleClose();
    // },
    open(plan) {
      // 每次都重新获取
      this.plan = {id: null}
      this.$nextTick(() => {
        this.plan = plan;
      });
      this.showDialog = true;
      // this.listenGoBack();
    },
    handleClose() {
      // window.removeEventListener('popstate', this.goBack, false);
      this.$emit('refresh');
      this.showDialog = false;
    },
  }
}
</script>

<style scoped>

.head-bar {
  background: white;
  height: 45px;
  line-height: 45px;
  padding: 0 10px;
  border: 1px solid #EBEEF5;
  box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
}

.container {
  height: 100vh;
  background: #F5F5F5;
}

.head-right {
  text-align: right;
}

</style>
