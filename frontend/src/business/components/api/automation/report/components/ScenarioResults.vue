<template>
  <el-card class="scenario-results">
    <el-tree :data="treeData"
             :expand-on-click-node="false"
             :default-expand-all="defaultExpand"
             highlight-current
             class="ms-tree ms-report-tree" ref="resultsTree">
          <span slot-scope="{ node, data}" style="width: 99%" @click="nodeClick(node)">
            <ms-scenario-result :node="data" :console="console" v-on:requestResult="requestResult"/>
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
    defaultExpand: {
      default: false,
      type: Boolean,
    }
  },
  created() {
    if (this.$refs.resultsTree && this.$refs.resultsTree.root) {
      this.$refs.resultsTree.root.expanded = true;
    }
  },
  methods: {
    requestResult(requestResult) {
      this.$emit("requestResult", requestResult);
    },
    nodeClick(node) {
      node.expanded = !node.expanded;
    }
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

</style>
