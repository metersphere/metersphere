<template>
  <div @click="exe" class="recycle" :class="{'is-active': condition.publicEnable}" v-if="this.isXpack">
    <el-row>
      <el-col :span="21"><i class="el-icon-reading"> {{ $t('project.case_public') }}</i></el-col>
      <el-col :span="3"><span style="color: #6C317C">{{ publicTotal }}</span></el-col>
    </el-row>
  </div>
</template>

<script>
import {hasLicense} from "@/common/js/utils";

export default {
  name: "ModulePublicButton",
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
    publicTotal: Number,
  },
  data() {
    return {
      isXPack: false
    }
  },
  created() {
    if (hasLicense()) {
      this.isXpack = true;
    } else {
      this.isXpack = false;
    }
  },
  methods: {
    // enableTrash() {
    //
    // }
  }
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
  background-color: #f3f6f9;
}

</style>
