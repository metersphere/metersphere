<template>
  <el-row type="flex" justify="start" :gutter="20" class="status-button">
    <el-col v-for="item in statusButtons"
            :key="item.value">
      <el-button
        size="mini"
        :disabled="isReadOnly"
        :type="item.type"
        :icon="status == item.value ? 'el-icon-check' : ''"
        :style="item.style"
        @click="setStatus(item.value)">
        {{item.label}}
      </el-button>
    </el-col>
  </el-row>
</template>

<script>
  export default {
    name: "TestPlanTestCaseStatusButton",
    props: {
      status: {
        type: String
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    data() {
      return {
        statusButtons: [
          {
            type: 'success',
            value: 'Pass',
            label: this.$t('test_track.plan_view.pass')
          },
          {
            type: 'danger',
            value: 'Failure',
            label: this.$t('test_track.plan_view.failure')
          },
          {
            type: 'warning',
            value: 'Blocking',
            label: this.$t('test_track.plan_view.blocking')
          },
          {
            type: 'info',
            value: 'Skip',
            label: this.$t('test_track.plan_view.skip'),
            style: "background-color: #783887; border-color: #783887"
          },
          {
            type: 'info',
            value: 'Prepare',
            label: this.$t('test_track.plan.plan_status_prepare'),
            style: "background-color: #D7D7D7; border-color: #D7D7D7"
          },
        ]
      }
    },
    methods: {
      setStatus(status) {
        if (this.isReadOnly) {
          return;
        }
        this.$emit('statusChange', status);
      }
    }
  }
</script>

<style scoped>
</style>
