<template>
  <div class="container" v-loading="result.loading" v-if="ready">
    <el-row type="flex">
      <el-col :span="12">
        <el-form :model="form" :rules="rules" ref="form">
          <div class="logo">
            <img src="../assets/logo-dark-MeterSphere.svg" style="width: 224px" alt="">
          </div>
          <div class="title">
            <span id="s1">{{$t('commons.login')}}</span>
            <span id="s2">MeterSphere</span>
          </div>
          <div class="border"></div>
          <div class="welcome">
            {{$t('commons.welcome')}}
          </div>
          <div class="form">
            <el-form-item v-slot:default>
              <el-radio-group v-model="form.authenticate">
                <el-radio label="LDAP" size="mini" v-if="openLdap">LDAP</el-radio>
                <el-radio label="LOCAL" size="mini" v-if="openLdap">普通登录</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item prop="username">
              <el-input v-model="form.username" :placeholder="$t('commons.login_username')" autofocus
                        autocomplete="off"/>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" :placeholder="$t('commons.password')" show-password autocomplete="off"
                        maxlength="20" show-word-limit/>
            </el-form-item>
          </div>
          <div class="btn">
            <el-button type="primary" class="submit" @click="submit('form')">
              {{$t('commons.login')}}
            </el-button>
          </div>
          <div class="msg">
            {{msg}}
          </div>
        </el-form>
      </el-col>
      <el-col :span="12" class="image">
        <div></div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {saveLocalStorage} from '../common/js/utils';
  import {DEFAULT_LANGUAGE} from "../common/js/constants";


  export default {
    name: "Login",
    data() {
      /*let validateEmail = (rule, value, callback) => {
        // eslint-disable-next-line no-useless-escape
        let EMAIL_REGEX = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        if (!EMAIL_REGEX.test(value)) {
          callback(new Error('邮箱格式不正确'));
        } else {
          callback();
        }
      };*/
      return {
        result: {},
        form: {
          username: '',
          password: '',
          authenticate: 'LOCAL'
        },
        rules: {
          username: [
            {required: true, message: this.$t('commons.input_login_username'), trigger: 'blur'},
          ],
          password: [
            {required: true, message: this.$t('commons.input_password'), trigger: 'blur'},
            {min: 6, max: 20, message: this.$t('commons.input_limit', [6, 20]), trigger: 'blur'}
          ]
        },
        msg: '',
        ready: false,
        openLdap: false
      }
    },
    beforeCreate() {
      this.$get("/isLogin").then(response => {
        if (!response.data.success) {
          if (response.data.message === 'sso') {
            window.location.href = "/sso/login"
          } else {
            this.ready = true;
          }
        } else {
          let user = response.data.data;
          saveLocalStorage(response.data);
          this.getLanguage(user.language);
          window.location.href = "/"
        }
      });
      this.$get("/ldap/open", response => {
        this.openLdap = response.data;
      })
    },
    created: function () {
      // 主页添加键盘事件,注意,不能直接在焦点事件上添加回车
      document.addEventListener("keydown", this.watchEnter);
    },
    destroyed() {
      //移除监听回车按键
      document.removeEventListener("keydown", this.watchEnter);
    },
    methods: {
      //监听回车按钮事件
      watchEnter(e) {
        let keyNum = e.which; //获取被按下的键值
        //判断如果用户按下了回车键（keycody=13）
        if (keyNum === 13) {
          // 按下回车按钮要做的事
          this.submit('form');
        }
      },
      submit(form) {
        this.$refs[form].validate((valid) => {
          if (valid) {
            switch (this.form.authenticate) {
              case "LOCAL":
                this.normalLogin();
                break;
              case "LDAP":
                this.ldapLogin();
                break;
              default:
                this.normalLogin();
            }
          } else {
            return false;
          }
        });
      },
      normalLogin() {
        this.result = this.$post("signin", this.form, response => {
          saveLocalStorage(response);
          this.getLanguage(response.data.language);
        });
      },
      ldapLogin() {
        this.result = this.$post("ldap/signin", this.form, response => {
          saveLocalStorage(response);
          this.getLanguage(response.data.language);
        });
      },
      getLanguage(language) {
        if (!language) {
          this.$get("language", response => {
            language = response.data;
            localStorage.setItem(DEFAULT_LANGUAGE, language);
            window.location.href = "/"
          })
        } else {
          window.location.href = "/"
        }
      }
    }
  }
</script>

<style scoped>
  .container {
    min-width: 800px;
    max-width: 1440px;
    height: 560px;
    margin: calc((100vh - 560px) / 2) auto 0;
    background-color: #FFFFFF;
  }

  .logo {
    margin: 30px 30px 0;
  }

  .title {
    margin-top: 50px;
    font-size: 32px;
    letter-spacing: 0;
    text-align: center;
  }

  .title > #s1 {
    color: #999999;
  }

  .title > #s2 {
    color: #151515;
  }

  .border {
    height: 2px;
    margin: 20px auto 20px;
    position: relative;
    width: 80px;
    background: #8B479B;
  }

  .welcome {
    margin-top: 50px;
    font-size: 14px;
    color: #999999;
    letter-spacing: 0;
    line-height: 18px;
    text-align: center;
  }

  .form {
    margin-top: 30px;
    padding: 0 40px;
  }

  .btn {
    margin-top: 40px;
    padding: 0 40px;
  }

  .btn > .submit {
    width: 100%;
    border-radius: 0;
    border-color: #8B479B;
    background-color: #8B479B;
  }

  .btn > .submit:hover {
    border-color: rgba(139, 71, 155, 0.9);
    background-color: rgba(139, 71, 155, 0.9);
  }

  .btn > .submit:active {
    border-color: rgba(139, 71, 155, 0.8);
    background-color: rgba(139, 71, 155, 0.8);
  }

  .msg {
    margin-top: 10px;
    padding: 0 40px;
    color: red;
    text-align: center;
  }

  .image {
    background: url(../assets/info.png);
    height: 560px;
  }
</style>

<style>
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Neue Haas Grotesk Text Pro", "Arial Nova", "Segoe UI", "Helvetica Neue", ".PingFang SC", "PingFang SC", "Source Han Sans SC", "Noto Sans CJK SC", "Source Han Sans CN", "Noto Sans SC", "Source Han Sans TC", "Noto Sans CJK TC", "Hiragino Sans GB", sans-serif;
    font-size: 14px;
    background-color: #F5F5F5;
    line-height: 26px;
    color: #2B415C;
    -webkit-font-smoothing: antialiased;
    margin: 0;
  }

  .form .el-input > .el-input__inner {
    border-radius: 0;
  }
</style>

