<template>
  <el-card class="scenario-results">
    <!--<ms-scenario-result v-for="(scenario, index) in scenarios" :key="index" :scenario="scenario" :indexNumber="index"-->
    <!--v-on:requestResult="requestResult"/>-->
    <el-tree node-key="resourceId" :data="treeData" class="ms-tree ms-report-tree"
             :expand-on-click-node="false"
             highlight-current>
      <span slot-scope="{ node, data}" style="width: 99%" @click="nodeClick(node)">
        <ms-scenario-result :node="data"
                            v-on:requestResult="requestResult"/>

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

  .card-content {
    height: calc(100vh - 196px);
    overflow-y: auto;
  }

  .ms-scenario-input {
    width: 100%;
  }

  .ms-main-div {
    background-color: white;
  }

  .ms-debug-div {
    border: 1px #DCDFE6 solid;
    border-radius: 4px;
    margin-right: 20px;
  }

  .ms-scenario-button {
    margin-left: 20px;
    padding: 7px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
  }

  .ms-api-col {
    background-color: #7C3985;
    border-color: #7C3985;
    margin-right: 10px;
    color: white;
  }

  .ms-font {
    color: #303133;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
    font-size: 13px;
  }

  .ms-col-one {
    margin-top: 5px;
  }

  #fab {
    right: 90px;
    z-index: 5;
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

  .head {
    border-bottom: 1px solid #303133;
    color: #303133;
    font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
    font-size: 13px;
  }

  .ms-opt-btn {
    position: fixed;
    right: 50px;
    z-index: 1;
  }

  .ms-sc-variable-header >>> .el-dialog__body {
    padding: 0px 20px;
  }

  .alt-ico {
    font-size: 15px;
    margin: 5px 10px 0px;
    float: right;
    color: #8c939d;
  }

  .alt-ico:hover {
    color: black;
    cursor: pointer;
    font-size: 18px;
  }

  .scenario-name {
    display: inline-block;
    margin: 0 5px;
    overflow-x: hidden;
    padding-bottom: 0;
    text-overflow: ellipsis;
    vertical-align: middle;
    white-space: nowrap;
    width: 200px;
  }
</style>
