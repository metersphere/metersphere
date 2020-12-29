<template>
  <div v-loading="loading">
    <el-tabs v-model="activeName" @tab-click="tabChange">
      <el-tab-pane label="模版" name="apiTemplate">
        <el-button type="primary" size="mini">导入JSON</el-button>
        <el-table
          :data="tableData"
          style="width: 100%;margin-bottom: 20px;"
          class="ms-el-header"
          row-key="id"
          default-expand-all
          @cell-click="editor"
          :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
          <el-table-column
            prop="name"
            label="">
            <template slot-scope="scope">
              <el-input v-if="scope.row.id === tabClickIndex && tabClickProperty === 'name'" v-model="scope.row.name" size="mini" style="padding-left: 20px" @blur="inputBlur(scope.row)"></el-input>
              <span v-else>{{scope.row.name}}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="required"
            label=""
            align="center"
            width="80">
            <template slot-scope="scope">
              <el-checkbox v-model="scope.row.required"/>
            </template>
          </el-table-column>
          <el-table-column
            prop="type"
            width="120px"
            label="">
            <template slot-scope="scope">
              <el-select v-model="scope.row.type" slot="prepend" size="small" @change="typeChange(scope.row)">
                <el-option v-for="item in typeData" :key="item.id" :label="item.label" :value="item.id"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column
            prop="value"
            label="">
            <template slot-scope="scope">
              <el-autocomplete
                size="small"
                :disabled="false"
                class="input-with-autocomplete"
                v-model="scope.row.value"
                :fetch-suggestions="funcSearch"
                :placeholder="$t('api_test.value')"
                value-key="name"
                highlight-first-item
                @select="change">
                <i slot="suffix" class="el-input__icon el-icon-edit pointer" @click="advanced(scope.row)"></i>
              </el-autocomplete>
            </template>
          </el-table-column>

          <el-table-column
            prop="describe"
            label="">
            <template slot-scope="scope">
              <el-input v-if="scope.row.id === tabClickIndex && tabClickProperty === 'describe'" v-model="scope.row.describe" size="mini"></el-input>
              <span v-else>{{scope.row.describe}}</span>
            </template>
          </el-table-column>

          <el-table-column
            prop="opt"
            label=""
            width="100">
            <template slot-scope="scope">
              <div>
                <i class="el-icon-setting" style="margin-left: 5px;cursor: pointer" @click="setting(scope.row)"/>
                <el-tooltip v-if="scope.row.type==='object'" class="item-tabs" effect="dark" content="添加子节点" placement="top-start" slot="label">
                  <i class="el-icon-plus" style="margin-left: 5px;cursor: pointer" @click="add(scope.row)"/>
                </el-tooltip>
                <el-tooltip v-else class="item-tabs" effect="dark" content="添加兄弟节点" placement="top-start" slot="label">
                  <i class="el-icon-plus" style="margin-left: 5px;cursor: pointer" @click="add(scope.row)"/>
                </el-tooltip>
                <i class="el-icon-close" style="margin-left: 5px;cursor: pointer" v-if="scope.row.id!= 'root'" @click="deleteRow(scope.row)"/>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="预览" name="preview">
      </el-tab-pane>
    </el-tabs>

    <ms-api-variable-advance ref="variableAdvance" :current-item="currentItem" @advancedRefresh="advancedRefresh"/>

  </div>
</template>
<script>
  import {getUUID} from "@/common/js/utils";
  import {JMETER_FUNC, MOCKJS_FUNC} from "@/common/js/constants";
  import MsApiVariableAdvance from "../../api/definition/components/ApiVariableAdvance";

  export default {
    name: "MsJsonTable",
    components: {MsApiVariableAdvance},
    data() {
      return {
        loading: false,
        activeName: "apiTemplate",
        parameters: [],
        currentItem: null,
        tableData: [{
          id: "root",
          parent: null,
          name: 'root',
          required: true,
          type: 'object',
          value: 'value',
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
        if (row.id === 'root') {
          this.tabClickIndex = null;
          this.tabClickProperty = '';
          return;
        }
        switch (column.property) {
          case 'name':
            this.tabClickIndex = row.id
            this.tabClickProperty = column.property
            break
          case '':
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
      jsonItem() {
        let obj = {
          id: getUUID(),
          name: 'field',
          required: true,
          type: 'string',
          value: '',
          parent: null,
          describe: '',
          children: [],
        }
        return obj;
      },
      typeChange(row) {
        if (row.type === "array") {
          let obj = this.jsonItem();
          obj.name = obj.name + "_" + row.children.length;
          row.children.push(obj);
        } else {
          row.children = [];
        }
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
        let obj = this.jsonItem();
        if (row.type === "object") {
          obj.parent = row.id;
          obj.name = obj.name + "_" + row.children.length;
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
              parentRow = this.recursiveFind(this.tableData[i].children, row);
            }
          }
        }
        if (parentRow) {
          obj.parent = parentRow.id;
          obj.name = obj.name + "_" + parentRow.children.length;
          parentRow.children.push(obj);
          return;
        }
        obj.name = obj.name + "_" + this.tableData.length;
        this.tableData.push(obj);
      },
      recursiveFind(arr, row) {
        for (let i in arr) {
          const index = arr.findIndex(d => d.id != undefined && row.parent != undefined && d.id === row.parent)
          if (index != -1) {
            return arr[i];
          }
          if (arr[i].children != undefined && arr[i].children.length > 0) {
            this.recursiveFind(arr[i].children, row);
          }
        }
      },

      recursiveRemove(arr, row) {
        for (let i in arr) {
          const index = arr.findIndex(d => d.id != undefined && row.id != undefined && d.id === row.id)
          if (index != -1) {
            arr.splice(index, 1);
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
      },
      tabChange() {

      },
      funcSearch(queryString, cb) {
        let funcs = MOCKJS_FUNC.concat(JMETER_FUNC);
        let results = queryString ? funcs.filter(this.funcFilter(queryString)) : funcs;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      funcFilter(queryString) {
        return (func) => {
          return (func.name.toLowerCase().indexOf(queryString.toLowerCase()) > -1);
        };
      },
      change: function () {
        let isNeedCreate = true;
        let removeIndex = -1;
        this.parameters.forEach((item, index) => {
          if (!item.name && !item.value) {
            // 多余的空行
            if (index !== this.parameters.length - 1) {
              removeIndex = index;
            }
            // 没有空行，需要创建空行
            isNeedCreate = false;
          }
        });
      },
      advanced(item) {
        this.$refs.variableAdvance.open();
        this.currentItem = item;
      },
      advancedRefresh(item) {

      }
    }
  }

</script>

<style scoped>
  .name-input >>> .el-input__inner {
    height: 25px;
    line-height: 25px;
  }

  .ms-el-header >>> .el-table {
    padding: 0px 0;
  }

  /deep/ th {
    /* 去除头部*/
    padding: 0px 0;
  }

  /deep/ td {
    padding: 6px 0;
  }

  .advanced-item-value >>> .el-dialog__body {
    padding: 15px 25px;
  }

  .pointer {
    cursor: pointer;
    color: #1E90FF;
  }
</style>
