<template>
  <div>
    <el-row :gutter="10" align="middle">
      <el-col :span="23">
        <el-input v-model="time" step="100" size="small" type="number"
                  :placeholder="$t('api_test.request.assertions.response_in_time')"/>
      </el-col>
      <el-col :span="1" class="assertion-btn">
        <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add" v-else/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {ResponseTime} from "../model/APIModel";

  export default {
    name: "MsApiAssertionResponseTime",

    props: {
      edit: Boolean,
      responseTime: ResponseTime
    },

    data() {
      return {
        time: this.responseTime.responseInTime
      }
    },

    methods: {
      add: function () {
        this.remove();
        setTimeout(() => {
          this.responseTime.responseInTime = this.time;
        })
      },
      remove: function () {
        this.responseTime.responseInTime = null;
      }
    }
  }
</script>

<style scoped>
  .assertion-btn {
    text-align: center;
  }
</style>
