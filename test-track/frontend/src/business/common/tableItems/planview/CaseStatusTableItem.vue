<template>
    <span>
      <el-tag v-if="value === $t('test_track.case.status_prepare')" class="status-label prepare">
        {{ $t('test_track.case.status_prepare') }}
      </el-tag>
      <el-tag v-else-if="value === $t('test_track.case.status_running')" class="status-label underway">
        {{ $t('test_track.case.status_running') }}
      </el-tag>
      <el-tag v-else-if="value === $t('test_track.case.status_finished')" class="status-label pass">
        {{ $t('test_track.case.status_finished') }}
      </el-tag>
      <el-tag v-else class="status-label prepare">
        {{value}}
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
</style>
