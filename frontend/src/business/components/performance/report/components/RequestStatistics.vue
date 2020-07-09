<template>
  <div>
    <el-table
      :data="tableData"
      stripe
      border
      style="width: 100%"
      show-summary
      :summary-method="getSummaries"
    >
      <el-table-column label="Requests" fixed width="450" align="center">
        <el-table-column
          prop="label"
          label="Label"
          width="450"/>
      </el-table-column>

      <el-table-column label="Executions" align="center">
        <el-table-column
          prop="samples"
          label="Samples"
          sortable
          width="110"
        />

        <el-table-column
          prop="ko"
          label="KO%"
          align="center"
        />

        <el-table-column
          prop="error"
          label="Error%"
          align="center"
        />
      </el-table-column>

      <el-table-column label="Response Times(ms)" align="center">
        <el-table-column
          prop="average"
          label="Average"
        />
        <el-table-column
          prop="min"
          label="Min"
        />
        <el-table-column
          prop="max"
          label="Max"
        />
        <el-table-column
          prop="tp90"
          label="90% line"
        />
        <el-table-column
          prop="tp95"
          label="95% line"
        />
        <el-table-column
          prop="tp99"
          label="99% line"
        />
      </el-table-column>

      <el-table-column label="Throughput">
        <el-table-column
          prop="transactions"
          label="Transactions"
          width="100"
        />
      </el-table-column>

      <el-table-column label="NetWork(KB/sec)" align="center">
        <el-table-column
          prop="received"
          label="Received"
          align="center"
          width="200"
        />
        <el-table-column
          prop="sent"
          label="Sent"
          align="center"
          width="200"
        />
      </el-table-column>

    </el-table>
  </div>
</template>

<script>
  export default {
    name: "RequestStatistics",
    data() {
      return {
        tableData: [],
        id: ''
      }
    },
    methods: {
      initTableData() {
        this.$get("/performance/report/content/" + this.id).then(res => {
          this.tableData = res.data.data;
        }).catch(() => {
          this.tableData = [];
        })
      },
      getSummaries(param) {
        const {data} = param;
        const sums = []
        let allSamples = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.samples);
        }, 0);
        let failSize = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.ko);
        }, 0);
        let averageTimeTotal = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.average) * parseFloat(currentValue.samples);
        }, 0);
        let tp90Total = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.tp90) * parseFloat(currentValue.samples);
        }, 0);
        let tp95Total = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.tp95) * parseFloat(currentValue.samples);
        }, 0);
        let tp99Total = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.tp99) * parseFloat(currentValue.samples);
        }, 0);
        let transactions = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.transactions);
        }, 0);
        transactions = transactions.toFixed(2);
        let received = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.received);
        }, 0);
        received = received.toFixed(2);
        let sent = data.reduce(function (total, currentValue) {
          return total + parseFloat(currentValue.sent);
        }, 0);
        sent = sent.toFixed(2);

        let error = (Math.round(failSize / allSamples * 10000) / 100) + '%';
        let averageTime = (averageTimeTotal / allSamples).toFixed(2);
        let tp90 = (tp90Total / allSamples).toFixed(2);
        let tp95 = (tp95Total / allSamples).toFixed(2);
        let tp99 = (tp99Total / allSamples).toFixed(2);
        let min = Math.min.apply(Math, data.map(function (o) {
          return parseFloat(o.min)
        }));
        let max = Math.max.apply(Math, data.map(function (o) {
          return parseFloat(o.max)
        }));

        sums.push('Total', allSamples, failSize, error, averageTime, min, max, tp90, tp95, tp99, transactions, received, sent);

        return sums;

      }
    },
    watch: {
      report: {
        handler(val){
          let status = val.status;
          this.id = val.id;
          if (status === "Completed" || status === "Running") {
            this.initTableData();
          } else {
            this.tableData = [];
          }
        },
        deep:true
      }
    },
    props: ['report']
  }
</script>

<style scoped>

</style>
