<template>
  <div v-loading="result ? result.loading : false" class="head-bar head-right">
    <el-row>
      <el-popover
        placement="right"
        width="300">
        <p>{{shareUrl}}</p>
        <div style="text-align: right; margin: 0">
          <el-button type="primary" size="mini" :disabled="!shareUrl"
                     v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}</el-button>
        </div>
        <el-button icon="el-icon-share" slot="reference" :disabled="!isTestManagerOrTestUser"
                   plain size="mini" @click="handleShare()">
          {{'分享'}}
        </el-button>
      </el-popover>
    </el-row>
    <el-row>
      <el-button icon="el-icon-receiving" v-if="!isDb" :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleSave()">
        {{'保存'}}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-download" :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExportHtml()">
        {{'导出'}}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-setting" v-if="!isDb"  :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleEditTemplate()">
        {{'配置'}}
      </el-button>
    </el-row>
    <test-plan-report-edit :plan-id="planId" :config.sync="report.config" ref="reportEdit"/>
  </div>
</template>

<script>

import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import {generateShareInfo} from "@/network/share";
import TestPlanReportEdit
  from "@/business/components/track/plan/view/comonents/report/detail/component/TestPlanReportEdit";
export default {
  name: "TestPlanReportButtons",
  components: {
    TestPlanReportEdit,
    TestPlanApiReport,
    },
  props: {
    planId:String,
    isShare: Boolean,
    report: Object,
    isDb: Boolean
  },
  data() {
    return {
      result: {},
      isTestManagerOrTestUser: true,
      shareUrl: '',
    };
  },
  methods: {
    handleEditTemplate() {
      this.$refs.reportEdit.open();
    },
    handleShare(){
      let pram = {};
      pram.customData = this.planId;
      pram.shareType = 'PLAN_REPORT';
      if (this.isDb) {
        pram.customData = this.report.id;
        pram.shareType = 'PLAN_DB_REPORT';
      }
      generateShareInfo(pram, (data) => {
        let thisHost = window.location.host;
        this.shareUrl = thisHost + "/sharePlanReport" + data.shareUrl;
      });
    },
    handleSave() {
      let param = {};
      this.buildParam(param);
      this.$get('/test/plan/report/saveTestPlanReport/'+this.planId+'/MANUAL', () => {
        this.result = this.$post('/case/report/edit', param, () => {
          this.$success(this.$t('commons.save_success'));
        });
      });
    },
    buildParam(param) {
      param.name = this.report.name;
      param.id = this.report.id;
      param.isNew = true;
    },
    handleExportHtml() {
      let config = {
        url: '/test/plan/report/export/' + this.planId,
        method: 'get',
        responseType: 'blob'
      };
      if (this.isDb) {
        config.url = '/test/plan/report/db/export/' + this.report.id;
      }
      if (this.isShare) {
        config.url = '/share' + config.url;
      }
      this.result = this.$download(config, this.report.name + '.html');
    }
  }
}
</script>

<style scoped>

.head-right {
  text-align: right;
  float: right;
}

.head-bar .el-button {
  position: relative;
  z-index: 99;
  margin-bottom: 10px;
  width: 80px;
  margin-right: 10px;
  display: block;
}

.el-button+.el-button {
  margin-left: 0px;
}

/*.head-bar {*/
/*  position: fixed;*/
/*  right: 10px;*/
/*  padding: 20px;*/
/*}*/

</style>
