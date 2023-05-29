<template>
  <div style="background-color: #f5f6f7; overflow: auto">
    <ms-container>
      <ms-main-container style="padding: 0px">
        <div class="track-home-layout">
          <el-row :gutter="16">
            <el-col :span="12">
              <case-count-card @redirectPage="redirectPage" />
            </el-col>
            <el-col :span="12">
              <relevance-case-card @redirectPage="redirectPage" />
            </el-col>
          </el-row>

          <el-row :gutter="16" style="margin-top: 16px">
            <el-col :span="12">
              <bug-count-card @redirectPage="redirectPage" />
            </el-col>
            <el-col :span="12">
              <case-maintenance />
            </el-col>
          </el-row>

          <el-row style="margin-top: 16px">
            <el-col style="background-color: #ffffff">
              <ms-failure-test-case-list @redirectPage="redirectPage" />
            </el-col>
          </el-row>

          <el-row style="margin-top: 16px">
            <el-col style="background-color: #ffffff">
              <review-list />
            </el-col>
          </el-row>

          <el-row style="margin-top: 16px">
            <el-col style="background-color: #ffffff">
              <ms-running-task-list
                :call-from="'track_home'"
                @redirectPage="redirectPage"
              />
            </el-col>
          </el-row>
        </div>
      </ms-main-container>
    </ms-container>
  </div>
</template>

<script>
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import CaseCountCard from "./components/CaseCountCard";
import RelevanceCaseCard from "./components/RelevanceCaseCard";
import CaseMaintenance from "./components/CaseMaintenance";
import BugCountCard from "./components/BugCountCard";
import ReviewList from "./components/ReviewList";
import MsRunningTaskList from "./components/RunningTaskList";
import { getUUID } from "metersphere-frontend/src/utils";
import MsFailureTestCaseList from "@/business/home/components/FailureTestCaseList";

// require("echarts/lib/component/legend");
export default {
  name: "TrackHome",
  components: {
    MsFailureTestCaseList,
    ReviewList,
    BugCountCard,
    RelevanceCaseCard,
    CaseCountCard,
    MsMainContainer,
    MsContainer,
    CaseMaintenance,
    MsRunningTaskList,
  },
  data() {
    return {};
  },
  methods: {
    redirectPage(page, dataType, selectType, projectId, protocol) {
      //api页面跳转
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home;
      let defaultVersion = "default";
      switch (page) {
        case "testCase":
          home = this.$router.resolve({
            name: "testCaseRedirect",
            params: {
              redirectID: uuid,
              dataType: dataType,
              dataSelectRange: selectType,
              projectId: projectId,
            },
          });
          break;
        case "testPlanEdit":
          home = this.$router.resolve("/track/plan/view/" + selectType);
          break;
        case "scenarioWithQuery":
          home = this.$router.resolve(
            "/api/automation/" +
            defaultVersion +
            "/" +
            uuid +
            "/" +
            dataType +
            "/" +
            selectType +
            "/" +
            projectId
          );
          break;
        case "api":
          home = this.$router.resolve(
            "/api/definition/" +
              defaultVersion +
              "/" +
              uuid +
              "/" +
              dataType +
              "/" +
              selectType+
              "/" +
              projectId +
              "/" +
            protocol
          );
          break;
        case "issue":
          home = this.$router.resolve(
            "/track/issue/" + uuid + "/" + dataType + "/" + selectType
          );
      }
      if (home) {
        window.open(home.href, "_blank");
      }
    },
  },
};
</script>

<style scoped>
:deep(.el-card__header) {
  border: 0px;
  padding: 24px;
}

:deep(.el-card__body) {
  border: 0px;
  padding: 0px;
  margin: 0px 24px 24px 24px;
}

.track-home-layout {
  margin: 12px 24px;
  min-width: 1100px;
}

.track-home-layout :deep(.dashboard-title) {
  font-size: 18px;
  font-weight: 500;
}

.track-home-layout :deep(.common-amount) {
  margin-top: 4px;
}

.track-home-layout :deep(.dashboard-card) {
  height: 408px;
}

.track-home-layout :deep(.main-info) {
  height: 208px;
}

.track-home-layout :deep(.main-info-card) {
  height: 208px;
  width: 100%;
  color: #646a73;
  background-color: #ffffff;
  box-sizing: border-box;
  border: 1px solid #dee0e3;
  border-radius: 4px;
}

.track-home-layout :deep(.addition-info) {
  height: 86px;
  margin: 16px 0px 0px 0px;
}

.track-home-layout :deep(.addition-info-title) {
  line-height: 22px;
  font-size: 14px;
  font-weight: 400;
  color: #646a73;
}

.track-home-layout :deep(.addition-info-text) {
  line-height: 28px;
  color: #783887;
  font-size: 20px;
  font-weight: 500;
}

.track-home-layout :deep(.addition-info-num) {
  line-height: 22px;
  color: #783887;
  font-size: 14px;
  font-weight: 500;
}

.track-home-layout :deep(.home-table-cell) {
  height: 40px;
  background-color: #f5f6f7;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #646a73;
  line-height: 22px;
}

.track-home-layout :deep(.el-card) {
  border: 0;
}

.track-home-layout :deep(.table-title) {
  color: #1f2329;
  font-weight: 500;
  font-size: 18px;
  line-height: 26px;
}

.track-home-layout :deep(.el-table__row),
.adjust-table :deep(.el-table__row .el-link) {
  height: 40px;
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
  color: #1f2329;
}

.track-home-layout :deep(.el-table__body tr:hover) {
  cursor: pointer;
}

.track-home-layout :deep(.el-table .cell) {
  padding-left: 12px;
  padding-right: 12px;
}
</style>
