<template>
  <test-plan-report-container
    id="overview"
    :title="$t('test_track.report.overview')"
  >
    <el-form class="form-info" v-loading="loading">
      <el-form-item :label="$t('test_track.report.testing_time') + ':'">
        {{ showTime }}
      </el-form-item>

      <el-row justify="space-between" class="select-time">
        <el-col :span="8" v-if="runMode">
          <el-form-item :label="$t('report.run_model') + ':'">
            {{ runMode }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('load_test.select_resource_pool') + ':'">
            {{ resourcePool }}
          </el-form-item>
        </el-col>
        <el-col :span="8"></el-col>
      </el-row>
      <el-row
        class="select-time"
        v-if="report.envGroupName || report.projectEnvMap"
        style="display: inline-block"
      >
        <div>
          <div style="float: left">
            {{ $t("commons.environment") + ":" }}
          </div>
          <div style="float: right">
            <div v-if="report.envGroupName" style="margin-left: 42px">
              <ms-tag
                type="danger"
                :content="$t('workspace.env_group.name')"
              ></ms-tag>
              {{ report.envGroupName }}
            </div>
            <div v-else-if="report.projectEnvMap" style="margin-left: 42px">
              <div
                v-for="(values, key) in report.projectEnvMap"
                :key="key"
                style="margin-right: 10px"
              >
                {{ key + ":" }}
                <ms-tag
                  v-for="(item, index) in values"
                  :key="index"
                  type="success"
                  :content="item"
                  style="margin-left: 2px"
                />
              </div>
            </div>
          </div>
        </div>
      </el-row>
      <el-row type="flex" justify="space-between" class="select-time">
        <el-col :span="8">
          <el-form-item
            :label="$t('test_track.report.total_number_tests') + ':'"
          >
            {{ report.caseCount }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item
            :label="$t('test_track.report.executive_finish_rate') + ':'"
          >
            {{
              (report.executeRate ? (report.executeRate * 100).toFixed(2) : 0) +
              "%"
            }}
            <ms-instructions-icon
              :content="$t('test_track.report.exacutive_rate_tip')"
            />
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('test_track.report.passing_rate') + ':'">
            {{
              (report.passRate ? (report.passRate * 100).toFixed(2) : 0) + "%"
            }}
            <ms-instructions-icon
              :content="$t('test_track.report.passing_rate_tip')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import TestPlanReportContainer from "@/business/plan/view/comonents/report/detail/TestPlanReportContainer";
import MsInstructionsIcon from "metersphere-frontend/src/components/MsInstructionsIcon";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {datetimeFormat} from "fit2cloud-ui/src/filters/time";

export default {
  name: "TestPlanOverviewReport",
  components: {
    MsInstructionsIcon,
    TestPlanReportContainer,
    MsFormDivider,
    MsTag,
  },
  props: {
    report: Object,
    runMode: String,
    resourcePool: String,
  },
  data() {
    return {
      loading: false,
      isEdit: false,
    };
  },
  computed: {
    showTime() {
      let startTime = "";
      let endTime = "";
      if (this.report.startTime) {
        startTime = datetimeFormat(this.report.startTime, false);
      }
      if (this.report.endTime) {
        endTime = datetimeFormat(this.report.endTime, false);
      }
      return startTime + " ~ " + endTime;
    },
  },
};
</script>

<style scoped>
.form-info {
  padding: 20px;
}

.el-form-item:first-child {
  margin-bottom: 0;
  height: 40px;
}
</style>
