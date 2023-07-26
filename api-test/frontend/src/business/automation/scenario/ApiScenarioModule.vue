<template>
  <div>
    <slot name="header"></slot>

    <ms-node-tree
      v-loading="result"
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
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @filter="filter"
      @nodeSelectEvent="nodeChange"
      ref="nodeTree">
      <template v-slot:header>
        <ms-search-bar :show-operator="showOperator && !isTrashData" :condition="condition" :commands="operators" />
        <module-trash-button
          v-if="!isReadOnly && !isTrashData"
          :condition="condition"
          :exe="enableTrash"
          :total="total" />
      </template>
    </ms-node-tree>

    <ms-add-basis-scenario :module-options="data" @saveAsEdit="saveAsEdit" @refresh="refresh" ref="basisScenario" />

    <api-import ref="apiImport" :moduleOptions="data" @refreshAll="$emit('refreshAll')" />
  </div>
</template>

<script>
import SelectMenu from '@/business/commons/SelectMenu';
import MsAddBasisScenario from '@/business/automation/scenario/AddBasisScenario';
import MsNodeTree from '@/business/commons/NodeTree';
import { buildTree } from 'metersphere-frontend/src/model/NodeTree';
import ModuleTrashButton from '../../definition/components/module/ModuleTrashButton';
import ApiImport from './common/ScenarioImport';
import MsSearchBar from 'metersphere-frontend/src/components/search/MsSearchBar';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import {
  addScenarioModule,
  delScenarioModule,
  dragScenarioModule,
  editScenarioModule,
  getModuleByProjectId,
  getModuleByRelevanceProjectId,
  posScenarioModule,
  postModuleByProjectId,
  postModuleByTrash,
} from '@/api/scenario-module';

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
      },
    },
    showOperator: Boolean,
    relevanceProjectId: String,
    pageSource: String,
    total: Number,
    isTrashData: Boolean,
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
      },
    },
    selectProjectId: {
      type: String,
      default() {
        return getCurrentProjectID();
      },
    },
  },
  computed: {
    isRelevanceModel() {
      return this.relevanceProjectId ? true : false;
    },
    projectId() {
      if (this.selectProjectId) {
        return this.selectProjectId;
      } else {
        return getCurrentProjectID();
      }
    },
  },
  data() {
    return {
      result: false,
      condition: {
        filterText: '',
        trashEnable: false,
      },
      param: {},
      data: [],
      currentModule: undefined,
      operators: [
        {
          label: this.$t('api_test.automation.add_scenario'),
          callback: this.addScenario,
          permissions: ['PROJECT_API_SCENARIO:READ+CREATE'],
        },
        {
          label: this.$t('api_test.api_import.label'),
          callback: this.handleImport,
          permissions: ['PROJECT_API_SCENARIO:READ+IMPORT_SCENARIO'],
        },
        {
          label: this.$t('report.export'),
          children: [
            {
              label: this.$t('report.export_to_ms_format'),
              permissions: ['PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO'],
              callback: () => {
                this.exportAPI();
              },
            },
            {
              label: this.$t('report.export_jmeter_format'),
              permissions: ['PROJECT_API_SCENARIO:READ+EXPORT_SCENARIO'],
              callback: () => {
                this.$emit('exportJmx');
              },
            },
          ],
        },
      ],
    };
  },
  mounted() {
    this.list();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    relevanceProjectId() {
      this.list();
    },
  },
  created() {
    this.$EventBus.$on('scenarioConditionBus', (param) => {
      this.param = param;
      this.list()
    });
  },
  beforeDestroy() {
    this.$EventBus.$off('scenarioConditionBus', (param) => {
      this.param = param;
    });
  },
  methods: {
    handleImport() {
      if (this.projectId) {
        this.result = getModuleByProjectId(this.projectId).then((response) => {
          if (response.data != undefined && response.data != null) {
            this.data = response.data;
            this.data.forEach((node) => {
              buildTree(node, { path: '' });
            });
          }
        });
        this.$refs.apiImport.open(this.currentModule);
      }
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    list(projectId) {
      if (this.isRelevanceModel) {
        this.result = getModuleByRelevanceProjectId(this.relevanceProjectId).then((response) => {
          this.setData(response);
        });
      } else if (this.isTrashData) {
        this.result = postModuleByTrash(projectId ? projectId : this.projectId, this.param).then((response) => {
          this.setData(response);
        });
      } else {
        this.result = postModuleByProjectId(projectId ? projectId : this.projectId, this.param).then((response) => {
          this.setData(response);
        });
      }
    },
    setData(response) {
      if (response.data != undefined && response.data != null) {
        this.data = response.data;
        this.data.forEach((node) => {
          node.name = node.name === '未规划场景' ? this.$t('api_test.automation.unplanned_scenario') : node.name;
          buildTree(node, { path: '' });
        });
        this.$emit('setModuleOptions', this.data);
        this.$emit('setNodeTree', this.data);
        if (this.$refs.nodeTree) {
          this.$refs.nodeTree.filter(this.condition.filterText);
        }
      }
    },
    edit(param) {
      param.projectId = this.projectId;
      param.protocol = this.condition.protocol;
      editScenarioModule(param).then(
        () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
          this.refresh();
        },
        (error) => {
          this.list();
        }
      );
    },
    add(param) {
      param.projectId = this.projectId;
      param.protocol = this.condition.protocol;
      addScenarioModule(param).then(
        () => {
          this.$success(this.$t('commons.save_success'));
          this.list();
        },
        (error) => {
          this.list();
        }
      );
    },
    remove(nodeIds) {
      delScenarioModule(nodeIds).then(
        () => {
          this.list();
          this.refresh();
          this.removeModuleId(nodeIds);
        },
        (error) => {
          this.list();
        }
      );
    },
    drag(param, list) {
      dragScenarioModule(param).then(
        () => {
          posScenarioModule(list).then(() => {
            this.list();
          });
        },
        (error) => {
          this.list();
        }
      );
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      if (node.data.id === 'root') {
        this.$emit('nodeSelectEvent', node, [], pNodes);
      } else {
        this.$emit('nodeSelectEvent', node, nodeIds, pNodes);
      }
      // this.nohupReloadTree(node.data.id);
    },
    //后台更新节点数据
    nohupReloadTree(selectNodeId) {
      if (this.isRelevanceModel) {
        getModuleByRelevanceProjectId(this.relevanceProjectId).then((response) => {
          this.setModuleList(response, selectNodeId);
        });
      } else if (this.isTrashData) {
        if (!this.projectId) {
          return;
        }
        postModuleByTrash(this.projectId, this.param).then((response) => {
          this.setModuleList(response, selectNodeId);
        });
      } else {
        if (!this.projectId) {
          return;
        }
        postModuleByProjectId(this.projectId, this.param).then((response) => {
          this.setModuleList(response, selectNodeId);
        });
      }
    },
    setModuleList(response, selectNodeId) {
      if (response.data != undefined && response.data != null) {
        this.data = response.data;
        this.data.forEach((node) => {
          node.name = node.name === '未规划场景' ? this.$t('api_test.automation.unplanned_scenario') : node.name;
          buildTree(node, { path: '' });
        });

        this.$nextTick(() => {
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter(this.condition.filterText);
            if (selectNodeId) {
              this.$refs.nodeTree.justSetCurrentKey(selectNodeId);
            }
          }
        });
      }
    },
    exportAPI() {
      this.$emit('exportAPI', this.data);
    },
    saveAsEdit(data) {
      this.$emit('saveAsEdit', data);
    },
    refresh() {
      this.$emit('refreshAll');
    },
    addScenario() {
      if (!this.projectId) {
        this.$warning(this.$t('commons.check_project_tip'));
        return;
      }
      if (!this.currentModule) {
        this.$error(this.$t('test_track.case.input_module'));
      }
      this.$refs.basisScenario.open(this.currentModule);
    },
    enableTrash() {
      this.condition.trashEnable = true;
      this.param.trashEnable = true;
      this.param.name = '';
      this.param.combine = {};
      this.param.filters = { status: ['Trash'] };
      this.result = postModuleByTrash(this.projectId, this.param).then((response) => {
        this.setData(response);
      });
      this.$emit('enableTrash', this.condition.trashEnable);
    },
    removeModuleId(nodeIds) {
      if (localStorage.getItem('scenarioModule') && localStorage.getItem('scenarioModule') === nodeIds[0]) {
        localStorage.setItem('scenarioModule', undefined);
      }
    },
  },
};
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
</style>
