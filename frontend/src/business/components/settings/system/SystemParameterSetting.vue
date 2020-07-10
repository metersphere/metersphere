<template>
  <div>
    <el-tabs class="system-setting" v-model="activeName">
      <el-tab-pane :label="$t('system_parameter_setting.mailbox_service_settings')" name="email">
        <email-setting/>
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.ldap_setting')" name="ldap">
        <ldap-setting/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import EmailSetting from "./EmailSetting";
  import LdapSetting from "./LdapSetting";
  export default {
    name: "SystemParameterSetting",
    components: {
      EmailSetting, LdapSetting
    },
    data() {
      return {
        formInline: {
          /*host: 'smtp.163.com',
          port: '465',
          account: 'xjj0608@163.com',
          password: '2345678',*/
        },

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
        activeName: 'email',
        rules: {
          host: [
            {
              required: true,
              message: ' '
            },
          ],
          port: [
            {
              required: true,
              message: ' '
            }
          ],
          account: [
            {
              required: true,
              message: ' '
            }]
        }
      }
    },

    activated() {
      this.query()
      this.change()
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
          if(response.data[4].paramValue!=""){
            this.$set(this.formInline, "SSL", JSON.parse(response.data[4].paramValue));
          }
          if(response.data[5].paramValue!=""){
            this.$set(this.formInline, "TLS", JSON.parse(response.data[5].paramValue));
          }
          if(response.data[6].paramValue!=""){
            this.$set(this.formInline, "SMTP", JSON.parse(response.data[6].paramValue));
          }
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
        this.change()
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
        this.query();
        this.showEdit = true;
        this.showCancel = false;
        this.showSave = false;
        this.show = true;
        this.change()
      }
    }
  }
</script>

<style scoped>

</style>
