<template>
  <ms-request-result
    :report-id="reportId"
    :is-test-plan="isTestPlan"
    :response="response"
  />
</template>

<script>
import MsRequestResult from './RequestResultTail';

export default {
  name: 'ApiResponseView',
  components: { MsRequestResult },
  computed: {
    reportId() {
      return this.getRouteParam('reportId');
    },
    isTestPlan() {
      return this.getRouteParam('isTestPlan');
    },
    response() {
      return this.getRouteParam('response');
    },
  },
  methods: {
    getRouteParam(name) {
      if (this.$route) {
        return this.$route.params[name];
      } else {
        return null;
      }
    },
  },
};
</script>

<style scoped></style>
