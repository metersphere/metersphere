<template>
  <div class="report-title title">{{title}}</div>
</template>

<script>
    export default {
      name: "MsReportTitle",
      props: {title: String}
    }
</script>

<style scoped>

  .report-title {
    font-size: 30px;
    font-weight: bold;
    height: 50px;
    text-align: center;
    margin-bottom: 20px;
  }

</style>
