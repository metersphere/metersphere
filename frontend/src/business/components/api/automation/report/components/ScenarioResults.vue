<template>
  <el-card class="scenario-results">
    <div v-if="errorReport > 0">
      <el-tooltip :content="$t('api_test.automation.open_expansion')" placement="top" effect="light">
        <i class="el-icon-circle-plus-outline  ms-open-btn ms-open-btn-left" v-prevent-re-click @click="openExpansion"/>
      </el-tooltip>
      <el-tooltip :content="$t('api_test.automation.close_expansion')" placement="top" effect="light">
        <i class="el-icon-remove-outline ms-open-btn" size="mini" @click="closeExpansion"/>
      </el-tooltip>
    </div>
    <el-tree :data="treeData"
             :expand-on-click-node="false"
             :default-expand-all="defaultExpand"
             :filter-node-method="filterNode"
             highlight-current
             class="ms-tree ms-report-tree" ref="resultsTree">
          <span slot-scope="{ node, data}" style="width: 99%" @click="nodeClick(node)">
            <ms-scenario-result :node="data" :console="console" v-on:requestResult="requestResult"
                                :isActive="isActive"/>
          </span>
    </el-tree>
  </el-card>
</template>

<script>
import MsScenarioResult from "./ScenarioResult";

export default {
  name: "MsScenarioResults",
  components: {MsScenarioResult},
  props: {
    scenarios: Array,
    treeData: Array,
    console: String,
    errorReport: Number,
    defaultExpand: {
      default: false,
      type: Boolean,
    }
  },
  data() {
    return {
      isActive: false
    }
  },
  created() {
    if (this.$refs.resultsTree && this.$refs.resultsTree.root) {
      this.$refs.resultsTree.root.expanded = true;
    }
  },
  methods: {
    filterNode(value, data) {
      if (!data.value && data.children && data.children.length > 0) {
        return true;
      }
      if (!data.value && !data.children && data.children.length === 0) {
        return false;
      }
      if (!value) return true;
      if (data.value) {
        if (value === 'errorReport') {
          if (data.errorCode && data.errorCode !== "") {
            return true;
          }
        }else if (value === 'unexecute') {
          if(data.value.status === 'unexecute'){
            return true;
          }
        }else {
          if (!data.errorCode || data.errorCode === "") {
            return data.value.error > 0;
          }
        }
      }
      return false;
    },
    filter(val) {
      this.$nextTick(() => {
        this.$refs.resultsTree.filter(val);
      });
    },
    requestResult(requestResult) {
      this.$emit("requestResult", requestResult);
    },
    nodeClick(node) {
      node.expanded = !node.expanded;
    },
    // 改变节点的状态
    changeTreeNodeStatus(node) {
      node.expanded = this.expandAll
      for (let i = 0; i < node.childNodes.length; i++) {
        // 改变节点的自身expanded状态
        node.childNodes[i].expanded = this.expandAll
        // 遍历子节点
        if (node.childNodes[i].childNodes.length > 0) {
          this.changeTreeNodeStatus(node.childNodes[i])
        }
      }
    },
    closeExpansion() {
      this.isActive = false;
      this.expandAll = false;
      this.changeTreeNodeStatus(this.$refs.resultsTree.store.root);
    },
    openExpansion() {
      this.isActive = true;
      this.expandAll = true;
      // 改变每个节点的状态
      this.changeTreeNodeStatus(this.$refs.resultsTree.store.root)
    },
  }
}
</script>

<style scoped>
.scenario-results {
  height: 100%;
}

.ms-report-tree >>> .el-tree-node__content {
  height: 100%;
  vertical-align: center;
}

/deep/ .el-drawer__body {
  overflow: auto;
}

/deep/ .el-step__icon.is-text {
  border: 1px solid;
}

/deep/ .el-drawer__header {
  margin-bottom: 0px;
}

/deep/ .el-link {
  font-weight: normal;
}

/deep/ .el-checkbox {
  color: #303133;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  font-size: 13px;
  font-weight: normal;
}

/deep/ .el-checkbox__label {
  padding-left: 5px;
}

.ms-sc-variable-header >>> .el-dialog__body {
  padding: 0px 20px;
}

.ms-open-btn {
  margin: 5px 5px 0px;
  color: #6D317C;
  font-size: 20px;
}

.ms-open-btn:hover {
  background-color: #F2F9EE;
  cursor: pointer;
  color: #67C23A;
}

.ms-open-btn-left {
  margin-left: 30px;
}

</style>
