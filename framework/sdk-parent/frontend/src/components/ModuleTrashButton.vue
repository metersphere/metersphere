<template>
  <div @click="exe" class="recycle">
    <div class="to-public">
      <svg-icon icon-class="icon_delete-trash_filled" style="width: 1.3em;height: 1.3em;"/>
      <span style="position: relative;top: -0.5px;left: 11px;">{{ $t('commons.trash') }}</span>
    </div>
  </div>
</template>

<script>
export default {
  name: "ModuleTrashButton",
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
  methods: {
    // enableTrash() {
    //
    // }
  }
}
</script>

<style scoped>

.recycle {
  height: 40px;
  position: absolute;
  bottom: 0;
  left: 0;
  margin: 0;
  width: 100%;
  border-top: 1px solid rgba(31, 35, 41, 0.15);
  font-family: 'PingFang SC';
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  flex: none;
  order: 3;
  flex-grow: 0;
  overflow-x: hidden;
  overflow-y: hidden;
}

.recycle:hover {
  background: rgba(31, 35, 41, 0.1);;
}

.recycle:hover {
  cursor: pointer;
}

i.el-icon-delete {
  margin-top: 10px;
  margin-left: 24px;
  min-width: 200px;
  display: flex;
}

.el-icon-delete:before {
  margin-right: 4px;
  font-size: 14px;
}

.to-public {
  display: flex;
  padding: 9px 24px;
  min-width: 200px;
}
</style>
