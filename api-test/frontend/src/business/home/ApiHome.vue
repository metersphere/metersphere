<template>
  <div style="background-color:#E5E5E5;overflow: auto">
    <ms-container>
      <ms-main-container style="padding: 0px">
        <div class="api-home-layout">
          <el-row :gutter=16>
            <el-col :span="12">
              <api-dashboard @redirectPage="redirectPage"/>
            </el-col>
            <el-col :span="12">
              <api-case-dashboard @redirectPage="redirectPage"/>
            </el-col>
          </el-row>
          <el-row :gutter=16 style="margin-top: 16px;">
            <el-col :span="12">
              <scenario-dashboard @redirectPage="redirectPage"/>
            </el-col>
            <el-col :span="12">
              <scenario-schedule-dashboard @redirectPage="redirectPage"/>
            </el-col>
          </el-row>
          <el-row style="margin-top: 16px">
            <el-col style="height: 369px;background-color: #FFFFFF;">
              <updated-api-list @redirectPage="redirectPage"/>
            </el-col>
          </el-row>
        </div>
      </ms-main-container>
    </ms-container>
  </div>

</template>

<script>
import {getUUID} from "metersphere-frontend/src/utils";
import MsContainer from "metersphere-frontend/src/components/MsContainer";
import MsMainContainer from "metersphere-frontend/src/components/MsMainContainer";
import ApiDashboard from "@/business/home/components/dashboard/ApiDashboard";
import ApiCaseDashboard from "@/business/home/components/dashboard/ApiCaseDashboard";
import ScenarioDashboard from "@/business/home/components/dashboard/ScenarioDashboard";
import ScenarioScheduleDashboard from "@/business/home/components/dashboard/ScenarioScheduleDashboard";
import updatedApiList from "@/business/home/components/table/UpdatedApiList";

export default {
  name: "ApiHome",

  components: {
    ApiDashboard,
    ApiCaseDashboard,
    MsContainer,
    MsMainContainer,
    ScenarioDashboard,
    ScenarioScheduleDashboard,
    updatedApiList
  },

  data() {
    return {}
  },
  activated() {
  },
  created() {

  },
  methods: {
    /**
     *
     * @param redirectPage 要跳转的页面
     * @param dataType     要查询的数据类型
     * @param selectRange  查询范围 （比如成功的、失败的、未执行的、已执行的等等）
     * @param selectParam 查询参数 (比如ID)
     */
    redirectPage(redirectPage, dataType, selectRange, selectParam) {
      //传入UUID是为了进行页面重新加载判断
      let uuid = getUUID();
      let home;
      switch (redirectPage) {
        case "api":
          home = this.$router.resolve({
            name: 'ApiDefinitionWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectRange}
          });
          break;
        case "scenario":
          home = this.$router.resolve({
            name: 'ApiAutomationWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectRange}
          });
          break;
        case "scenarioReport":
          home = this.$router.resolve({
            name: 'ApiReportListWithQuery',
            params: {redirectID: uuid, dataType: dataType, dataSelectRange: selectRange}
          });
          break;
        case "testPlanEdit":
          home = this.$router.resolve('/track/plan/view/' + selectParam)
          break;
      }
      if (home) {
        window.open(home.href, '_blank');
      }
    }
  }
}
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

.api-home-layout {
  margin: 12px 24px;
  min-width: 1100px;
}

.api-home-layout :deep(.dashboard-title) {
  font-size: 18px;
  font-weight: 500;
}

.api-home-layout :deep(.common-amount) {
  margin-top: 4px;
}

.api-home-layout :deep(.dashboard-card) {
  height: 392px;
}

.api-home-layout :deep(.main-info) {
  height: 197px;
}

.api-home-layout :deep(.main-info-card) {
  height: 197px;
  width: 100%;
  background-color: #FFFFFF;
  box-sizing: border-box;
  border: 1px solid #DEE0E3;
  border-radius: 4px;
}

.api-home-layout :deep(.addition-info) {
  height: 86px;
  margin: 16px 0px 0px 0px;
}

.api-home-layout :deep(.addition-info-title) {
  line-height: 22px;
  font-size: 14px;
  font-weight: 400;
}

.api-home-layout :deep(.addition-info-text) {
  line-height: 28px;
  color: #783887;
  font-size: 20px;
  font-weight: 500;
}

.api-home-layout :deep(.addition-info-num) {
  line-height: 22px;
  color: #783887;
  font-size: 14px;
  font-weight: 500;
}

.api-home-layout :deep(.home-table-cell) {
  height: 38px;
  background-color: #F5F6F7;
  font-size: 14px;
  font-weight: 500;
  border: 1px solid rgba(31, 35, 41, 0.15);
  border-right-width: 0;
  border-left-width: 0;
  color: #1F2329;
  line-height: 22px;
}

</style>
