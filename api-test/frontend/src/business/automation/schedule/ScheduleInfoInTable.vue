<template>
  <div v-if="schedule">
    <span v-if="schedule.scheduleStatus === 'OPEN'">
      <el-tooltip placement="bottom-start" effect="light">
        <div slot="content">
          {{ $t('home.table.run_rule') }}: {{ schedule.scheduleCorn }}<br />
          {{ $t('test_track.plan.next_run_time') }}：<span>{{ schedule.scheduleExecuteTime | datetimeFormat }}</span>
        </div>
        <el-switch
          @click.stop.native
          v-model="schedule.enable"
          inactive-color="#DCDFE6"
          @change="scheduleChange"
          :disabled="hasPermission">
        </el-switch>
      </el-tooltip>
    </span>
    <span v-else-if="schedule.scheduleStatus === 'SHUT'">
      <el-switch
        @click.stop.native
        v-model="schedule.enable"
        inactive-color="#DCDFE6"
        @change="scheduleChange"
        :disabled="hasPermission">
      </el-switch>
    </span>
    <span v-else>
      <el-link @click.stop="scheduleTask" :disabled="hasPermission" style="color: #783887">{{
        $t('schedule.not_set')
      }}</el-link>
    </span>

    <ms-schedule-maintain ref="scheduleMaintain" @refreshTable="refreshTable" :request="request" />
  </div>
</template>

<script>
import { operationConfirm } from 'metersphere-frontend/src/utils';
import MsScheduleMaintain from '@/business/automation/schedule/ScheduleMaintain.vue';

export default {
  name: 'ScheduleInfoInTable',
  components: { MsScheduleMaintain },
  props: {
    schedule: Object,
    hasPermission: Boolean,
    scenario: Object,
    request: {},
  },
  methods: {
    scheduleTask() {
      this.$emit('openSchedule');
      this.$refs.scheduleMaintain.open(this.scenario);
    },
    refreshTable() {
      this.$emit('refreshTable');
    },
    scheduleChange() {
      let message = this.$t('api_test.home_page.running_task_list.confirm.close_title');
      if (this.schedule.enable) {
        message = this.$t('api_test.home_page.running_task_list.confirm.open_title');
      }
      operationConfirm(
        this,
        message,
        () => {
          this.$emit('scheduleChange', this.schedule);
        },
        () => {
          this.schedule.enable = !this.schedule.enable;
        }
      );
    },
  },
};
</script>

<style scoped></style>
