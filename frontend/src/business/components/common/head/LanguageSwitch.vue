<template>
  <el-dropdown size="medium" @command="changeLanguage" class="align-right">
    <span class="dropdown-link">
        <font-awesome-icon :icon="['fas', 'language']" size="lg"/>
    </span>
    <template v-slot:dropdown>
      <el-dropdown-menu>
        <el-dropdown-item :command="key" v-for="(value, key) in languageMap" :key="key">
          {{ value }} <i class="el-icon-check" v-if="language === value"/>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
import {DEFAULT_LANGUAGE, EN_US, TokenKey, ZH_CN, ZH_TW} from '@/common/js/constants';
import {getCurrentUser} from "@/common/js/utils";

export default {
  name: "MsLanguageSwitch",
  inject: [
    'reload'
  ],
  data() {
    return {
      currentUserInfo: {},
      language: '',
      languageMap: {
        [ZH_CN]: '简体中文',
        [EN_US]: 'English',
        [ZH_TW]: '繁體中文',
      }
    };
  },
  props: {
    color: String
  },
  created() {
    let lang = this.currentUser().language;
    this.currentUserInfo = this.currentUser();
    if (!lang) {
      lang = localStorage.getItem(DEFAULT_LANGUAGE);
    }
    this.checkLanguage(lang)
  },
  methods: {
    checkLanguage(lang) {
      if (!lang) return;
      this.$setLang(lang);
      switch (lang) {
        case ZH_CN:
          this.language = this.languageMap[ZH_CN];
          break;
        case ZH_TW:
          this.language = this.languageMap[ZH_TW];
          break;
        case EN_US:
          this.language = this.languageMap[EN_US];
          break;
        default:
          this.language = this.languageMap[ZH_CN];
          break;
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    changeLanguage(language) {
      let user = {
        id: this.currentUser().id,
        language: language
      };
      this.checkLanguage(language);
      this.result = this.$post("/user/update/current", user, response => {
        localStorage.setItem(TokenKey, JSON.stringify(response.data));
        // 刷新路由
        this.reload();
      });
    }
  }
}
</script>

<style scoped>
.dropdown-link {
  cursor: pointer;
  font-size: 12px;
  color: rgb(245, 245, 245);
  line-height: 40px;
  padding-right: 20px;
}

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

/deep/ .el-submenu__title {
  padding-left: 5px;
}
</style>
