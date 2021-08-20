<template>
  <div class="share-button">
    <el-popover
      placement="right"
      width="260">
      <p>{{shareUrl}}</p>
      <div style="text-align: right; margin: 0">
        <el-button type="primary" size="mini"
                   v-clipboard:copy="shareUrl">{{ $t("commons.copy") }}</el-button>
      </div>
      <i class="el-icon-share" @click="$emit('click')" slot="reference" style="margin-right: 10px;cursor: pointer"></i>
    </el-popover>
  </div>
</template>

<script>
export default {
  name: "MsShareButton",
  props: ['shareUrl']
}
</script>

<style scoped>
.share-button {
  margin: 10px;
  font-size: 18px;
}
</style>
