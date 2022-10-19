<template>
  <div>
    <slot name="header"></slot>
    <ms-node-tree
      v-loading="result.loading"
      :tree-nodes="data"
      :allLabel="$t('project.all_file')"
      :type="isReadOnly ? 'view' : 'edit'"
      :delete-permission="['PROJECT_API_SCENARIO:READ+DELETE']"
      :add-permission="['PROJECT_API_SCENARIO:READ+CREATE']"
      :update-permission="['PROJECT_API_SCENARIO:READ+EDIT']"
      :default-label="$t('commons.module_title')"
      :show-case-num="showCaseNum"
      :operation_type_add="treeOperationType"
      :operation_type_edit="treeOperationType"
      @add="add"
      @edit="edit"
      @drag="drag"
      @remove="remove"
      @refresh="list"
      @filter="filter"
      @nodeSelectEvent="nodeChange"
      @addOperation="fileTreeModuleAdd"
      @editOperation="fileTreeModuleEdit"
      ref="nodeTree">
      <template v-slot:header>
        <ms-search-bar
          :show-operator="showOperator"
          :condition="condition"/>
        <ms-my-file :condition="condition" :exe="myFile" :total='total' v-if="loading"/>
      </template>
    </ms-node-tree>
    <file-module-dialog @refresh="list" ref="fileModuleDialog"/>
  </div>
</template>

<script>
import MsNodeTree from "./NodeTree.vue";
import {buildTree} from "metersphere-frontend/src/model/NodeTree";
import MsMyFile from "./MyFile";
import MsSearchBar from "metersphere-frontend/src/components/search/MsSearchBar";
import {getCurrentProjectID, getCurrentUserId} from "metersphere-frontend/src/utils/token";
import FileModuleDialog from "@/business/menu/file/dialog/FileModuleDialog";
import {
  createFileModule,
  deleteFileModule,
  dragModule,
  getFileMeta,
  getFileModule,
  modifyFileModule,
  posModule
} from "../../../../api/file";
import {hasLicense} from "metersphere-frontend/src/utils/permission";

export default {
  name: 'MsFileModule',
  components: {
    MsSearchBar,
    MsMyFile,
    MsNodeTree,
    FileModuleDialog,
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
    showCaseNum: {
      type: Boolean,
      default() {
        return true;
      }
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    treeOperationType() {
      let returnStr = hasLicense() ? 'external' : 'simple'
      return returnStr;
    }
  },
  data() {
    return {
      result: {},
      nodeLoading: false,
      total: 0,
      loading: true,
      condition: {
        filterText: "",
        trashEnable: false
      },
      data: [],
      currentModule: undefined,
    }
  },
  mounted() {
    this.myFiles();
    this.list();
  },
  watch: {
    'condition.filterText'() {
      this.filter();
    },
    relevanceProjectId() {
      this.myFiles();
      this.list();
    }
  },
  methods: {
    fileTreeModuleAdd(param) {
      this.$refs.fileModuleDialog.open('create', param);
    },
    fileTreeModuleEdit(data) {
      this.$refs.fileModuleDialog.open('edit', data);
    },
    reload() {
      this.loading = false
      this.$nextTick(() => {
        this.loading = true
      });
    },
    filter() {
      this.$refs.nodeTree.filter(this.condition.filterText);
    },
    myFiles() {
      this.nodeLoading = getFileMeta(getCurrentProjectID(), getCurrentUserId()).then(res => {
        this.total = res.data;
      });
    },
    list(projectId) {
      this.nodeLoading = getFileModule(projectId ? projectId : this.projectId).then(res => {
        if (res.data) {
          this.data = res.data;
          this.data.forEach(node => {
            node.name = node.name === 'DEF_MODULE' ? this.$t('commons.module_title') : node.name
            buildTree(node, {path: ''});
          });
          this.$emit('setModuleOptions', this.data);
          this.$emit('setNodeTree', this.data);
          if (this.$refs.nodeTree) {
            this.$refs.nodeTree.filter(this.condition.filterText);
          }
        }
      })
    },
    edit(param) {
      param.projectId = this.projectId;
      param.protocol = this.condition.protocol;
      modifyFileModule(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.list();
        this.refresh();
      }).catch(() => {
        this.list();
      });
    },
    add(param) {
      param.projectId = this.projectId;
      param.protocol = this.condition.protocol;
      createFileModule(param).then(() => {
        this.$success(this.$t('commons.save_success'));
        this.list();
      }).catch(() => {
        this.list();
      });
    },
    remove(nodeIds) {
      deleteFileModule(nodeIds).then(() => {
        this.list();
        this.refresh();
      }).catch(() => {
        this.list();
      });
    },
    drag(param, list) {
      dragModule(param).then(() => {
        posModule(list).then(() => {
          this.list();
        })
      }).catch(() => {
        this.list();
      });
    },
    nodeChange(node, nodeIds, pNodes) {
      this.currentModule = node.data;
      if (!this.condition.filters) {
        this.condition.filters = {createUser: []};
      } else {
        this.condition.filters.createUser = [];
      }
      this.reload();
      if (node.data.id === 'root') {
        this.$emit("nodeSelectEvent", node, [], pNodes);
      } else {
        this.$emit("nodeSelectEvent", node, nodeIds, pNodes);
      }
    },
    refresh() {
      this.myFiles();
      this.list(getCurrentProjectID());
    },
    myFile() {
      this.$nextTick(() => {
        let nodes = document.getElementsByClassName("el-tree-node");
        for (let i = 0, len = nodes.length; i < len; i++) {
          nodes[i].classList.remove("is-current");
        }
      });
      if (!this.condition.filters) {
        this.condition.filters = {createUser: [getCurrentUserId()]};
      } else {
        this.condition.filters.createUser = [getCurrentUserId()];
      }
      this.condition.filters.moduleIds = [];
      this.$emit("myFile");
      this.reload();
    }
  }
}
</script>

<style scoped>
.custom-tree-node {
  flex: 1 1 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
}

.node-operate > i {
  color: #409eff;
  margin: 0 5px;
}

:deep(.el-tree-node__content) {
  height: 33px;
}
</style>
