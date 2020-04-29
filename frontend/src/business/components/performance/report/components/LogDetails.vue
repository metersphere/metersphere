<template>
  <div>
    {{logContent}}
  </div>
</template>

<script>
  export default {
    name: "LogDetails",
    props: ['id', 'status'],
    data() {
      return {
        logContent: null
      }
    },
    methods: {
      initTableData() {
        this.$get("/performance/report/log/" + this.id, res => {
          this.logContent = res.data;
        })
      }
    },
    watch: {
      status() {
        if ("Completed" === this.status) {
          this.initTableData()
        }
      }
    },
  }
</script>

<style scoped>
  .logging-content {
    white-space: pre-line;
    overflow: auto;
  }

</style>
