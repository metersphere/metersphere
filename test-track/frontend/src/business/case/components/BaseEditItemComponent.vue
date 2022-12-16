<template>
  <div>
    <template v-if="edit">
      <slot name="header"></slot>
      <slot
        name="content"
        :editFactor="selfEditable"
        :autoSave="autoSave"
      ></slot>
      <slot name="footer">
        <!-- !autoSave -->
      </slot>
    </template>
    <template v-else>
      <slot name="readonly">
        <template>
          <div
            class="text"
            v-if="contentObject.content && contentObject.contentType == 'TEXT'"
            @click="handleReadTextClick"
          >
            {{ contentObject.content }}
          </div>
          <div
            class="select"
            v-else-if="
              contentObject.content && contentObject.contentType == 'SELECT'
            "
            @click="handleReadTextClick"
          >
            {{ contentObject.content }}
          </div>
          <div class="empty" v-else>暂无</div>
        </template>
      </slot>
    </template>
  </div>
</template>
<script>
export default {
  name: "BaseEditItemComponent",
  data() {
    return {
      selfEditable: false,
    };
  },
  props: {
    editable: {
      type: Boolean,
      default: true,
    },
    autoSave: {
      type: Boolean,
      default: true,
    },
    contentObject: {
      type: Object,
      default() {
        return { prefix: "", content: "", suffix: "", contentType: "TEXT" };
      },
    },
  },
  computed: {
    edit() {
      console.log(this.selfEditable);
      return this.editable || this.selfEditable;
    },
  },
  methods: {
    changeSelfEditable(edit) {
      this.selfEditable = edit;
    },
    handleReadTextClick() {
      this.selfEditable = true;
    },
    handleReadTextHover() {
      this.selfEditable = true;
    },
  },
};
</script>
<style scoped lang="scss">
@import "@/business/style/index.scss";
.text {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1f2329;
}
.empty {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #8f959e;
}
.select {
  width: px2rem(256);
  height: 32px;
  line-height: 32px;
  padding-left: px2rem(6);
  padding-right: px2rem(6);
}
.select:hover {
  background: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
  cursor: pointer;
}
</style>
