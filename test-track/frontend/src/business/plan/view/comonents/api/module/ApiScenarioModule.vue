<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('api_test.automation.all_scenario')"
      :type="isReadOnly ? 'view' : 'edit'"
      :delete-permission="['PROJECT_API_SCENARIO:READ+DELETE']"
      :add-permission="['PROJECT_API_SCENARIO:READ+CREATE']"
      :update-permission="['PROJECT_API_SCENARIO:READ+EDIT']"
      :default-label="$t('api_test.automation.unplanned_scenario')"
      local-suffix="api_scenario"
      :show-case-num="showCaseNum"
      :hide-opretor="isTrashData"
      @refresh="list"
      @filter="filter"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
        <ms-search-bar
          :show-operator="false"
          :condition="condition"
          :commands="null"/>
        <module-trash-button v-if="!isReadOnly && !isTrashData" :condition="condition" :exe="enableTrash"
                             :total='total'/>
      </template>

    </ms-node-tree>
  </div>

</template>

<script>

import MsNodeTree from "metersphere-frontend/src/components/module/MsNodeTree";
import {buildTree} from "metersphere-frontend/src/model/NodeTree";
import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
import {getCurrentProjectID} from "@/business/utils/sdk-utils";
import {apiAutomationModuleProjectList} from "@/api/remote/api/api-automation-module";
import {apiAutomationModulePlanList} from "@/api/remote/plan/test-plan-scenario";

export default {
  name: 'MsApiScenarioModule',
  components: {
    MsSearchBar,
    MsNodeTree,
  },
  props: {
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showOperator: Boolean,
    relevanceProjectId: String,
    planId: String,
    pageSource: String,
    total: Number,
    isTrashData: Boolean,
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
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
      return this.planId ? true : false;
    },
    isRelevanceModel() {
      return this.relevanceProjectId ? true : false;
    },
    projectId() {
      if (this.selectProjectId) {
        return this.selectProjectId;
      } else {
        return getCurrentProjectID();
      }
    }
  },
  data() {
    return {
      result: {},
      condition: {
        filterText: "",
        trashEnable: false
      },
      data: [],
      currentModule: undefined,
    }
  },
  mounted() {
    this.list();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    'condition.trashEnable'() {
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    planId() {
      this.list();
    },
    relevanceProjectId() {
      this.list();
    }
  },
  methods: {
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    list(projectId) {
      if (!projectId) {
        projectId = this.projectId ? this.projectId : getCurrentProjectID();
      }
      if (this.isPlanModel) {
        apiAutomationModulePlanList(this.planId)
          .then((response) => {
            this.getData(response);
          });
      } else if (this.isRelevanceModel) {
        apiAutomationModuleProjectList(this.relevanceProjectId)
          .then((response) => {
            this.getData(response);
          });
      } else {
        apiAutomationModuleProjectList(projectId)
          .then((response) => {
            this.getData(response);
          });
      }
    },
    getData(response) {
      if (response.data != undefined && response.data != null) {
        this.data = response.data;
        this.data.forEach(node => {
          node.name = node.name === '未规划场景' ? this.$t('api_test.automation.unplanned_scenario') : node.name
          buildTree(node, {path: ''});
        });
        this.$emit('setModuleOptions', this.data);
        this.$emit('setNodeTree', this.data);
        if (this.$refs.nodeTree) {
          this.$refs.nodeTree.filter(this.condition.filterText);
        }
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
    //后台更新节点数据
    nohupReloadTree(selectNodeId) {
      if (this.isPlanModel) {
        apiAutomationModulePlanList(this.planId)
          .then((response) => {
            this.getNohupReloadData(response, selectNodeId);
          });
      } else if (this.isRelevanceModel) {
        apiAutomationModuleProjectList(this.relevanceProjectId)
          .then((response) => {
            this.getNohupReloadData(response, selectNodeId);
          });
      }
    },
    getNohupReloadData(response, selectNodeId) {
      if (response.data != undefined && response.data != null) {
        this.data = response.data;
        this.data.forEach(node => {
          node.name = node.name === '未规划场景' ? this.$t('api_test.automation.unplanned_scenario') : node.name
          buildTree(node, {path: ''});
        });

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
      this.$emit("refreshAll");
    }
  }
}
</script>

<style scoped>
.ms-el-input {
  height: 25px;
  line-height: 25px;
}

.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.father .child {
  display: none;
}

.father:hover .child {
  display: block;
}

.node-title {
  width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0 5px;
  overflow: hidden;
}

.node-operate > i {
  color: #409eff;
  margin: 0 5px;
}

:deep(.el-tree-node__content) {
  height: 33px;
}

.ms-api-buttion {
  width: 30px;
}

</style>
