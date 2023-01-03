<template>
  <el-row type="flex" justify="center">
    <el-card class="license-card" :body-style="{padding: '0'}">
      <div class="license-container">
        <div class="license-header">
          <img src="/assets/logo-dark-MeterSphere.svg" alt="">
        </div>
        <div class="license-content">
          <div v-if="license.status !== 'Fail'">
            <table>
              <tr v-if="license.serialNo">
                <th>{{ $t('license.serial_num') }}</th>
                <td>{{ license.serialNo }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.corporation') }}</th>
                <td>{{ license.corporation }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.time') }}</th>
                <td>
                  <label v-if="status === 'expired'" style="color: red">
                    {{ license.expired }} ({{ $t('license.expired') }})
                  </label>
                  <label v-else>{{ license.expired }}</label>
                </td>
              </tr>
              <tr>
                <th>{{ $t('license.product') }}</th>
                <td>{{ license.product }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.edition') }}</th>
                <td>{{ license.edition }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.licenseVersion') }}</th>
                <td>{{ license.licenseVersion }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.count') }}</th>
                <td>{{ license.count }}</td>
              </tr>
              <tr>
                <th>{{ $t('license.status') }}</th>
                <td>
                  <label class="ms-license-label" v-if="status === 'expired'">
                    {{ $t('license.expired') }}
                  </label>
                  <label class="ms-license-label" v-else-if="status === 'valid'">
                    {{ $t('license.valid') }}
                  </label>
                  <label class="ms-license-label" v-else>{{ $t('license.invalid') }}</label>
                </td>
              </tr>
              <tr v-if="license.remark">
                <th>{{ $t('license.remark') }}</th>
                <td>{{ license.remark }}</td>
              </tr>
            </table>
          </div>
          <el-link type="primary" class="license-update" @click="create()" :disabled="disabled">
            {{ $t('license.valid_license') }}
          </el-link>
          <ms-valid-license :visible.sync="visible" @confirm="confirm"></ms-valid-license>
        </div>
      </div>
    </el-card>
  </el-row>
</template>

<script>
import MsValidLicense from "./ValidLicense";
import {hasPermission, saveLicense, hasLicense} from "metersphere-frontend/src/utils/permission";
import {getLicense} from "../../../api/license";
import {getModuleList} from "metersphere-frontend/src/api/module";

export default {
  name: "MxLicense",
  components: {
    MsValidLicense,
  },
  data() {
    return {
      visible: false,
      result: {},
      license: {},
      status: '',
    }
  },
  computed: {
    disabled() {
      return !hasPermission("SYSTEM_AUTH:READ+EDIT");
    }
  },
  created() {
    this.search();
  },
  methods: {
    search() {
      this.result = getLicense()
        .then(response => {
          if (response.data.license) {
            this.license = response.data.license;
          }
          this.status = response.data.status;
        })
    },
    create() {
      this.visible = true;
    },
    confirm(value) {
      if (value !== undefined && value !== null && value.license != null) {
        this.license.corporation = value.license.corporation;
        this.license.expired = value.license.expired;
        this.license.product = value.license.product;
        this.license.edition = value.license.edition;
        this.license.licenseVersion = value.license.licenseVersion;
        this.license.licenseCount = value.license.licenseCount;
        this.license.status = value.status;
        this.license.serialNo = value.license.serialNo;
        this.license.remark = value.license.remark;
        saveLicense(value.status);
        if (hasLicense()) {
          getModuleList()
            .then(response => {
              let modules = {};
              response.data.forEach(m => {
                modules[m.key] = m.status;
              });
              localStorage.setItem('modules', JSON.stringify(modules));
              location.reload();
            })
        }
      }
    },
  }
}
</script>

<style scoped>
.license-card {
  padding: 0;
  margin-top: 5%;
  width: 640px;
  background-color: rgb(250, 250, 250);
}

.license-container {
  margin: auto;
  position: relative;
}

.license-header {
  height: 100px;
  text-align: center;
  padding: 20px 0;
  background-color: rgb(44, 42, 72);
}

.license-header img {
  width: 540px;
  height: 100px;
}

.license-content {
  font-size: 16px;
  padding: 20px 50px;
}

.license-content table {
  width: 100%;
}

.license-content table th {
  text-align: left;
  width: 45%;
}

.license-update {
  font-size: 16px;
  margin-top: 20px;
}
</style>
