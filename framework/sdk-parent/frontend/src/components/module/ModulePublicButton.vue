<template>
  <div class="recycle" v-if="this.isXpack" @click="exe">
    <div style="padding: 9px 13px 9px 13px;" class="to-public">
      <svg-icon icon-class="icon_folder-share" style="width: 1.3em;height: 1.3em;position: relative;top: 4px;left: 1px;margin-right: 9px;"/>
      <span style="margin-right: 12px;">{{ $t('project.case_public') }}</span>
      <span style="color: #8F959E">{{ '(' + publicTotal + ')' }}</span>
      <svg-icon icon-class="icon_arrow-right_outlined_1" style="float: right;position: relative;top: 5px;width: 1.3em;height: 1.3em;"/>
    </div>
  </div>
</template>

<script>
import {hasLicense} from "../../utils/permission";

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
  methods: {}
}
</script>

<style scoped>

.recycle {
  padding: 14px 0px 14px 13px px;
  height: 26px;
  line-height: 26px;
  height: 52px;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}

.recycle:hover {
  color: #6d317c;
  cursor: pointer;
}

.recycle span {
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  align-items: center;
  color: #1F2329;
  flex: none;
  order: 1;
  flex-grow: 0;
}

.to-public {
  width: auto;
  border-radius: 4px;
}

.to-public:hover {
  background: rgba(31, 35, 41, 0.1);
}
</style>
