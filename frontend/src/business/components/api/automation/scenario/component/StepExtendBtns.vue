<template>
  <div>
    <el-dropdown @command="handleCommand" class="scenario-ext-btn">
      <el-link type="primary" :underline="false">
        <el-icon class="el-icon-more"></el-icon>
      </el-link>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="copy">复制步骤</el-dropdown-item>
        <el-dropdown-item command="remove" v-tester>删除步骤</el-dropdown-item>
        <el-dropdown-item command="scenarioVar" v-tester>查看场景变量</el-dropdown-item>
        <el-dropdown-item command="openScenario" v-tester>打开场景</el-dropdown-item>
        <el-dropdown-item command="saveAs" v-tester>另存为接口定义</el-dropdown-item>
        <!--<el-tooltip content="Copy" placement="top">-->
        <!--<el-button size="mini" icon="el-icon-copy-document" circle @click="copyRow" :disabled="data && data.disabled"/>-->
        <!--</el-tooltip>-->
        <!--<el-tooltip :content="$t('commons.remove')" placement="top">-->
        <!--<el-button size="mini" icon="el-icon-delete" type="danger" circle @click="remove" :disabled="data && data.disabled"/>-->
        <!--</el-tooltip>-->

      </el-dropdown-menu>
    </el-dropdown>
    <ms-reference-view @openScenario="openScenario" ref="viewRef"/>
    <ms-schedule-maintain ref="scheduleMaintain" @refreshTable="refreshTable"/>

  </div>
</template>

<script>
  import MsReferenceView from "@/business/components/api/automation/scenario/ReferenceView";
  import MsScheduleMaintain from "@/business/components/api/automation/schedule/ScheduleMaintain"
  import {getCurrentProjectID, getUUID} from "@/common/js/utils";

  export default {
    name: "StepExtendBtns",
    components: {MsReferenceView, MsScheduleMaintain},
    props: {
      row: Object
    },
    methods: {
      handleCommand(cmd) {
        switch (cmd) {
          case  "copy":
            this.$emit('copy');
            break;
          case "remove":
            this.$emit('remove');
            break;
          case "scenarioVar":
            this.$emit('copy');
            break;
          case "openScenario":
            this.$emit('copy');
            break;
          case "saveAs":
            this.$emit('copy');
            break;
        }
      },
      createPerformance(row) {
        this.infoDb = false;
        let url = "/api/automation/genPerformanceTestJmx";
        let run = {};
        let scenarioIds = [];
        scenarioIds.push(row.id);
        run.projectId = getCurrentProjectID();
        run.ids = scenarioIds;
        run.id = getUUID();
        run.name = row.name;
        this.$post(url, run, response => {
          let jmxObj = {};
          jmxObj.name = response.data.name;
          jmxObj.xml = response.data.xml;
          jmxObj.attachFiles = response.data.attachFiles;
          jmxObj.attachByteFiles = response.data.attachByteFiles;
          this.$store.commit('setTest', {
            name: row.name,
            jmx: jmxObj
          })
          this.$router.push({
            path: "/performance/test/create"
          })
        });
      },
      openScenario(item) {
        this.$emit('openScenario', item)
      },
      refreshTable() {

      }
    }
  }
</script>

<style scoped>
  .scenario-ext-btn {
    margin-left: 10px;
  }
</style>
