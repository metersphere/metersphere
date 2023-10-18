<template>
  <div
    class="el-input-tag input-tag-wrapper"
    :class="[size ? 'el-input-tag--' + size : '']"
    style="height: auto"
    @click="focusTagInput">

    <el-tag
      class="ms-top"
      v-for="(tag, idx) in innerTags"
      v-bind="$attrs"
      type="info"
      :key="tag"
      :size="size"
      :closable="!readOnly"
      :disable-transitions="false"
      @close="remove(idx)">
      <el-tooltip class="item" effect="light" :content="tag" placement="top" :enterable="false" v-if="tag && tag.length > 10">
        <span>{{ tag && tag.length > 10 ? tag.substring(0, 10) + "..." : tag }}</span>
      </el-tooltip>
      <span v-else>
        {{ tag }}
      </span>
    </el-tag>
    <input
      :disabled="readOnly"
      class="tag-input el-input"
      v-model="newTag"
      :placeholder=defaultPlaceHolder
      @keydown.delete.stop="removeLastTag"
      @keydown="addNew"
      @blur="addNew"/>
  </div>
</template>

<script>
export default {
  name: 'MsInputTag',
  props: {
    currentScenario: {},
    placeholder: {
      type: String,
    },
    errorInfo: String,
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
      default: "tags"
    }
  },
  created() {
    if (!this.currentScenario[this.prop]) {
      this.currentScenario[this.prop] = [];
    }
    if (this.placeholder) {
      this.defaultPlaceHolder = this.placeholder;
    }
  },
  data() {
    return {
      defaultPlaceHolder: this.$t('commons.tag_tip'),
      newTag: '',
      innerTags: this.currentScenario[this.prop] ? [...this.currentScenario[this.prop]] : []
    }
  },
  watch: {
    innerTags() {
      this.currentScenario[this.prop] = this.innerTags;
      this.tagChange();
    },
    'currentScenario.tags'() {
      if (this.prop === 'tags') {
        if (!this.currentScenario[this.prop] || this.currentScenario[this.prop] === '' || this.currentScenario[this.prop].length === 0) {
          if (this.innerTags.length !== 0) {
            this.innerTags = [];
          }
        }
      }

    },
  },
  methods: {
    focusTagInput() {
      if (!this.readOnly && this.$el.querySelector('.tag-input')) {
        this.$el.querySelector('.tag-input').focus()
      }
    },
    addNew(e) {
      if (e && (!this.addTagOnKeys.includes(e.keyCode)) && (e.type !== 'blur')) {
        return
      }
      if (e) {
        e.stopPropagation()
        e.preventDefault()
      }
      let addSuccess = false
      if (this.newTag.includes(',')) {
        this.newTag.split(',').forEach(item => {
          if (this.addTag(item.trim())) {
            addSuccess = true
          }
        })
      } else {
        if (this.addTag(this.newTag.trim())) {
          addSuccess = true
        }
      }
      if (addSuccess) {
        this.tagChange()
        this.newTag = ''
      }
      this.$emit("onblur");
    },
    addTag(tag) {
      tag = tag.trim()
      if (tag.length > 50) {
        this.$error(this.$t("commons.tag_length_tip", [50]));
        return false;
      }
      if (tag && !this.innerTags.includes(tag)) {
        this.innerTags.push(tag)
        return true
      } else {
        if (tag !== "" && this.errorInfo) {
          this.$error(this.errorInfo);
        }
      }
      return false
    },
    remove(index) {
      this.innerTags.splice(index, 1);
      this.tagChange();
      this.$nextTick(() => {
        //删除tag元素操作是在输入框中去掉元素，也应当触发onblur操作
        this.$emit("onblur");
      });
    },
    removeLastTag() {
      if (this.newTag) {
        return
      }
      this.innerTags.pop()
      this.tagChange()
    },
    tagChange() {
      this.$emit('input', this.innerTags)
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
  outline: none;
  padding-left: 0;
  width: auto;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  margin-left: 12px;
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

span.ms-top.el-tag.el-tag--info.el-tag--small.el-tag--light {
  flex-direction: row;
  align-items: center;
  padding: 1px 6px;
  gap: 4px;
  height: 24px;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 2px;
  flex: none;
  flex-grow: 0;
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  position: relative;
  top: 6px;

}

span.ms-top.el-tag.el-tag--info.el-tag--small.el-tag--light span{
  display: inline-block;
  max-width: 500px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgb(31, 35, 41);
}

:deep(.el-tag .el-icon-close::before) {
  display: block;
  font-size: 26px;
  position: relative;
  top: -15px;
  left: -6px;
}
</style>
