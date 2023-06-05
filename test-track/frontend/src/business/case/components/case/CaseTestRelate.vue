<template>
  <div class="relate-container">
    <div class="header-wrap">
      <div class="menu-row">
        <el-dropdown placement="bottom" @command="handleCommand" :disabled="readOnly" :class="{'disable-row' : readOnly}">
          <div style="line-height: 32px; color: #1F2329; cursor: pointer">
            <i class="el-icon-connection" style="margin-right: 4.3px"></i>{{$t("test_track.case.relate_test")}}
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="api">{{
              $t(
                "api_test.home_page.failed_case_list.table_value.case_type.api"
              )
            }}</el-dropdown-item>
            <el-dropdown-item command="scenario">{{
              $t(
                "api_test.home_page.failed_case_list.table_value.case_type.scene"
              )
            }}</el-dropdown-item>
            <!-- v-xpack -->
            <el-dropdown-item command="ui" v-xpack>{{
              $t("api_test.home_page.failed_case_list.table_value.case_type.ui")
            }}</el-dropdown-item>
            <el-dropdown-item command="performance">{{
              $t(
                "api_test.home_page.failed_case_list.table_value.case_type.load"
              )
            }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="opt-row">
        <ms-new-ui-search :condition.sync="condition" @search="search" />
      </div>
    </div>
    <div class="table-wrap">
      <ms-table
        v-loading="result.loading"
        :show-select-all="false"
        :data="data"
        :enable-max-height="false"
        :enable-selection="false"
        :operators="operators"
        :max-height="'calc(100vh)'"
        @refresh="initTable"
      >
        <ms-table-column
          prop="num"
          label="ID"
          sortable
          min-width="100px"
          width="100px"
        >
        </ms-table-column>

        <ms-table-column
          prop="name"
          :label="$t('case.case_name')"
          min-width="316px"
          width="316px"
          sortable
        />

        <ms-table-column
          prop="projectName"
          :label="$t('commons.project')"
          sortable
          min-width="180px"
          width="180px"
        />

        <ms-table-column
          v-if="versionEnable"
          sortable
          prop="versionName"
          :label="$t('commons.version')"
          min-width="100px"
          width="100px"
        />

        <ms-table-column :label="$t('test_resource_pool.type')" prop="type">
          <template v-slot:default="{ row }">
            {{ typeMap[row.testType] }}
          </template>
        </ms-table-column>
      </ms-table>
    </div>

    <!-- origin -->
    <test-case-api-relate
      :case-id="caseId"
      :versionEnable="versionEnable"
      :not-in-ids="notInIds"
      @refresh="initTable"
      ref="apiCaseRelevance"
    />

    <test-case-scenario-relate
      :case-id="caseId"
      :versionEnable="versionEnable"
      :not-in-ids="notInIds"
      @refresh="initTable"
      ref="apiScenarioRelevance"
    />

    <test-case-ui-scenario-relate
      :case-id="caseId"
      :versionEnable="versionEnable"
      :not-in-ids="notInIds"
      @refresh="initTable"
      ref="uiScenarioRelevance"
    />

    <test-case-load-relate
      :case-id="caseId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      @refresh="initTable"
      ref="loadRelevance"
    />
  </div>
</template>

<script>
import MsNewUiSearch from "metersphere-frontend/src/components/new-ui/MsSearch";
import MsTable from "metersphere-frontend/src/components/new-ui/MsTable";
import MsTableColumn from "metersphere-frontend/src/components/table/MsTableColumn";
import TestCaseApiRelate from "@/business/case/components/case/relate/CaseApiRelate";
import { deleteRelateTest, getRelateTest } from "@/api/testCase";
import { operationConfirm } from "@/business/utils/sdk-utils";
import TestCaseScenarioRelate from "@/business/case/components/case/relate/CaseScenarioRelate";
import TestCaseUiScenarioRelate from "@/business/case/components/case/relate/CaseUiScenarioRelate";
import TestCaseLoadRelate from "@/business/case/components/case/relate/CaseLoadRelate";
import TestCaseUiScenarioRelevance from "@/business/plan/view/comonents/ui/TestCaseUiScenarioRelevance";

export default {
  name: "CaseTestRelate",
  components: {
    TestCaseUiScenarioRelevance,
    TestCaseLoadRelate,
    TestCaseScenarioRelate,
    TestCaseApiRelate,
    MsTableColumn,
    MsTable,
    TestCaseUiScenarioRelate,
    MsNewUiSearch,
  },
  data() {
    return {
      condition: {},
      data: [],
      result: {},
      typeMap: {
        testcase: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.api"
        ),
        automation: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.scene"
        ),
        performance: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.load"
        ),
        uiAutomation: this.$t(
          "api_test.home_page.failed_case_list.table_value.case_type.ui"
        ),
      },
      operators: [
        {
          tip: this.$t("case.disassociate"),
          isTextButton: true,
          exec: this.remove,
          isDisable: () => {
            return this.readOnly;
          },
        },
      ],
      notInIds: null,
    };
  },
  props: ["caseId", "readOnly", "versionEnable"],
  watch: {
    caseId() {
      this.initTable();
    },
  },
  activated() {
    this.initTable();
  },
  methods: {
    handleCommand(key) {
      if (!this.caseId) {
        this.$warning(this.$t("api_test.automation.save_case_info"));
        return;
      }
      if (key === "api") {
        this.$refs.apiCaseRelevance.open();
      } else if (key === "scenario") {
        this.$refs.apiScenarioRelevance.open();
      } else if (key === "performance") {
        this.$refs.loadRelevance.open();
      } else if (key === "ui") {
        this.$refs.uiScenarioRelevance.open();
      }
    },
    remove(row) {
      this.$confirm(this.$t('case.cancel_relate_case_tips_content'), this.$t('case.cancel_relate_case_tips_title'), {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        deleteRelateTest(row.testCaseId, row.testId).then(() => {
          this.$success(this.$t('test_track.cancel_relevance_success'), false);
          this.initTable();
        });
      });
    },
    initTable() {
      if (this.caseId) {
        getRelateTest(this.caseId).then((response) => {
          let data = response.data || [];
          if (this.condition && this.condition.name && data) {
            //过滤 data
            this.data = data.filter((v) => {
              return (
                this.like(this.condition.name, v.name) ||
                this.like(this.condition.name, v.num)
              );
            });
          } else {
            this.data = data;
          }
          this.$emit("setCount", this.data.length);
          this.notInIds = this.data.map((i) => i.testId);
        });
      }
    },
    // since v2.6
    search() {
      this.initTable();
    },

    like(key, target) {
      if (key === undefined || target === undefined) {
        return false;
      }
      target = target + "";
      return target.indexOf(key) !== -1;
    },
  },
};
</script>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.el-button.el-button--default.el-button--small {
  background: #ffffff;
  border-radius: 4px;
}
.relate-container {
  .header-wrap {
    display: flex;
    height: px2rem(72);
    justify-content: space-between;
    align-items: center;
    .menu-row {
      width: 98px;
      height: 32px;
      left: 112px;
      top: 251px;
      background: #ffffff;
      border: 1px solid #DCDFE6;
      border-radius: 4px;
      text-align: center;
      color: #783887;
    }
    .opt-row {
    }
  }

  .table-wrap {
  }
}

.relate-container .header-wrap .menu-row:hover {
  background-color: whitesmoke;
}

.disable-row {
  cursor: not-allowed;
}

.el-dropdown-menu__item:hover {
  background-color: rgba(31, 35, 41, 0.1)!important;
}

.el-dropdown-menu__item {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  color: #1F2329!important;
}
</style>
