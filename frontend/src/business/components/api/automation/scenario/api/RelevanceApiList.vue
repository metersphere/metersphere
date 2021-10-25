<template>
  <div>
    <api-list-container
      :is-api-list-enable="isApiListEnable"
      @isApiListEnableChange="isApiListEnableChange">

      <api-table-list
        :table-data="tableData"
        :condition="condition"
        :select-node-ids="selectNodeIds"
        :result="result"
        :total="total"
        :current-protocol="currentProtocol"
        :screen-height="screenHeight"
        @setSelectRow="setSelectRow"
        @refreshTable="initTable"
        ref="apitable">

        <template v-slot:header>
          <ms-environment-select :project-id="projectId" v-if="isTestPlan" :is-read-only="isReadOnly"
                                 @setEnvironment="setEnvironment" ref="msEnvironmentSelect"/>
        </template>

      </api-table-list>

    </api-list-container>

    <table-select-count-bar :count="selectRows.size"/>
  </div>

</template>

<script>

  import ApiListContainer from "../../../definition/components/list/ApiListContainer";
  import MsEnvironmentSelect from "../../../definition/components/case/MsEnvironmentSelect";
  import TableSelectCountBar from "./TableSelectCountBar";
  import {buildBatchParam} from "@/common/js/tableUtils";
  import {
    TEST_PLAN_RELEVANCE_API_DEFINITION_CONFIGS,
  } from "@/business/components/common/components/search/search-components";
  import ApiTableList from "@/business/components/api/definition/components/complete/ApiTableList";

  export default {
    name: "RelevanceApiList",
    components: {
      ApiTableList,
      TableSelectCountBar,
      MsEnvironmentSelect,
      ApiListContainer,
    },
    data() {
      return {
        condition: {
          components: TEST_PLAN_RELEVANCE_API_DEFINITION_CONFIGS
        },
        result: {},
        screenHeight: 'calc(100vh - 400px)',//屏幕高度,
        tableData: [],
        environmentId: "",
        total: 0,
        selectRows: new Set()
      }
    },
    props: {
      currentProtocol: String,
      selectNodeIds: Array,
      visible: {
        type: Boolean,
        default: false,
      },
      isApiListEnable: {
        type: Boolean,
        default: false,
      },
      isReadOnly: {
        type: Boolean,
        default: false
      },
      isCaseRelevance: {
        type: Boolean,
        default: false,
      },
      projectId: String,
      planId: String,
      isTestPlan: Boolean,
    },
    created: function () {
      this.initTable();
    },
    watch: {
      selectNodeIds() {
        this.initTable();
      },
      currentProtocol() {
        this.initTable();
      },
      projectId() {
        this.initTable();
      }
    },
    methods: {
      setSelectRow(setSelectRow) {
        this.selectRows = setSelectRow;
      },
      isApiListEnableChange(data) {
        this.$emit('isApiListEnableChange', data);
      },
      initTable(projectId) {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.condition.moduleIds = this.selectNodeIds;
        if (this.trashEnable) {
          this.condition.filters = {status: ["Trash"]};
          this.condition.moduleIds = [];
        }
        if (projectId != null && typeof projectId === 'string') {
          this.condition.projectId = projectId;
        } else if (this.projectId != null) {
          this.condition.projectId = this.projectId;
        }

        if (this.currentProtocol != null) {
          this.condition.protocol = this.currentProtocol;
        } else {
          this.condition.protocol = "HTTP";
        }

        let url = '/api/definition/list/';
        if (this.isTestPlan) {
          url = '/api/definition/list/relevance/';
          this.condition.planId = this.planId;
        }

        this.$nextTick(() => {
          this.result = this.$post(url + this.$refs.apitable.currentPage + "/" + this.$refs.apitable.pageSize, this.condition, response => {
            this.total = response.data.itemCount;
            this.tableData = response.data.listObject;
            this.tableData.forEach(item => {
              if (item.tags && item.tags.length > 0) {
                item.tags = JSON.parse(item.tags);
              }
            });
          });
        });
      },
      setEnvironment(data) {
        this.environmentId = data.id;
      },
      getConditions() {
        let sampleSelectRows = this.selectRows;
        let param = buildBatchParam(this);
        param.ids = Array.from(sampleSelectRows).map(row => row.id);
        return param;
      },
      clear() {
        if (this.$refs.apitable) {
          this.$refs.apitable.clear();
        }
      },
      clearEnvAndSelect() {
        this.environmentId = "";
        if (this.$refs.msEnvironmentSelect) {
          this.$refs.msEnvironmentSelect.environmentId = "";
        }
        this.clear();
      }
    },
  }
</script>

<style scoped>
.card-content >>> .el-button-group {
  float: left;
}
</style>
