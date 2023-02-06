<template>
  <div class="clearfix report-title">
    <el-row>
      <div class="report-left">
        <div class="title">
          【{{ type }}】- {{ title }}
          <span v-if="report && (report.endTime || report.createTime)">
            <span style="margin-left: 10px">{{ $t("report.test_start_time") }}：</span>
            <span class="time"> {{ report.createTime | datetimeFormat }}</span>
            <span style="margin-left: 10px">{{ $t("report.test_end_time") }}：</span>
            <span class="time"> {{ report.endTime | datetimeFormat }}</span>
          </span>
        </div>
      </div>
      <div class="report-right">
        <img class="logo" src="/assets/logo-MeterSphere.png"/>
      </div>
    </el-row>
    <el-row type="flex" style="margin-top: 5px">
      <el-col v-if="this.mode">
        <span> {{ $t("report.run_model") + ":" }} </span>
        <span style="color: #61c550; ">
            {{ getModeName(this.mode) }}
          </span>
      </el-col>
      <el-col v-if="this.poolName">
        <span> {{ $t("load_test.select_resource_pool") + ":" }} </span>
        <span style="color: #61c550; ">
            {{ this.poolName }}
          </span>
      </el-col>
    </el-row>
    <el-row v-if="showProjectEnv" style="margin-top: 5px">
      <el-col>
        <span> {{ $t("commons.environment") + ":" }} </span>
        <span
          v-for="(values, key) in projectEnvMap"
          :key="key"
          style="margin-right: 10px">
        {{ key + ":" }}
        <ms-tag
          v-for="(item, index) in values"
          :key="index"
          type="success"
          :content="item"
          style="margin-left: 2px"
        />
      </span>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsTag from "../MsTag";

export default {
  name: "MsReportTitle",
  components: {MsTag},
  props: {
    title: String,
    type: String,
    report: Object,
    projectEnvMap: {},
    mode: String,
    poolName: String,
  },
  data() {
    return {};
  },
  computed: {
    showProjectEnv() {
      return this.projectEnvMap && JSON.stringify(this.projectEnvMap) !== "{}";
    },
  },
  methods: {
    getModeName(mode) {
      switch (mode) {
        case "serial":
          return this.$t("run_mode.serial");
        case "parallel":
          return this.$t("run_mode.parallel");
      }
    },
  },
};
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
  vertical-align: middle;
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
