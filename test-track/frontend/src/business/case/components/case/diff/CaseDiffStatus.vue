<template>
  <div class="diff-wrap">
    <div class="status diff-create" v-if="diffStatus == 1">
      {{ getDIffContent(diffStatus) }}
    </div>
    <div class="status diff-delete" v-if="diffStatus == 2">
      {{ getDIffContent(diffStatus) }}
    </div>
    <div class="status diff-change" v-if="diffStatus == 3">
      {{ getDIffContent(diffStatus) }}
    </div>
  </div>
</template>
<script>
export default {
  name: "CaseDiffStatus",
  props: {
    diffStatus: Number,
  },
  methods: {
    getDIffContent(status) {
      if (status == 1) {
        return "新建";
      }
      if (status == 2) {
        return "删除";
      }
      if (status == 3) {
        return "格式变化";
      }
      return "";
    },
  },
};
</script>
<style lang="scss" scoped>
.diff-create {
  min-width: 32px;
  padding: 0px 4px;
  height: 16px;
  background: rgba(52, 199, 36, 0.2);
  border-radius: 2px;
  line-height: 16px;
  color: #2ea121;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
}
.diff-delete {
  min-width: 32px;
  padding: 0px 4px;
  height: 16px;
  background: rgba(245, 74, 69, 0.2);
  border-radius: 2px;
  line-height: 16px;
  color: #d83931;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
}
.diff-change {
  min-width: 56px;
  padding: 0px 4px;
  height: 16px;
  background: rgba(255, 136, 0, 0.2);
  border-radius: 2px;
  line-height: 16px;
  color: #de7802;
  font-weight: 500;
  font-size: 12px;
  text-align: center;
}
.line-through {
  text-decoration-line: line-through;
  color: #8f959e !important;
  font-size: 14px;
  font-weight: 400;
  font-size: 14px;
}
</style>
