<template>
  <div v-loading="result.loading">

     <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="search" @create="create"
                         :create-tip="$t('user.create')" :title="$t('commons.member')"/>
      </template>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="status" :label="$t('commons.status')">
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
            <ms-table-operator @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
              <template v-slot:behind>
                <ms-table-operator-button :tip="$t('member.edit_password')" icon="el-icon-s-tools"
                                          type="success" @exec="editPassword(scope.row)"/>
              </template>
            </ms-table-operator>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="search" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <el-dialog :title="$t('commons.verification')" :visible.sync="checkPasswordVisible" width="30%"
               @close="closeCheckPassword" :destroy-on-close="true">
      <el-form :model="checkPasswordForm" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="checkPasswordForm">
        <el-form-item :label="$t('commons.password')" prop="password">
          <el-input type="password" v-model="checkPasswordForm.password" autocomplete="off" show-password></el-input>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="checkPasswordVisible = false"
          @confirm="setAdmin('checkPasswordForm')"/>
      </template>
    </el-dialog>

    <!--Create user-->
    <el-dialog :title="$t('user.create')" :visible.sync="createVisible" width="30%" @closed="handleClose"
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
        <el-form-item :label="$t('commons.password')" prop="password">
          <el-input v-model="form.password" autocomplete="off" show-password/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="createVisible = false"
          @confirm="createUser('createUserForm')"/>
      </template>
    </el-dialog>

    <!--Modify user information in system settings-->
    <el-dialog :title="$t('user.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true"
               @close="handleClose">
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
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateUser('updateUserForm')"/>
      </template>
    </el-dialog>
   <!--Changing user password in system settings-->
    <el-dialog :title="$t('member.edit_password')" :visible.sync="editPasswordVisible" width="30%" left>
      <el-form :model="ruleForm" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="editPasswordForm" class="demo-ruleForm">
        <el-form-item :label="$t('member.new_password')" prop="newpassword">
          <el-input type="password" v-model="ruleForm.newpassword" autocomplete="off" show-password></el-input>
        </el-form-item>
        <el-form-item >
          <el-input v-model="ruleForm.id" autocomplete="off" :disabled="true" style="display:none"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <ms-dialog-footer
          @cancel="editPasswordVisible = false"
          @confirm="editUserPassword('editPasswordForm')"/>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "MsUser",
    components: {MsCreateBox, MsTablePagination, MsTableHeader, MsTableOperator, MsDialogFooter, MsTableOperatorButton},
    data() {
      return {
        queryPath: '/user/special/list',
        deletePath: '/user/special/delete/',
        createPath: '/user/special/add',
        updatePath: '/user/special/update',
        editPasswordPath: '/user/special/password',
        result: {},
        createVisible: false,
        updateVisible: false,
        editPasswordVisible: false,
        checkPasswordVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        condition: {},
        tableData: [],
        form: {},
        checkPasswordForm: {},
        ruleForm: {},
        setAdminParam: {},
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
          ],
          password: [
            {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
            {
              required: true,
              pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,16}$/,
              message: this.$t('member.password_format_is_incorrect'),
              trigger: 'blur'
            }
          ],
          newpassword: [
            {required: true, message: this.$t('user.input_password'), trigger: 'blur'},
            {
              required: true,
              pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[^]{8,16}$/,
              message: this.$t('member.password_format_is_incorrect'),
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
      editPassword(row) {
        this.editPasswordVisible = true;
        this.ruleForm = Object.assign({}, row);
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
      editUserPassword(editPasswordForm){
        this.$refs[editPasswordForm].validate(valid=>{
          if(valid){
            this.result = this.$post(this.editPasswordPath, this.ruleForm, response => {
              this.$success(this.$t('commons.modify_success'));
              this.editPasswordVisible = false;
              this.search();
              window.location.reload();
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
      handleClose() {
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
      },
      closeCheckPassword() {
        this.checkPasswordForm = {};
      },
      setAdmin(checkPasswordForm) {
        let user = getCurrentUser();
        this.$set(this.setAdminParam, 'adminId', user.id);
        this.$set(this.setAdminParam, 'password', this.checkPasswordForm.password);
        this.$refs[checkPasswordForm].validate(valid => {
          if (valid) {
            this.$post("/user/set/admin", this.setAdminParam, () => {
              this.$success(this.$t('commons.modify_success'));
              this.checkPasswordVisible = false;
            })
          } else {
            return false;
          }
        })
      }
    }
  }
</script>

<style scoped>
</style>
