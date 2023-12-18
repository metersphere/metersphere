<template>
  <div v-if="dialogVisible" class="batch-move" v-loading="result.loading">
    <el-dialog
               :visible.sync="dialogVisible"
               :before-close="close"
               :destroy-on-close="true"
               width="40%"
               append-to-body
               :close-on-click-modal="false">

      <el-tooltip :content="contentTitle" placement="top" width="width">
        <span class="tooltipStyle" v-if="selectCount > 1">将"{{ moveCaseTitle|ellipsis }}"等{{selectCount}}条{{moveType}} 移动到</span>
        <span class="tooltipStyle" v-else>将"{{ moveCaseTitle|ellipsis }}" 移动到</span>
      </el-tooltip>

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
                   @keydown.enter.native.prevent size="small" :disabled="!currentKey || btnDisable" style="margin-left: 12px">{{ $t('commons.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";

export default {
  name: "ReviewOrPlanBatchMove",
  components: {
    MsDialogFooter
  },
  data() {
    return {
      moveCaseTitle: '',
      moveType: '',
      treeNodes: [],
      selectIds: [],
      selectCount: 0,
      selectNode: {},
      dialogVisible: false,
      currentKey: "",
      moduleOptions: [],
      filterText: "",
      result: {},
      contentTitle:"",
      btnDisable: false
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
    open(caseTitle, treeNodes, selectIds, selectCount, moduleOptions, moveType) {
      this.moveCaseTitle = caseTitle;
      this.dialogVisible = true;
      this.treeNodes = treeNodes;
      this.selectIds = selectIds;
      this.selectCount = selectCount;
      this.moduleOptions = moduleOptions;
      this.moveType = moveType;
      this.contentTitle = this.$t('test_track.case.resource_batch_move_to', [this.moveCaseTitle, this.selectCount, this.moveType])
    },
    save() {
      this.btnDisable = true;
      if (!this.currentKey) {
        this.$warning(this.$t('test_track.case.input_module'), false);
        this.btnDisable = false;
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
      this.$emit('moveSave', param);
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
  },
  filters: {
    //文字数超出时，超出部分使用...
    ellipsis(value) {
      if (!value) {
        return '';
      }
      if (value.length > 25) {
        return value.slice(0, 25) + '...';
      }
      return value;
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

.tooltipStyle{
  overflow: hidden;
  text-overflow: ellipsis;
  -o-text-overflow: ellipsis;
  white-space:nowrap;
  width:100%;
  height:34px;
  display: inline-block;
  title:content;
  font-size: large;
}
</style>
