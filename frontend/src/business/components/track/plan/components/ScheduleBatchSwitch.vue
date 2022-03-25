<template>

  <el-dialog :visible.sync="dialogFormVisible"
             :before-close="close"
             :close-on-click-modal="false"
             :destroy-on-close="true"
             append-to-body
             width="30%">
    <div>
      <div style="margin-bottom: 8px;">
        <i class="el-icon-warning" style="color: #e6a23c;"/>
        <span v-if="size > 0 " style="margin-left: 4px; font-size: 14px">
          {{ $t('test_track.plan.check_schedule_enabled', [size]) }}
        </span>
        <span v-else style="margin-left: 4px; font-size: 14px">
          {{ $t('test_track.plan.no_check_schedule_enabled') }}
        </span>
      </div>
      <div v-if=" size  > 0 ">
        <el-switch
          v-model="schedule.enable"
          :inactive-text="$t('test_track.plan.batch_update_schedule_enable', [size])"
          active-color="#783887">
        </el-switch>
        <p style="font-size: 10px;color: gray;margin-bottom: -10px">
          {{ $t('test_track.plan.batch_update_schedule_enable_alert') }}
        </p>
      </div>
    </div>
    <template v-slot:footer v-if="size > 0">
      <ms-dialog-footer
        @cancel="close"
        @confirm="saveTest"/>
    </template>


  </el-dialog>

</template>

<script>
import MsDialogFooter from "../../../common/components/MsDialogFooter";
import {listenGoBack, removeGoBackListener} from "@/common/js/utils";


export default {
  components: {MsDialogFooter},
  data() {
    return {
      name: "MsTestPlanScheduleBatchSwitch",
      schedule: {
        enable: true,
        taskIds: [],
        selectAll: false,
        queryTestPlanRequest: {}
      },
      size: 0,
      selectRows: new Set(),
      allDataRows: new Set(),
      dialogFormVisible: false
    }
  },
  props: {
    treeNodes: {
      type: Array
    },
    currentProject: {
      type: Object
    }
  },
  methods: {
    open(ids, size, selectAll, condition) {
      if (selectAll) {
        this.schedule.selectAll = true;
        this.schedule.queryTestPlanRequest = condition;
      }
      this.size = size;
      this.schedule.taskIds = ids;
      listenGoBack(this.close);
      this.dialogFormVisible = true;
    },
    saveTest() {
      this.$post("/test/plan/schedule/Batch/updateEnable", this.schedule, () => {
        this.$success(this.$t('commons.modify_success'));
        this.close();
        this.$emit("refresh");
      });
    },
    close() {
      this.schedule = {
        enable: true,
        taskIds: [],
        selectAll: false,
        queryTestPlanRequest: {}
      };
      this.dialogFormVisible = false;
      removeGoBackListener(this.close);
    }
  }
}
</script>

<style scoped>
</style>
