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
    this.init();
  },
  watch: {
    body() {
      this.init();
    },
  },
  methods: {
    init() {
      if (!this.body) {
        return;
      }
      this.tables = [];
      this.titles = [];
      let rowArray = this.body.split('\n');
      // 过多会有性能问题
      if (rowArray.length > 100) {
        rowArray = rowArray.slice(0, 100);
      }
      this.getTableData(rowArray);
    },
    getTableData(rowArray) {
      if (!Array.isArray(rowArray) || rowArray.length === 0) {
        console.warn('Invalid input: rowArray should be a non-empty array.');
        return;
      }

      let titles = [];
      const result = [];

      for (let i = 0; i < rowArray.length; i++) {
        const colArray = rowArray[i].split('\t');

        if (i === 0) {
          // 第一行为标题行
          titles = colArray;
        } else if (colArray.length !== titles.length) {
          // 如果列数与标题长度不一致，递归解析剩余数据
          const remainingRows = rowArray.slice(i);
          this.getTableData(colArray.length === 1 && colArray[0] === '' ? remainingRows.slice(1) : remainingRows);
          break;
        } else {
          // 构建当前行的对象
          const item = titles.reduce((acc, title, index) => {
            acc[title] = colArray[index] || '';
            return acc;
          }, {});

          // 限制表格行数不超过 100 行
          if (result.length < 100) {
            result.push(item);
          }
        }
      }

      if (titles.length > 0 && result.length > 0) {
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
