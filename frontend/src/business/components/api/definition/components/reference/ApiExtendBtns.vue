<template>
  <el-dropdown @command="handleCommand" class="scenario-ext-btn">
    <el-link type="primary" :underline="false">
      <el-icon class="el-icon-more"></el-icon>
    </el-link>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item command="ref">{{ $t('api_test.automation.view_ref') }}</el-dropdown-item>
      <!--<el-dropdown-item :disabled="isCaseEdit" command="add_plan">{{ $t('api_test.automation.batch_add_plan') }}</el-dropdown-item>-->
    </el-dropdown-menu>
    <ms-reference-view ref="viewRef"/>
    <!--测试计划-->
    <el-drawer :visible.sync="planVisible" :destroy-on-close="true" direction="ltr" :withHeader="false" :title="$t('test_track.plan_view.test_result')" :modal="false" size="90%">
      <ms-test-plan-list @addTestPlan="addTestPlan"/>
    </el-drawer>
  </el-dropdown>
</template>

<script>
  import MsReferenceView from "./ReferenceView";
  import MsTestPlanList from "../../../automation/scenario/testplan/TestPlanList";

  export default {
    name: "MsApiExtendBtns",
    components: {MsReferenceView, MsTestPlanList},
    props: {
      row: Object,
      isCaseEdit: Boolean,
    },
    data() {
      return {
        planVisible: false,
      }
    },

    methods: {
      handleCommand(cmd) {
        if (this.row.id) {
          switch (cmd) {
            case  "ref":
              this.$refs.viewRef.open(this.row);
              break;
            case  "add_plan":
              this.addCaseToPlan();
              break;
          }
        } else {
          this.$warning(this.$t('api_test.automation.save_case_info'))
        }
      },
      addCaseToPlan() {
        this.planVisible = true;
      },
      addTestPlan(plans) {
        let obj = {planIds: plans, apiIds: [this.row.id]};
        this.planVisible = false;
        this.$post("/api/automation/scenario/plan", obj, response => {
          this.$success(this.$t("commons.save_success"));
        });
      }
    }
  }
</script>

<style scoped>
  .scenario-ext-btn {
    margin-left: 10px;
  }
</style>
