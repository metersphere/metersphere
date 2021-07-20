<template>
  <div>

    <slot name="header"></slot>

    <ms-node-tree
      :is-display="getIsRelevance"
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('commons.all_module_title')"
      :type="isReadOnly ? 'view' : 'edit'"
      :delete-permission="['PROJECT_API_SCENARIO:READ+DELETE']"
      :add-permission="['PROJECT_API_SCENARIO:READ+CREATE']"
      :update-permission="['PROJECT_API_SCENARIO:READ+EDIT']"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">

      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator"
          :condition="condition"
          :commands="operators"/>
        <module-trash-button v-if="!isReadOnly" :condition="condition" :exe="enableTrash" :total='total'/>
      </template>

    </ms-node-tree>

    <ms-add-basis-scenario
      @saveAsEdit="saveAsEdit"
      @refresh="refresh"
      ref="basisScenario"/>

    <api-import ref="apiImport" :moduleOptions="data" @refreshAll="$emit('refreshAll')"/>
  </div>

</template>

<script>
  import SelectMenu from "../../../track/common/SelectMenu";
  import MsAddBasisScenario from "@/business/components/api/automation/scenario/AddBasisScenario";
  import MsNodeTree from "../../../track/common/NodeTree";
  import {buildNodePath, buildTree} from "../../definition/model/NodeTree";
  import ModuleTrashButton from "../../definition/components/module/ModuleTrashButton";
  import ApiImport from "./common/ScenarioImport";
  import MsSearchBar from "@/business/components/common/components/search/MsSearchBar";
  import {getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: 'MsApiScenarioModule',
    components: {
      MsSearchBar,
      ApiImport,
      ModuleTrashButton,
      MsNodeTree,
      MsAddBasisScenario,
      SelectMenu,
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
      getIsRelevance(){
        if(this.pageSource !== 'scenario'){
          return this.openType;
        }else {
          return "scenario";
        }
      }
    },
    data() {
      return {
        openType: 'relevance',
        result: {},
        condition: {
          filterText: "",
          trashEnable: false
        },
        data: [],
        currentModule: undefined,
        operators: [
          {
            label: this.$t('api_test.automation.add_scenario'),
            callback: this.addScenario,
            permissions: ['PROJECT_API_SCENARIO:READ+CREATE']
          },
          {
            label: this.$t('api_test.api_import.label'),
            callback: this.handleImport,
            permissions: ['PROJECT_API_SCENARIO:READ+IMPORT_SCENARIO']
          },
          {
            label: this.$t('report.export'),
            children: [
              {
                label: this.$t('report.export_to_ms_format'),
                permissions: ['PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO'],
                callback: () => {
                  this.exportAPI();
                }
              },
              {
                label: this.$t('report.export') + 'JMETER 格式',
                permissions: ['PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO'],
                callback: () => {
                  this.$emit('exportJmx');
                }
              }
            ]
          }
        ]
      }
    },
    mounted() {
      this.list();
    },
    watch: {
      'condition.filterText'(val) {
        this.$refs.nodeTree.filter(val);
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
      handleImport() {
        if (this.projectId) {
          this.result = this.$get("/api/automation/module/list/" + this.projectId, response => {
            if (response.data != undefined && response.data != null) {
              this.data = response.data;
              this.data.forEach(node => {
                buildTree(node, {path: ''});
              });
            }
          });
          this.$refs.apiImport.open(this.currentModule);
        }
      },
      list(projectId) {
        let url = undefined;
        if (this.isPlanModel) {
          url = '/api/automation/module/list/plan/' + this.planId;
        } else if (this.isRelevanceModel) {
          url = "/api/automation/module/list/" + this.relevanceProjectId;
        } else {
          url = "/api/automation/module/list/" + (projectId ? projectId : this.projectId);
          if (!this.projectId) {
            return;
          }
        }
        this.result = this.$get(url, response => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
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
      },
      edit(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/automation/module/edit", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      add(param) {
        param.projectId = this.projectId;
        param.protocol = this.condition.protocol;
        this.$post("/api/automation/module/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        }, (error) => {
          this.list();
        });

      },
      remove(nodeIds) {
        this.$post("/api/automation/module/delete", nodeIds, () => {
          this.list();
          this.refresh();
        }, (error) => {
          this.list();
        });
      },
      drag(param, list) {
        this.$post("/api/automation/module/drag", param, () => {
          this.$post("/api/automation/module/pos", list, () => {
            this.list();
          });
        }, (error) => {
          this.list();
        });
      },
      nodeChange(node, nodeIds, pNodes) {
        this.currentModule = node.data;
        this.condition.trashEnable = false;
        if (node.data.id === 'root') {
          this.$emit("nodeSelectEvent", node, [], pNodes);
        } else {
          this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
        }
      },
      exportAPI() {
        this.$emit('exportAPI', this.data);
      },
      // debug() {
      //   this.$emit('debug');
      // },
      saveAsEdit(data) {
        this.$emit('saveAsEdit', data);
      },
      refresh() {
        this.$emit("refreshTable");
      },
      addScenario() {
        if (!this.projectId) {
          this.$warning(this.$t('commons.check_project_tip'));
          return;
        }
        this.$refs.basisScenario.open(this.currentModule);
      },
      enableTrash() {
        this.condition.trashEnable = true;
        this.$emit('enableTrash', this.condition.trashEnable);
      }
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

  /deep/ .el-tree-node__content {
    height: 33px;
  }

  .ms-api-buttion {
    width: 30px;
  }

</style>
