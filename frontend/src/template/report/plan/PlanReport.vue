<template>

  <div>
    {{test}}
    <el-button type="primary" plain size="mini" @click="console.log('aaaa')">sdfds</el-button>
    <ms-pie-chart/>
  </div>


</template>

<script>


import MsPieChart from "../../../business/components/common/components/MsPieChart";
export default {
  name: "ApiDocumentsPage",
  components: {
    MsPieChart

  },
  data() {
    return {
      test: 'testaaa'
    }
  },
  props: {

  },
  created: function () {

  },
  watch: {
  },
  computed: {

  },
  methods: {

  },
}
</script>

<style scoped>

</style>
