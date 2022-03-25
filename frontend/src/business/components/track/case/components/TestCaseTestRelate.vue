<template>
  <div>

    <el-dropdown  @command="handleCommand">
      <el-button type="primary" size="mini" :disabled="readOnly">
        {{$t('test_track.case.relate_test')}}<i class="el-icon-arrow-down el-icon--right"></i>
      </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="api">{{ $t('api_test.home_page.failed_case_list.table_value.case_type.api') }}</el-dropdown-item>
        <el-dropdown-item command="scenario">{{ $t('api_test.home_page.failed_case_list.table_value.case_type.scene') }}</el-dropdown-item>
        <el-dropdown-item command="performance">{{$t('api_test.home_page.failed_case_list.table_value.case_type.load')}}</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>

    <ms-table
      v-loading="result.loading"
      :show-select-all="false"
      :data="data"
      :enable-selection="false"
      :operators="operators"
      @refresh="initTable">

      <ms-table-column
        prop="num"
        label="ID">
      </ms-table-column>

      <ms-table-column
        prop="name"
        :label="$t('test_track.case.name')"/>

      <ms-table-column
        prop="projectName"
        :label="$t('commons.project')"/>

      <ms-table-column
        v-if="versionEnable"
        prop="versionName"
        :label="$t('commons.version')"/>

      <ms-table-column
        :label="$t('test_resource_pool.type')"
        prop="type">
        <template v-slot:default="{row}">
          {{typeMap[row.testType]}}
        </template>
      </ms-table-column>
    </ms-table>

    <test-case-api-relate
      :case-id="caseId"
      :versionEnable="versionEnable"
      :not-in-ids="notInIds"
      @refresh="initTable"
      ref="apiCaseRelevance"/>

    <test-case-scenario-relate
      :case-id="caseId"
      :versionEnable="versionEnable"
      :not-in-ids="notInIds"
      @refresh="initTable"
      ref="apiScenarioRelevance"/>

    <test-case-load-relate
      :case-id="caseId"
      :not-in-ids="notInIds"
      :versionEnable="versionEnable"
      @refresh="initTable"
      ref="loadRelevance"/>

  </div>
</template>

<script>
import MsTable from "@/business/components/common/components/table/MsTable";
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import TestCaseApiRelate from "@/business/components/track/case/components/TestCaseApiRelate";
import {deleteRelateTest, getRelateTest} from "@/network/testCase";
import TestCaseScenarioRelate from "@/business/components/track/case/components/TestCaseScenarioRelate";
import TestCaseLoadRelate from "@/business/components/track/case/components/TestCaseLoadRelate";
export default {
  name: "TestCaseTestRelate",
  components: {TestCaseLoadRelate, TestCaseScenarioRelate, TestCaseApiRelate, MsTableColumn, MsTable},
  data() {
    return {
      data: [],
      result: {},
      typeMap: {
        testcase: this.$t('api_test.home_page.failed_case_list.table_value.case_type.api'),
        automation: this.$t('api_test.home_page.failed_case_list.table_value.case_type.scene'),
        performance: this.$t('api_test.home_page.failed_case_list.table_value.case_type.load')
      },
      operators: [
        {
          tip: this.$t('test_track.plan_view.cancel_relevance'), icon: "el-icon-unlock",
          exec: this.remove,
          type: 'danger',
          isDisable: () => {
            return this.readOnly;
          }
        }
      ],
      notInIds: null
    }
  },
  props: ['caseId', 'readOnly', 'versionEnable'],
  watch: {
    caseId() {
      this.initTable();
    }
  },
  methods: {
    handleCommand(key) {
      if (!this.caseId) {
        this.$warning(this.$t('api_test.automation.save_case_info'));
        return;
      }
      if (key === 'api') {
        this.$refs.apiCaseRelevance.open();
      } else if (key === 'scenario') {
        this.$refs.apiScenarioRelevance.open();
      } else if (key === 'performance') {
        this.$refs.loadRelevance.open();
      }
    },
    remove(row) {
      deleteRelateTest(row.testCaseId, row.testId, () => {
        this.initTable();
      });
    },
    initTable() {
      this.result = getRelateTest(this.caseId, (data) => {
        this.data = data;
        this.notInIds = data.map(i => i.testId);
      });
    },
  }
}
</script>

<style scoped>
</style>
