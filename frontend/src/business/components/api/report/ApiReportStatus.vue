<template>
  <div>
    <el-tag size="mini" type="info" v-if="row.status === 'Starting'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="primary" effect="plain" v-else-if="row.status === 'Running'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="success" v-else-if="row.status === 'Success'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
      {{ showStatus(row.status) }}
    </el-tag>
    <el-tooltip placement="top" v-else-if="row.status === 'Error' && row.description" effect="light">
      <template v-slot:content>
        <div>{{row.description}}</div>
      </template>
      <el-tag size="mini" type="danger">
        {{ showStatus(row.status) }}
      </el-tag>
    </el-tooltip>
    <el-tag v-else-if="row.status === 'Error' && !row.description" size="mini" type="danger">
      {{ showStatus(row.status) }}
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
      showStatus(status) {
        return status === "unexecute" ? "NotExecute" :
          status.toLowerCase()[0].toUpperCase() + status.toLowerCase().substr(1);
      }
    }
  }
</script>

<style scoped>

</style>
