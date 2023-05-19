<template>
  <div ref="pdfViewer" class="pdfViewer" v-loading="loading"></div>
</template>

<script>
import * as PdfJs from 'pdfjs-dist'
import pdfJsWorker from 'pdfjs-dist/build/pdf.worker.entry'
PdfJs.GlobalWorkerOptions.workerSrc = pdfJsWorker

export default {
  name: 'TestCasePdfViewBaseJs',
  props: {
    /**
     * pdf地址
     */
    pdfUrl: {
      type: String,
      default: '',
    },
    /**
     * cMap的url地址(用于解决中文乱码或中文显示为空白的问题). 该资源的物理路径在: sdk-frontend/assets/pdfjs-cmaps/目录下
     * 打包后通过gateway直接访问即可
     */
    cMapUrl: {
      type: String,
      default: '/assets/pdfjs-cmaps/',
    },
    /**
     * pdf缩放比例
     */
    scale: {
      type: Number,
      default: 1.85,
    },
  },
  data() {
    return {
      loading: false,
      pdfDocRef: null,
    }
  },
  watch: {
    scale() {
      this.renderPdf()
    },
  },
  mounted() {
    this.loading = true;
    this.renderPdf()
  },
  methods: {
    renderPdf() {
      const pdfViewerDom = this.$refs.pdfViewer;
      if (this.pdfUrl) {
        // 获取pdf文件
        const pdfLoadingTask = PdfJs.getDocument({
          url: this.pdfUrl,
          cMapUrl: this.cMapUrl,
          cMapPacked: true
        })
        pdfLoadingTask.promise.then(pdfDoc => {
          if (pdfDoc && pdfViewerDom) {
            // 缓存pdf内容
            this.pdfDocRef = pdfDoc
            this.loading = false;
            this.renderPdfCanvas(pdfViewerDom, pdfDoc, this.scale)
          }
        })
      }
    },
    /**
     * 渲染pdf文件的指定页到画板
     * @param pdfViewerDom 承载pdf画板的dom容器
     * @param pdfDoc pdf文件
     * @param pageNum 需要渲染的页码
     * @param scale 缩放比例
     */
    renderPdfOnePage(pdfViewerDom, pdfDoc, pageNum, scale) {
      // 创建画布
      const canvas = document.createElement('canvas')
      pdfViewerDom.appendChild(canvas)
      // 获取2d上下文
      const context = canvas.getContext('2d')
      pdfDoc.getPage(pageNum).then(page => {
        // 获取当前pdf页内容, 并设置缩放
        const viewport = page.getViewport({ scale: scale })
        const realCanvas = context.canvas
        realCanvas.width = viewport.width
        realCanvas.height = viewport.height
        // 将pdf当前页内容画到2d画板中
        // @ts-ignore
        page.render({ canvasContext: context, viewport })
      })
    },
    /**
     * 渲染pdf的画布
     * @param pdfViewerDom 承载pdf画布的dom容器
     * @param pdfDoc pdf文档
     * @param scale 缩放比例
     */
    renderPdfCanvas(pdfViewerDom, pdfDoc, scale) {
      // 清除原来的pdf画布
      pdfViewerDom.innerHTML = ''
      // 获取总页数
      const totalPage = pdfDoc.numPages
      // 获取父元素宽度
      // let pdfViewer = document.querySelector(".pdfViewer");
      // let pdfViewerWidth = window.getComputedStyle(pdfViewer).getPropertyValue("width").replaceAll("px", "")
      // 获取显示容器, canvas占满
      for (let i = 1; i <= totalPage; i++) {
        // 循环处理pdf的每页
        this.renderPdfOnePage(pdfViewerDom, pdfDoc, i, scale);
      }
    }
  },
}
</script>

<style scoped>
canvas {
  width: 100%!important;
}
</style>
