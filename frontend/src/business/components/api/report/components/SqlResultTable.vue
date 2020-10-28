<template>
  <el-table
    :data="tableData"
    border
    size="mini"
    highlight-current-row>
    <el-table-column v-for="(title, index) in titles" :key="index" :label="title" min-width="15%">
      <template v-slot:default="scope">
        <el-popover
          placement="top"
          trigger="click">
          <el-container>
            <div>{{ scope.row[title] }}</div>
          </el-container>
          <span class="table-content" slot="reference">{{ scope.row[title] }}</span>
        </el-popover>
      </template>
    </el-table-column>
  </el-table>
</template>

<script>
    export default {
      name: "MsSqlResultTable",
      data() {
        return {
          tableData: [],
          titles: []
        }
      },
      props: {
        body: String
      },
      created() {
        if (!this.body) {
          return;
        }
        let rowArry = this.body.split("\n");
        let title;
        let result = [];
        for (let i = 0; i < rowArry.length; i++) {
          let colArray = rowArry[i].split("\t");
          if (i === 0) {
            title = colArray;
          } else {
            let item = {};
            for (let j = 0; j < colArray.length; j++) {
              item[title[j]] = (colArray[j] ? colArray[j] : "");
            }
            result.push(item);
          }
        }
        this.titles = title;
        this.tableData = result;
        this.tableData.splice(this.tableData.length - 3, 3);
      }
    }
</script>

<style scoped>

  .el-table >>> .cell {
    white-space: nowrap;
  }

  .table-content {
    cursor: pointer;
  }

  .el-container {
    overflow:auto;
    max-height: 500px;
  }

</style>
