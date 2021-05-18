<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="65%"
             title="设置权限"
             :destroy-on-close="true"
             v-loading="result.loading"
             top="5%"
  >
    <div style="height: 60vh;overflow: auto">
      <el-table
        :span-method="objectSpanMethod"
        border
        :data="tableData"
        style="width: 100%">
        <el-table-column
          prop="type"
          label="功能菜单"
          width="180">
          <template v-slot:default="scope">
            <span>{{ userGroupType[scope.row.type] ? userGroupType[scope.row.type] : scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="resource"
          label="操作对象"
          width="180">
          <template v-slot:default="scope">
            {{scope.row.resource.name}}
          </template>
        </el-table-column>
        <el-table-column
          prop="permissions"
          label="权限">
          <template v-slot:default="scope">
            <group-permission :permissions="scope.row.permissions" :selected.sync="tableData"/>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-button type="primary" @click="onSubmit" size="small" style="margin-top: 10px;margin-left: 5px;">确定</el-button>
    <el-button @click="cancel" size="small">取消</el-button>
  </el-dialog>
</template>

<script>
import GroupOperator from "@/business/components/settings/system/group/GroupOperator";
import GroupPermission from "@/business/components/settings/system/group/GroupPermission";
import {USER_GROUP_SCOPE} from "@/common/js/table-constants";

export default {
  name: "EditPermission",
  component: {
    GroupPermission,
  },
  data() {
    return {
      dialogVisible: false,
      tableData: [],
      selected: [],
      group: {},
      result: {},
      spanArr: []
    }
  },
  components: {
    GroupPermission,
    GroupOperator
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    }
  },
  methods: {
    open(row) {
      this.tableData = [];
      this.spanArr = [];
      this.dialogVisible = true;
      this.group = Object.assign({}, row);
      this.getGroupJson();
    },
    getGroupJson() {
      this.result = this.$post("/user/group/permission", this.group, result => {
        let data = result.data;
        if (data) {
          this.tableData = data.permissions;
          for (let i = 0; i < this.tableData.length; i++) {
            if (i === 0) {
              this.spanArr.push(1);
              this.pos = 0
            } else {
              // 判断当前元素与上一个元素是否相同
              if (this.tableData[i].type === this.tableData[i - 1].type) {
                this.spanArr[this.pos] += 1;
                this.spanArr.push(0);
              } else {
                this.spanArr.push(1);
                this.pos = i;
              }
            }
          }
        }
      })
    },
    onSubmit() {
      let param = {};
      let permissions = [];
      this.tableData.forEach(td => {
        permissions.push(...td.permissions);
      })
      param.userGroupId = this.group.id;
      param.permissions = permissions;
      this.result = this.$post('/user/group/permission/edit', param, () => {
        this.$success(this.$t('commons.save_success'));
        this.dialogVisible = false;
      })
    },
    cancel() {
      this.dialogVisible = false;
    },
    objectSpanMethod({row, column, rowIndex, columnIndex}) {
      if (columnIndex === 0) {
        const _row = this.spanArr[rowIndex];
        const _col = _row > 0 ? 1 : 0;
        return {
          rowspan: _row,
          colspan: _col
        }
      }
    },
  }
}
</script>

<style scoped>

</style>
