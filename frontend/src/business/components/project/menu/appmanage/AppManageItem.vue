<template>
  <div class="item_outer">
    <el-row type="flex" justify="space-between">
      <el-col :span="prependSpan">
        <div class="item_prepend">
          <slot name="prepend">
            <span :class="titleClass">{{ title }}</span>
            <span class="prepend_description">{{ description }}</span>
          </slot>
        </div>
      </el-col>
      <el-col :span="middleSpan">
        <div class="item_middle">
          <slot name="middle"></slot>
        </div>
      </el-col>
      <el-col :span="appendSpan">
        <div class="item_append">
          <slot name="append"></slot>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
export default {
  name: "AppManageItem",
  props: {
    title: {
      type: String,
      default() {
        return "";
      }
    },
    description: {
      type: String,
      default() {
        return "";
      }
    },
    prependSpan: {
      type: Number,
      default() {
        return 12;
      }
    },
    middleSpan: {
      type: Number,
      default() {
        return 0;
      }
    },
    appendSpan: {
      type: Number,
      default() {
        return 12;
      }
    }
  },
  computed: {
    // todo 垂直居中
    titleClass() {
      return this.description ?
        ['prepend_title'] : ['prepend_title', 'item_prepend_lh'];
    }
  }
}
</script>

<style scoped>
.item_outer {
  border-radius: 2px;
  width: 580px;
  height: 35px;
  border: 1px solid #DCDFE6;
  padding-bottom: 15px;
}

.item_outer:not(:last-child) {
  margin-bottom: -1px;
}

.item_prepend {
  margin-top: 6px;
}

.item_prepend_lh {
  line-height: 35px;
}

.item_middle {
  line-height: 50px;
}

.item_append {
  position: absolute;
  line-height: 50px;
  right: 20px;
}

.prepend_title {
  margin-left: 10px;
}

.prepend_description {
  display: block;
  color: #929295;
  margin-left: 10px;
  font-size: 13px;
}
</style>
