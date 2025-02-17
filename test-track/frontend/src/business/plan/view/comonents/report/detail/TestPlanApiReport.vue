<template>
  <test-plan-report-container
    id="api"
    :title="$t('test_track.report.analysis_api')"
  >
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane
        v-if="resultEnable"
        :label="$t('test_track.report.test_result')"
        name="first"
      >
        <api-result :api-result="report.apiResult" />
      </el-tab-pane>
      <el-tab-pane v-if="failureEnable" name="second">
        <template v-slot:label>
          <tab-pane-count
            :title="$t('test_track.report.fail_case')"
            :count="failureSize"
          />
        </template>
        <api-cases
          :is-db="isDb"
          :share-id="shareId"
          :is-share="isShare"
          :report="report"
          :is-template="isTemplate"
          :plan-id="planId"
          @setSize="setFailureSize"
        />
        <el-button
          class="rerun-button"
          plain
          size="mini"
          v-if="
            !isTemplate &&
            showRerunBtn &&
            (failureSize > 0 || unExecuteSize > 0) &&
            isRerun
          "
          @click="rerun"
        >
          {{ $t("api_test.automation.rerun") }}
        </el-button>
      </el-tab-pane>
      <el-tab-pane
        style="min-height: 500px"
        name="third"
        v-if="errorReportEnable"
      >
        <template v-slot:label>
          <tab-pane-count
            :title="$t('error_report_library.option.name')"
            :count="errorReportSize"
          />
        </template>
        <api-cases
          :is-db="isDb"
          :is-error-report="true"
          :share-id="shareId"
          :is-share="isShare"
          :report="report"
          :is-template="isTemplate"
          :plan-id="planId"
          @setSize="setErrorReportSize"
        />
        <el-button
          class="rerun-button"
          plain
          size="mini"
          v-if="
            !isTemplate &&
            showRerunBtn &&
            (failureSize > 0 || unExecuteSize > 0) &&
            isRerun
          "
          @click="rerun"
        >
          {{ $t("api_test.automation.rerun") }}
        </el-button>
      </el-tab-pane>
      <el-tab-pane
        style="min-height: 500px"
        name="fourth"
        v-if="unExecuteEnable"
      >
        <template v-slot:label>
          <tab-pane-count
            :title="$t('api_test.home_page.detail_card.unexecute')"
            :count="unExecuteSize"
          />
        </template>
        <api-cases
          :is-db="isDb"
          :is-un-execute="true"
          :share-id="shareId"
          :is-share="isShare"
          :report="report"
          :is-template="isTemplate"
          :plan-id="planId"
          @setSize="setUnExecuteSize"
        />

        <el-button
          class="rerun-button"
          plain
          size="mini"
          v-if="
            !isTemplate &&
            showRerunBtn &&
            (failureSize > 0 || unExecuteSize > 0) &&
            isRerun
          "
          @click="rerun"
        >
          {{ $t("api_test.automation.rerun") }}
        </el-button>
      </el-tab-pane>

      <el-tab-pane style="min-height: 500px" name="fifth" v-if="allEnable">
        <template v-slot:label>
          <tab-pane-count
            :title="$t('test_track.report.all_case')"
            :count="allSize"
          />
        </template>
        <api-cases
          :is-db="isDb"
          :is-all="true"
          :share-id="shareId"
          :is-share="isShare"
          :report="report"
          :is-template="isTemplate"
          :plan-id="planId"
          @setSize="setAllSize"
        />
        <el-button
          class="rerun-button"
          plain
          size="mini"
          v-if="
            !isTemplate &&
            showRerunBtn &&
            (failureSize > 0 || unExecuteSize > 0) &&
            isRerun
          "
          @click="rerun"
        >
          {{ $t("api_test.automation.rerun") }}
        </el-button>
      </el-tab-pane>
    </el-tabs>
  </test-plan-report-container>
</template>

<script>
import MsFormDivider from "metersphere-frontend/src/components/MsFormDivider";
import ApiResult from "@/business/plan/view/comonents/report/detail/component/ApiResult";
import TestPlanReportContainer from "@/business/plan/view/comonents/report/detail/TestPlanReportContainer";
import ApiCases from "@/business/plan/view/comonents/report/detail/component/ApiCases";
import TabPaneCount from "@/business/plan/view/comonents/report/detail/component/TabPaneCount";
import { apiTestExecRerun } from "@/api/remote/ui/api-test";

export default {
  name: "TestPlanApiReport",
  components: {
    TabPaneCount,
    ApiCases,
    TestPlanReportContainer,
    ApiResult,
    MsFormDivider,
  },
  data() {
    return {
      activeName: "first",
      failureSize: 0,
      errorReportSize: 0,
      unExecuteSize: 0,
      allSize: 0,
      showRerunBtn: true,
    };
  },
  created() {
    this.showRerunBtn = !this.isShare;
  },
  props: ["report", "planId", "isTemplate", "isShare", "shareId", "isDb"],
  computed: {
    resultEnable() {
      let disable =
        this.report.config &&
        this.report.config.api.children.result.enable === false;
      return (
        !disable &&
        this.report.apiResult &&
        ((this.report.apiResult.apiCaseData &&
          this.report.apiResult.apiCaseData.length > 0) ||
          (this.report.apiResult.apiScenarioData &&
            this.report.apiResult.apiScenarioData.length > 0))
      );
    },
    failureEnable() {
      let disable =
        this.report.config &&
        this.report.config.api.children.failure.enable === false;
      return !disable;
    },
    errorReportEnable() {
      let disable =
        this.report.config &&
        this.report.config.api.children.errorReport &&
        this.report.config.api.children.errorReport.enable === false;
      return !disable;
    },
    unExecuteEnable() {
      let disable =
        this.report.config &&
        this.report.config.api.children.unExecute &&
        this.report.config.api.children.unExecute.enable === false;
      return !disable;
    },
    allEnable() {
      let disable =
        this.report.config &&
        this.report.config.api.children.all.enable === false;
      return !disable;
    },
    isRerun() {
      const isReportNotRunning =
        this.report && this.report.status !== "RUNNING";
      return (
        isReportNotRunning &&
        (this.report.apiFailureCases ||
          this.report.unExecuteCases ||
          this.report.scenarioFailureCases ||
          this.report.unExecuteScenarios ||
          this.report.loadFailureCases)
      );
    },
  },
  watch: {
    resultEnable() {
      this.initActiveName();
    },
    failureEnable() {
      this.initActiveName();
    },
    errorReportEnable() {
      this.initActiveName();
    },
    allEnable() {
      this.initActiveName();
    },
  },
  mounted() {
    this.initActiveName();
  },
  methods: {
    initActiveName() {
      if (this.resultEnable) {
        this.activeName = "first";
      } else if (this.failureEnable) {
        this.activeName = "second";
      } else if (this.errorReportEnable) {
        this.activeName = "third";
      } else if (this.allEnable) {
        this.activeName = "fourth";
      }
    },
    setFailureSize(size) {
      this.failureSize = size;
    },
    setErrorReportSize(size) {
      this.errorReportSize = size;
    },
    setUnExecuteSize(size) {
      this.unExecuteSize = size;
    },
    setAllSize(size) {
      this.allSize = size;
    },
    handleClick(tab, event) {},
    rerun() {
      let type = "TEST_PLAN";
      let scenarios = [];
      let cases = [];
      let performanceCases = [];
      let rerunObj = {
        type: type,
        reportId: this.report.id,
        scenarios: scenarios,
        cases: cases,
        performanceCases: performanceCases,
      };
      // 获取需要重跑的用例
      if (this.report && this.report.apiFailureCases) {
        this.format(cases, this.report.apiFailureCases);
      }
      if (this.report && this.report.unExecuteCases) {
        this.format(cases, this.report.unExecuteCases);
      }
      // 获取需要重跑的场景
      if (this.report && this.report.scenarioFailureCases) {
        this.format(scenarios, this.report.scenarioFailureCases);
      }
      if (this.report && this.report.unExecuteScenarios) {
        this.format(scenarios, this.report.unExecuteScenarios);
      }
      // 获取需要重跑的性能用例
      if (this.report && this.report.loadFailureCases) {
        this.format(performanceCases, this.report.loadFailureCases);
      }
      apiTestExecRerun(rerunObj).then((res) => {
        if (res.data !== "SUCCESS") {
          this.$error(res.data);
        } else {
          this.$success(this.$t("api_test.automation.rerun_success"));
        }
      });
    },
    format(cases, data) {
      if (this.report && data) {
        data.forEach((item) => {
          if (item) {
            let obj = {
              id: item.id,
              reportId: item.reportId,
              userId: item.createUser,
            };
            cases.push(obj);
          }
        });
      }
    },
  },
};
</script>

<style scoped>
.rerun-button {
  position: absolute;
  top: 10px;
  right: 10px;
  margin-right: 10px;
  z-index: 1100;
  background-color: #f2f9ef;
  color: #87c45d;
}
</style>
