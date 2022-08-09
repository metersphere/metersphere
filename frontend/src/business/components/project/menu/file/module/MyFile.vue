<template>
  <div @click="exe" class="recycle" :class="(condition.filters && condition.filters.createUser.length >0) ? 'is-active': ''">
    <el-row>
      <el-col :span="21"><i class="el-icon-folder"> {{ $t('project.my_file') }}</i></el-col>
      <el-col :span="3"><span style="color: var(--primary_color);">{{ total }}</span></el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "MsMyFile",
  props: {
    condition: {
      type: Object,
      default() {
        return {};
      }
    },
    exe: {
      type: Function
    },
    total: Number,
  },
}
</script>

<style scoped>

.recycle {
  padding-left: 25px;
  margin-top: 15px;
  height: 26px;
  line-height: 26px;
  margin-bottom: -10px;
}

.recycle:hover {
  color: #6d317c;
  cursor: pointer;
}

.is-active {
  background-color: #f0f7ff;
}

</style>
