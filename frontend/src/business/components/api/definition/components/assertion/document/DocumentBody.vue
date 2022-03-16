<template>
  <div class="ms-border" style="margin-top: 10px">
    <div style="margin-bottom: 10px">
      <el-row :gutter="10" type="flex" justify="space-between" align="middle">
        <el-col>
           <span class="ms-import" @click="importData">
             <i class="el-icon-edit-outline" style="font-size: 16px"/>
             {{ $t('commons.import') }}
           </span>
          <span v-if="apiId!=='none'">
             <el-checkbox v-model="checked" @change="checkedAPI">{{ $t('commons.follow_api') }}</el-checkbox>
          </span>
        </el-col>
      </el-row>
    </div>
    <el-table
      :data="tableData"
      :span-method="objectSpanMethod"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      @cell-mouse-enter="editRow"
      @cell-mouse-leave="editLeave"
      row-key="id"
      border
      default-expand-all
      :height="300"
      v-loading="loading">

      <el-table-column prop="name" :label="$t('api_test.definition.request.esb_table.name')" width="230">
        <template slot-scope="scope">
          <el-input v-if="(scope.row.status && scope.column.fixed && scope.row.id!=='root') || (scope.row.type !=='object' && !scope.row.name)" v-model="scope.row.name" style="width: 140px" size="mini" :placeholder="$t('api_test.definition.request.esb_table.name')"/>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="include" width="78" :label="$t('api_test.request.assertions.must_contain')"
                       :scoped-slot="renderHeader">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.include" @change="handleCheckOneChange" :disabled="checked || scope.row.type==='array'"/>
        </template>
      </el-table-column>

      <el-table-column prop="typeVerification" width="100" :label="$t('api_test.request.assertions.type_verification')"
                       :scoped-slot="renderHeaderType">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.typeVerification" @change="handleCheckOneChange" :disabled="checked"/>
        </template>
      </el-table-column>

      <el-table-column prop="type" :label="$t('api_test.definition.request.esb_table.type')" width="120">
        <template slot-scope="scope">
          <el-select v-model="scope.row.type" :placeholder="$t('commons.please_select')" size="mini" @change="changeType(scope.row)" :disabled="checked">
            <el-option v-for="item in typeSelectOptions " :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </template>
      </el-table-column>

      <el-table-column prop="contentVerification" :label="$t('api_test.request.assertions.content_verification')"
                       width="200">
        <template slot-scope="scope">
          <el-select v-model="scope.row.contentVerification" :placeholder="$t('commons.please_select')" size="mini"
                     :disabled="checked">
            <el-option v-for="item in verificationOptions " :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column prop="expectedOutcome" :label="$t('api_test.request.assertions.expected_results')"
                       min-width="200">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status && scope.column.fixed" v-model="scope.row.expectedOutcome" size="mini"/>
          <span v-else>{{ scope.row.expectedOutcome }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="arrayVerification" width="140" :label="$t('api_test.request.assertions.check')"
                       :scoped-slot="renderHeaderArray">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.arrayVerification" @change="handleCheckOneChange"
                       v-if="scope.row.type==='array'" :disabled="checked"/>
        </template>
      </el-table-column>

      <el-table-column :label="$t('commons.operating')" width="130" fixed="right">
        <template v-slot:default="scope">
          <span>
            <el-tooltip effect="dark" :content="$t('api_test.request.assertions.add_check')" placement="top-start">
              <el-button icon="el-icon-document-checked" circle type="primary" size="mini"
                         @click="addVerification(scope.row)"
                         :disabled="scope.row.type ==='object' ||scope.row.type ==='array' || checked || scope.row.id==='root'"/>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('api_test.request.assertions.add_subfield')" placement="top-start">
              <el-button icon="el-icon-plus" circle type="primary" size="mini" style="margin-left: 10px"
                         @click="addRow(scope.row)"
                         :disabled="(scope.row.type !=='object' && scope.row.type !=='array')  || checked"/>
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('commons.remove')" placement="top-start">
              <el-button icon="el-icon-delete" type="primary" circle size="mini" style="margin-left: 10px" @click="remove(scope.row)" :disabled="checked || scope.row.id==='root'"/>
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>
    <ms-document-import :document="document" @setJSONData="setJSONData" ref="import"/>
  </div>
</template>

<script>

import {getUUID} from "@/common/js/utils";

export default {
  name: "MsDocumentBody",
  components: {
    MsDocumentImport: () => import("./DocumentImport"),
  },
  props: {
    document: {},
    apiId: String,
    showOptionsButton: Boolean,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      verificationOptions: [
        {value: 'none', label: this.$t('api_test.request.assertions.none')},
        {value: 'value_eq', label: this.$t('api_test.request.assertions.value_eq')},
        {value: 'value_not_eq', label: this.$t('api_test.request.assertions.value_not_eq')},
        {value: 'value_in', label: this.$t('api_test.request.assertions.value_in')},
        {value: 'length_eq', label: this.$t('api_test.request.assertions.length_eq')},
        {value: 'length_not_eq', label: this.$t('api_test.request.assertions.length_not_eq')},
        {value: 'length_gt', label: this.$t('api_test.request.assertions.length_gt')},
        {value: 'length_lt', label: this.$t('api_test.request.assertions.length_lt')},
        {value: 'regular', label: this.$t('api_test.request.assertions.regular_match')}
      ],
      typeSelectOptions: [
        {value: 'object', label: 'object'},
        {value: 'array', label: 'array'},
        {value: 'string', label: 'string'},
        {value: 'integer', label: 'integer'},
        {value: 'number', label: 'number'},
        {value: 'boolean', label: 'boolean'},
      ],
      requiredSelectOptions: [
        {value: true, label: this.$t('commons.selector.required')},
        {value: false, label: this.$t('commons.selector.not_required')},
      ],
      checked: false,
      tableData: Array,
    }
  },
  created() {
    if (this.document.type === "JSON") {
      this.checked = this.document.data.jsonFollowAPI && this.document.data.jsonFollowAPI !== "false" ? true : false;
    } else if (this.document.type === "XML") {
      this.checked = this.document.data.xmlFollowAPI && this.document.data.xmlFollowAPI !== "false" ? true : false;
    }
    this.changeData();
  },
  watch: {
    'document.type'() {
      if (this.document.type === "JSON") {
        this.checked = this.document.data.jsonFollowAPI && this.document.data.jsonFollowAPI !== "false" ? true : false;
      } else if (this.document.type === "XML") {
        this.checked = this.document.data.xmlFollowAPI && this.document.data.xmlFollowAPI !== "false" ? true : false;
      }
      this.changeData();
    }
  },
  methods: {
    removeDoc() {
      this.$emit('remove');
    },
    setJSONData(data) {
      this.checked = false;
      this.document.data.jsonFollowAPI = "";
      this.document.data.xmlFollowAPI = "";
      this.tableData = data;
      if (this.document.type === "JSON") {
        this.document.data.json = this.tableData;
      } else if (this.document.type === "XML") {
        this.document.data.xml = this.tableData;
      }
    },
    checkedAPI() {
      this.document.data.jsonFollowAPI = "";
      this.document.data.xmlFollowAPI = "";
      this.changeData();
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    getCase() {
      let url = "/api/testcase/get/" + this.apiId;
      this.$get(url, response => {
        if (response.data) {
          this.getAPI(response.data.apiDefinitionId);
          if (this.document.type === "JSON") {
            this.document.data.jsonFollowAPI = response.data.apiDefinitionId;
          } else {
            this.document.data.xmlFollowAPI = response.data.apiDefinitionId;
          }
        }
      });
    },
    getAPI(id) {
      let url = "/api/definition/getDocument/" + (id ? id : this.apiId) + "/" + this.document.type;
      this.$get(url, response => {
        if (response.data) {
          this.tableData = response.data;
        }
      });
    },
    getDocument() {
      // 来自场景步骤，请求id为用例id
      if (this.document && this.document.nodeType && this.document.nodeType === "scenario") {
        this.getCase();
      } else {
        this.getAPI();
      }
    },
    changeData() {
      if (this.document.data) {
        this.tableData = [];
        if (this.document.type === "JSON") {
          this.document.data.jsonFollowAPI = this.checked ? this.apiId : "";
          if (this.document.data.jsonFollowAPI) {
            this.getDocument();
          } else {
            this.tableData = this.document.data.json;
          }
        } else if (this.document.type === "XML") {
          this.document.data.xmlFollowAPI = this.checked ? this.apiId : "";
          if (this.document.data.xmlFollowAPI) {
            this.getDocument();
          } else {
            this.tableData = this.document.data.xml;
          }
        }
        this.reload();
      }
      if (this.tableData && this.tableData.length > 0) {
        this.tableData.forEach(row => {
          if (row.name == null || row.name === "") {
            this.remove(row);
          }
        })
      }
    },
    objectSpanMethod({row, column, rowIndex, columnIndex}) {
      if (columnIndex === 0 || columnIndex === 1 || columnIndex === 2 || columnIndex === 3) {
        return {
          rowspan: row.rowspan,
          colspan: row.rowspan > 0 ? 1 : 0
        };
      }
    },
    changeType(row) {
      row.children = [];
    },
    getValue(key) {
      let value = "";
      this.verificationOptions.forEach(item => {
        if (key === item.value) {
          value = item.label;
        }
      })
      return value;
    },
    renderHeader(h, {column}) {
      return h(
        'span', [
          h('el-checkbox', {
            style: 'margin-right:5px;',
            disabled: this.checked,
            on: {
              change: this.handleCheckAllChange
            },
          }),
          h('span', column.label)
        ]
      )
    },
    renderHeaderType(h, {column}) {
      return h(
        'span', [
          h('el-checkbox', {
            style: 'margin-right:5px;',
            disabled: this.checked,
            on: {
              change: this.handleType
            }
          }),
          h('span', column.label)
        ]
      )
    },
    renderHeaderArray(h, {column}) {
      return h(
        'span', [
          h('el-checkbox', {
            style: 'margin-right:5px;',
            disabled: this.checked,
            on: {
              change: this.handleArray
            }
          }),
          h('span', column.label)
        ]
      )
    },
    childrenChecked(arr, type, val) {
      if (arr && arr.length > 0) {
        arr.forEach(item => {
          if (type === 1) {
            item.include = val
            if (item.type === 'array') {
              item.include = false;
            }
          }
          if (type === 2) {
            item.typeVerification = val
          }
          if (type === 3) {
            item.arrayVerification = val
          }
          if (item.children && item.children.length > 0) {
            this.childrenChecked(item.children, type, val);
          }
        })
      }
    },
    handleCheckAllChange(val) {
      if (this.checked) {
        return;
      }
      this.tableData.forEach(item => {
        item.include = val;
        if (item.type === 'array') {
          item.include = false;
        }
        this.childrenChecked(item.children, 1, val);
      })
    },
    handleType(val) {
      if (this.checked) {
        return;
      }
      this.tableData.forEach(item => {
        item.typeVerification = val;
        this.childrenChecked(item.children, 2, val);
      })
    },
    handleArray(val) {
      if (this.checked) {
        return;
      }
      this.tableData.forEach(item => {
        item.arrayVerification = val;
        this.childrenChecked(item.children, 3, val);
      })
    },
    handleCheckOneChange(val) {
    },
    importData() {
      this.$refs.import.openOneClickOperation();
    },
    remove(row) {
      this.removeTableRow(row);
    },
    addRow(row) {
      //首先保存当前行内容
      if (row.type !== "array") {
        row.type = "object";
      }
      let newRow = this.getNewRow();
      row.children.push(newRow);
    },
    verSet(arr, row) {
      // 第三条
      if (row.groupId) {
        const index = arr.findIndex(d => d.id === row.groupId);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan + 1;
        }
      } else if (row.rowspan > 0) {
        const index = arr.findIndex(d => d.id === row.id);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan + 1;
        }
      } else {
        row.rowspan = 2;
      }
      arr.forEach(item => {
        // 找到当前行的位置
        if (item.id === row.id) {
          let newRow = JSON.parse(JSON.stringify(row));
          newRow.id = getUUID();
          newRow.groupId = !row.groupId ? row.id : row.groupId;
          newRow.rowspan = 0;
          if (row.type !== "object" || row.type !== "array") {
            newRow.children = [];
          }
          const index = arr.findIndex(d => d.id === item.id);
          if (index !== -1) {
            arr.splice(index + 1, 0, newRow);
          } else {
            arr.push(newRow);
          }
        }
        if (item.children && item.children.length > 0) {
          this.verSet(item.children, row);
        }
      })
    },
    addVerification(row) {
      if (!row.groupId && row.rowspan == 0) {
        row.rowspan = 2;
      }
      this.verSet(this.tableData, row);
    },
    confirm(row) {
      this.validateRowData(row) ? row.status = '' : row.status;
      if (row.status === "") {
        return true;
      }
      return false;
    },
    getNewRow() {
      let row = {
        id: getUUID(),
        name: "",
        include: false,
        status: true,
        typeVerification: false,
        type: "string",
        groupId: "",
        rowspan: 1,
        arrayVerification: false,
        contentVerification: "none",
        expectedOutcome: "",
        children: []
      };
      return row;
    },
    validateRowData(row) {
      if (row.name == null || row.name === '') {
        this.$warning("参数名" + "不能为空，且不能包含英文小数点[.]");
        return false;
      } else if (row.name.indexOf(".") > 0) {
        this.$warning("参数名[" + row.name + "]不合法，不能包含英文小数点\".\"!");
        return false;
      } else if (row.type == null || row.type === '') {
        this.$warning("类型" + "不能为空!");
        return false;
      }
      return true;
    },
    editRow(row, column, cell) {
      if (this.checked) {
        return;
      }
      if (row.expectedOutcome === true) {
        row.expectedOutcome = "true";
      }
      if (row.expectedOutcome === false) {
        row.expectedOutcome = "false";
      }
      column.fixed = true;
      row.status = true;
    },
    editLeave(row, column, cell, event) {
      column.fixed = false;
      row.status = false;
    },
    removeVerSet(arr, row) {
      // 第三条
      if (!row.groupId) {
        const index = arr.findIndex(d => d.groupId === row.id);
        if (index !== -1) {
          // 把合并权限让出去
          arr[index].rowspan = row.rowspan - 1;
          arr[index].id = row.id;
          arr[index].groupId = "";
        }
      } else if (row.groupId) {
        const index = arr.findIndex(d => d.id === row.groupId);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan - 1;
        }
      } else if (row.rowspan > 1) {
        const index = arr.findIndex(d => d.id === row.id);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan - 1;
        }
      } else {
        row.rowspan = 1;
      }
      arr.forEach(item => {
        if (item.children && item.children.length > 0) {
          this.removeVerSet(item.children, row);
        }
      })
    },
    removeTableRow(row) {
      this.removeVerSet(this.tableData, row);
      this.removeFromDataStruct(this.tableData, row);
    },
    removeFromDataStruct(dataStruct, row) {
      if (dataStruct == null || dataStruct.length === 0) {
        return;
      }
      let rowIndex = dataStruct.indexOf(row);
      if (rowIndex >= 0) {
        dataStruct.splice(rowIndex, 1);
      } else {
        dataStruct.forEach(itemData => {
          if (itemData.children != null && itemData.children.length > 0) {
            this.removeFromDataStruct(itemData.children, row);
          }
        });
      }
    },
  }
}
</script>

<style scoped>
.ms-import {
  margin: 10px
}

.ms-import:hover {
  cursor: pointer;
  border-color: #783887;
}

.assertion-btn {
  text-align: center;
  width: 60px;
}
</style>
