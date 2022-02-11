<template>

  <el-dialog :visible.sync="dialogFormVisible"
             :before-close="close"
             width="14%">
    <div>
      <el-switch
        v-model="schedule.enable"
        :inactive-text="$t('test_track.plan.batch_update_schedule_enable', [size])"
        active-color="#783887">
      </el-switch>
      <p style="font-size: 10px;color: gray;margin-bottom: -10px">
        {{ $t('test_track.plan.batch_update_schedule_enable_alert') }}</p>
    </div>
    <template v-slot:footer>
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
        taskIds: []
      },
      size: 0,
      selectRows: new Set(),
      allDataRows: new Set(),
      dialogFormVisible: false,
      test: {}
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
    open(ids, size) {
      if (size) {
        this.size = size;
      } else {
        this.size = this.$parent.selectDataCounts;
      }
      this.schedule.taskIds = ids;
      listenGoBack(this.close);
      this.dialogFormVisible = true;
    },
    saveTest() {
      this.$post("/test/plan/schedule/Batch/updateEnable", this.schedule, () => {
        this.$success(this.$t('commons.modify_success'));
        this.dialogFormVisible = false;
        this.$emit("refreshTable");
      });
    },
    close() {
      removeGoBackListener(this.close);
      this.dialogFormVisible = false;
    }
  }
}
</script>

<style scoped>
</style>
