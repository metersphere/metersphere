<template>
  <test-case-relevance-base
    pageKey="API_CASE_RELEVANCE_DIALOG"
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
        :show-case-num="false"
        :is-relevance="true"
        ref="nodeTree" />
    </template>

    <mx-version-select
      v-xpack
      :project-id="projectId"
      :default-version="currentVersion"
      @changeVersion="currentVersionChange" />

    <api-table-list
      :table-data="tableData"
      :project-id="projectId"
      :total="total"
      :condition="condition"
      :select-node-ids="selectNodeIds"
      :result="result"
      :current-protocol="currentProtocol"
      @refreshTable="initTable"
      ref="apitable" />
  </test-case-relevance-base>
</template>

<script>
import { addRelationship, definitionRelationship } from '@/api/definition';
import MsApiModule from '@/business/definition/components/module/ApiModule';
import TestCaseRelevanceBase from '@/business/commons/TestCaseRelevanceBase';
import ApiTableList from '@/business/definition/components/complete/ApiTableList';

export default {
  name: 'ApiRelationshipRelevance',
  components: {
    ApiTableList,
    TestCaseRelevanceBase,
    MsApiModule,
    MxVersionSelect: () => import('metersphere-frontend/src/components/version/MxVersionSelect'),
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
      projectId: '',
      result: false,
      total: 0,
      tableData: [],
      currentVersion: null,
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
    },
  },
  methods: {
    open() {
      this.condition = {};
      this.$refs.baseRelevance.open();
      this.initTable();
      if (this.$refs.nodeTree) {
        this.$refs.nodeTree.list();
      }
    },
    initTable() {
      this.condition.filters = { status: ['Prepare', 'Underway', 'Completed'] };
      this.condition.moduleIds = this.selectNodeIds;
      this.condition.projectId = this.projectId;

      if (this.currentProtocol != null) {
        this.condition.protocol = this.currentProtocol;
      } else {
        this.condition.protocol = 'HTTP';
      }

      this.condition.versionId = this.currentVersion;

      this.$nextTick(() => {
        if (this.apiDefinitionId) {
          this.condition.id = this.apiDefinitionId;
          this.result = definitionRelationship(
            this.$refs.apitable.currentPage,
            this.$refs.apitable.pageSize,
            this.condition
          ).then((response) => {
            this.total = response.data.itemCount;
            this.tableData = response.data.listObject;
            this.tableData.forEach((item) => {
              if (item.tags && item.tags.length > 0) {
                item.tags = JSON.parse(item.tags);
              }
            });
          });
        }
      });
    },
    buildPagePath(path) {
      return path + '/' + this.$refs.apitable.currentPage + '/' + this.$refs.apitable.pageSize;
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
      param.condition = this.condition;

      this.result = addRelationship(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.$refs.baseRelevance.close();
        this.$emit('refresh');
      });
    },
    currentVersionChange(currentVersion) {
      this.currentVersion = currentVersion || null;
      this.initTable();
    },
  },
};
</script>

<style scoped></style>
