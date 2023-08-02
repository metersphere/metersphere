<template>
  <div v-loading="loading">

    <slot name="header"></slot>

    <ms-node-tree
        v-if="refreshDataOver"
        v-loading="loading"
        :tree-nodes="data"
        :type="isReadOnly ? 'view' : 'edit'"
        :allLabel="$t('api_test.definition.api_all')"
        :default-label="$t('api_test.definition.unplanned_api')"
        :hide-opretor="true"
        local-suffix="api_definition"
        @refresh="list"
        @filter="filter"
        :show-case-num="showCaseNum"
        :delete-permission="['PROJECT_API_DEFINITION:READ+DELETE_API']"
        :add-permission="['PROJECT_API_DEFINITION:READ+CREATE_API']"
        :update-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']"
        @nodeSelectEvent="nodeChange"
        ref="nodeTree">

      <template v-slot:header>
        <api-module-header
            :show-operator="showOperator"
            :condition="condition"
            :current-module="currentModule"
            :is-read-only="isReadOnly"
            :moduleOptions="data"
            :options="options"
            :total="total"
            :select-project-id="projectId"
            @refreshTable="$emit('refreshTable')"
            @schedule="$emit('schedule')"
            @refresh="refresh"/>
      </template>

    </ms-node-tree>

  </div>

</template>

<script>
import {OPTIONS} from "metersphere-frontend/src/model/JsonData";
import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import {buildTree} from "metersphere-frontend/src/model/NodeTree";
import {apiModuleGetUserDefaultApiType, apiModuleProjectList} from "@/api/remote/api/api-module";
import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
import {getCurrentProjectID} from "@/business/utils/sdk-utils";
import {apiCaseModulePlanList} from "@/api/remote/plan/test-plan-api-case";
import ApiModuleHeader from "./ApiModuleHeader";

export default {
  name: 'MsApiModule',
  components: {
    MsNodeTree,
    MsSearchBar,
    ApiModuleHeader
  },
  data() {
    return {
      result: {},
      loading: false,
      refreshDataOver: true,
      condition: {
        protocol: OPTIONS[0].value,
        filterText: "",
        trashEnable: false
      },
      data: [],
      currentModule: {},
    }
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    defaultProtocol: String,
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
      }
    },
    showOperator: Boolean,
    planId: String,
    relevanceProjectId: String,
    currentVersion: String,
    reviewId: String,
    pageSource: String,
    selectDefaultProtocol: Boolean,
    total: Number,
    isRelevance: Boolean,
    options: {
      type: Array,
      default() {
        return OPTIONS;
      }
    },
    selectProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      }
    }
  },
  computed: {
    isPlanModel() {
      return !!this.planId;
    },
    isRelevanceModel() {
      return !!this.relevanceProjectId;
    },
    projectId() {
      if (this.selectProjectId) {
        return this.selectProjectId;
      } else {
        return getCurrentProjectID();
      }
    }
  },
  mounted() {
    this.initProtocol();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    'condition.protocol'() {
      this.$emit('protocolChange', this.condition.protocol);
      this.list();
    },
    'condition.trashEnable'() {
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    planId() {
      this.list();
    },
    defaultProtocol() {
      if (this.condition.protocol !== this.defaultProtocol) {
        this.condition.protocol = this.defaultProtocol;
      }
    },
    relevanceProjectId() {
      this.list();
    }
  },
  methods: {
    initProtocol() {
      //不是跳转来的页面不查询上次请求类型
      let isRedirectPage = this.isRedirect();
      if (this.$route.params.type) {
        this.condition.protocol = this.$route.params.type;
        this.$emit('protocolChange', this.condition.protocol);
        this.list();
      } else if (!this.isRelevance && !isRedirectPage && this.selectDefaultProtocol) {
        //展示页面是非引用页面才会查询上一次接口类型
        apiModuleGetUserDefaultApiType(this).then(response => {
          this.condition.protocol = response.data;
          this.$emit('protocolChange', this.condition.protocol);
          this.list();
        })
      } else {
        this.$emit('protocolChange', this.condition.protocol);
        this.list();
      }
    },
    isRedirect() {
      let isRedirectPage = false;
      let routeParamObj = this.$route.params.paramObj;
      if (routeParamObj) {
        if (routeParamObj.dataSelectRange) {
          let item = JSON.parse(JSON.stringify(routeParamObj.dataSelectRange)).param;
          if (item !== undefined) {
            let type = item.taskGroup.toString();
            if (type === "SWAGGER_IMPORT") {
              isRedirectPage = true;
            }
          }
        }
      } else {
        if (this.$route.params.dataSelectRange) {
          let item = JSON.parse(JSON.stringify(this.$route.params.dataSelectRange)).param;
          if (item !== undefined) {
            let type = item.taskGroup.toString();
            if (type === "SWAGGER_IMPORT") {
              isRedirectPage = true;
            }
          }
        }
      }
      return isRedirectPage;
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    list() {
      this.loading = true;
      if (this.isPlanModel) {
        apiCaseModulePlanList(this.planId, this.condition.protocol)
            .then((response) => {
              this.getData(response);
            });
      } else if (this.isRelevanceModel) {
        apiModuleProjectList(this.relevanceProjectId, this.condition.protocol)
            .then((response) => {
              this.getData(response);
            });
      } else {
        apiModuleProjectList(getCurrentProjectID())
            .then((response) => {
              this.getData(response);
            });
      }
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      if (node.data.id === 'root') {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
      this.nohupReloadTree(node.data.id);
    },
    nohupReloadTree(selectNodeId) {
      this.loading = true;
      if (this.isPlanModel) {
        apiCaseModulePlanList(this.planId, this.condition.protocol)
            .then((response) => {
              this.getNohupReloadData(response, selectNodeId);
            });
      } else if (this.isRelevanceModel) {
        apiModuleProjectList(this.relevanceProjectId, this.condition.protocol)
            .then((response) => {
              this.getNohupReloadData(response, selectNodeId);
            });
      }
    },
    getData(response) {
      this.loading = false;
      if (response.data) {
        this.data = response.data;
        this.data.forEach(node => {
          node.name = node.name === '未规划接口' ? this.$t('api_test.definition.unplanned_api') : node.name
          buildTree(node, {path: ''});
        });
        this.$emit('setModuleOptions', this.data);
        this.$emit('setNodeTree', this.data);
        if (this.$refs.nodeTree) {
          this.$refs.nodeTree.filter(this.condition.filterText);
        }
      }
    },
    getNohupReloadData(response, selectNodeId) {
      this.loading = false;
      if (response.data) {
        let treeData = response.data;
        treeData.forEach(node => {
          node.name = node.name === '未规划接口' ? this.$t('api_test.definition.unplanned_api') : node.name
          buildTree(node, {path: ''});
        });
        this.data = treeData;

        this.$nextTick(() => {
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter(this.condition.filterText);
            if (selectNodeId) {
              this.$refs.nodeTree.justSetCurrentKey(selectNodeId);
            }
          }
        })
      }
    },
    refresh() {
      this.list();
      this.$emit('refreshTable');
    },
  }
}
</script>

<style scoped>

</style>
