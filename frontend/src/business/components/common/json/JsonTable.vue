<template>
  <div v-loading="loading">
    <el-table
      :data="tableData"
      style="width: 100%;margin-bottom: 20px;"
      row-key="id"
      default-expand-all
      @cell-click="editor"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column
        prop="name"
        label="名称">
        <template slot-scope="scope">
          <el-input v-if="scope.row.id === tabClickIndex && tabClickProperty === 'name'" v-model="scope.row.name" size="mini" style="padding-left: 20px" @blur="inputBlur(scope.row)"></el-input>
          <span v-else>{{scope.row.name}}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="required"
        label="必输项"
        align="center"
        width="80">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.required"/>
        </template>
      </el-table-column>
      <el-table-column
        prop="type"
        width="120px"
        label="类型">
        <template slot-scope="scope">
          <el-select v-model="scope.row.type" slot="prepend" size="small">
            <el-option v-for="item in typeData" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        prop="value"
        label="内容">
        <template slot-scope="scope">
          <el-input v-if="scope.row.id === tabClickIndex && tabClickProperty === 'value'" v-model="scope.row.value" size="mini"></el-input>
          <span v-else>{{scope.row.value}}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="describe"
        label="描述">
        <template slot-scope="scope">
          <el-input v-if="scope.row.id === tabClickIndex && tabClickProperty === 'describe'" v-model="scope.row.describe" size="mini"></el-input>
          <span v-else>{{scope.row.describe}}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="opt"
        label="操作"
        width="100">
        <template slot-scope="scope">
          <div>
            <i class="el-icon-setting" style="margin-left: 5px;cursor: pointer" @click="setting(scope.row)"/>
            <i class="el-icon-plus" style="margin-left: 5px;cursor: pointer" @click="add(scope.row)"/>
            <i class="el-icon-close" style="margin-left: 5px;cursor: pointer" @click="deleteRow(scope.row)"/>
          </div>
        </template>
      </el-table-column>

    </el-table>

  </div>

</template>
<script>
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "MsJsonTable",
    components: {},
    data() {
      return {
        loading: false,
        tableData: [{
          id: "root",
          parent: null,
          name: 'root',
          required: true,
          type: 'object',
          value: 'object',
          describe: 'describe',
          editor: false,
          children: [],
        }]
        ,
        tabClickProperty: "",
        tabClickIndex: "",
        typeData: [
          {id: 'string', label: 'string'},
          {id: 'number', label: 'number'},
          {id: 'array', label: 'array'},
          {id: 'object', label: 'object'},
          {id: 'boolean', label: 'boolean'},
          {id: 'integer', label: 'integer'}
        ]
      }
    },
    methods: {
      editor(row, column) {
        switch (column.property) {
          case 'name':
            this.tabClickIndex = row.id
            this.tabClickProperty = column.property
            break
          case 'value':
            this.tabClickIndex = row.id
            this.tabClickProperty = column.property
            break;
          case 'describe':
            this.tabClickIndex = row.id
            this.tabClickProperty = column.property
            break;
          default:
            return
        }
      },
      inputBlur() {
        this.tabClickIndex = null;
        this.tabClickProperty = '';
        this.reload();
      },
      reload() {
        this.loading = true;
        this.$nextTick(() => {
          this.loading = false
        })
      },
      setting() {

      },
      add(row) {
        let obj = {
          id: getUUID(),
          name: 'field',
          required: true,
          type: 'string',
          value: 'test',
          parent: null,
          describe: 'describe',
          children: [],
        }
        console.log("all", this.tableData)
        console.log("row", row)

        if (row.type === "object") {
          obj.parent = row.id;
          row.children.push(obj);
          return;
        }
        let parentRow = {};
        const index = this.tableData.findIndex(d => d.id != undefined && row.parent != undefined && d.id === row.parent);
        if (index != -1) {
          parentRow = this.tableData[index];
        } else {
          for (let i in this.tableData) {
            if (this.tableData[i].children) {
              parentRow = this.recursiveRemove(this.tableData[i].children, row);
            }
          }
        }
        console.log(parentRow)
        if (parentRow) {
          obj.parent = parentRow.id;
          parentRow.children.push(obj);
          return;
        }
        this.tableData.push(obj);
      },
      recursiveRemove(arr, row) {
        for (let i in arr) {
          const index = arr.findIndex(d => d.id != undefined && row.id != undefined && d.id === row.id)
          if (index != -1) {
            arr.splice(index, 1);
            return arr[i];
          }
          if (arr[i].children != undefined && arr[i].children.length > 0) {
            this.recursiveRemove(arr[i].children, row);
          }
        }
      },
      deleteRow(row) {
        const index = this.tableData.findIndex(d => d.id != undefined && row.id != undefined && d.id === row.id)
        if (index == -1) {
          this.tableData.forEach(item => {
            if (item.children) {
              this.recursiveRemove(item.children, row);
            }
          })
        } else {
          this.tableData.splice(index, 1);
        }
      }
    }
  }

</script>

<style scoped>
  .name-input >>> .el-input__inner {
    height: 25px;
    line-height: 25px;
  }
</style>
