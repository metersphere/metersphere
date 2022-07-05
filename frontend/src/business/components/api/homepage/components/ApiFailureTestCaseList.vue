<template>
  <div class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <el-table border :data="tableData" class="adjust-table table-content" height="300px">
      <el-table-column prop="sortIndex" :label="$t('api_test.home_page.failed_case_list.table_coloum.index')"
                       width="100" show-overflow-tooltip/>
      <el-table-column prop="caseName" :label="$t('api_test.home_page.failed_case_list.table_coloum.case_name')"
                       width="150">
        <template v-slot:default="{row}">
          <el-link type="info" @click="redirect(row.caseType,row)">
            {{ row.caseName }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column
        prop="caseType"
        column-key="caseType"
        :label="$t('api_test.home_page.failed_case_list.table_coloum.case_type')"
        width="150"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <ms-tag v-if="scope.row.caseType === 'apiCase'" type="success" effect="plain"
                  :content="$t('api_test.home_page.failed_case_list.table_value.case_type.api')"/>
          <ms-tag v-if="scope.row.caseType === 'scenario'" type="warning" effect="plain"
                  :content="$t('api_test.home_page.failed_case_list.table_value.case_type.scene')"/>
          <ms-tag v-if="scope.row.caseType === 'load'" type="danger" effect="plain"
                  :content="$t('api_test.home_page.failed_case_list.table_value.case_type.load')"/>
          <ms-tag v-if="scope.row.caseType === 'testCase'" effect="plain"
                  :content="$t('api_test.home_page.failed_case_list.table_value.case_type.functional')"/>
        </template>
      </el-table-column>
      <el-table-column prop="testPlan" :label="$t('api_test.home_page.failed_case_list.table_coloum.test_plan')">
        <template v-slot:default="{row}">
          <div>
            <el-link type="info" @click="redirect('testPlanEdit',row.testPlanId)">
              {{ row.testPlan }}
            </el-link>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="failureTimes" :label="$t('api_test.home_page.failed_case_list.table_coloum.failure_times')"
                       width="100" show-overflow-tooltip/>
    </el-table>
  </div>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";
import {getCurrentProjectID, getCurrentWorkspaceId, getUUID} from "@/common/js/utils";

export default {
  name: "MsApiFailureTestCaseList",

  components: {
    MsTag
  },

  data() {
    return {
      result: {},
      tableData: [],
      loading: false
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    search() {
      if (this.projectId) {
        this.result = this.$get("/api/faliureCaseAboutTestPlan/" + this.projectId + "/false/10", response => {
          this.tableData = response.data;
        });
      }
    },
    redirect(pageType, param) {
      let definitionData;
      switch (pageType) {
        case "testPlanEdit":
          this.$emit('redirectPage', 'testPlanEdit', null, param);
          break;
        case "apiCase":
          this.$get('/api/testcase/findById/' + param.id, (response) => {
            let apiCase = response.data;
            if (!apiCase) {
              this.$message.error(this.$t('commons.api_case') + this.$t('commons.not_exist'))
            } else {
              this.$get('/api/definition/get/' + apiCase.apiDefinitionId, (response) => {
                let api = response.data;
                if (!api) {
                  this.$message.error(this.$t('api_test.home_page.api_details_card.title') + this.$t('commons.not_exist'))
                } else {
                  if (param.protocol === 'dubbo://') {
                    param.protocol = 'DUBBO'
                  }
                  let paramObj = {
                    redirectID: getUUID(),
                    dataType: "apiTestCase",
                    dataSelectRange: 'single:' + param.id,
                    projectId: getCurrentProjectID(),
                    type: api.protocol,
                    workspaceId: getCurrentWorkspaceId(),
                  };
                  definitionData = this.$router.resolve({
                    name: 'ApiDefinitionWithQuery',
                    params: {
                      redirectID: getUUID(),
                      dataType: "apiTestCase",
                      dataSelectRange: 'single:' + param.id,
                      projectId: getCurrentProjectID(),
                      type: api.protocol,
                      workspaceId: getCurrentWorkspaceId(),
                    }
                  });
                  window.open(definitionData.href, '_blank');
                }
              });
            }
          });
          break;
        case "scenario":
          this.$emit('redirectPage', 'scenarioWithQuery', 'scenario', 'edit:' + param.id);
          break;
      }
    }
  },


  created() {
    this.search();
  },
  activated() {
    this.search();
  }
}
</script>

<style scoped>

.el-table {
  cursor: pointer;
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

</style>
