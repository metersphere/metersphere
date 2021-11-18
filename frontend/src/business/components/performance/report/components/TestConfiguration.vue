<template>
  <el-tabs>
    <el-tab-pane :label="$t('load_test.pressure_config')">
      <performance-pressure-config :is-read-only="true" :test="test" :test-id="testId"/>
    </el-tab-pane>
    <el-tab-pane :label="$t('load_test.advanced_config')">
      <performance-advanced-config :is-read-only="true" :test-id="testId"/>
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
    report: Object,
    test: Object,
    testId: String,
  }
};
</script>

<style scoped>

</style>
