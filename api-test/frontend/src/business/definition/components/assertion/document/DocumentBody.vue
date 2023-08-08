<template>
  <div class="ms-border" style="margin-top: 10px">
    <div style="margin-bottom: 10px">
      <el-row :gutter="10" type="flex" justify="space-between" align="middle">
        <el-col>
          <span class="ms-import" @click="importData">
            <i class="el-icon-edit-outline" style="font-size: 16px" />
            {{ $t('commons.import') }}
          </span>
          <span v-if="apiId !== 'none'">
            <el-checkbox v-model="checked" @change="checkedAPI" :disabled="isReadOnly">{{ $t('commons.follow_api') }}</el-checkbox>
          </span>
        </el-col>
      </el-row>
    </div>
    <el-table
      :data="tableData"
      :span-method="objectSpanMethod"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      @cell-mouse-enter="editRow"
      @cell-mouse-leave="editLeave"
      row-key="id"
      border
      lazy
      :key="tableId"
      :load="loadChild"
      :height="300"
      v-loading="loading"
      ref="table">
      <el-table-column prop="name" :label="$t('api_test.definition.request.esb_table.name')" width="230" fixed="left">
        <template slot-scope="scope">
          <el-input
            v-if="
              (scope.row.status && scope.column.fixed && scope.row.id !== 'root') ||
              (scope.row.type !== 'object' && !scope.row.name)
            "
            v-model="scope.row.name"
            style="width: 140px"
            size="mini"
            :placeholder="$t('api_test.definition.request.esb_table.name')"
            :disabled="isReadOnly"/>
          <el-input
            v-else
            :disabled="document.type === 'JSON' || isReadOnly"
            v-model="scope.row.name"
            style="width: 140px"
            size="mini"
            :placeholder="$t('api_test.definition.request.esb_table.name')"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="include"
        width="85"
        :label="$t('api_test.request.assertions.must_contain')"
        :disabled="isReadOnly"
        :render-header="renderHeader">
        <template slot-scope="scope">
          <el-checkbox
            v-model="scope.row.include"
            @change="handleCheckOneChange"
            :disabled="checked || scope.row.type === 'array' || isReadOnly" />
        </template>
      </el-table-column>

      <el-table-column
        prop="typeVerification"
        width="115"
        :label="$t('api_test.request.assertions.type_verification')"
        :render-header="renderHeaderType">
        <template slot-scope="scope">
          <el-checkbox v-model="scope.row.typeVerification" @change="handleCheckOneChange" :disabled="checked || isReadOnly" />
        </template>
      </el-table-column>

      <el-table-column prop="type" :label="$t('api_test.definition.request.esb_table.type')" width="120">
        <template slot-scope="scope">
          <el-select
            v-model="scope.row.type"
            :placeholder="$t('commons.please_select')"
            size="mini"
            @change="changeType(scope.row)"
            :disabled="checked || isReadOnly">
            <el-option v-for="item in typeSelectOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column
        prop="contentVerification"
        :label="$t('api_test.request.assertions.content_verification')"
        width="200">
        <template slot-scope="scope">
          <el-select
            v-model="scope.row.contentVerification"
            :placeholder="$t('commons.please_select')"
            size="mini"
            :disabled="checked || isReadOnly">
            <el-option v-for="item in verificationOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column
        prop="expectedOutcome"
        :label="$t('api_test.request.assertions.expected_results')"
        min-width="200">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status && scope.column.fixed" v-model="scope.row.expectedOutcome" size="mini" :disabled="isReadOnly"/>
          <span v-else>{{ scope.row.expectedOutcome }}</span>
        </template>
      </el-table-column>

      <el-table-column
        prop="arrayVerification"
        width="140"
        :label="$t('api_test.request.assertions.check')"
        :disabled="isReadOnly"
        :scoped-slot="renderHeaderArray">
        <template slot-scope="scope">
          <el-checkbox
            v-model="scope.row.arrayVerification"
            @change="handleCheckOneChange"
            v-if="scope.row.type === 'array'"
            :disabled="checked || isReadOnly" />
        </template>
      </el-table-column>

      <el-table-column :label="$t('commons.operating')" width="130" fixed="right">
        <template v-slot:default="scope">
          <span>
            <el-tooltip effect="dark" :content="$t('api_test.request.assertions.add_check')" placement="top-start">
              <el-button
                icon="el-icon-document-checked"
                circle
                type="primary"
                size="mini"
                @click="addVerification(scope.row)"
                :disabled="
                  scope.row.type === 'object' || scope.row.type === 'array' || checked || scope.row.id === 'root' || isReadOnly
                " />
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('api_test.request.assertions.add_subfield')" placement="top-start">
              <el-button
                icon="el-icon-plus"
                circle
                type="primary"
                size="mini"
                style="margin-left: 10px"
                @click="addRow(scope.row)"
                :disabled="(scope.row.type !== 'object' && scope.row.type !== 'array') || checked || isReadOnly" />
            </el-tooltip>
            <el-tooltip effect="dark" :content="$t('commons.remove')" placement="top-start">
              <el-button
                icon="el-icon-delete"
                type="primary"
                circle
                size="mini"
                style="margin-left: 10px"
                @click="remove(scope.row)"
                :disabled="checked || scope.row.id === 'root' || isReadOnly" />
            </el-tooltip>
          </span>
        </template>
      </el-table-column>
    </el-table>
    <ms-document-import :document="document" @setJSONData="setJSONData" ref="import" />
  </div>
</template>

<script>
import { getApiCaseById } from '@/api/api-test-case';
import { getApiDocument } from '@/api/definition';
import { getUUID } from 'metersphere-frontend/src/utils';
import {isSafeNumber, parse} from "lossless-json";

export default {
  name: 'MsDocumentBody',
  components: {
    MsDocumentImport: () => import('./DocumentImport'),
  },
  props: {
    document: {},
    apiId: String,
    showOptionsButton: Boolean,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      loading: false,
      verificationOptions: [
        { value: 'none', label: this.$t('api_test.request.assertions.none') },
        {
          value: 'value_eq',
          label: this.$t('api_test.request.assertions.value_eq'),
        },
        {
          value: 'value_not_eq',
          label: this.$t('api_test.request.assertions.value_not_eq'),
        },
        {
          value: 'value_in',
          label: this.$t('api_test.request.assertions.value_in'),
        },
        {
          value: 'length_eq',
          label: this.$t('api_test.request.assertions.length_eq'),
        },
        {
          value: 'length_not_eq',
          label: this.$t('api_test.request.assertions.length_not_eq'),
        },
        {
          value: 'length_gt',
          label: this.$t('api_test.request.assertions.length_gt'),
        },
        {
          value: 'length_lt',
          label: this.$t('api_test.request.assertions.length_lt'),
        },
        {
          value: 'regular',
          label: this.$t('api_test.request.assertions.regular_match'),
        },
      ],
      typeSelectOptions: [
        { value: 'object', label: 'object' },
        { value: 'array', label: 'array' },
        { value: 'string', label: 'string' },
        { value: 'integer', label: 'integer' },
        { value: 'number', label: 'number' },
        { value: 'boolean', label: 'boolean' },
      ],
      requiredSelectOptions: [
        { value: true, label: this.$t('commons.selector.required') },
        { value: false, label: this.$t('commons.selector.not_required') },
      ],
      checked: false,
      tableData: Array,
      originalData: Array,
      mapData: new Map(),
      tableId: '',
    };
  },

  created() {
    if (this.document.type === 'JSON') {
      this.checked = this.document.data.jsonFollowAPI && this.document.data.jsonFollowAPI !== 'false' ? true : false;
    } else if (this.document.type === 'XML') {
      this.checked = this.document.data.xmlFollowAPI && this.document.data.xmlFollowAPI !== 'false' ? true : false;
    }
    this.changeData();
  },
  watch: {
    'document.type'() {
      if (this.document.type === 'JSON') {
        this.checked = this.document.data.jsonFollowAPI && this.document.data.jsonFollowAPI !== 'false' ? true : false;
      } else if (this.document.type === 'XML') {
        this.checked = this.document.data.xmlFollowAPI && this.document.data.xmlFollowAPI !== 'false' ? true : false;
      }
      this.changeData();
    },
  },
  methods: {
    removeDoc() {
      this.$emit('remove');
    },
    setJSONData(data) {
      this.checked = false;
      this.document.data.jsonFollowAPI = '';
      this.document.data.xmlFollowAPI = '';
      this.tableId = getUUID();
      //处理第一层级数据children为null
      this.tableDataList(data);
      if (this.document.type === 'JSON') {
        this.document.data.json = this.originalData;
      } else if (this.document.type === 'XML') {
        this.document.data.xml = this.originalData;
      }
    },
    tableDataList(data) {
      this.$set(this.document, 'originalData', data);
      this.$set(this.document, 'tableData', this.mapData);
      this.originalData = data;
      this.tableData = JSON.parse(JSON.stringify(data)).map((item) => {
        // hasChildren 表示需要展示一个箭头图标
        item.hasChildren = item.children && item.children.length > 0;
        item.idList = [item.id];
        if (item.id === 'root') {
          this.$set(this.document, 'rootData', item);
        }
        item.children = [];
        return item;
      });
    },
    loadChild(tree, treeNode, resolve) {
      // 层级关系备份
      const idCopy = JSON.parse(JSON.stringify(tree.idList));

      // 查找下一层数据
      let resolveArr;
      if (tree.children.length === 0) {
        resolveArr = this.originalData;
        //找到最后一层children
        let id;
        while ((id = tree.idList.shift())) {
          const tarItem = resolveArr.find((item) => item.id === id);
          tarItem.loadedChildren = true;
          resolveArr = tarItem.children;
        }
      } else {
        resolveArr = tree;
        resolveArr = tree.children;
      }

      // 处理下一层数据的属性
      resolveArr = JSON.parse(JSON.stringify(resolveArr));
      resolveArr.forEach((item) => {
        item.hasChildren = item.children && item.children.length > 0;
        item.children = [];
        if (typeof item.expectedOutcome === 'string') {
          item.expectedOutcome = this.removeNumberFunctionFromString(item.expectedOutcome);
        }
        // 此处深拷贝，以防各个item的idList混乱
        item.idList = JSON.parse(JSON.stringify(idCopy));
        item.parentId = item.idList[item.idList.length - 1];
        item.idList.push(item.id);
      });

      // 标识已经加载子节点
      tree.loadedChildren = true;
      // 渲染子节点
      resolve(resolveArr);
      if (tree.id === 'root') {
        this.$set(this.document, 'rootData', tree);
      }
      this.mapData.set(tree.id, resolveArr);
    },
    checkedAPI() {
      this.document.data.jsonFollowAPI = '';
      this.document.data.xmlFollowAPI = '';
      this.changeData();
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    getCase() {
      getApiCaseById(this.apiId).then((response) => {
        if (response.data) {
          this.getAPI(response.data.apiDefinitionId);
          if (this.document.type === 'JSON') {
            this.document.data.jsonFollowAPI = response.data.apiDefinitionId;
          } else {
            this.document.data.xmlFollowAPI = response.data.apiDefinitionId;
          }
        }
      });
    },
    parseAndValidateNumber(value) {
      if (!isSafeNumber(value) || Number(value).toString().length < value.length) {
        // 大数、超长小数、科学计数法、小数位全为 0 等情况下，JS 精度丢失，所以需要用字符串存储
        return `Number(${value.toString()})`;
      }
      return Number(value);
    },
    removeNumberFunctionFromString(string) {
      const regex = /"?Number\(([\d.e+-]+)\)"?/g;
      return string.replace(regex, '$1');
    },
    getAPI(id) {
      getApiDocument(id ? id : this.apiId, this.document.type).then((response) => {
        if (response.data) {
          this.tableDataList(parse(response.data, undefined, this.parseAndValidateNumber));
        }
      });
    },
    getDocument() {
      // 来自场景步骤，请求id为用例id
      if (this.document && this.document.nodeType && this.document.nodeType === 'scenario') {
        this.getCase();
      } else if (
        this.document &&
        this.document.nodeType &&
        this.document.nodeType === 'Case' &&
        this.document.apiDefinitionId
      ) {
        this.getAPI(this.document.apiDefinitionId);
        if (this.document.type === 'JSON') {
          this.document.data.jsonFollowAPI = this.document.apiDefinitionId;
        } else {
          this.document.data.xmlFollowAPI = this.document.apiDefinitionId;
        }
      } else {
        this.getAPI();
      }
    },
    changeData() {
      if (this.document.data) {
        this.tableData = [];
        if (this.document.type === 'JSON') {
          this.document.data.jsonFollowAPI = this.checked ? this.apiId : '';
          if (this.document.data.jsonFollowAPI) {
            this.getDocument();
          } else {
            this.tableDataList(this.document.data.json);
          }
        } else if (this.document.type === 'XML') {
          this.document.data.xmlFollowAPI = this.checked ? this.apiId : '';
          if (this.document.data.xmlFollowAPI) {
            this.getDocument();
          } else {
            this.tableDataList(this.document.data.xml);
          }
        }
        if (!this.document.data.include) {
          this.document.data.include = false;
        }
        if (!this.document.data.typeVerification) {
          this.document.data.typeVerification = false;
        }
        this.reload();
      }
    },
    objectSpanMethod({ row, column, rowIndex, columnIndex }) {
      if (columnIndex === 0 || columnIndex === 1 || columnIndex === 2 || columnIndex === 3) {
        return {
          rowspan: row.rowspan,
          colspan: row.rowspan > 0 ? 1 : 0,
        };
      }
    },
    changeType(row) {
      if (this.mapData && this.mapData.has(row.id)) {
        this.clearChild(this.mapData.get(row.id));
        row.hasChildren = false;
        row.idList = [row.parentId, row.id];
      }
      this.changeChild(this.originalData, row);
    },
    clearChild(data) {
      data.forEach((item) => {
        this.remove(item);
      });
      if (data && data.length > 0) {
        this.clearChild(data);
      }
    },
    changeChild(data, row) {
      data.forEach((item) => {
        if (item.id === row.id) {
          item.children = [];
          row.hasChildren = false;
        } else {
          this.changeChild(item.children, row);
        }
      });
    },
    getValue(key) {
      let value = '';
      this.verificationOptions.forEach((item) => {
        if (key === item.value) {
          value = item.label;
        }
      });
      return value;
    },
    renderHeader(h, {column}) {
      return h('div', [
        h('span', column.label),
        h('el-checkbox', {
          style: 'margin-left:5px;',
          on: {
            change: this.handleCheckAllChange,
          },
          props: {
            checked: this.document.data.include,
            disabled: this.checked || this.isReadOnly,
          },
        }),
        h(
          'el-tooltip',
          {
            props: {
              content: this.$t('api_definition.document_tooltip'),
              placement: 'top' // 悬停内容展示的位置
            }
          },
          [
            h('i', {
              class: 'el-icon-question',
              style: 'margin-left:2px;'
            })
          ]
        )
      ]);
    },
    renderHeaderType(h, {column}) {
      return h('div', [
        h('span', column.label),
        h('el-checkbox', {
          style: 'margin-left:5px;',
          on: {
            change: this.handleType,
          },
          props: {
            checked: this.document.data.typeVerification,
            disabled: this.checked || this.isReadOnly,
          },
        }),
        h(
          'el-tooltip',
          {
            props: {
              content: this.$t('api_definition.document_tooltip'),
              placement: 'top' // 悬停内容展示的位置
            }
          },
          [
            h('i', {
              class: 'el-icon-question',
              style: 'margin-left:2px;'
            })
          ]
        )
      ]);
    },

    renderHeaderArray(h, { column }) {
      return h('span', [
        h('el-checkbox', {
          style: 'margin-right:5px;',
          disabled: this.checked || this.isReadOnly,
          on: {
            change: this.handleArray,
          },
        }),
        h('span', column.label),
      ]);
    },
    childrenChecked(arr, type, val) {
      if (arr && arr.length > 0) {
        arr.forEach((item) => {
          if (type === 1) {
            item.include = val;
            if (item.type === 'array') {
              item.include = false;
            }
          }
          if (type === 2) {
            item.typeVerification = val;
          }
          if (type === 3) {
            item.arrayVerification = val;
          }
          if (item.children && item.children.length > 0) {
            this.childrenChecked(item.children, type, val);
          }
        });
      }
    },
    handleCheckAllChange(val) {
      this.document.data.include = val;
      if (this.checked) {
        return;
      }
      this.tableData.forEach((item) => {
        item.include = val;
        if (item.type === 'array') {
          item.include = false;
        }
        this.childrenChecked(item.children, 1, val);
      });
      this.mapData.forEach((value, key) => {
        if (value && value.length > 0) {
          value.forEach((item) => {
            item.include = val;
            if (item.type === 'array') {
              item.include = false;
            } else {
              item.include = val;
            }
          });
        }
      });
    },
    handleType(val) {
      this.document.data.typeVerification = val;
      if (this.checked) {
        return;
      }
      this.tableData.forEach((item) => {
        item.typeVerification = val;
        this.childrenChecked(item.children, 2, val);
      });
      this.mapData.forEach((value, key) => {
        if (value && value.length > 0) {
          value.forEach((item) => {
            item.typeVerification = val;
          });
        }
      });
    },
    handleArray(val) {
      if (this.checked) {
        return;
      }
      this.originalData.forEach((item) => {
        item.arrayVerification = val;
        this.childrenChecked(item.children, 3, val);
      });
      this.tableDataList(this.originalData);
    },
    handleCheckOneChange(val) {},
    importData() {
      if (!this.isReadOnly){
        this.$refs.import.openOneClickOperation();
      }
    },
    remove(row) {
      this.removeTableRow(row);
    },
    addRow(row) {
      if (!row.idList && row.idList.length === 0) {
        row.idList.push(row.id);
      }
      //首先保存当前行内容
      if (row.type !== 'array') {
        row.type = 'object';
      }
      let newRow = this.getNewRow();
      newRow.idList = [row.id, newRow.id];
      newRow.parentId = row.id;
      if (this.mapData.has(row.id)) {
        this.mapData.get(row.id).push(newRow);
      } else {
        this.getChild(this.originalData, row);
        row.children.push(newRow);
        row.hasChildren = true;
        if (row.parentId) {
          let brotherRow = this.getNewRow();
          brotherRow.idList = [row.parentId, brotherRow.id];
          brotherRow.parentId = row.parentId;
          this.mapData.get(row.parentId).push(brotherRow);
          this.remove(brotherRow);
        }
      }
    },
    getChild(data, row) {
      data.forEach((item) => {
        if (item.id === row.id) {
          row.children = item.children;
        } else {
          this.getChild(item.children, row);
        }
      });
    },

    verSet(arr, row) {
      // 第三条
      arr = this.mapData.get(row.idList[row.idList.length - 2]);
      if (row.groupId) {
        const index = arr.findIndex((d) => d.id === row.groupId);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan + 1;
        }
      } else if (row.rowspan > 0) {
        const index = arr.findIndex((d) => d.id === row.id);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan + 1; //
        }
      } else {
        row.rowspan = 2;
      }
      arr.forEach((item) => {
        // 找到当前行的位置
        if (item.id === row.id) {
          let newRow = JSON.parse(JSON.stringify(row));
          newRow.id = getUUID();
          newRow.groupId = !row.groupId ? row.id : row.groupId;
          newRow.rowspan = 0;
          newRow.idList.splice(row.idList.length - 1, 0, newRow.id);
          if (row.type !== 'object' || row.type !== 'array') {
            newRow.children = [];
          }
          const index = arr.findIndex((d) => d.id === item.id);
          if (index !== -1) {
            arr.splice(index + 1, 0, newRow);
          } else {
            arr.push(newRow);
          }
        }
      });
    },
    addVerification(row) {
      if (!row.groupId && row.rowspan == 0) {
        row.rowspan = 2;
      }
      this.verSet(this.tableData, row);
    },
    confirm(row) {
      this.validateRowData(row) ? (row.status = '') : row.status;
      if (row.status === '') {
        return true;
      }
      return false;
    },
    getNewRow() {
      let row = {
        id: getUUID(),
        name: '',
        include: false,
        status: true,
        typeVerification: false,
        type: 'string',
        groupId: '',
        rowspan: 1,
        arrayVerification: false,
        contentVerification: 'none',
        expectedOutcome: '',
        children: [],
      };
      return row;
    },
    validateRowData(row) {
      if (row.name == null || row.name === '') {
        this.$warning('参数名' + '不能为空，且不能包含英文小数点[.]');
        return false;
      } else if (row.name.indexOf('.') > 0) {
        this.$warning('参数名[' + row.name + ']不合法，不能包含英文小数点"."!');
        return false;
      } else if (row.type == null || row.type === '') {
        this.$warning('类型' + '不能为空!');
        return false;
      }
      return true;
    },
    editRow(row, column, cell) {
      if (this.checked) {
        return;
      }
      if (row.expectedOutcome === true) {
        row.expectedOutcome = 'true';
      }
      if (row.expectedOutcome === false) {
        row.expectedOutcome = 'false';
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
        const index = arr.findIndex((d) => d.groupId === row.id);
        if (index !== -1) {
          // 把合并权限让出去
          arr[index].rowspan = row.rowspan - 1;
          arr[index].id = row.id;
          arr[index].groupId = '';
        }
      } else if (row.groupId) {
        const index = arr.findIndex((d) => d.id === row.groupId);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan - 1;
        }
      } else if (row.rowspan > 1) {
        const index = arr.findIndex((d) => d.id === row.id);
        if (index !== -1) {
          arr[index].rowspan = arr[index].rowspan - 1;
        }
      } else {
        row.rowspan = 1;
      }
    },
    removeTableRow(row) {
      this.removeVerSet(this.mapData.get(row.parentId) ? this.mapData.get(row.parentId) : this.tableData, row);
      this.removeFromDataStruct(this.mapData.get(row.parentId) ? this.mapData.get(row.parentId) : this.tableData, row);
    },
    removeFromDataStruct(dataStruct, row) {
      if (dataStruct == null || dataStruct.length === 0) {
        return;
      }
      let rowIndex = dataStruct.indexOf(row);
      if (rowIndex >= 0) {
        dataStruct.splice(rowIndex, 1);
      }
    },
  },
};
</script>

<style scoped>
.ms-import {
  margin: 10px;
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
