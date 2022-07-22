<template>
  <div></div>
</template>

<script>
export default {
  name: "DownloadNotice",
  data() {
    return {
      notify: {}
    }
  },
  watch: {
    '$store.state.fileDownloadList': {
      handler(data) {
        JSON.parse(JSON.stringify(data)).forEach(item => {
          const domList = [...document.getElementsByClassName(item.id)];
          let tipDialog = domList.find(i => i.className == item.id);
          if (tipDialog) {
            // 更新对话框的进度
            tipDialog.innerText = item.progress + '%';
          } else {
            if (!item.progress) {
              // 容错处理，后端报错，删除当前进度对象
              this.$store.commit('deleteDownloadFile', item.id);
              return;
            }
            // 新建弹框，并在notify中加入该弹框对象
            this.notify[item.id] = this.$notify.success({
              dangerouslyUseHTMLString: true,
              message: `<p style="width: 100px;">
                            正在下载
                            <span class="${item.id}" style="float: right">
                                   ${item.progress}%
                            </span>
                        </p>`,
              showClose: false,
              duration: 0
            });
          }

          if (item.progress === 100) {
            this.notify[item.id].close();
            // close()事件是异步的，这里直接删除会报错，利用 setTimeout
            setTimeout(() => delete this.notify[item.id], 1000);
            this.$store.commit('deleteDownloadFile', item.id);
          }
        })
      },
      deep: true
    }
  }

}
</script>

<style scoped>

</style>
