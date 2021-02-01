<template>
  <el-dialog :title="$t('api_test.scenario.variables')" :close-on-click-modal="false"
             :visible.sync="visible" class="environment-dialog" width="60%"
             @close="close">
    <div>
      <el-row>
        <el-col :span="12">
          <div style="border:1px #DCDFE6 solid; min-height: 400px;border-radius: 4px ;width: 100% ;">

            <el-table ref="table" border :data="variables" class="adjust-table" @select-all="select" @select="select"
                      v-loading="loading" @row-click="edit">
              <el-table-column type="selection" width="38"/>
              <el-table-column prop="num" label="ID" sortable/>
              <el-table-column prop="name" :label="$t('api_test.variable_name')" sortable show-overflow-tooltip/>
              <el-table-column prop="type" :label="$t('test_track.case.type')">
                <template v-slot:default="scope">
                  <span>{{types.get(scope.row.type)}}</span>
                </template>
              </el-table-column>
              <el-table-column prop="value" :label="$t('api_test.value')" show-overflow-tooltip/>
            </el-table>
          </div>
        </el-col>
        <el-col :span="12">
          <ms-edit-constant v-if="editData.type=='CONSTANT'" ref="parameters" :editData.sync="editData"/>
          <ms-edit-counter v-if="editData.type=='COUNTER'" ref="counter" :editData.sync="editData"/>
          <ms-edit-random v-if="editData.type=='RANDOM'" ref="random" :editData.sync="editData"/>
          <ms-edit-list-value v-if="editData.type=='LIST'" ref="listValue" :editData="editData"/>
          <ms-edit-csv v-if="editData.type=='CSV'" ref="csv" :editData.sync="editData"/>
        </el-col>

      </el-row>

    </div>

    <template v-slot:footer>
      <div style="margin:20px">
        <el-button style="margin-right:10px" @click="deleteVariable">{{$t('commons.delete')}}</el-button>

        <el-dropdown split-button type="primary" @command="handleClick" @click="handleClick('CONSTANT')" placement="top-end">
          {{$t('commons.add')}}
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="CONSTANT">常量</el-dropdown-item>
            <el-dropdown-item command="LIST">列表</el-dropdown-item>
            <el-dropdown-item command="CSV">CSV</el-dropdown-item>
            <el-dropdown-item command="COUNTER">计数器</el-dropdown-item>
            <el-dropdown-item command="RANDOM">随机数</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </template>


  </el-dialog>
</template>

<script>
  import MsEditConstant from "./EditConstant";
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import MsEditCounter from "./EditCounter";
  import MsEditRandom from "./EditRandom";
  import MsEditListValue from "./EditListValue";
  import MsEditCsv from "./EditCsv";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "MsVariableList",
    components: {
      MsEditConstant,
      MsDialogFooter,
      MsTableHeader,
      MsTablePagination,
      MsEditCounter,
      MsEditRandom,
      MsEditListValue,
      MsEditCsv
    },
    data() {
      return {
        variables: [],
        types: new Map([
          ['CONSTANT', '常量'],
          ['LIST', '列表'],
          ['CSV', 'CSV'],
          ['COUNTER', '计数器'],
          ['RANDOM', '随机数']
        ]),
        visible: false,
        selection: [],
        loading: false,
        currentPage: 1,
        editData: {},
        pageSize: 10,
        total: 0,
      }
    },
    methods: {
      handleClick(command) {
        this.editData = {};
        this.editData.type = command;
        this.addParameters(this.editData);
      },
      edit(row) {
        this.editData = row;
      },
      addParameters(v) {
        v.id = getUUID();
        if (v.type === 'CSV') {
          v.delimiter = ",";
        }
        this.variables.push(v);
        let index = 1;
        this.variables.forEach(item => {
          item.num = index;
          index++;
        })
      },
      select(selection) {
        this.selection = selection.map(s => s.id);
      },
      isSelect(row) {
        return this.selection.includes(row.id)
      },
      open: function (variables) {
        this.variables = variables;
        this.visible = true;
        this.editData = {type: "CONSTANT"};
        this.addParameters(this.editData);
      },
      close() {
        this.visible = false;
        let saveVariables = [];
        this.variables.forEach(item => {
          if (item.name && item.name != "") {
            saveVariables.push(item);
          }
        })
        this.$emit('setVariables', saveVariables);
      },
      deleteVariable() {
        let ids = Array.from(this.selection);
        if (ids.length == 0) {
          this.$warning("请选择一条数据删除");
          return;
        }
        ids.forEach(row => {
          const index = this.variables.findIndex(d => d.id === row);
          this.variables.splice(index, 1);
        })
        this.selection = [];
      }
    }
  }
</script>

<style scoped>

</style>
