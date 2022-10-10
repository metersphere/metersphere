export default {
  id: "commonStore",
  state: () => ({
    fileDownloadList: [], // 文件下载进度列表,
  }),
  persist: false,
  getters: {},
  actions: {
    updateDownProgress(downloadFile) {
      // 实时更新下载进度条
      let loadingFile = this.fileDownloadList.find(item => item.id === downloadFile.id);
      if (loadingFile) {
        loadingFile.progress = downloadFile.progress;
      } else {
        this.fileDownloadList.push(downloadFile);
      }
    },
    deleteDownloadFile(props) {
      this.fileDownloadList.splice(this.fileDownloadList.findIndex(item => item.id === props), 1);
    },
  }
}
