<template>
    <span>
      <el-tag v-if="value === 'Prepare'" class="status-label prepare">
        {{ $t('api_test.home_page.detail_card.unexecute') }}
      </el-tag>
      <el-tag v-else-if="value === 'Again'" class="status-label again">
         {{ $t('test_track.plan_view.again') }}
      </el-tag>
      <el-tag v-else-if="value === 'Underway'" class="status-label underway">
        {{ $t('test_track.plan.plan_status_running') }}
      </el-tag>
      <el-tag v-else-if="value === 'Pass' || value === 'Completed'" class="status-label pass">
        {{ $t('test_track.plan_view.pass') }}
      </el-tag>
      <el-tag v-else-if="value === 'UnPass'" class="status-label unPass">
        {{ $t('test_track.plan_view.not_pass') }}
      </el-tag>
      <el-tag v-else-if="value === 'STOP' || value === 'stop'" class="status-label stop">
        {{ $t('test_track.plan_view.stop') }}
      </el-tag>
      <el-tag v-else-if="value === 'Failure'" class="status-label failure">
        {{ $t('test_track.plan_view.failure') }}
      </el-tag>
      <el-tag v-else-if="value === 'Blocking'" class="status-label blocking">
        {{ $t('test_track.plan_view.blocking') }}
      </el-tag>
      <el-tag v-else-if="value === 'Skip'" class="status-label skip">
        {{ $t('test_track.plan_view.skip') }}
      </el-tag>
      <el-tag v-else-if="value === 'ErrorReportResult'" class="status-label errorReportResult">
        {{ $t('error_report_library.option.name') }}
      </el-tag>
      <el-tag v-else-if="value === 'ApiCaseStop' || value === 'ApiCasePrepare'" class="status-label apiCaseStop">
        {{ $t('api_test.home_page.detail_card.unexecute') }}
      </el-tag>
      <el-tag v-else class="status-label prepare">
        {{ value }}
      </el-tag>
    </span>
</template>

<script>
import MsTag from "metersphere-frontend/src/components/MsTag";

export default {
  name: "StatusTableItem",
  components: {MsTag},
  props: {
    value: {
      type: String
    }
  }
}
</script>

<style scoped>
.status-label {
  height: 24px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
}

.pass {
  background-color: rgba(52, 199, 36, 0.2);
  color: #34C724;
}

.underway {
  background-color: rgba(120, 56, 135, 0.2);
  color: #783887;
}

.prepare {
  background-color: rgba(31, 35, 41, 0.1);
  color: #646A73;
}

.failure, .unPass, .stop{
  background-color: rgba(245, 74, 69, 0.2);
  color: #D83931;
}

.skip {
  background-color: rgba(0, 214, 185, 0.2);
  color: #078372;
}

.blocking, .skip {
  background-color: rgba(255, 136, 0, 0.2);
  color: #DE7802;
}

.errorReportResult, .apiCaseStop{
  background: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.again {
  background-color: rgba(120, 56, 135, 0.2);
  color: #783887;
}
</style>
