<template>
  <el-menu :unique-opened="true" class="header-user-menu align-right"
           mode="horizontal"
           background-color="#2c2a48"
           text-color="#fff"
           active-text-color="#fff"
  >
    <el-submenu index="1">
      <template slot="title">
        <font-awesome-icon class="icon global" :icon="['fas', 'globe']"/>
        <span>{{language}}</span>
      </template>
      <el-menu-item @click="changeLanguage('zh_CN')">
        简体中文<i class="el-icon-check" v-if="currentUserInfo.language==='zh_CN' || !currentUserInfo.language"/>
      </el-menu-item>
      <el-menu-item @click="changeLanguage('zh_TW')">
        繁體中文<i class="el-icon-check" v-if="currentUserInfo.language==='zh_TW'"/>
      </el-menu-item>
      <el-menu-item @click="changeLanguage('en_US')">
        English<i class="el-icon-check" v-if="currentUserInfo.language==='en_US'"/>
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
  import {TokenKey, ZH_CN, ZH_TW, EN_US} from '../../../../common/js/constants';
  import {getCurrentUser} from "../../../../common/js/utils";

  export default {
    name: "MsLanguageSwitch",
    data() {
      return {
        currentUserInfo: {},
        language: ''
      };
    },
    created() {
      let lang = this.currentUser().language;
      this.currentUserInfo = this.currentUser();
      if (!lang) {
        lang = 'zh_CN';
      }
      this.$setLang(lang);
      switch (lang) {
        case ZH_CN:
          this.language = '简体中文';
          break;
        case ZH_TW:
          this.language = '繁體中文';
          break;
        case EN_US:
          this.language = 'English';
          break;
        default:
          this.language = '简体中文';
          break;
      }
    },
    methods: {
      currentUser: () => {
        return getCurrentUser();
      },
      changeLanguage(language) {
        let user = {
          id: this.currentUser().id,
          language: language
        };
        this.result = this.$post("/user/update/current", user, response => {
          this.$success(this.$t('commons.modify_success'));
          localStorage.setItem(TokenKey, JSON.stringify(response.data));
          window.location.reload();
        });
      }
    }
  }
</script>

<style scoped>

  .el-icon-check {
    color: #44b349;
    margin-left: 10px;
  }

  .align-right {
    float: right;
  }

  .icon {
    width: 24px;
  }

  .global {
    color: #fff;
  }
</style>
