<template>
  <div style=" height:calc(100vh - 190px)">
    <el-tabs v-model="activeName" type="card" class="sample-tabs">
      <el-tab-pane :label="$t('plan.error_samples')" name="errorSample">
        <error-samples-table :error-samples="errorSamples"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('plan.all_samples')" name="allSample">
        <error-samples-table :error-samples="samples"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import ErrorSamplesTable from "./ErrorSamplesTable.vue";

export default {
  name: "SamplesTabs",
  components: {ErrorSamplesTable},
  data() {
    return {
      activeName: 'errorSample',
      errorSamples: {
        sampleCount: {},
        samples: {}
      },
    };
  },
  comments: {
    ErrorSamplesTable
  },
  props: ['samples'],
  created() {
    this.initErrorSamples();
  },
  activated() {
    this.initErrorSamples();
  },
  methods: {
    initErrorSamples() {
      this.errorSamples = {
        sampleCount: {},
        samples: {}
      };
      if (this.samples && this.samples.sampleCount) {
        for (let sampleName in this.samples.sampleCount) {
          let sampleCountObj = this.samples.sampleCount[sampleName];
          for (let code in sampleCountObj) {
            if (code !== '200') {
              if (!this.errorSamples.sampleCount[sampleName]) {
                this.errorSamples.sampleCount[sampleName] = {};
                this.errorSamples.samples[sampleName] = {};
              }
              this.errorSamples.sampleCount[sampleName][code] = this.samples.sampleCount[sampleName][code] || {};
              this.errorSamples.samples[sampleName][code] = this.samples.samples[sampleName][code] || [];
            }
          }
        }
      }
    },
  },
};
</script>
<style scoped>
.sample-tabs :deep(.el-tabs__nav) {
  float: right;
}
</style>
