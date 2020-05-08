<template>
  <div v-loading="result.loading">

    <el-card>
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="btnTips" :title="$t('commons.member')"/>
      </template>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')" width="120"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="status" :label="$t('commons.status')" width="100">
          <template v-slot:default="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)"/>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>

    </el-card>

    <el-dialog :title="$t('user.create')" :visible.sync="createVisible" width="30%" @closed="closeFunc"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button @click="createUser('createUserForm')" @keydown.enter.native.prevent type="primary"
                     size="medium">{{$t('commons.save')}}</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog :title="$t('user.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <span class="dialog-footer">
          <el-button @click="updateUser('updateUserForm')" @keydown.enter.native.prevent type="primary"
                     size="medium">{{$t('commons.save')}}
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsTableOperator from "../../common/components/MsTableOperator";

  export default {
    name: "MsUser",
    components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTableOperator},
    data() {
      return {
        queryPath: '/user/special/list',
        deletePath: '/user/special/delete/',
        createPath: '/user/special/add',
        updatePath: '/user/special/update',
        result: {},
        createVisible: false,
        updateVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        btnTips: this.$t('user.create'),
        condition: {},
        tableData: [],
        form: {},
        rule: {
          id: [
            {required: true, message: this.$t('user.input_id'), trigger: 'blur'},
            {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'}
          ],
          name: [
            {required: true, message: this.$t('user.input_name'), trigger: 'blur'},
            {min: 2, max: 20, message: this.$t('commons.input_limit', [2, 20]), trigger: 'blur'},
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: this.$t('user.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          phone: [
            {
              required: false,
              pattern: '^1(3|4|5|7|8)\\d{9}$',
              message: this.$t('user.mobile_number_format_is_incorrect'),
              trigger: 'blur'
            }
          ],
          email: [
            {required: true, message: this.$t('user.input_email'), trigger: 'blur'},
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@([A-Za-z0-9]+\.)+[A-Za-z]{2,6}$/,
              message: this.$t('user.email_format_is_incorrect'),
              trigger: 'blur'
            }
          ]
        }
      }
    },
    created() {
      this.search();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        this.updateVisible = true;
        this.form = Object.assign({}, row);
      },
      del(row) {
        this.$confirm(this.$t('user.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get(this.deletePath + row.id, () => {
            this.$success(this.$t('commons.delete_success'));
            this.search();
          });
        }).catch(() => {
          this.$info(this.$t('commons.delete_cancel'));
        });
      },
      createUser(createUserForm) {
        this.$refs[createUserForm].validate(valid => {
          if (valid) {
            this.result = this.$post(this.createPath, this.form, () => {
              this.$success(this.$t('commons.save_success'));
              this.search();
              this.createVisible = false;
            });
          } else {
            return false;
          }
        })
      },
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valid => {
          if (valid) {
            this.result = this.$post(this.updatePath, this.form, () => {
              this.$success(this.$t('commons.modify_success'));
              this.updateVisible = false;
              this.search();
            });
          } else {
            return false;
          }
        })
      },
      search() {
        this.result = this.$post(this.buildPagePath(this.queryPath), this.condition, response => {
          let data = response.data;
          this.total = data.itemCount;
          this.tableData = data.listObject;
        })
      },
      closeFunc() {
        this.form = {};
      },
      changeSwitch(row) {
        this.$post(this.updatePath, row, () => {
          this.$success(this.$t('commons.modify_success'));
        })
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      }
    }
  }
</script>

<style scoped>

</style>
