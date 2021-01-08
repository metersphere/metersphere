<template>
  <el-dialog :title="$t('api_test.scenario.variables')"
             :visible.sync="visible" class="environment-dialog" width="60%"
             @close="close">
    <div>
      <el-table ref="table" border :data="variables" class="adjust-table" @select-all="select" @select="select"
                v-loading="loading">
        <el-table-column type="selection" width="38"/>
        <el-table-column prop="num" label="ID" sortable/>
        <el-table-column prop="name" :label="$t('api_test.variable_name')" sortable show-overflow-tooltip/>
        <el-table-column prop="type" :label="$t('test_track.case.type')">
          <template v-slot:default="scope">
            <span>{{types.get(scope.row.type)}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="value" :label="$t('api_test.value')" show-overflow-tooltip/>

        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="{row}">
            <el-button type="text" @click="edit(row)">{{ $t('commons.edit') }}</el-button>
          </template>
        </el-table-column>

      </el-table>
    </div>

    <template v-slot:footer>
      <div style="margin:20px">
        <el-button style="margin-right:10px" @click="deleteVariable">{{$t('commons.delete')}}</el-button>

        <el-dropdown split-button type="primary" @command="handleClick" placement="top-end">
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

    <ms-edit-constant ref="parameters" @addParameters="addParameters"/>
    <ms-edit-counter ref="counter" @addParameters="addParameters"/>
    <ms-edit-random ref="random" @addParameters="addParameters"/>
    <ms-edit-list-value ref="listValue" @addParameters="addParameters"/>
    <ms-edit-csv ref="csv" @addParameters="addParameters"/>

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
        pageSize: 10,
        total: 0,
      }
    },
    methods: {
      handleClick(command) {
        switch (command) {
          case "CONSTANT":
            this.$refs.parameters.open();
            break;
          case "LIST":
            this.$refs.listValue.open();
            break;
          case "CSV":
            this.$refs.csv.open();
            break;
          case "COUNTER":
            this.$refs.counter.open();
            break;
          case "RANDOM":
            this.$refs.random.open();
            break;
        }
      },
      edit(row) {
        switch (row.type) {
          case "CONSTANT":
            this.$refs.parameters.open(row);
            break;
          case "LIST":
            this.$refs.listValue.open(row);
            break;
          case "CSV":
            this.$refs.csv.open(row);
            break;
          case "COUNTER":
            this.$refs.counter.open(row);
            break;
          case "RANDOM":
            this.$refs.random.open(row);
            break;
        }
      },
      addParameters(v) {
        v.id = getUUID();
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
      },
      close() {
        this.visible = false;
        this.$emit('setVariables', this.variables);
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
      }
    }
  }
</script>

<style scoped>

</style>
