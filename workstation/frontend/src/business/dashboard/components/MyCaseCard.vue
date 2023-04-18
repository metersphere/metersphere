<template>
  <el-card
    shadow="never"
    body-style="margin-top: 24px; padding: 0;border:none;"
    class="table-card"
  >
    <el-row :gutter="10">
      <el-col :span="24">
        <span class="top-css">{{ $t("workstation.creation_case") }}</span>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col v-if="showCount">
        <ms-border-pie-chart
          :pie-data="loadCharData"
          :autoresize="true"
          :text="totalCount.toString()"
          :text-title="$t('workstation.case_count')"
          :subtext="subtextStr"
          :radius="['70%', '96%']"
          :height="255"
        />
      </el-col>
      <el-col v-else>
        <img
          style="
            height: 100px;
            width: 100px;
            padding-top: 10%;
            padding-left: 40%;
          "
          src="/assets/module/figma/icon_none.svg"
        />
        <p class="right-other-css" v-permission="['PROJECT_TRACK_CASE:READ']">
          {{ $t("workstation.creation_case_tip") }} &nbsp;&nbsp;<span
            style="color: var(--primary_color)"
            @click="toCreatCase()"
            >{{ $t("permission.project_track_case.create") }}</span
          >
        </p>
      </el-col>
    </el-row>
  </el-card>
</template>
<script>
import MsBorderPieChart from "metersphere-frontend/src/components/MsBorderPieChart";
import { getMyCreatedCaseGroupContMap } from "@/api/workstation";

export default {
  name: "MyCaseCard",
  components: { MsBorderPieChart },
  data() {
    return {
      showCount: false,
      totalCount: 0,
      weekTotalCount: 0,
      subtextStr: "",
      loadCharData: [],
    };
  },
  methods: {
    getCaseCount() {
      let isWeek = false;
      this.result = getMyCreatedCaseGroupContMap(isWeek).then((response) => {
        let tableData = response.data;
        const testCaseCount = {
          value: tableData.testCaseCount === 0 ? "-" : tableData.testCaseCount,
          name: this.$t("workstation.table_name.track_case"),
        };
        this.loadCharData.push(testCaseCount);

        const apiTestCaseCount = {
          value:
            tableData.apiTestCaseCount === 0 ? "-" : tableData.apiTestCaseCount,
          name: this.$t("workstation.table_name.api_case"),
        };
        this.loadCharData.push(apiTestCaseCount);

        const apiScenarioCaseCount = {
          value:
            tableData.apiScenarioCaseCount === 0
              ? "-"
              : tableData.apiScenarioCaseCount,
          name: this.$t("workstation.table_name.scenario_case"),
        };
        this.loadCharData.push(apiScenarioCaseCount);

        const loadTestCount = {
          value: tableData.loadTestCount === 0 ? "-" : tableData.loadTestCount,
          name: this.$t("test_track.plan.load_case.case"),
        };
        this.loadCharData.push(loadTestCount);

        this.totalCount =
          tableData.testCaseCount +
          tableData.apiTestCaseCount +
          tableData.apiScenarioCaseCount +
          tableData.loadTestCount;
        if (this.totalCount > 0) {
          this.getCaseWeekCount();
        }
      });
    },
    toCreatCase() {
      let caseData = this.$router.resolve({
        path: "/track/case/create",
      });
      window.open(caseData.href, "_blank");
    },
    getCaseWeekCount() {
      let isWeek = true;
      getMyCreatedCaseGroupContMap(isWeek).then((response) => {
        let tableData = response.data;
        this.weekTotalCount =
          tableData.testCaseCount +
          tableData.apiTestCaseCount +
          tableData.apiScenarioCaseCount +
          tableData.loadTestCount;
        if (this.weekTotalCount > 0) {
          this.subtextStr = "本周：+" + this.weekTotalCount + " >";
        } else {
          this.subtextStr = "本周：+0 >";
        }
        this.showCount = true;
      });
    },
  },
  created() {
    this.getCaseCount();
  },
};
</script>
<style scoped>
.table-card {
  height: 100%;
}

.right-css {
  text-align: right;
  margin-top: 100px;
}

.right-two-css {
  font-weight: 650;
  color: #783987;
  font-size: 21px;
}

.right-one-css {
  font-weight: 700;
  font-size: 43px;
  color: #783987;
}

.top-css {
  font-weight: 650;
  font-style: normal;
  font-size: 18px;
  align-self: flex-start;
  padding: 0px 0px 0px 0px;
  box-sizing: border-box;
  width: 100%;
  color: #1f2329;
  margin-left: 24px;
  line-height: 26px;
}

.right-other-css {
  color: #969393;
  cursor: pointer;
  padding-left: 34%;
}

.table-card {
  border: none;
  color: rgba(192, 196, 204, 0.98);
}
</style>
