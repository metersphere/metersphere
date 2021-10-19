<template>
  <api-table-list
    :table-data="tableData"
    :condition="condition"
    :select-node-ids="selectNodeIds"
    :result="result"
    :current-protocol="currentProtocol"
    :current-page="currentPage"
    :page-size="pageSize"
    @refreshTable="initTable"
    ref="apitable"/>
</template>

<script>

  import {
    TEST_PLAN_RELEVANCE_API_DEFINITION_CONFIGS,
  } from "@/business/components/common/components/search/search-components";
  import ApiTableList from "@/business/components/api/definition/components/complete/ApiTableList";

  export default {
    name: "RelationshipRelevanceList",
    components: {
      ApiTableList,
    },
    data() {
      return {
        condition: {
          components: TEST_PLAN_RELEVANCE_API_DEFINITION_CONFIGS
        },
        result: {},
        tableData: [],
        currentPage: 1,
        pageSize: 10,
        environmentId: "",
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
      projectId: String,
      apiDefinitionId: String
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
      initTable(projectId) {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.condition.moduleIds = this.selectNodeIds;
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

        if (this.apiDefinitionId) {
          this.condition.id = this.apiDefinitionId;
          this.result = this.$post(this.buildPagePath('/api/definition/relationship/relate'), this.condition, response => {
            this.total = response.data.itemCount;
            this.tableData = response.data.listObject;
            this.tableData.forEach(item => {
              if (item.tags && item.tags.length > 0) {
                item.tags = JSON.parse(item.tags);
              }
            });
          });
        }
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
    },
  }
</script>

<style scoped>
</style>
