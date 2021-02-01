<template>
  <div>
    <div v-if="request.protocol === 'HTTP'">
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-if="request.url" v-model="request.url" style="width: 85%;margin-top: 10px" size="small">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
      <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-else v-model="request.path" style="width: 85%;margin-top: 10px" size="small">
        <el-select v-model="request.method" slot="prepend" style="width: 100px" size="small">
          <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
      </el-input>
    </div>

    <div v-if="request.protocol === 'TCP' && isCustomizeReq">
      <el-form>
        <el-row>
          <el-col :span="8">
            <el-form-item :label="$t('api_test.request.tcp.server')" prop="server">
              <el-input class="server-input"  v-model="request.server" maxlength="300" show-word-limit size="small"/>
            </el-form-item>
          </el-col>
         <el-col :span="12">
           <el-form-item :label="$t('api_test.request.tcp.port')" prop="port" label-width="60px">
             <el-input-number v-model="request.port" controls-position="right" :min="0" :max="65535" size="small"/>
           </el-form-item>
         </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script>
import {REQ_METHOD} from "@/business/components/api/definition/model/JsonData";

export default {
  name: "CustomizeReqInfo",
  props: ['request', 'isCustomizeReq'],
  data() {
    return {
      reqOptions: REQ_METHOD,
    }
  }
}
</script>

<style scoped>
  .server-input {
    width: 50%;
  }
</style>
