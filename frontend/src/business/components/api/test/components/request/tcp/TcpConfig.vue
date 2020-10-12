<template>
  <el-form class="tcp" :model="config" :rules="rules" ref="request" label-width="auto" :disabled="isReadOnly">

    <el-form-item label="TCPClient" prop="classname">
      <el-select v-model="config.classname" style="width: 100%">
        <el-option v-for="c in classes" :key="c" :label="c" :value="c"/>
      </el-select>
    </el-form-item>

    <el-row type="flex" justify="space-between">
      <el-col :lg="8">
        <el-form-item :label="$t('api_test.request.tcp.server')" prop="server">
          <el-input v-model="config.server" maxlength="300" show-word-limit/>
        </el-form-item>
      </el-col>
      <el-col :lg="6">
        <el-form-item :label="$t('api_test.request.tcp.port')" prop="port" label-width="60px">
          <el-input-number v-model="config.port" controls-position="right" :min="0" :max="65535"/>
        </el-form-item>
      </el-col>
      <el-col :lg="6">
        <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout" label-width="80px">
          <el-input-number v-model="config.ctimeout" controls-position="right" :min="0"/>
        </el-form-item>
      </el-col>
      <el-col :lg="6">
        <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout" label-width="80px">
          <el-input-number v-model="config.timeout" controls-position="right" :min="0"/>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row type="flex" justify="space-between">
      <el-col>
        <el-form-item label-width="0">
          <el-switch
            v-model="config.reUseConnection"
            :active-text="$t('api_test.request.tcp.re_use_connection')">
          </el-switch>
        </el-form-item>
      </el-col>
      <el-col>
        <el-form-item label-width="0">
          <el-switch
            v-model="config.closeConnection"
            :active-text="$t('api_test.request.tcp.close_connection')">
          </el-switch>
        </el-form-item>
      </el-col>
      <el-col>
        <el-form-item label-width="0">
          <el-switch
            v-model="config.nodelay"
            :active-text="$t('api_test.request.tcp.no_delay')">
          </el-switch>
        </el-form-item>
      </el-col>
      <el-col>
        <el-form-item :label="$t('api_test.request.tcp.so_linger')" prop="soLinger">
          <el-input v-model="config.soLinger"/>
        </el-form-item>
      </el-col>
      <el-col>
        <el-form-item :label="$t('api_test.request.tcp.eol_byte')" prop="eolByte">
          <el-input v-model="config.eolByte"/>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="10">
      <el-col :span="12">
        <el-form-item :label="$t('api_test.request.tcp.username')" prop="username">
          <el-input v-model="config.username" maxlength="100" show-word-limit/>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item :label="$t('api_test.request.tcp.password')" prop="password">
          <el-input v-model="config.password" maxlength="30" show-word-limit show-password
                    autocomplete="new-password"/>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import {TCPConfig} from "@/business/components/api/test/model/ScenarioModel";

export default {
  name: "MsTcpConfig",
  props: {
    config: TCPConfig,
    rules: {
      type: Object,
      default: () => {
        return {}
      }
    }
  },
  data() {
    return {
      classes: TCPConfig.CLASSES,
    }
  },
}
</script>

<style scoped>
.tcp >>> .el-input-number {
  width: 100%;
}
</style>
