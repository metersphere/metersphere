<template>
  <el-dropdown @command="handleCommand" class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
      <el-dropdown-item command="schedule" v-tester>{{ $t('api_test.automation.schedule') }}</el-dropdown-item>
    </el-dropdown-menu>
    <ms-reference-view ref="viewRef"/>
    <ms-schedule-maintain ref="scheduleMaintain" />
  </el-dropdown>
</template>

<script>
  import MsReferenceView from "@/business/components/api/automation/scenario/ReferenceView";
  import MsScheduleMaintain from "@/business/components/api/automation/schedule/ScheduleMaintain"

  export default {
    name: "MsScenarioExtendButtons",
    components: { MsReferenceView,MsScheduleMaintain},
    props: {
      row: Object
    },
    methods: {
      handleCommand(cmd) {
        switch (cmd) {
          case  "ref":
            this.$refs.viewRef.open(this.row);
            break;
          case "schedule":
            this.$refs.scheduleMaintain.open(this.row);
            break;
        }
      },
    }
  }
</script>

<style scoped>
  .scenario-ext-btn {
    margin-left: 10px;
  }
</style>
