<template>
  <div>
    <el-table
        ref="samplesTable"
        :data="tableData"
        border
        header-cell-class-name="sample-table-header"
        style="width: 100%">
      <el-table-column
          prop="name"
          label="Sample"
          min-width="180">
      </el-table-column>
      <el-table-column
          prop="count"
          label="Samples"
          min-width="180">
      </el-table-column>
      <el-table-column
          prop="error"
          label="Errors"
          min-width="180">
      </el-table-column>

      <el-table-column
          prop="percentOfErrors"
          label="% In Errors"
          min-width="180">
      </el-table-column>

      <el-table-column
          prop="percentOfSamples"
          label="% In Samples"
          min-width="180">
      </el-table-column>

      <el-table-column
          prop="code"
          label="Response Code"
          min-width="180">
        <template v-slot:default="scope">
          <span v-if="scope.row.code === '200' " style="color: #44b349">{{ scope.row.code }}</span>
          <span v-else style="color: #E6113C">{{ scope.row.code }}</span>
        </template>
      </el-table-column>

      <el-table-column
          :label="$t('commons.operating')"
          min-width="180">
        <template v-slot="scope">
          <el-link @click="openRecord(scope.row)">{{ $t('operating_log.info') }}</el-link>
        </template>
      </el-table-column>
    </el-table>
    <samples-drawer ref="sampleDrawer" :samples="samplesRecord"/>
  </div>
</template>

<script>
import SamplesDrawer from "./SamplesDrawer.vue";

export default {
  name: "ErrorSamplesTable",
  components: {SamplesDrawer},
  data() {
    return {
      id: '',
      drawer: false,
      tableData: [],
      samplesRecord: [],
      sampleRows: {},
    };
  },
  comments: {
    SamplesDrawer
  },
  props: ['errorSamples'],
  created() {
    this.initTableData();
  },
  methods: {
    initTableData() {
      if (this.errorSamples && this.errorSamples.sampleCount) {
        let allSampleCount = 0;
        let errorCount = 0;
        for (let sampleName in this.errorSamples.sampleCount) {
          let sampleCountObj = this.errorSamples.sampleCount[sampleName];
          let index = 0;
          for (let code in sampleCountObj) {
            let codeCount = sampleCountObj[code];
            let sampleTableData = {};
            sampleTableData.name = sampleName;
            sampleTableData.code = code;
            sampleTableData.count = codeCount;
            this.tableData.push(sampleTableData);
            index++;
            if (code !== '200') {
              errorCount += codeCount;
              sampleTableData.error = codeCount;
            } else {
              sampleTableData.error = 0;
            }
            allSampleCount += codeCount;
          }
          this.sampleRows[sampleName] = index;
        }
        this.tableData.forEach(item => {
          item.percentOfErrors = (item.error / errorCount * 100).toFixed(2) + '%';
          item.percentOfSamples = (item.count / allSampleCount * 100).toFixed(2) + '%';
        });
      } else {
        this.tableData = [];
      }
      this.$nextTick(() => {
        this.$refs.samplesTable.doLayout();
      }, 500)
    },
    objectSpanMethod({row, column, rowIndex, columnIndex}) {
      if (columnIndex === 0) {
        let rowspan = this.sampleRows[row.name];
        if (rowspan != 0) {
          this.sampleRows[row.name] = 0;
          return {
            rowspan: rowspan,
            colspan: 1,
          };
        } else {
          return {
            rowspan: 0,
            colspan: 1,
          };
        }
      }
    },
    handleClose(done) {
      done();
    },
    openRecord(row) {
      let drawerSamples = this.errorSamples.samples[row.name][row.code];
      this.$refs.sampleDrawer.openRecord(drawerSamples);
    },
  },
};
</script>
<style scoped>
.el-table :deep(.sample-table-header) {
  color: #1a1a1a;
}
</style>
