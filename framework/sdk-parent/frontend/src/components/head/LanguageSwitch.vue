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
import {DEFAULT_LANGUAGE} from '../../utils/constants';
import {getCurrentUser} from "../../utils/token";
import {useUserStore} from "@/store";
import {fullScreenLoading, stopFullScreenLoading} from "../../utils";

const userStore = useUserStore();

export default {
  name: "MsLanguageSwitch",
  data() {
    return {
      currentUserInfo: {},
      language: '',
      languageMap: {
        "zh-CN": "中文(简体)",
        "zh-TW": "中文(繁體)",
        "en-US": "English",
      }
    };
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
      this.language = this.languageMap[lang];
    },
    currentUser: () => {
      return getCurrentUser();
    },
    changeLanguage(language) {
      let data = {
        id: getCurrentUser().id,
        language: language
      };
      this.checkLanguage(language);
      const loading = fullScreenLoading(this);
      userStore.userSetLanguage(data)
        .then(response => {
          stopFullScreenLoading(loading);
          userStore.$patch(response.data)
          location.reload();
        })
    }
  }
}
</script>

<style scoped>
.dropdown-link {
  cursor: pointer;
  font-size: 12px;
  line-height: 40px;
  padding-right: 20px;
}

.el-icon-check {
  color: #783887;
  margin-left: 10px;
  font-weight: bold;
}

.align-right {
  float: right;
}

.icon {
  width: 24px;
}

.global {
  color: var(--color);
}

:deep(.el-submenu__title) {
  padding-left: 5px;
}
</style>
