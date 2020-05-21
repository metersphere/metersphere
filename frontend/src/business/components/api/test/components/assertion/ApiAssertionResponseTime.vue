<template>
  <div>
    <el-row :gutter="10" type="flex" justify="space-between" align="middle">
      <el-col>
        <el-input v-model="time" step="100" size="small" type="number"
                  :placeholder="$t('api_test.request.assertions.response_in_time')"/>
      </el-col>
      <el-col class="assertion-btn">
        <el-button type="danger" size="mini" icon="el-icon-delete" circle @click="remove" v-if="edit"/>
        <el-button type="primary" size="small" icon="el-icon-plus" plain @click="add" v-else/>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import {ResponseTime} from "../../model/ScenarioModel";

  export default {
    name: "MsApiAssertionResponseTime",

    props: {
      edit: Boolean,
      duration: ResponseTime,
      callback: Function
    },

    data() {
      return {
        time: this.duration.value
      }
    },

    methods: {
      add: function () {
        setTimeout(() => {
          this.duration.value = this.time;
          this.callback();
        })
      },
      remove: function () {
        this.duration.value = undefined;
      }
    }
  }
</script>

<style scoped>
  .assertion-btn {
    text-align: center;
    width: 60px;
  }
</style>
