<template>
    <div>
      <slot name="header"></slot>

      <ms-node-tree
        :is-display="getIsRelevance"
        v-loading="loading"
        :tree-nodes="data"
        :allLabel="$t('ui.all_scenario')"
        :type="isReadOnly ? 'view' : 'edit'"
        :delete-permission="['PROJECT_UI_SCENARIO:READ+DELETE']"
        :add-permission="['PROJECT_UI_SCENARIO:READ+CREATE']"
        :update-permission="['PROJECT_UI_SCENARIO:READ+EDIT']"
        :default-label="'未规划场景'"
        :show-case-num="showCaseNum"
        :hide-opretor="isTrashData"
        local-suffix="ui_scenario"
        @refresh="list"
        @filter="filter"
        @nodeSelectEvent="nodeChange"
        class="element-node-tree"
        ref="nodeTree">

        <template v-slot:header>
          <ms-search-bar
            :show-operator="showOperator"
            :condition="condition"
            :commands="null"/>
        </template>

      </ms-node-tree>
    </div>

  </template>

  <script>
  import MsNodeTree from "metersphere-frontend/src/components/new-ui/MsNodeTree";
  import {buildTree} from "metersphere-frontend/src/model/NodeTree";
  import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
  import {getCurrentProjectID} from "@/business/utils/sdk-utils";
  import {uiScenarioModulePlanList} from "@/api/remote/ui/test-plan-ui-scenario-case";
  import {getUiScenarioModuleListByCondition} from "@/api/remote/ui/ui-scenario-module";

  export default {
    name: 'UiScenarioModule',
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
      pageSource: String,
      total: Number,
      isTrashData: Boolean,
      planId: String,
      showCaseNum: {
        type: Boolean,
        default: true
      },
      caseCondition: Object
    },
    computed: {
      isPlanModel() {
        return this.planId ? true : false;
      },
      isRelevanceModel() {
        return this.relevanceProjectId ? true : false;
      },
      projectId() {
        return getCurrentProjectID();
      },
      getIsRelevance() {
        if (this.pageSource !== 'scenario') {
          return this.openType;
        } else {
          return "scenario";
        }
      }
    },
    data() {
      return {
        openType: 'relevance',
        loading: false,
        condition: {
          filterText: "",
          trashEnable: false
        },
        data: [],
        currentModule: undefined
      }
    },
    mounted() {
      this.list();
    },
    watch: {
      'condition.filterText'() {
        this.filter();
      },
      relevanceProjectId() {
        this.list(this.relevanceProjectId);
      }
    },
    methods: {
      refresh() {
        this.$emit("refreshTable");
      },
      filter() {
        this.$refs.nodeTree.filter(this.condition.filterText);
      },
      list(projectId) {
        if (this.isPlanModel) {
          this.loading = true;
          uiScenarioModulePlanList(this.planId)
            .then(response => {
              this.loading = false;
              if (response.data != undefined && response.data != null) {
                this.data = response.data;
                this.data.forEach(node => {
                  buildTree(node, {path: ''});
                });
              }
            });
        } else {
          this.loading = true;
          getUiScenarioModuleListByCondition(projectId ? projectId : this.projectId, 'scenario', this.caseCondition)
            .then(r => {
              if (r && r.data) {
                this.loading = false;
                this.data = r.data;
                this.data.forEach(node => {
                  buildTree(node, {path: ''});
                });
                this.$emit('setModuleOptions', this.data);
                this.$emit('setNodeTree', this.data);
                if (this.$refs.nodeTree) {
                  this.$refs.nodeTree.filter(this.condition.filterText);
                }
              }
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
      },
    }
  }
  </script>

  <style scoped>
  .node-tree {
    margin-top: 15px;
    margin-bottom: 15px;
  }

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

  .element-node-tree {
    width: 100%;
    /* min-width: 290px; */
  }

  :deep(.element-node-tree .recycle .el-col.el-col-3) {
    text-align: center;
  }

  </style>
