<template>
  <el-card class="table-card" shadow="never" v-loading="loading" body-style="padding:10px;">
    <template v-slot:header>
      <span class="table-title">
        {{ $t('api_test.home_page.failed_case_list.title') }}
      </span>
    </template>
    <div v-loading="loading" element-loading-background="#FFFFFF">
      <div v-show="loadError"
           style="width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center;align-items: center">
        <img style="height: 100px;width: 100px;"
             src="/assets/figma/icon_load_error.svg"/>
        <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
      </div>
      <div v-show="!loadError">
        <el-table :data="tableData" class="adjust-table table-content"
                  :header-cell-style="{backgroundColor: '#F5F6F7'}" height="224px">
          <el-table-column prop="sortIndex" :label="$t('home.case.index')" fixed show-overflow-tooltip/>
          <el-table-column prop="caseName" :label="$t('home.case.case_name')" fixed>
            <template v-slot:default="{row}">
              <el-link type="info" @click="redirect(row.caseType,row.id)"
                       :disabled="(row.caseType === 'apiCase' && apiCaseReadOnly) || (row.caseType === 'scenario' && apiScenarioReadOnly) ||
                  (row.caseType === 'load' && loadCaseReadOnly) || (row.caseType === 'testCase' && testCaseReadOnly)">
                {{ row.caseName }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column
            prop="caseType"
            column-key="caseType"
            :label="$t('home.case.case_type')"
            fixed
            show-overflow-tooltip>
            <template v-slot:default="scope">
              <basic-case-type-label :value="scope.row.caseType"></basic-case-type-label>
            </template>
          </el-table-column>
          <el-table-column prop="testPlan" :label="$t('home.case.test_plan')">
            <template v-slot:default="{row}">
              <div>
                <el-link type="info" @click="redirect('testPlanEdit',row.testPlanId)">
                  {{ row.testPlan }}
                </el-link>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="failureTimes" :label="$t('home.case.failure_times')"
                           fixed show-overflow-tooltip/>
        </el-table>
        <home-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize" layout="prev, pager, next, sizes"
                         :total="total"/>
      </div>
    </div>
  </el-card>
</template>

<script>
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {homeTestPlanFailureCaseGet} from "@/api/remote/api/api-home";
import {hasPermission} from "@/business/utils/sdk-utils";
import HomePagination from "@/business/home/components/pagination/HomePagination";
import BasicCaseTypeLabel from "@/business/home/components/table/BasicCaseTypeLabel";

export default {
  name: "MsFailureTestCaseList",

  components: {
    MsTag, HomePagination, BasicCaseTypeLabel
  },

  data() {
    return {
      tableData: [],
      loading: false,
      loadError: false,
      testCaseReadOnly: false,
      apiCaseReadOnly: false,
      apiScenarioReadOnly: false,
      loadCaseReadOnly: false,
      currentPage: 1,
      pageSize: 5,
      total: 0,
    }
  },
  props: {
    selectFunctionCase: Boolean,
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    search() {
      if (this.projectId) {
        this.loading = true;
        this.loadError = false;
        homeTestPlanFailureCaseGet(this.projectId, this.selectFunctionCase, 10, this.currentPage, this.pageSize)
          .then((r) => {
            this.loading = false;
            this.loadError = false;
            this.total = r.data.itemCount;
            this.tableData = r.data.listObject;
          }).catch(() => {
            this.loading = false;
            this.loadError = true;
          });
      }
    },
    redirect(pageType, param) {
      switch (pageType) {
        case "testPlanEdit":
          this.$emit('redirectPage', 'testPlanEdit', null, param);
          break;
        case "apiCase":
          this.$emit('redirectPage', 'api', 'apiTestCase', 'single:' + param);
          break;
        case "scenario":
          this.$emit('redirectPage', 'scenarioWithQuery', 'scenario', 'edit:' + param);
          break;
      }
    }
  },
  created() {
    this.search();
    this.testCaseReadOnly = !hasPermission('PROJECT_TRACK_CASE:READ');
    this.apiCaseReadOnly = !hasPermission('PROJECT_API_DEFINITION:READ');
    this.apiScenarioReadOnly = !hasPermission('PROJECT_API_SCENARIO:READ');
    this.loadCaseReadOnly = !hasPermission('PROJECT_PERFORMANCE_TEST:READ');
  },
  activated() {
    this.search();
  }
}
</script>

<style scoped>
.table-title {
  color: #1F2329;
  font-weight: 500;
  font-size: 18px!important;
  line-height: 26px;
}

.el-card :deep( .el-card__header ) {
  border-bottom: 0px solid #EBEEF5;
}
</style>
