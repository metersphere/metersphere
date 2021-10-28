<template>
  <div>
    <el-table
      v-for="(table, index) in tables"
      :key="index"
      :data="table.tableData"
      border
      size="mini"
      highlight-current-row>
      <el-table-column v-for="(title, index) in table.titles" :key="index" :label="title" min-width="150px">
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
  </div>
</template>

<script>
export default {
  name: "MsSqlResultTable",
  data() {
    return {
      tables: [],
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
    let rowArray = this.body.split("\n");
    // 过多会有性能问题
    if (rowArray.length > 100) {
      rowArray = rowArray.slice(0, 100);
    }
    this.getTableData(rowArray);
  },
  methods: {
    getTableData(rowArray) {
      let titles;
      let result = [];
      for (let i = 0; i < rowArray.length; i++) {
        let colArray = rowArray[i].split("\t");
        if (i === 0) {
          titles = colArray;
        } else {
          if (colArray.length != titles.length) {
            // 创建新的表
            if (colArray.length === 1 && colArray[0] === '') {
              this.getTableData(rowArray.slice(i + 1));
            } else {
              this.getTableData(rowArray.slice(i));
            }
            break;
          } else {
            let item = {};
            for (let j = 0; j < colArray.length; j++) {
              item[titles[j]] = (colArray[j] ? colArray[j] : "");
            }
            // 性能考虑每个表格取值不超过一百
            if (result.length < 100) {
              result.push(item);
            }
          }
        }
      }
      this.tables.splice(0, 0, {
        titles: titles,
        tableData: result
      });
    }
  }
}
</script>

<style scoped>

.el-table {
  margin-bottom: 20px;
}

.el-table >>> .cell {
  white-space: nowrap;
}

.table-content {
  cursor: pointer;
}

.el-container {
  overflow: auto;
  max-height: 500px;
}

</style>
