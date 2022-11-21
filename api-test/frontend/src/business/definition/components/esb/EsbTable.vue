<template>
  <div class="ms-border">
    <el-table
      :data="tableData"
      @cell-dblclick="editRow"
      row-key="uuid"
      v-loading="loading"
      :expand-row-keys="expands"
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }">
      <el-table-column prop="esbName" :label="$t('api_test.definition.request.esb_table.name')" width="230">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.name" style="width: 140px"></el-input>
          <span v-else>{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" :label="$t('api_test.definition.request.esb_table.type')" width="120">
        <template slot-scope="scope">
          <el-select v-if="scope.row.status" v-model="scope.row.type" :placeholder="$t('commons.please_select')">
            <el-option v-for="item in typeSelectOptions" :key="item.value" :label="item.value" :value="item.value">
            </el-option>
          </el-select>
          <span v-else>{{ scope.row.type }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="length" :label="$t('api_test.definition.request.esb_table.length')" width="80">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.contentType"></el-input>
          <span v-else>{{ scope.row.contentType }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" :label="$t('api_test.definition.request.esb_table.desc')" min-width="200">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.description"></el-input>
          <span v-else>{{ scope.row.description }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="value" :label="$t('api_test.definition.request.esb_table.value')" width="180">
        <template slot-scope="scope">
          <el-input v-if="scope.row.status" v-model="scope.row.value"></el-input>
          <span v-else>{{ scope.row.value }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="showOptionsButton" :label="$t('commons.operating')" width="140" fixed="right">
        <template v-slot:default="scope">
          <span>
            <el-button
              size="mini"
              p="$t('commons.next_level')"
              icon="el-icon-plus"
              type="primary"
              circle
              @click="nextRow(scope.row)"
              class="ht-btn-confirm" />
            <el-button
              size="mini"
              p="$t('commons.copy')"
              icon="el-icon-copy-document"
              type="primary"
              circle
              @click="copyDataStructConfirm(scope.row)"
              class="ht-btn-confirm" />
            <el-button
              size="mini"
              p="$t('commons.remove')"
              icon="el-icon-close"
              circle
              @click="remove(scope.row)"
              class="ht-btn-remove" />
          </span>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="showOptionsButton">
      <el-button class="ht-btn-add" size="mini" p="$t('commons.add')" icon="el-icon-circle-plus-outline" @click="add">
        {{ $t('commons.add') }}
      </el-button>
      <el-button
        class="ht-btn-add"
        size="mini"
        p="$t('commons.add')"
        icon="el-icon-circle-plus-outline"
        @click="saveTableData"
        >{{ $t('commons.save') }}
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'EsbTable',
  components: {},
  props: {
    tableData: Array,
    showOptionsButton: Boolean,
  },
  data() {
    return {
      loading: false,
      expands: [],
      typeSelectOptions: [
        { value: 'object', label: '[object]' },
        { value: 'string', label: '[string]' },
        { value: 'array', label: '[array]' },
      ],
      requiredSelectOptions: [
        { value: true, label: '必填' },
        { value: false, label: '非必填' },
      ],
      tableExpandRowKayArray: ['name', 'systemName'],
    };
  },
  created() {
    if (this.tableData && this.tableData.length > 0) {
      this.tableData.forEach((row) => {
        if (row.name == null || row.name === '') {
          this.remove(row);
        }
      });
      this.expands.push(this.tableData[0].uuid);
    }
  },
  methods: {
    saveTableData: function () {
      this.$emit('saveTableData', this.tableData);
    },
    remove: function (row) {
      this.removeTableRow(row);
    },
    add: function (r) {
      let row = this.getNewRow(null);
      this.pushTableData(row);
    },
    nextRow: function (row) {
      //首先保存当前行内容
      let confirmRowFlag = this.confirm(row);
      if (confirmRowFlag) {
        let nextRow = this.getNewRow(null);
        this.pushTableData(nextRow, row.uuid);
      }
    },
    //复制当前数据结构
    copyDataStructConfirm: function (row) {
      this.$alert(this.$t('api_test.definition.request.esb_copy_confirm') + ' ？', '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.copyDataStruct(row);
          }
        },
      });
    },
    copyDataStruct: function (row) {
      let parentRow = this.selectParentRow(this.tableData, row.uuid);
      let newRow = this.createNewId(row);
      if (parentRow != null) {
        if (parentRow.children == null) {
          parentRow.children = [];
        }
        parentRow.children.push(newRow);
      } else {
        this.tableData.push(newRow);
      }
    },
    createNewId(row) {
      let newRow = this.getNewRow(row);
      if (row.children != null && row.children.length > 0) {
        row.children.forEach((child) => {
          let newChild = this.createNewId(child);
          newRow.children.push(newChild);
        });
      }
      return newRow;
    },
    selectParentRow(esbDataStruct, rowId) {
      let returnRow = null;
      if (esbDataStruct == null || esbDataStruct.length === 0) {
        return returnRow;
      }
      esbDataStruct.forEach((esbData) => {
        if (esbData.children != null && esbData.children.length > 0) {
          esbData.children.forEach((child) => {
            if (child.uuid === rowId) {
              returnRow = esbData;
              return returnRow;
            }
          });
        }
        if (returnRow == null) {
          if (esbData.children != null && esbData.children.length > 0) {
            returnRow = this.selectParentRow(esbData.children, rowId);
            if (returnRow != null) {
              return returnRow;
            }
          }
        }
      });
      return returnRow;
    },
    confirm: function (row) {
      this.validateRowData(row) ? (row.status = '') : row.status;
      if (row.status === '') {
        return true;
      }
      return false;
    },
    getNewRow(param) {
      if (param == null) {
        let row = {
          name: '',
          systemName: '',
          status: 'edit',
          type: '[object]',
          contentType: '',
          required: false,
          description: '',
          uuid: this.uuid(),
          children: [],
        };
        return row;
      } else {
        let row = {
          name: param.name,
          systemName: param.systemName,
          status: '',
          type: param.type,
          contentType: param.contentType,
          required: param.required,
          description: param.description,
          uuid: this.uuid(),
          children: [],
        };
        return row;
      }
    },
    validateRowData(row) {
      if (row.name == null || row.name === '') {
        // this.$warning(this.$t('load_test.input_ip'));
        this.$warning('ESB参数名' + '不能为空，且不能包含英文小数点[.]');
        return false;
      } else if (row.name.indexOf('.') > 0) {
        this.$warning('ESB参数名[' + row.name + ']不合法，不能包含英文小数点"."!');
        return false;
      } else if (row.type == null || row.type === '') {
        this.$warning('类型' + '不能为空!');
        return false;
        // }else if(row.required == null || row.required == '' ){
        // }else if(row.required != false && row.required != true){
        //   this.$warning("是否必填"+"不能为空!");
        //   return false;
      }
      return true;
    },
    editRow: function (row) {
      row.status = 'edit';
    },
    uuid: function () {
      return (((1 + Math.random()) * 0x100000) | 0).toString(16).substring(1);
    },
    pushTableData: function (dataRow, rowId) {
      if (rowId === '' || rowId == null) {
        if (this.tableData == null) {
          this.tableData = [];
        }
        this.tableData.push(dataRow);
      } else {
        this.appendDataWithDeepForeach(this.tableData, rowId, dataRow);
      }
    },
    appendDataWithDeepForeach(data, rowId, appendData) {
      data.forEach((row) => {
        if (row.uuid === rowId) {
          if (row.children == null) {
            row.children = [];
          }
          row.children.push(appendData);
        } else if (row.children.length > 0) {
          let appendResult = this.appendDataWithDeepForeach(row.children, rowId, appendData);
          if (appendResult) {
            return appendResult;
          }
        }
      });
      return false;
    },
    removeTableRow: function (row) {
      this.removeFromEsbDataStruct(this.tableData, row);
    },
    removeFromEsbDataStruct(esbDataStruct, row) {
      if (esbDataStruct == null || esbDataStruct.length === 0) {
        return;
      }
      let rowIndex = esbDataStruct.indexOf(row);
      if (rowIndex >= 0) {
        esbDataStruct.splice(rowIndex, 1);
      } else {
        esbDataStruct.forEach((esbData) => {
          if (esbData.children != null && esbData.children.length > 0) {
            this.removeFromEsbDataStruct(esbData.children, row);
          }
        });
      }
    },
  },
};
</script>

<style scoped>
.ht-btn-remove {
  color: white;
  background-color: #dcdfe6;
}

.ht-btn-confirm {
  color: white;
  /*background-color: #1483F6;*/
}

.ht-btn-add {
  border: 0px;
  margin-top: 10px;
  color: #783887;
  background-color: white;
}

.ht-tb {
  background-color: white;
  border: 0px;
}
</style>
