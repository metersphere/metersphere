<template>
  <div v-loading="result.loading">
    <el-card>

      <template v-slot:header>
        <div>
          <el-row type="flex" justify="space-between" align="middle">
            <span class="title">{{$t('commons.personal_info')}}</span>
          </el-row>
        </div>
      </template>

      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')" width="120"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" width="180">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog :title="$t('member.modify_personal_info')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
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
            <el-button type="primary" onkeydown="return false;"
                       @click="updateUser('updateUserForm')" size="medium">{{$t('commons.save')}}</el-button>
          </span>
        </template>
      </el-dialog>

    </el-card>
  </div>
</template>

<script>
  import {TokenKey} from "../../../../common/js/constants";

  export default {
    data() {
      return {
        result: {},
        updateVisible: false,
        tableData: [],
        updatePath: '/user/update/currentuser',
        form: {},
        rule: {
          name: [
            {required: true, message: this.$t('member.input_name'), trigger: 'blur'},
            { min: 2, max: 10, message: this.$t('commons.input_limit', [2, 10]), trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: this.$t('member.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          phone: [
            {
              required: false,
              pattern: '^1(3|4|5|7|8)\\d{9}$',
              message: this.$t('member.mobile_number_format_is_incorrect'),
              trigger: 'blur'
            }
          ],
          email: [
            { required: true, message: this.$t('member.input_email'), trigger: 'blur' },
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@([A-Za-z0-9]+\.)+[A-Za-z]{2,6}$/,
              message: this.$t('member.email_format_is_incorrect'),
              trigger: 'blur'
            }
          ]
        }
      }
    },
    name: "MsPersonSetting",
    created() {
      this.initTableData();
    },
    methods: {
      currentUser: () => {
        let user = localStorage.getItem(TokenKey);
        return JSON.parse(user);
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
      },
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.updatePath, this.form,response => {
              this.$success(this.$t('commons.modify_success'));
              localStorage.setItem(TokenKey, JSON.stringify(response.data));
              this.updateVisible = false;
              this.initTableData();
              window.location.reload();
            });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.result = this.$get("/user/info/" + this.currentUser().id, response => {
          let data = response.data;
          let dataList = [];
          dataList[0] = data;
          this.tableData = dataList;
        })
      },
      closeFunc() {
        this.form = {};
      }
    }
  }
</script>

<style scoped>

</style>
