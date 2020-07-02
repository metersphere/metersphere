<template>
  <div>
    <el-card class="box-card" v-loading="result.loading">
      <el-form :model="form" size="small" :rules="rules" :disabled="show" ref="form">
        <el-form-item label="LDAP地址" prop="url">
          <el-input v-model="form.url" placeholder="请输入LDAP地址 (如 ldap://localhost:389)"></el-input>
        </el-form-item>
        <el-form-item label="绑定DN" prop="dn">
          <el-input v-model="form.dn" placeholder="请输入DN"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password auto-complete="new-password"></el-input>
        </el-form-item>
        <el-form-item label="用户OU" prop="ou">
          <el-input v-model="form.ou" placeholder="输入用户OU (使用|分隔各OU)"></el-input>
        </el-form-item>
        <el-form-item label="用户过滤器" prop="filter">
          <el-input v-model="form.filter" placeholder="输入过滤器 [可能的选项是cn或uid或sAMAccountName=%(user)s]"></el-input>
        </el-form-item>
        <el-form-item label="LDAP属性映射" prop="mapping">
          <el-input v-model="form.mapping" placeholder="属性映射"></el-input>
        </el-form-item>
        <el-form-item label="启用LDAP认证" prop="open">
          <el-checkbox v-model="form.open"></el-checkbox>
        </el-form-item>
      </el-form>

      <div>
        <el-button type="primary" size="small" :disabled="!show" @click="testConnection">测试连接</el-button>
        <el-button type="primary" size="small" :disabled="!show">测试登录</el-button>
        <el-button v-if="showEdit" size="small" @click="edit">编辑</el-button>
        <el-button type="success" v-if="showSave" size="small" @click="save('form')">保存</el-button>
        <el-button type="info" v-if="showCancel" size="small" @click="cancel">取消</el-button>
      </div>
    </el-card>
  </div>
</template>

<script>
  export default {
    name: "LdapSetting",
    data() {
      return {
        form: {
          open: false
        },
        result: {},
        show: true,
        showEdit: true,
        showSave: false,
        showCancel: false,
        rules: {
          url: {required: true, message: '请输入LDAP地址', trigger: ['change']},
          dn: {required: true, message: '请输入DN', trigger: ['change']},
          password: {required: true, message: '请输入密码', trigger: ['change']},
          ou: {required: true, message: '请输入OU', trigger: ['change']},
        }
      }
    },
    created() {
      this.init();
    },
    methods: {
      init() {
        this.result = this.$get("/system/ldap/info", response => {
          this.form = response.data;
          this.form.open = this.form.open === 'true' ? true : false;
        })
      },
      edit() {
        this.show = false;
        this.showEdit = false;
        this.showSave = true;
        this.showCancel = true;
      },
      cancel() {
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.init();
      },
      testConnection() {
        this.result = this.$post("/ldap/connect", this.form, response => {
          this.$success("连接成功！")
        })
      },
      save(form) {

        let param = [
          {paramKey: "ldap.url", paramValue: this.form.url, type: "text", sort: 1},
          {paramKey: "ldap.dn", paramValue: this.form.dn, type: "text", sort: 2},
          {paramKey: "ldap.password", paramValue: this.form.password, type: "password", sort: 3},
          {paramKey: "ldap.ou", paramValue: this.form.ou, type: "text", sort: 4},
          {paramKey: "ldap.filter", paramValue: this.form.filter, type: "text", sort: 5},
          {paramKey: "ldap.mapping", paramValue: this.form.mapping, type: "text", sort: 6},
          {paramKey: "ldap.open", paramValue: this.form.open, type: "text", sort: 7}
        ]

        this.$refs[form].validate(valid => {
          if (valid) {
            this.result = this.$post("/system/save/ldap", param, response => {
              this.show = true;
              this.showEdit = true;
              this.showSave = false;
              this.showCancel = false;
              this.$success("保存成功")
              this.init();
            });
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
