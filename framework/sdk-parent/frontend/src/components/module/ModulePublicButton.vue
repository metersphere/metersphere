<template>
  <div class="recycle" v-if="this.isXpack" @click="exe">
    <div style="padding: 9px 13px 9px 13px;" class="to-public">
      <svg-icon icon-class="icon_folder-share" style="width: 1.3em;height: 1.3em;position: relative;top: 2px;left: 1px;margin-right: 9px;"/>
      <span style="margin-right: 12px;" class="to-public-content">{{ $t('project.case_public') }}</span>
      <span style="color: #8F959E">{{ '(' + publicTotal + ')' }}</span>
      <svg-icon icon-class="icon_arrow-right_outlined_1" class-name="enter-public" style="display: none; width: 1.1em; height: 1.1em; order: 100; margin-left: auto; margin-top: 2px;"/>
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
      isXpack: false
    }
  },
  created() {
    this.isXpack = hasLicense();
  },
}
</script>

<style scoped>
.recycle {
  padding-bottom: 24px;
  line-height: 26px;
  height: 26px;
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

.to-public-content {
  margin-left: 5px;
}

.to-public {
  width: auto;
  border-radius: 4px;
  overflow-x: hidden;
  display: flex;
}

.to-public:hover {
  background: rgba(31, 35, 41, 0.1);
}

.to-public:hover .enter-public{
  display: block!important;
}
</style>
