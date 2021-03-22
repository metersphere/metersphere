<template>
  <ms-container>
    <ms-main-container>
      sss
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import {checkoutTestManagerOrTestUser} from "@/common/js/utils";

export default {
  name: "PerformanceReportCompare",
  components: {MsMainContainer, MsContainer},
  mounted() {
    console.log(111);
  },
  computed: {
    queryDataType: function () {
      let reportIds = this.$route.params.reportIds;
      return reportIds;
    },
    isReadOnly() {
      return !checkoutTestManagerOrTestUser();
    }
  },
  data() {
    return {}
  },
  methods: {}

}
</script>

<style scoped>

</style>
