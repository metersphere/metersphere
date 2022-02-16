<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span v-if="!debug">
          <el-input v-if="nameIsEdit" size="mini" @blur="handleSave(report.name)" @keyup.enter.native="handleSaveKeyUp" style="width: 200px" v-model="report.name" maxlength="60" show-word-limit/>
          <span v-else>
            <router-link v-if="isSingleScenario" :to="{name: 'ApiAutomation', params: { dataSelectRange: 'edit:' + scenarioId }}">
              {{ report.name }}
            </router-link>
            <span v-else>
              {{ report.name }}
            </span>
            <i class="el-icon-edit" style="cursor:pointer" @click="nameIsEdit = true" @click.stop/>
          </span>
        </span>
        <span v-if="report.endTime || report.createTime">
          <span style="margin-left: 10px">{{$t('report.test_start_time')}}：</span>
          <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
          <span style="margin-left: 10px">{{$t('report.test_end_time')}}：</span>
          <span class="time"> {{ report.endTime | timestampFormatDate }}</span>
        </span>
        <el-button v-if="!isPlan && (!debug || exportFlag) && !isTemplate" v-permission="['PROJECT_API_REPORT:READ+EXPORT']" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleExport(report.name)" style="margin-right: 10px">
          {{ $t('test_track.plan_view.export_report') }}
        </el-button>
        <el-button v-if="showCancelButton" class="export-button" plain  size="mini" @click="returnView()" >
          {{$t('commons.cancel')}}
        </el-button>
      </el-col>
    </el-row>
  </header>
</template>

<script>

export default {
  name: "MsApiReportViewHeader",
  props: {
    report: {},
    debug: Boolean,
    showCancelButton: {
      type: Boolean,
      default: true,
    },
    isTemplate: Boolean,
    exportFlag: {
      type: Boolean,
      default: false,
    },
    isPlan: Boolean
  },
  computed: {
    path() {
      return "/api/test/edit?id=" + this.report.testId;
    },
    scenarioId() {
      if (typeof this.report.scenarioId === 'string') {
        return this.report.scenarioId;
      } else {
        return "";
      }
    },
    isSingleScenario() {
      try {
        JSON.parse(this.report.scenarioId);
        return false;
      } catch (e) {
        return true;
      }
    }
  },
  data() {
    return {
      isReadOnly: false,
      nameIsEdit: false,
    }
  },
  created() {

  },
  methods: {
    handleExport(name) {
      this.$emit('reportExport', name);
    },
    handleSave(name) {
      this.nameIsEdit = false;
      this.$emit('reportSave', name);
    },
    handleSaveKeyUp($event) {
      $event.target.blur();
    },
    returnView(){
      this.$router.push('/api/automation/report');
    }
  }
}
</script>

<style scoped>

.export-button {
  float: right;
  margin-right: 10px;
}

</style>
