<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="dialogVisible" width="65%"
             :title="$t('group.set_permission')"
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
          :label="$t('group.functional_menu')"
          width="180">
          <template v-slot:default="scope">
            <span v-if="scope.row.type !== 'PROJECT'">{{ userGroupType[scope.row.type] ? userGroupType[scope.row.type] : scope.row.type }}</span>
            <span v-else>{{_computedMenuName(scope.row.resource)}}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="resource"
          :label="$t('group.operation_object')"
          width="180">
          <template v-slot:default="scope">
            {{scope.row.resource.name}}
          </template>
        </el-table-column>
        <el-table-column
          prop="permissions"
          :label="$t('group.permission')">
          <template v-slot:default="scope">
            <group-permission :permissions="scope.row.permissions" :selected.sync="tableData"/>
          </template>
        </el-table-column>
        <el-table-column
          width="50px"
          :label="$t('group.check_all')">
          <template v-slot:default="scope">
            <div style="text-align: center;">
              <el-checkbox @change="handleSelectAll($event, scope.row.permissions)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <el-button type="primary" @click="onSubmit" size="small" style="margin-top: 10px;margin-left: 5px;">{{ $t('commons.confirm') }}</el-button>
    <el-button @click="cancel" size="small">{{ $t('commons.cancel') }}</el-button>
  </el-dialog>
</template>

<script>
import GroupPermission from "@/business/components/settings/system/group/GroupPermission";
import {PROJECT_GROUP_SCOPE, USER_GROUP_SCOPE} from "@/common/js/table-constants";

export default {
  name: "EditPermission",
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
          this._getUniteMenu();
        }
      })
    },
    _getUniteMenu() {
      let menu = ['TRACK', 'API', 'PERFORMANCE'];
      for (let i = 0; i < this.tableData.length; i++) {
        if (i === 0) {
          this.spanArr.push(1);
          this.pos = 0
        } else {
          // 判断当前元素与上一个元素是否相同
          let sign = false;
          if (this.tableData[i].type !== 'PROJECT') {
            sign = this.tableData[i].type === this.tableData[i - 1].type;
          } else {
            sign = !menu.includes(this.tableData[i].resource.id.split('_')[1]) ?
              true : this.tableData[i].resource.id.split('_')[1] === this.tableData[i - 1].resource.id.split('_')[1]
          }
          if (sign) {
            this.spanArr[this.pos] += 1;
            this.spanArr.push(0);
          } else {
            this.spanArr.push(1);
            this.pos = i;
          }
        }
      }
    },
    _computedMenuName(resource) {
      return PROJECT_GROUP_SCOPE[resource.id.split('_')[1]] ? PROJECT_GROUP_SCOPE[resource.id.split('_')[1]] : '项目';
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
    handleSelectAll(check, permissions) {
      permissions.map(p => p.checked = check);
    }
  }
}
</script>

<style scoped>

</style>
