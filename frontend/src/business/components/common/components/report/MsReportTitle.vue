<template>
  <div class="clearfix report-title">
    <el-row>
      <div class="report-left">
        <div class="title">
          【{{ type }}】- {{ title }}
          <span v-if="report && (report.endTime || report.createTime)">
          <span style="margin-left: 10px">{{ $t('report.test_start_time') }}：</span>
          <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
          <span style="margin-left: 10px">{{ $t('report.test_end_time') }}：</span>
          <span class="time"> {{ report.endTime | timestampFormatDate }}</span>
        </span>
        </div>
      </div>
      <div class="report-right">
        <img class="logo" src="@/assets/logo-MeterSphere.png">
      </div>
    </el-row>
    <el-row v-if="showProjectEnv" type="flex">
      <span> {{ $t('commons.environment') + ':' }} </span>
      <div v-for="(values,key) in projectEnvMap" :key="key" style="margin-right: 10px">
        {{ key + ":" }}
        <ms-tag v-for="(item,index) in values" :key="index" type="success" :content="item"
                style="margin-left: 2px"/>
      </div>
    </el-row>
  </div>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";

export default {
  name: "MsReportTitle",
  components: {MsTag},
  props: {title: String, type: String, report: Object, projectEnvMap: {}},
  data() {
    return {}
  },
  computed: {
    showProjectEnv() {
      return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== '{}';
    },
  }
}
</script>

<style scoped>

.report-left {
  float: left;
}

.report-right {
  float: right;
}

.logo {
  height: 30px;
  line-height: 30px;
  vertical-align: middle
}


.report-left {
  font-size: 15px;
}

.time {
  margin-left: 10px;
}

.title {
  margin-bottom: 5px;
}


</style>
