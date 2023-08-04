<template>
  <div v-if="isShow" v-loading="loading" style="height: calc(100vh - 80px); overflow: auto">
    <el-form :model="formInline" ref="formInline" label-position="top"
             :disabled="show" size="small">

      <el-form-item :label="$t('display.logo')" prop="logo">
        <el-col :span="8">
          <el-upload
            accept=".jpg,.jpeg,.png"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUploadLogo"
            :on-exceed="handleExceed"
            :limit="1"
            :file-list="logoList">
            <el-button icon="el-icon-plus" size="mini"></el-button>
            <span slot="tip" class="el-upload__tip"></span>
          </el-upload>
          <div v-if="logoList[0]">
            <span> {{ logoList[0].name }} </span>
            <el-link :underline="false" @click="handleDelete('logoList')" :disabled="show">&times;</el-link>
          </div>
        </el-col>
      </el-form-item>
      <el-form-item :label="$t('display.sysTitle')" prop="title">
        <el-input v-model="formInline.sysTitle" :placeholder="'MeterSphere'" maxlength="25" show-word-limit/>
      </el-form-item>
      <el-form-item :label="$t('display.loginImage')" prop="loginImage">
        <el-col :span="8">
          <el-upload
            accept=".jpg,.jpeg,.png"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUploadLoginImage"
            :on-exceed="handleExceed"
            :limit="1"
            :file-list="loginImageList">
            <el-button icon="el-icon-plus" size="mini"></el-button>
            <span slot="tip" class="el-upload__tip"></span>
          </el-upload>
          <div v-if="loginImageList[0]">
            <span> {{ loginImageList[0].name }} </span>
            <el-link :underline="false" @click="handleDelete('loginImageList')" :disabled="show">&times;</el-link>
          </div>
        </el-col>
      </el-form-item>
      <el-form-item :label="$t('display.loginLogo')" prop="loginLogo">
        <el-col :span="8">
          <el-upload
            accept=".jpg,.jpeg,.png"
            action=""
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUploadLoginLogo"
            :on-exceed="handleExceed"
            :limit="1"
            :file-list="loginLogoList">
            <el-button icon="el-icon-plus" size="mini"></el-button>
            <span slot="tip" class="el-upload__tip"></span>
          </el-upload>
          <div v-if="loginLogoList[0]">
            <span> {{ loginLogoList[0].name }} </span>
            <el-link :underline="false" @click="handleDelete('loginLogoList')" :disabled="show">&times;</el-link>
          </div>
        </el-col>
      </el-form-item>
      <el-form-item :label="$t('display.loginTitle')" prop="loginTitle">
        <el-input v-model="formInline.loginTitle" :placeholder="$t('commons.welcome')" maxlength="30" show-word-limit/>
      </el-form-item>
      <el-form-item :label="$t('display.pageTitle')" prop="title">
        <el-input v-model="formInline.title" :placeholder="'MeterSphere'" maxlength="25" show-word-limit/>
      </el-form-item>
      <el-form-item :label="$t('display.theme_style')" prop="themeStyle">
        <div class="ms-theme-setting" :draggable="show">
          <div class="ms-theme-setting-item">
            <div class="ms-theme-setting-selectIcon">
              <el-radio v-model="formInline.sideTheme" class="ms-el-radio__label" label="theme-default">
                {{ $t('display.theme_default') }}
              </el-radio>
            </div>
            <img src="/assets/images/default.svg" alt="dark">
          </div>
          <div class="ms-theme-setting-item">
            <div class="ms-theme-setting-selectIcon">
              <el-radio v-model="formInline.sideTheme" class="ms-el-radio__label" label="theme-light">
                {{ $t('display.theme_light') }}
              </el-radio>
            </div>
            <img src="/assets/images/light.svg" alt="light">
          </div>
          <div class="ms-theme-setting-item">
            <div class="ms-theme-setting-selectIcon">
              <el-radio v-model="formInline.sideTheme" class="ms-el-radio__label" label="theme-customize">
                {{ $t('display.theme_follow') }}
              </el-radio>
            </div>
            <div class="ms-theme-setting-selectIcon-top"></div>
          </div>
        </div>
      </el-form-item>

      <el-form-item :label="$t('commons.theme_color')">
        <el-color-picker
          v-model="formInline.theme"
          class="theme-picker"
          size="small"
          popper-class="theme-picker-dropdown"/>
      </el-form-item>
      <el-form-item :label="$t('display.css_file')" prop="loginTitle">
        <el-col :span="8">
          <el-upload
            accept=".css"
            action=""
            :show-file-list="false"
            :http-request="handleUploadcss"
            :on-exceed="handleExceed"
            :limit="1"
            :file-list="cssList">
            <el-button icon="el-icon-plus" size="mini"></el-button>
            <span slot="tip" class="el-upload__tip"></span>
          </el-upload>
          <div v-if="cssList[0]">
            <span> {{ cssList[0].name }} </span>
            <el-link :underline="false" @click="handleDelete('cssList')" :disabled="show">&times;</el-link>
          </div>
        </el-col>
      </el-form-item>
    </el-form>
    <div>
      <el-button @click="edit" v-if="showEdit" size="small" v-permission="['SYSTEM_SETTING:READ+EDIT']">
        {{ $t('commons.edit') }}
      </el-button>
      <el-button type="success" @click="save()" v-if="showSave" :disabled="disabledSave" size="small">
        {{ $t('commons.save') }}
      </el-button>
      <el-button @click="cancel" type="info" v-if="showCancel" size="small">{{ $t('commons.cancel') }}</el-button>
    </div>
  </div>
</template>

<script>
import {setAsideColor, setCustomizeColor, setLightColor} from "metersphere-frontend/src/utils";
import {getDisplayInfo, saveDisplay} from "@/api/display";

export default {
  name: "MxDisplay",
  props: {
    isShow: {
      type: Boolean,
      default: true,
    }
  },
  data() {
    return {
      formInline: {
        logo: null,
        loginLogo: null,
        loginImage: null,
        css: null,
        loginTitle: null,
        sysTitle: null,
        title: null,
        theme: null,
        sideTheme: "theme-default"
      },
      input: '',
      visible: true,
      loading: false,
      logoList: [],
      loginLogoList: [],
      loginImageList: [],
      cssList: [],
      uploadList: [],
      showEdit: true,
      showSave: false,
      showCancel: false,
      show: true,
      disabledConnection: false,
      disabledSave: false,
    }
  },
  mounted() {
    this.query();
  },
  methods: {
    beforeUpload(file) {
      //判断文件是不是以图片格式结尾的 .jpg,.jpeg,.png png，tif，gif，pcx，tga，exif，fpx，svg，psd，cdr，pcd，dxf，ufo，eps，ai，raw，WMF，webp，avif，apng
      if (!/\.(jpg|jpeg|png|JPG|PNG|tif|gif|pcx|tga|exif|fpx|svg|psd|cdr|pcd|dxf|ufo|eps|ai|raw|WMF|webp|avif|apng)$/.test(file.name)) {
        this.$error(this.$t('load_test.file_type_limit'));
        return false;
      }
      return true;
    },
    handleUploadLogo(uploadResources) {
      this.logoList.push(uploadResources.file);
    },
    handleUploadLoginLogo(uploadResources) {
      this.loginLogoList.push(uploadResources.file);
    },
    handleUploadLoginImage(uploadResources) {
      this.loginImageList.push(uploadResources.file);
    },
    handleUploadcss(uploadResources) {
      this.cssList.push(uploadResources.file);
    },
    handleExceed() {
      this.$error(this.$t('load_test.file_size_limit'));
    },
    handleDelete(name) {
      this[name] = [];
    },
    edit() {
      this.showEdit = false;
      this.showSave = true;
      this.showCancel = true;
      this.show = false;
    },
    setAsideTheme() {
      if (this.formInline.sideTheme === "theme-light") {
        setLightColor();
      } else if (this.formInline.sideTheme === "theme-default") {
        setAsideColor();
      } else {
        setCustomizeColor(this.formInline.theme);
      }
    },
    save() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;

      let data = this.getSaveOption();
      localStorage.removeItem('color');
      localStorage.removeItem('sysTitle');
      localStorage.removeItem('sideTheme');
      this.loading = true;
      saveDisplay(data)
        .then(() => {
          this.loading = false;
          if (this.formInline.theme) {
            localStorage.setItem('color', this.formInline.theme);
          }
          if (this.formInline.sysTitle) {
            localStorage.setItem("sysTitle", this.formInline.sysTitle);
          }
          if (this.formInline.sideTheme) {
            localStorage.setItem("sideTheme", this.formInline.sideTheme);
          }
          if (this.formInline.logo) {
            this.shortcutIcon();
          }
          this.setAsideTheme();
          this.$success(this.$t('commons.save_success'));
          window.location.reload();
        });
    },
    getSaveOption() {
      let formData = new FormData();
      let uploadList = [];
      if (this.logoList.length > 0) {
        let file = this.logoList[0];
        let name = 'ui.logo' + "," + file.name;
        if (!file.db) {
          let newfile = new File([file], name, {type: file.type});
          uploadList.push(newfile);
        }
      }
      if (this.loginLogoList.length > 0) {
        let file = this.loginLogoList[0];
        let name = 'ui.loginLogo' + "," + file.name;
        if (!file.db) {
          let newfile = new File([file], name, {type: file.type});
          uploadList.push(newfile);
        }
      }
      if (this.loginImageList.length > 0) {
        let file = this.loginImageList[0];
        let name = 'ui.loginImage' + "," + file.name;
        if (!file.db) {
          let newfile = new File([file], name, {type: file.type});
          uploadList.push(newfile);
        }
      }
      if (this.cssList.length > 0) {
        let file = this.cssList[0];
        let name = 'ui.css' + "," + file.name;
        if (!file.db) {
          let newfile = new File([file], name, {type: file.type});
          uploadList.push(newfile);
        }
      }

      if (uploadList.length > 0) {
        uploadList.forEach(f => {
          formData.append("file", f);
        });
      }
      let param = [
        {
          paramKey: "ui.logo",
          paramValue: this.formInline.logo,
          type: "file",
          fileName: this.logoList[0] ? this.logoList[0].name : null,
          sort: 1
        },
        {
          paramKey: "ui.loginLogo",
          paramValue: this.formInline.loginLogo,
          type: "file",
          fileName: this.loginLogoList[0] ? this.loginLogoList[0].name : null,
          sort: 2
        },
        {
          paramKey: "ui.loginImage",
          paramValue: this.formInline.loginImage,
          type: "file",
          fileName: this.loginImageList[0] ? this.loginImageList[0].name : null,
          sort: 3
        },
        {paramKey: "ui.loginTitle", paramValue: this.formInline.loginTitle, type: "text", sort: 4},
        {paramKey: "ui.sysTitle", paramValue: this.formInline.sysTitle, type: "text", sort: 7},
        {paramKey: "ui.sideTheme", paramValue: this.formInline.sideTheme, type: "text", sort: 8},
        {paramKey: "ui.title", paramValue: this.formInline.title, type: "text", sort: 5},
        {paramKey: "ui.theme", paramValue: this.formInline.theme, type: "text", sort: 6},
        {
          paramKey: "ui.css",
          paramValue: this.formInline.css,
          type: "file",
          fileName: this.cssList[0] ? this.cssList[0].name : null,
          sort: 9
        },
      ]
      let requestJson = JSON.stringify(param);
      formData.append('request', new Blob([requestJson], {
        type: "application/json"
      }));

      return formData;
    },
    query() {
      getDisplayInfo()
        .then(response => {
          if (response.data[0].paramValue) {
            this.formInline.logo = response.data[0].paramValue;
            this.logoList.push({name: response.data[0].fileName, db: true});
          }
          if (response.data[1].paramValue) {
            this.formInline.loginLogo = response.data[1].paramValue;
            this.loginLogoList.push({name: response.data[1].fileName, db: true});
          }
          if (response.data[2].paramValue) {
            this.formInline.loginImage = response.data[2].paramValue;
            this.loginImageList.push({name: response.data[2].fileName, db: true});
          }
          this.formInline.loginTitle = response.data[3].paramValue;
          if (response.data[6]) {
            this.formInline.sysTitle = response.data[6].paramValue;
          }
          if (response.data[7]) {
            this.formInline.sideTheme = response.data[7].paramValue;
          }
          this.formInline.title = response.data[4].paramValue;
          if (response.data[5] && response.data[5].paramValue) {
            this.formInline.theme = response.data[5].paramValue;
          }
          if (response.data[8].paramValue) {
            this.formInline.css = response.data[8].paramValue;
            this.cssList.push({name: response.data[8].fileName, db: true});
          }
          if (response.data[0].paramValue) {
            this.shortcutIcon();
          }
          this.setAsideTheme();

        })
    },
    shortcutIcon() {
      let link = document.querySelector("link[rel*='icon']") || document.createElement('link');
      link.type = 'image/x-icon';
      link.rel = 'shortcut icon';
      link.href = '/display/file/logo';
      document.getElementsByTagName('head')[0].appendChild(link);
    },
    cancel() {
      this.showEdit = true;
      this.showCancel = false;
      this.showSave = false;
      this.show = true;
      this.query();
    }
  },
}
</script>


<style scoped>
.ms-theme-setting {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-top: 10px;
  margin-bottom: 20px;
  margin-left: 50px;
}

.ms-theme-setting-item {
  position: relative;
  margin-right: 55px;
  border-radius: 2px;
  cursor: pointer;
  font-size: 12px;
  font-family: Microsoft YaHei;
}

img {
  width: 48px;
  height: 48px;
}

.ms-el-radio__label :deep(.el-radio__label) {
  font-size: 13px;
  padding-left: 2px;
}

:deep(.el-radio) {
  font-size: 13px;
  color: #606266;
  box-sizing: border-box;
  font-weight: normal;
}


.ms-theme-setting-selectIcon {
  position: absolute;
  top: 0;
  right: 10px;
  width: 100%;
  height: 100%;
  padding-top: 10px;
  margin-right: 40px;
  color: var(--color);
}

.ms-theme-setting-selectIcon-top {
  width: 48px;
  height: 60px;
}

.el-form-item--mini.el-form-item, .el-form-item--small.el-form-item {
  margin-bottom: 5px;
}
</style>
