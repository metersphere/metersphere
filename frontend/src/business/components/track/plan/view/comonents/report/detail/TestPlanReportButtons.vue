<template>
  <div v-loading="result ? result.loading : false" class="head-bar head-right">
    <el-row>
      <el-popover
        placement="right"
        width="300">
        <p>{{shareUrl}}</p>
        <span style="color: red;float: left;margin-left: 10px;">{{ $t('test_track.report.valid_for_24_hours') }}</span>
        <div style="text-align: right; margin: 0">
          <el-button type="primary" size="mini" :disabled="!shareUrl"
                     v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}</el-button>
        </div>
        <el-button icon="el-icon-share" slot="reference" :disabled="!isTestManagerOrTestUser"
                   plain size="mini" @click="handleShare()">
          {{ $t('test_track.report.share') }}
        </el-button>
      </el-popover>
    </el-row>
    <el-row>
      <el-button icon="el-icon-receiving" v-if="!isDb" :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleSave()">
        {{ $t('commons.save')}}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-download" :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleExportHtml()">
        {{ $t('commons.export')}}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-setting" v-if="!isDb"  :disabled="!isTestManagerOrTestUser" plain size="mini" @click="handleEditTemplate()">
        {{ $t('test_track.report.configuration') }}
      </el-button>
    </el-row>
    <test-plan-report-edit :plan-id="planId" :config.sync="report.config" ref="reportEdit"/>
  </div>
</template>

<script>

import TestPlanApiReport from "@/business/components/track/plan/view/comonents/report/detail/TestPlanApiReport";
import {generateShareInfoWithExpired} from "@/network/share";
import TestPlanReportEdit
  from "@/business/components/track/plan/view/comonents/report/detail/component/TestPlanReportEdit";
import {editPlanReport, saveTestPlanReport} from "@/network/test-plan";
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
      generateShareInfoWithExpired(pram, (data) => {
        let thisHost = window.location.host;
        this.shareUrl = thisHost + "/sharePlanReport" + data.shareUrl;
      });
    },
    handleSave() {
      let param = {};
      this.buildParam(param);
      editPlanReport({id: this.planId, reportSummary: this.report.summary ? this.report.summary : ''}, () => {
        saveTestPlanReport(this.planId, () => {
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
