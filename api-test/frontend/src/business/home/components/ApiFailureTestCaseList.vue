<template>
  <div class="table-card" v-loading="result" body-style="padding:10px;">
    <el-table border :data="tableData" class="adjust-table table-content" height="300px">
      <el-table-column prop="sortIndex" :label="$t('home.case.index')"
                       width="100" show-overflow-tooltip/>
      <el-table-column prop="caseName" :label="$t('home.case.case_name')"
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
        :label="$t('home.case.case_type')"
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
                       width="100" show-overflow-tooltip/>
    </el-table>
  </div>
</template>

<script>
import {getCaseById} from "@/api/api-test-case";
import {getDefinitionById} from "@/api/definition";
import {getFailureCaseAboutTestPlan} from "@/api/home";
import MsTag from "metersphere-frontend/src/components/MsTag";
import {getCurrentProjectID, getCurrentWorkspaceId} from "metersphere-frontend/src/utils/token";
import {getUUID} from "metersphere-frontend/src/utils";

export default {
  name: "MsApiFailureTestCaseList",

  components: {
    MsTag
  },

  data() {
    return {
      result: false,
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
        this.result = getFailureCaseAboutTestPlan(this.projectId, false).then(response => {
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
          getCaseById(param.id).then((response) => {
            let apiCase = response.data;
            if (!apiCase) {
              this.$message.error(this.$t('commons.api_case') + this.$t('commons.not_exist'))
            } else {
              getDefinitionById(apiCase.apiDefinitionId).then((response) => {
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

.el-card :deep(.el-card__header) {
  border-bottom: 0px solid #EBEEF5;
}

</style>
