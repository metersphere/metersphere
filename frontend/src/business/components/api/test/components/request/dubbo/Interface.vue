<template>
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" size="small">
    <el-button class="get-provider" type="primary" size="small" @click="getProviderList">Get Provider List</el-button>
    <el-row>
      <el-col :span="12">
        <el-form-item label="Interfaces" prop="interfaces">
          <el-select v-model="request.interface" class="select-100">
            <el-option v-for="i in interfaces" :key="i.value" :label="i.label" :value="i.value"/>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="Methods" prop="methods">
          <el-select v-model="request.method" class="select-100">
            <el-option v-for="i in methods" :key="i.value" :label="i.label" :value="i.value"/>
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="12">
        <el-form-item label="Interface" prop="interface">
          <el-input v-model="request.interface" maxlength="300" show-word-limit
                    :placeholder="$t('commons.input_content')"/>
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="Method" prop="method">
          <el-input v-model="request.method" maxlength="300" show-word-limit
                    :placeholder="$t('commons.input_content')"/>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
  import {DubboRequest} from "@/business/components/api/test/model/ScenarioModel";

  export default {
    name: "MsDubboInterface",
    props: {
      request: DubboRequest
    },
    data() {
      return {
        interfaces: [],
        methods: [],
        rules: {
          interface: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
          method: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ]
        }
      }
    },

    methods: {
      getProviderList() {

      }
    }
  }
</script>

<style scoped>
  .get-provider {
    margin-bottom: 22px;
  }

  .select-100 {
    width: 100%;
  }
</style>
