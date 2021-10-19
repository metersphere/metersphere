<template>

  <test-case-relevance-base
    @setProject="setProject"
    @save="saveCaseRelevance"
    :multiple-project="false"
    ref="baseRelevance">

    <template v-slot:aside>
      <ms-api-module
        :relevance-project-id="projectId"
        @nodeSelectEvent="nodeChange"
        @protocolChange="handleProtocolChange"
        @refreshTable="initTable"
        :is-read-only="true"
        ref="nodeTree"/>
    </template>

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

  </test-case-relevance-base>

</template>

<script>

  import MsApiModule from "@/business/components/api/definition/components/module/ApiModule";
  import TestCaseRelevanceBase from "@/business/components/track/plan/view/comonents/base/TestCaseRelevanceBase";
  import ApiTableList from "@/business/components/api/definition/components/complete/ApiTableList";

  export default {
    name: "ApiRelationshipRelevance",
    components: {
      ApiTableList,
      TestCaseRelevanceBase,
      MsApiModule,
    },
    props: ['apiDefinitionId', 'relationshipType'],
    data() {
      return {
        showCasePage: true,
        currentProtocol: null,
        currentModule: null,
        selectNodeIds: [],
        condition: {},
        currentRow: {},
        projectId: "",
        result: {},
        currentPage: 1,
        pageSize: 10,
        tableData: []
      };
    },
    watch: {
      projectId() {
        this.initTable();
      },
      selectNodeIds() {
        this.initTable();
      },
      currentProtocol() {
        this.$refs.nodeTree.list();
        this.initTable();
      }
    },
    methods: {
      open() {
        this.$refs.baseRelevance.open();
        this.initTable();
        if (this.$refs.nodeTree) {
          this.$refs.nodeTree.list();
        }
      },
      initTable() {
        this.condition.filters = {status: ["Prepare", "Underway", "Completed"]};
        this.condition.moduleIds = this.selectNodeIds;
        this.condition.projectId = this.projectId;

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
      setProject(projectId) {
        this.projectId = projectId;
      },
      nodeChange(node, nodeIds, pNodes) {
        this.selectNodeIds = nodeIds;
      },
      handleProtocolChange(protocol) {
        this.currentProtocol = protocol;
      },
      saveCaseRelevance() {
        let param = {};
        param.ids = this.$refs.apitable.getSelectIds();
        param.request = this.condition;
        if (this.relationshipType === 'PRE') {
          param.targetIds = param.ids;
        } else {
          param.sourceIds = param.ids;
        }
        param.id = this.apiDefinitionId;
        param.type = 'API';

        this.result = this.$post('/relationship/edge/save/batch', param, () => {
          this.$success(this.$t('commons.save_success'));
          this.$refs.baseRelevance.close();
          this.$emit('refresh');
        });
      },
    }
  }
</script>

<style scoped>
</style>
