<template>
  <div>
    <el-tag size="mini" type="info" v-if="row.status === 'Starting'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="primary" effect="plain" v-else-if="row.status === 'Running'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="success" v-else-if="getStatus(row.status) === 'success'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="danger" v-else-if="getStatus(row.status) === 'error'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="danger" style="background-color: #F6972A; color: #FFFFFF" v-else-if="row.status === 'errorReportResult'">
      FakeError
    </el-tag>
    <el-tag v-else size="mini" type="info">
      {{ showStatus(row.status) }}
    </el-tag>
  </div>
</template>

<script>
  export default {
    name: "MsApiReportStatus",

    props: {
      row: Object
    },
    methods: {
      getStatus(status) {
        return status.toLowerCase();
      },
      showStatus(status) {
        return status === "unexecute"? "NotExecute":
          status.toLowerCase()[0].toUpperCase() + status.toLowerCase().substr(1);
      }
    }
  }
</script>

<style scoped>

</style>
