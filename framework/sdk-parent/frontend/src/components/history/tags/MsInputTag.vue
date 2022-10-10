<template>
  <div
    class="el-input-tag input-tag-wrapper"
    :class="[size ? 'el-input-tag--' + size : '']"
    style="height: auto">

    <el-tag
      :class="getClass(tag)"
      v-for="(tag) in innerTags"
      v-bind="$attrs"
      type="info"
      :key="tag"
      :size="size"
      :closable="!readOnly"
      :disable-transitions="false">
      {{ getTag(tag) }}
    </el-tag>
    <input
      :disabled="readOnly"
      class="tag-input el-input"
      v-model="newTag"
      :placeholder="$t('commons.tag_tip')"/>
  </div>
</template>
<script>
export default {
  name: 'MsInputTag',
  props: {
    data: {},
    addTagOnKeys: {
      type: Array,
      default: () => [13, 188, 9]
    },
    readOnly: {
      type: Boolean,
      default: false
    },
    size: {type: String, default: "small"},
    prop: {
      type: String,
      default: "diffValue"
    }
  },
  created() {
    if (!this.data[this.prop]) {
      this.data[this.prop] = [];
    }
  },
  data() {
    return {
      newTag: '',
      innerTags: this.data[this.prop] ? [...this.data[this.prop]] : []
    }
  },
  watch: {
    innerTags() {
      this.data[this.prop] = this.innerTags;
    }
  },
  methods: {
    getTag(tag) {
      if (tag && (tag.indexOf("++") !== -1 || tag.indexOf("--") !== -1)) {
        tag = tag.substring(2);
      }
      return tag && tag.length > 10 ? tag.substring(0, 10) + "..." : tag;
    },
    getClass(tag) {
      if (tag && tag.indexOf("++") !== -1) {
        return "ms-tag-add";
      }
      if (tag && tag.indexOf("--") !== -1) {
        return "ms-tag-del";
      }
      return "";
    }
  }
}
</script>

<style scoped>
.input-tag-wrapper {
  position: relative;
  font-size: 14px;
  background-color: #fff;
  background-image: none;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
  color: #606266;
  display: inline-block;
  outline: none;
  padding: 0 10px 0 5px;
  transition: border-color .2s cubic-bezier(.645, .045, .355, 1);
  width: 100%;
}

.el-tag {
  margin-right: 4px;
}

.tag-input {
  background: transparent;
  border: 0;
  color: #303133;
  font-size: 12px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", Arial, sans-serif;
  outline: none;
  padding-left: 0;
  width: 100px;
}

.el-input-tag {
  height: 40px;
  line-height: 40px;
}

.el-input-tag--mini {
  height: 28px;
  line-height: 28px;
  font-size: 12px;
}

.el-input-tag--small {
  line-height: 30px;
}

.el-input-tag--medium {
  height: 36px;
  line-height: 36px;
}

.ms-tag-del {
  text-decoration: line-through;
  text-decoration-color: red;
  -moz-text-decoration-line: line-through;
  background: #F3E6E7;
}

.ms-tag-add {
  background: #E2ECDC;
}
</style>
