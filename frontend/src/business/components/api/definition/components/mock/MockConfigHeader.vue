<template>
  <el-header style="width: 100% ;padding: 0px">
    <el-card>
        <el-row>
          <el-col :span="1">
            {{$t('commons.name')}}
          </el-col>
          <el-col :span="11">
            <el-input class="ms-http-input" style="width: 80%" size="small" v-model="mockExpectConfig.name"/>
          </el-col>
          <el-col :span="1">
            {{$t('commons.tag')}}
          </el-col>
          <el-col :span="11">
              <ms-input-tag :currentScenario="mockExpectConfig" style="width: 80%;height: 100%;white-space: nowrap;overflow: hidden"  v-if="showHeadTable" ref="tag"/>
          </el-col>
        </el-row>
    </el-card>
  </el-header>
</template>

<script>

import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";

export default {
  name: "MockConfigHeader",
  components: {MsInputTag},
  data() {
    return {
    }
  },
  props: {
    mockExpectConfig: Object,
    showHeadTable:Boolean,
  },
  created() {
  },
  watch: {
  },
  methods: {
  }
}
</script>

<style scoped>

.el-card-btn {
  float: right;
  top: 20px;
  right: 0px;
  padding: 0;
  background: 0 0;
  border: none;
  outline: 0;
  cursor: pointer;
  font-size: 18px;
  margin-left: 30px;
}

.variable-combine {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 10px;
}

.el-col {
  height: 32px;
  line-height: 32px;
}

.ms-api-header-select {
  margin-left: 20px;
  min-width: 100px;
}

.el-col {
  height: 32px;
  line-height: 32px;
}

.api-el-tag {
  color: white;
}

.select-all {
  margin-right: 10px;
}

.ms-col-name {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 100px;
}
</style>
