<template>
  <ms-container>
    <ms-main-container>
      <el-card>

      </el-card>
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
    let reportId = this.$route.path.split('/')[4];
    console.log(reportId);
    let items = localStorage.getItem("compareReportIds");
    console.log(JSON.parse(items));
  },
  computed: {
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
