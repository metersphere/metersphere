<template>
  <el-tabs>
    <el-tab-pane :label="$t('load_test.pressure_config')">
      <performance-pressure-config :is-read-only="true" :test="test" :report="report" :report-id="reportId"
                                   :is-share="isShare" :share-id="shareId" @fileChange="fileChange"/>
    </el-tab-pane>
    <el-tab-pane :label="$t('load_test.advanced_config')">
      <performance-advanced-config :is-read-only="true" :report-id="reportId" :report="report" :is-share="isShare"
                                   :share-id="shareId" ref="advancedConfig"/>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
import PerformancePressureConfig from "@/business/components/performance/test/components/PerformancePressureConfig";
import PerformanceAdvancedConfig from "@/business/components/performance/test/components/PerformanceAdvancedConfig";
import PerformanceBasicConfig from "@/business/components/performance/test/components/PerformanceBasicConfig";

export default {
  name: "TestConfiguration",
  components: {PerformanceBasicConfig, PerformancePressureConfig, PerformanceAdvancedConfig},
  props: {
    test: Object,
    testId: String,
    reportId: String,
    report: Object,
    isShare: Boolean,
    shareId: String,
  },
  methods: {
    fileChange(threadGroups) {
      let csvSet = new Set;
      threadGroups.forEach(tg => {
        if (tg.csvFiles) {
          tg.csvFiles.map(item => csvSet.add(item));
        }
      });
      let csvFiles = [];
      for (const f of csvSet) {
        csvFiles.push({name: f, csvSplit: false, csvHasHeader: true});
      }

      this.$refs.advancedConfig.csvFiles = csvFiles;
    },
  }
};
</script>

<style scoped>

</style>
