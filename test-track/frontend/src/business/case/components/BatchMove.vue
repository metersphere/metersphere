<template>
  <div v-if="dialogVisible" class="batch-move" v-loading="result.loading">
    <el-dialog :title="this.$t(isMoveBatch ? 'test_track.case.batch_move_to' : 'test_track.case.batch_copy_to', [moveCaseTitle, selectNum])"
               :visible.sync="dialogVisible"
               :before-close="close"
               :destroy-on-close="true"
               width="40%"
               append-to-body
               :close-on-click-modal="false">
      <el-input :placeholder="$t('test_track.module.search_by_name')" v-model="filterText" size="small" prefix-icon="el-icon-search"/>

      <el-scrollbar style="margin-top: 12px; border: 1px solid #DEE0E3; border-radius: 4px;">
        <div style="max-height: 336px; padding: 8px 16px 8px 16px;">
          <el-tree
            class="filter-tree node-tree"
            :data="treeNodes"
            node-key="id"
            :filter-node-method="filterNode"
            :expand-on-click-node="false"
            highlight-current
            style="overflow: auto"
            @node-click="nodeClick"
            ref="tree"
          >
            <template v-slot:default="{node}">
              <span>
                <span class="node-icon" style="position: relative; top: 3px">
                  <svg-icon :icon-class="node.isCurrent ? 'icon_folder_selected' : 'icon_folder'"/>
                </span>
                <span class="node-title">{{node.label}}</span>
              </span>
            </template>
          </el-tree>
        </div>
      </el-scrollbar>
      <template v-slot:footer>
        <el-button @click="close" size="small">{{ $t('commons.cancel') }}</el-button>
        <el-button v-prevent-re-click :type="!currentKey ? 'info' : 'primary'" @click="save"
                   @keydown.enter.native.prevent size="small" :disabled="!currentKey" style="margin-left: 12px">{{ $t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
  import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";

export default {
  name: "BatchMove",
  components: {
    MsDialogFooter
  },
  data() {
    return {
      moveCaseTitle: '',
      treeNodes: [],
      selectIds: [],
      selectNode: {},
      dialogVisible: false,
      currentKey: "",
      moduleOptions: [],
      filterText: "",
      result: {},
      isMoveBatch: false,
      selectNum: 0
    }
  },
  props: {
    publicEnable: {
      type: Boolean,
      default: false,
    },
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    open(isMoveBatch, caseTitle, treeNodes, selectNum, moduleOptions) {
      this.isMoveBatch = isMoveBatch;
      this.moveCaseTitle = caseTitle;
      this.dialogVisible = true;
      this.treeNodes = treeNodes;
      this.selectNum = selectNum;
      this.moduleOptions = moduleOptions;
    },
    save() {
      if (!this.currentKey) {
        this.$warning(this.$t('test_track.case.input_module'), false);
        return;
      }
      let param = {};
      param.nodeId = this.currentKey;
      if (this.moduleOptions) {
        this.moduleOptions.forEach(item => {
          if (item.id === this.currentKey) {
            param.nodePath = item.path;
          }
        });
      }
      param.ids = this.selectIds;
      if (this.publicEnable) {
        this.$emit('copyPublic', param);
      } else {
        this.$emit('moveSave', param);
      }
    },
    refresh() {
      this.$emit("refresh");
    },
    close() {
      this.filterText = "";
      this.dialogVisible = false;
      this.selectNode = {};
      this.currentKey = "";
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    nodeClick() {
      this.currentKey = this.$refs.tree.getCurrentKey();
    }
  }
}
</script>

<style scoped>
.node-title {
  width: 0;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1 1 auto;
  padding: 0 5px;
  overflow: hidden;
}

:deep(.el-tree-node__expand-icon.el-icon-caret-right:before) {
  color: #646A73;
  font-size: 15px;
}

:deep(.is-leaf.el-tree-node__expand-icon.el-icon-caret-right:before) {
  color: transparent;
}

:deep(.el-tree-node__content) {
  width: auto;
  height: 40px;
  border-radius: 4px;
}

:deep(.el-tree-node__content:hover){
  background-color: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content) {
  background-color: rgba(120, 56, 135, 0.1);
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content  .node-title) {
  color: #783887;
  font-weight: 500;
}

:deep(.el-tree--highlight-current .el-tree-node.is-current > .el-tree-node__content  .node-title:after) {
  color: #783887;
  font-weight: 500;
}

.svg-icon {
  width: 1.2em;
  height: 1.2em;
}

:deep(.el-button--small span) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: -5px;
}

.el-button--small {
  min-width: 80px;
  height: 32px;
  border-radius: 4px;
}
</style>
