<template>
  <div v-loading="loading" class="head-bar head-right">
    <el-row>
      <el-popover
        placement="right"
        width="300">
        <p>{{ shareUrl }}</p>
        <span style="color: red;float: left;margin-left: 10px;"
              v-if="application.typeValue">{{ $t('commons.validity_period') + application.typeValue }}</span>
        <div style="text-align: right; margin: 0">
          <el-button type="primary" size="mini" :disabled="!shareUrl"
                     v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}
          </el-button>
        </div>
        <el-button icon="el-icon-share" slot="reference" :disabled="!isTestManagerOrTestUser"
                   plain size="mini" @click="handleShare()">
          {{ $t('test_track.report.share') }}
        </el-button>
      </el-popover>
    </el-row>
    <el-row>
      <el-button icon="el-icon-receiving" v-if="!isDb" :disabled="!isTestManagerOrTestUser" plain size="mini"
                 @click="handleSave()">
        {{ $t('commons.save') }}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-download" :disabled="!isTestManagerOrTestUser"
                 v-permission="['PROJECT_TRACK_REPORT:READ+EXPORT']" plain size="mini" @click="handleExportHtml()">
        {{ $t('commons.export') }}
      </el-button>
    </el-row>
    <el-row>
      <el-button icon="el-icon-setting" v-if="!isDb" :disabled="!isTestManagerOrTestUser" plain size="mini"
                 @click="handleEditTemplate()">
        {{ $t('test_track.report.configuration') }}
      </el-button>
    </el-row>
    <test-plan-report-edit :plan-id="planId" :config.sync="report.config" ref="reportEdit"/>
  </div>
</template>

<script>

import TestPlanApiReport from "@/business/plan/view/comonents/report/detail/TestPlanApiReport";
import {generateShareInfoWithExpired, getShareRedirectUrl} from "@/api/share";
import TestPlanReportEdit from "@/business/plan/view/comonents/report/detail/component/TestPlanReportEdit";
import {editPlanReport, saveTestPlanReport} from "@/api/remote/plan/test-plan";
import {CURRENT_LANGUAGE, getCurrentUser} from "@/business/utils/sdk-utils";
import {getProjectApplicationConfig} from "@/api/project-application";
export default {
  name: "TestPlanReportButtons",
  components: {
    TestPlanReportEdit,
    TestPlanApiReport,
  },
  props: {
    planId: String,
    isShare: Boolean,
    report: Object,
    isDb: Boolean
  },
  data() {
    return {
      loading: false,
      isTestManagerOrTestUser: true,
      shareUrl: '',
      application: {},
    };
  },
  methods: {
    handleEditTemplate() {
      this.$refs.reportEdit.open();
    },
    handleShare() {
      let param = {};
      param.customData = this.planId;
      param.shareType = 'PLAN_REPORT';
      if (this.isDb) {
        param.customData = this.report.id;
        param.shareType = 'PLAN_DB_REPORT';
      }
      param.lang = localStorage.getItem(CURRENT_LANGUAGE);
      generateShareInfoWithExpired(param)
        .then((response) => {
          this.shareUrl = getShareRedirectUrl(response.data);
        });
      this.getProjectApplication();
    },
    getProjectApplication() {
      getProjectApplicationConfig('TRACK_SHARE_REPORT_TIME')
        .then(res => {
          if (res.data) {
            let quantity = res.data.typeValue ? res.data.typeValue.substring(0, res.data.typeValue.length - 1) : null;
            let unit = res.data.typeValue ? res.data.typeValue.substring(res.data.typeValue.length - 1) : null;
            if (unit === 'H') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.hour');
            } else if (unit === 'D') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.day');
            } else if (unit === 'M') {
              res.data.typeValue = quantity + this.$t('commons.workspace_unit') + this.$t('commons.date_unit.month');
            } else if (unit === 'Y') {
              res.data.typeValue = quantity + this.$t('commons.date_unit.year');
            }
            if (quantity == null && unit == null) {
              res.data.typeValue = '24' + this.$t('commons.date_unit.hour');
            }
            this.application = res.data;
          }
        });
    },
    handleSave() {
      let param = {};
      this.buildParam(param);
      editPlanReport({id: this.planId, reportSummary: this.report.summary ? this.report.summary : ''})
        .then(() => {
          saveTestPlanReport(this.planId)
            .then(() => {
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
      let url = '/test/plan/report/export/' + this.planId;
      if (this.isDb) {
        url = '/test/plan/report/db/export/' + this.report.id;
      }
      if (this.isShare) {
        url = '/share' + url;
      }
      let lang = 'zh_CN';
      let user = getCurrentUser();
      if (user && user.language) {
        lang = user.language;
      }
      url = url + '/' + lang;
      this.loading = true;
      this.$fileDownload(url, this.report.name + '.html')
        .then(() => {
          this.loading = false;
          this.$success(this.$t("organization.integration.successful_operation"));
        });
    },
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

.el-button + .el-button {
  margin-left: 0px;
}
</style>
