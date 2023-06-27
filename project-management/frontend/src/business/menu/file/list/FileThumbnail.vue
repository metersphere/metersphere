<template>
  <div>
    <div style="height: calc(100vh - 160px);overflow-y:auto;overflow-x:hidden">
      <el-row :gutter="20">
        <el-col :span="4" v-for="item in data" :key="item.id">
          <el-card :body-style="{ padding: '0px' }" class="ms-card-item" @click.native="handleView(item)">
            <div v-loading="imageContents[item.id]==='' || imageContents[item.id] === 'loading'" class="ms-image"
                 v-if="isImage(item)">
              <img style="height: 100%;width: 100%;object-fit:cover;background-color: #FFFFFF"
                   :src="imageContents[item.id]"/>
            </div>
            <div class="ms-image" v-else>
              <div class="ms-file">
                <div class="icon-title">{{ getType(item.type) }}</div>
              </div>
            </div>
            <span class="ms-file-name-width">{{ item.name }}</span>
          </el-card>
        </el-col>
      </el-row>
    </div>
    <ms-table-pagination
      :current-page.sync="currentPage"
      :page-size.sync="pageSize"
      :total="total"
      :change="change"/>
    <ms-edit-file-metadata
      :module-options="nodeTree"
      :metadata-array="data"
      :condition="condition"
      @setCurrentPage="setCurrentPage"
      @download="handleDownload"
      @refreshModule="refreshModule"
      @delete="handleDelete"
      @reload="change" ref="editMetadata"/>
  </div>
</template>

<script>
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import MsEditFileMetadata from "../edit/EditFileMetadata";
import {getFileBytes} from "@/api/file";

export default {
  name: "MsFileThumbnail",
  components: {MsTablePagination, MsEditFileMetadata},
  data() {
    return {
      currentPage: 1,
      pageSize: 10,
      total: 0,
      imageContents: {},
      images: ["bmp", "jpg", "png", "tif", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd", "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "WMF", "webp", "avif", "apng", "jpeg"]
    };
  },
  props: {
    page: Number,
    size: Number,
    pageTotal: Number,
    data: Array,
    condition: Object,
    nodeTree: []
  },
  created() {
    this.imageContents = {};
    this.currentPage = this.page;
    this.pageSize = this.size;
    this.total = this.pageTotal;
  },
  watch: {
    pageTotal(val) {
      this.total = val;
    }
  },
  methods: {
    refreshModule() {
      this.$emit('refreshModule');
    },
    setCurrentPage(page) {
      this.currentPage = page;
      this.$emit("setCurrentPage", page);
    },
    handleDownload(data) {
      this.$emit("download", data);
    },
    handleDelete(data) {
      this.$emit("delete", data);
    },
    handleView(row) {
      this.$refs.editMetadata.open(row, this.pageSize, this.currentPage, this.total);
    },
    getType(type) {
      return type || "";
    },
    change() {
      this.$emit("change", this.pageSize, this.currentPage);
    },
    isImage(item) {
      let type = item.type;
      let isImage = (type && this.images.indexOf(type.toLowerCase()) !== -1);
      if (isImage) {
        if (isImage && (!this.imageContents[item.id] || this.imageContents[item.id] === '')) {
          this.$set(this.imageContents, item.id, "loading");
          getFileBytes(item.id).then(res => {
            let fileRsp = res.data;
            this.$set(this.imageContents, item.id, "data:image/png;base64," + fileRsp.bytes);
          })
        }
      }
      return isImage;
    }
  },
}
</script>


<style scoped>
.ms-image {
  width: 100%;
  height: 180px;
  display: block;
  background: #407C51;
  padding: 0px;
  margin: 0px;
}

.ms-image:hover {
  border-color: var(--color);
  cursor: pointer;
  transform: scale(1.0);
}

.ms-file {
  text-align: center;
  padding-top: 85px
}

.ms-file-name-width {
  padding: 5px 0px 5px 2px;
  font-size: 13px;
  display: inline-block;
  overflow-x: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 95%;
  height: 20px;
}

.icon-title {
  color: #fff;
  text-align: center;
  font-size: 16px;
}


.ms-card-item {
  margin-top: 10px;
}

</style>
