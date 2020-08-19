<template>
  <div v-loading="result.loading">
    <!--邮件表单-->
    <el-form :model="formInline" :rules="rules" ref="formInline" class="demo-form-inline"
             :disabled="show" v-loading="loading" size="small">
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_host')" prop="host">
            <el-input v-model="formInline.host" :placeholder="$t('system_parameter_setting.SMTP_host')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_port')" prop="port">
            <el-input v-model="formInline.port" :placeholder="$t('system_parameter_setting.SMTP_port')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_account')" prop="account">
            <el-input v-model="formInline.account" :placeholder="$t('system_parameter_setting.SMTP_account')"
                      v-on:input="change()"></el-input>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('system_parameter_setting.SMTP_password')" prop="password">
            <el-input v-model="formInline.password" :placeholder="$t('system_parameter_setting.SMTP_password')"
                      autocomplete="new-password" show-password type="text" @focus="changeType" ref="input">
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!---->
      <div style="border: 0px;margin-bottom: 20px;margin-top: 20px">
        <el-checkbox v-model="formInline.SSL" :label="$t('system_parameter_setting.SSL')"></el-checkbox>
      </div>
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.TLS" :label="$t('system_parameter_setting.TLS')"></el-checkbox>
      </div>
      <div style="border: 0px;margin-bottom: 20px">
        <el-checkbox v-model="formInline.SMTP" :label="$t('system_parameter_setting.SMTP')"></el-checkbox>
      </div>
      <template v-slot:footer>
      </template>
    </el-form>
    <div>
      <el-button type="primary" @click="testConnection('formInline')" :disabled="disabledConnection" size="small">
        {{ $t('system_parameter_setting.test_connection') }}
      </el-button>
      <el-button @click="edit" v-if="showEdit" size="small">{{ $t('commons.edit') }}</el-button>
      <el-button type="success" @click="save('formInline')" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>

export default {
  name: "EmailSetting",
  data() {
    return {
      formInline: {},
      input: '',
      visible: true,
      result: {},
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledConnection: false,
      disabledSave: false,
      loading: false,
      rules: {
        host: [
          {
            required: true,
            message: this.$t('system_parameter_setting.host'),
            trigger: ['change', 'blur']
          },
        ],
        port: [
          {
            required: true,
            message: this.$t('system_parameter_setting.port'),
            trigger: ['change', 'blur']
          }
        ],
        account: [
          {
            required: true,
            message: this.$t('system_parameter_setting.account'),
            trigger: ['change', 'blur']
          }]
      }
    }
  },

  created() {
    this.query()
  },
  methods: {
    changeType() {
      this.$refs.input = 'password'
    },
    query() {
      this.result = this.$get("/system/mail/info", response => {
        this.$set(this.formInline, "host", response.data[0].paramValue);
        this.$set(this.formInline, "port", response.data[1].paramValue);
        this.$set(this.formInline, "account", response.data[2].paramValue);
        this.$set(this.formInline, "password", response.data[3].paramValue);
        this.$set(this.formInline, "SSL", JSON.parse(response.data[4].paramValue));
        this.$set(this.formInline, "TLS", JSON.parse(response.data[5].paramValue));
        this.$set(this.formInline, "SMTP", JSON.parse(response.data[6].paramValue));
        this.$nextTick(() => {
          this.$refs.formInline.clearValidate();
        })
      })
    },
    change() {
      if (!this.formInline.host || !this.formInline.port || !this.formInline.account) {
        this.disabledConnection = true;
        this.disabledSave = true;
      } else {
        this.disabledConnection = false;
        this.disabledSave = false;
      }
    },
    testConnection(formInline) {
      let param = {
        "smtp.server": this.formInline.host,
        "smtp.port": this.formInline.port,
        "smtp.account": this.formInline.account,
        "smtp.password": this.formInline.password,
        "smtp.ssl": this.formInline.SSL,
        "smtp.tls": this.formInline.TLS,
        "smtp.smtp": this.formInline.SMTP,
      };
      this.$refs[formInline].validate((valid) => {
        if (valid) {
          this.result = this.$post("/system/testConnection", param, response => {
            this.$success(this.$t('commons.connection_successful'));
          })
        } else {
          return false;
        }
      })
    },
    edit() {
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
      this.show = false;
    },
    save(formInline) {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      let param = [
        {paramKey: "smtp.host", paramValue: this.formInline.host, type: "text", sort: 1},
        {paramKey: "smtp.port", paramValue: this.formInline.port, type: "text", sort: 2},
        {paramKey: "smtp.account", paramValue: this.formInline.account, type: "text", sort: 3},
        {paramKey: "smtp.password", paramValue: this.formInline.password, type: "password", sort: 4},
        {paramKey: "smtp.ssl", paramValue: this.formInline.SSL, type: "text", sort: 5},
        {paramKey: "smtp.tls", paramValue: this.formInline.TLS, type: "text", sort: 6},
        {paramKey: "smtp.smtp", paramValue: this.formInline.SMTP, type: "text", sort: 7}
      ]

      this.$refs[formInline].validate(valid => {
        if (valid) {
          this.result = this.$post("/system/edit/email", param, response => {
            let flag = response.success;
            if (flag) {
              this.$success(this.$t('commons.save_success'));
            } else {
              this.$message.error(this.$t('commons.save_failed'));
            }
          });
        } else {
          return false;
        }
      })
    },
    cancel() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      this.query();
    }

  }
}
</script>

<style scoped>

</style>
