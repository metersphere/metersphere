<template>
  <div class="prerequisite-item-layout" v-if="data.stepModel === 'TEXT'">
    <h3>{{title}}</h3>
    <span>{{content}}</span>
  </div>
  <el-table v-else
    :data="data.steps" header-cell-class-name="case-desc-table-header-cell"
    style="width: 100%" class="case-desc-table">
    <el-table-column type="index" :label="$t('test_track.home.serial_number')" />
    <el-table-column prop="desc" :label="$t('test_track.case.step_desc')"/>
    <el-table-column prop="result" :label="$t('test_track.case.expected_results')"/>
  </el-table>
</template>

<script>
export default {
  name: "MsCaseDescTextItem",
  props: {
    data: Object,
    title: String,
    content: String
  }
}
</script>

<style scoped>
.prerequisite-item-layout h3{
  font-weight: bold;
  margin: 0 0 10px;
  font-size: 14px;
}

.case-desc-table {
  margin: 0 24px;
  width: 90%!important;
  max-height: calc(100vh - 244px);
}

:deep(.case-desc-table-header-cell) {
  font-family: 'PingFang SC';
  font-size: 14px;
  font-weight: 500;
  color: #646A73;
  line-height: 22px;
  align-items: center;
  flex: none;
  order: 0;
  flex-grow: 0;
}

.case-desc-table :deep(.el-table__body-wrapper) {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  flex-grow: 0;
  flex-shrink: 0;
  flex-basis: auto;
  order: 1;
  flex-grow: 0;
}
</style>
