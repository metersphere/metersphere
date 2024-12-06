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
          <el-popover placement="top" trigger="click">
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
  name: 'MsSqlResultTable',
  data() {
    return {
      tables: [],
      titles: [],
    };
  },
  props: {
    body: String,
  },
  created() {
    if (!this.body) {
      return;
    }
    let rowArray = this.body.split('\n');
    this.getTableData(rowArray);
    if (this.tables.length > 1) {
      for (let i = 0; i < this.tables.length; i++) {
        if (this.tables[i].titles.length === 1 && i < this.tables.length - 1) {
          this.tables[i].tableData.splice(this.tables[i].tableData.length - 1, 1);
        }
      }

      let lastTable = this.tables[this.tables.length - 1];
      if (lastTable.titles.length === 1) {
        if (lastTable.tableData.length > 4) {
          lastTable.tableData.splice(lastTable.tableData.length - 4, 4);
        } else {
          this.tables.splice(this.tables.length - 1, 1);
        }
      } else {
        this.tables.splice(this.tables.length - 1, 1);
      }
    } else {
      let table = this.tables[0];
      table.tableData.splice(table.tableData.length - 4, 4);
    }
  },
  methods: {
    getTableData(rowArray) {
      if (!Array.isArray(rowArray) || rowArray.length === 0) {
        console.warn('Input is not a valid array or is empty.');
        return;
      }

      let titles = [];
      const result = [];

      for (let i = 0; i < rowArray.length; i++) {
        const colArray = rowArray[i].split('\t');

        if (i === 0) {
          // 第一行为标题
          titles = colArray;
        } else if (colArray.length !== titles.length) {
          // 如果当前行与标题列数不一致，递归解析剩余数据
          const remainingRows = rowArray.slice(i);
          if (colArray.length === 1 && colArray[0] === '') {
            this.getTableData(remainingRows.slice(1)); // 跳过空行
          } else {
            this.getTableData(remainingRows);
          }
          break;
        } else {
          // 构造数据对象
          const item = titles.reduce((obj, title, index) => {
            obj[title] = colArray[index] || '';
            return obj;
          }, {});
          result.push(item);
        }
      }

      if (titles.length > 0) {
        // 插入到 tables 数组的开头
        this.tables.unshift({
          titles,
          tableData: result,
        });
      }
    },
  },
};
</script>

<style scoped>
.el-table {
  margin-bottom: 20px;
}

.el-table :deep(.cell) {
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
