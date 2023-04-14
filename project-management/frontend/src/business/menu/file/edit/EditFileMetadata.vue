<template>
  <el-dialog
    :close-on-click-modal="false"
    :visible.sync="visible"
    width="900px"
    @close="saveAndClose"
    destroy-on-close
    ref="editFile"
  >
    <span slot="title" class="dialog-footer">
      <span>{{ data.name }}</span>
      <i
        class="el-icon-download ms-header-menu"
        @click="download"
        v-permission="['PROJECT_FILE:READ+DOWNLOAD+JAR']"
      />
      <i
        class="el-icon-delete ms-header-menu"
        @click="deleteData"
        v-permission="['PROJECT_FILE:READ+DELETE+JAR']"
      />
      <el-button
        v-if="isRepositoryFile()"
        :loading="isPullBtnLoading"
        class="ms-header-menu"
        size="mini"
        @click="filePull"
        style="padding: 2px; font-size: 12px"
        >pull</el-button
      >
    </span>

    <el-tabs
      v-if="visible"
      tab-position="right"
      v-model="showPanel"
      :class="isRepositoryFile() ? '' : 'file-metadata-tab'"
    >
      <el-tab-pane
        name="baseInfo"
        :label="isRepositoryFile() ? $t('test_track.plan_view.base_info') : ''"
      >
        <el-row align="center" v-loading="loading">
          <el-col style="margin: 10px" :span="10">
            <el-row :gutter="20" style="background: #f5f6f8; height: 480px">
              <el-col :span="2" class="ms-left-col">
                <i
                  class="el-icon-arrow-left ms-icon-arrow"
                  @click="beforeData"
                />
              </el-col>
              <el-col :span="18" style="padding-top: 80px">
                <el-card
                  :body-style="{ padding: '0px' }"
                  v-if="isImage(data.type) && !isRepositoryFile()"
                >
                  <img
                    :src="'/project/file/metadata/info/' + data.id"
                    class="ms-edit-image"
                  />
                </el-card>
                <el-card :body-style="{ padding: '0px' }" v-else>
                  <div class="ms-edit-image">
                    <div class="ms-file-item">
                      <div class="icon-title">{{ getType(data.type) }}</div>
                    </div>
                  </div>
                </el-card>
              </el-col>
              <el-col :span="2" class="ms-right-col">
                <i
                  class="el-icon-arrow-right ms-icon-arrow"
                  @click="nextData"
                />
              </el-col>
            </el-row>
          </el-col>
          <el-col :span="13">
            <el-container>
              <el-main>
                <el-form
                  :model="data"
                  :rules="rules"
                  label-position="right"
                  label-width="80px"
                  size="small"
                  ref="form"
                >
                  <!-- 基础信息 -->
                  <el-form-item :label="$t('load_test.file_name')" prop="name">
                    <el-input
                      class="ms-file-item-input"
                      size="small"
                      v-model="data.name"
                      :disabled="isRepositoryFile() || !canEdit"
                      show-word-limit
                      @blur="save"
                    />
                  </el-form-item>
                  <el-form-item
                    :label="$t('commons.description')"
                    prop="description"
                  >
                    <el-input
                      class="ms-http-textarea"
                      v-model="data.description"
                      type="textarea"
                      :rows="2"
                      size="small"
                      @blur="save"
                    />
                  </el-form-item>

                  <el-form-item
                    v-if="isRepositoryFile()"
                    :label="$t('project.project_file.file.branch')"
                    prop="type"
                  >
                    <span>{{ fileBranch }}</span>
                  </el-form-item>

                  <el-form-item
                    v-if="isRepositoryFile()"
                    :label="$t('project.project_file.file.path')"
                    prop="type"
                  >
                    <span>{{ data.path }}</span>
                  </el-form-item>

                  <el-form-item :label="$t('load_test.file_type')" prop="type">
                    <span>{{ data.type }}</span>
                  </el-form-item>
                  <el-form-item :label="$t('load_test.file_size')" prop="size">
                    <span>{{ formatFileSize(data.size) }}</span>
                  </el-form-item>

                  <el-form-item
                    :label="$t('api_test.automation.tag')"
                    prop="tags"
                  >
                    <ms-input-tag :currentScenario="data" ref="tag" />
                  </el-form-item>

                  <el-form-item
                    :label="$t('test_track.case.module')"
                    prop="moduleId"
                  >
                    <ms-select-tree
                      :disabled="isRepositoryFile() || !canEdit"
                      size="small"
                      :data="moduleOptions"
                      :defaultKey="data.moduleId"
                      @getValue="setModule"
                      :obj="moduleObj"
                      clearable
                      checkStrictly
                    />
                  </el-form-item>

                  <el-form-item
                    :label="$t('project.creator')"
                    prop="createUser"
                  >
                    <span>{{ data.createUser }}</span>
                  </el-form-item>

                  <el-form-item
                    :label="$t('commons.create_time')"
                    prop="createTime"
                  >
                    <span>{{ data.createTime | datetimeFormat }}</span>
                  </el-form-item>

                  <el-form-item
                    :label="'加载Jar包'"
                    prop="loadJar"
                    v-if="data.type === 'JAR'"
                  >
                    <el-switch
                      v-model="data.loadJar"
                      :active-text="$t('project.file_jar_message')"
                      @change="save"
                      :disabled="!canEdit"
                    />
                  </el-form-item>
                  <el-form-item
                    v-if="isRepositoryFile()"
                    :label="$t('commons.version')"
                  >
                    {{ getCommitId() }}
                  </el-form-item>
                  <el-form-item
                    v-else
                    :label="$t('project.upload_file_again')"
                    prop="files"
                  >
                    <el-upload
                      style="width: 38px; float: left"
                      action="#"
                      :before-upload="beforeUploadFile"
                      :http-request="handleUpload"
                      :show-file-list="false"
                      v-permission="['PROJECT_FILE:READ+UPLOAD+JAR']"
                    >
                      <el-button icon="el-icon-plus" size="mini" />
                    </el-upload>
                  </el-form-item>
                </el-form>
              </el-main>
            </el-container>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane
        name="relevanceCase"
        v-if="isRepositoryFile()"
        :label="$t('test_track.review_view.relevance_case')"
      >
        <file-case-relevance-list :file-metadata-ref-id="data.refId" />
      </el-tab-pane>
      <el-tab-pane
        name="versionHistory"
        v-if="isRepositoryFile()"
        :label="$t('project.project_file.repository.version_history')"
      >
        <file-version-list :file-metadata-ref-id="data.refId" />
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script>
import { operationConfirm } from "metersphere-frontend/src/utils";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import {
  getFileMetaPages,
  modifyFileMeta,
  pullGitFile,
  uploadFileMeta,
} from "../../../../api/file";
import FileVersionList from "@/business/menu/file/list/FileVersionList";
import FileCaseRelevanceList from "@/business/menu/file/list/FileCaseRelevanceList";
import { hasPermission } from "metersphere-frontend/src/utils/permission";

export default {
  name: "MsEditFileMetadata",
  components: {
    MsSelectTree: () =>
      import("metersphere-frontend/src/components/select-tree/SelectTree"),
    MsInputTag: () => import("metersphere-frontend/src/components/MsInputTag"),
    FileVersionList,
    FileCaseRelevanceList,
  },
  data() {
    return {
      data: {},
      visible: false,
      isFirst: false,
      isLast: false,
      isPullBtnLoading: false,
      showPanel: "baseInfo",
      results: [],
      fileBranch: "",
      moduleObj: {
        id: "id",
        label: "name",
      },
      rules: {
        name: [
          {
            min: 1,
            max: 200,
            message: this.$t("commons.input_limit", [1, 200]),
            trigger: "blur",
          },
        ],
        description: [
          {
            max: 3000,
            message: this.$t("commons.input_limit", [0, 3000]),
            trigger: "blur",
          },
        ],
      },
      total: 0,
      pageSize: 10,
      loading: false,
      currentPage: 1,
      images: [
        "bmp",
        "jpg",
        "png",
        "tif",
        "gif",
        "pcx",
        "tga",
        "exif",
        "fpx",
        "svg",
        "psd",
        "cdr",
        "pcd",
        "dxf",
        "ufo",
        "eps",
        "ai",
        "raw",
        "WMF",
        "webp",
        "avif",
        "apng",
        "jpeg",
      ],
    };
  },
  computed: {
    canEdit() {
      return hasPermission("PROJECT_FILE:READ+UPLOAD+JAR");
    },
  },
  props: {
    moduleOptions: Array,
    metadataArray: {
      type: Array,
      default() {
        return [];
      },
    },
    condition: {
      type: Object,
      default() {
        return {};
      },
    },
  },
  methods: {
    getCommitId() {
      if (this.data && this.data.attachInfo) {
        return JSON.parse(this.data.attachInfo).commitId;
      } else {
        return "";
      }
    },
    filePull() {
      this.isPullBtnLoading = true;
      let formData = { id: this.data.id };
      pullGitFile(formData)
        .then(() => {
          this.$success(
            this.$t("commons.update") +
              this.$t("api_test.automation.request_success")
          );
          this.isPullBtnLoading = false;
          this.$emit("reload");
          this.close();
        })
        .catch(() => {
          this.isPullBtnLoading = false;
        });
    },
    beforeUploadFile(file) {
      if (!this.fileValidator(file)) {
        return false;
      }
      if (file.size / 1024 / 1024 > 500) {
        this.$warning(this.$t("api_test.request.body_upload_limit_size"));
        return false;
      }
      return true;
    },
    fileValidator(file) {
      return file.size > 0;
    },
    setModule(id, data) {
      if (this.data.moduleId !== id) {
        this.data.moduleId = id;
        this.save();
        this.$emit("refreshModule");
      }
    },
    close() {
      this.showPanel = "baseInfo";
      this.visible = false;
    },
    saveAndClose() {
      this.save();
      this.$emit("setCurrentPage", this.currentPage);
      this.$nextTick(() => {
        this.$emit("getProjectFiles");
        this.showPanel = "baseInfo";
        this.visible = false;
      });
    },
    open(data, size, page, t) {
      this.showPanel = "baseInfo";
      this.pageSize = size;
      this.currentPage = page;
      this.total = t;
      this.data = data;
      this.results = this.metadataArray;
      this.visible = true;
      if (this.isRepositoryFile() && data.attachInfo) {
        let attachInfoObj = JSON.parse(data.attachInfo);
        this.fileBranch = attachInfoObj.branch;
      } else {
        this.fileBranch = "";
      }
    },
    isRepositoryFile() {
      return this.data.storage === "GIT";
    },
    save() {
      this.$refs["form"].validate((valid) => {
        if (valid) {
          let request = JSON.parse(JSON.stringify(this.data));
          request.tags = JSON.stringify(request.tags);
          if (request.tags && request.tags.length > 1000) {
            this.$warning(
              this.$t("api_test.automation.tag") +
                this.$t("commons.input_limit", [0, 1000])
            );
            return;
          }
          this.loading = true;
          modifyFileMeta(request)
            .then(() => {
              this.loading = false;
              this.$emit("reload");
            })
            .catch(() => {
              this.$error(this.$t("commons.save_failed"));
              this.loading = false;
              this.$emit("reload");
            });
        }
      });
    },
    getType(type) {
      if (this.isRepositoryFile()) {
        return "Repository " + type || "";
      } else {
        return type || "";
      }
    },
    isImage(type) {
      return type && this.images.indexOf(type.toLowerCase()) !== -1;
    },
    download() {
      this.$emit("download", this.data);
    },
    deleteData() {
      operationConfirm(
        this,
        this.$t("project.file_delete_tip", [this.data.name]),
        () => {
          this.close();
          this.data.confirm = true;
          this.$emit("delete", this.data);
        }
      );
    },
    handleUpload(uploadResources) {
      let file = uploadResources.file;
      uploadFileMeta(file, this.data).then((res) => {
        this.$success(this.$t("commons.save_success"));
        if (res.data && res.data.data) {
          this.data = res.data.data;
          if (this.data.tags) {
            this.data.tags = JSON.parse(this.data.tags);
          }
          this.$emit("reload");
        }
      });
    },
    getProjectFiles(before) {
      this.loading = true;
      getFileMetaPages(
        getCurrentProjectID(),
        this.currentPage,
        this.pageSize,
        this.condition
      ).then((res) => {
        let data = res.data;
        let { itemCount, listObject } = data;
        this.total = itemCount;
        this.results = listObject;
        this.results.forEach((item) => {
          if (item.tags && item.tags.length > 0) {
            item.tags = JSON.parse(item.tags);
          }
        });
        if (this.results.length > 0) {
          let index = before ? this.results.length - 1 : 0;
          this.data = this.results[index];
        }
        this.loading = false;
      });
    },
    beforeData() {
      this.showPanel = "baseInfo";
      const index = this.results.findIndex((e) => e.id === this.data.id);
      this.isFirst = index <= 0;
      if (!this.isFirst) {
        this.data = this.results[index - 1];
      } else {
        if (this.currentPage === 1) {
          this.$warning(this.$t("project.file_first"));
        } else {
          // 向上翻页
          this.currentPage--;
          this.getProjectFiles(true);
        }
      }
    },
    formatFileSize(val) {
      if (isNaN(val)) {
        return "";
      }
      let list = ["bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
      let temp = Math.floor(Math.log(val) / Math.log(2));
      if (temp < 1) {
        temp = 0;
      }
      let num = Math.floor(temp / 10);
      val = val / Math.pow(2, 10 * num);
      if (val.toString().length > val.toFixed(2).toString().length) {
        val = val.toFixed(2);
      }
      return val + " " + list[num];
    },
    nextData() {
      this.showPanel = "baseInfo";
      const index = this.results.findIndex((e) => e.id === this.data.id);
      this.isLast = this.results.length - 1 === index;
      if (!this.isLast) {
        this.data = this.results[index + 1];
      } else {
        let totalPages = Math.ceil(this.total / this.pageSize);
        if (totalPages === this.currentPage) {
          this.$warning(this.$t("project.file_last"));
        } else {
          // 向后翻页
          this.currentPage++;
          this.getProjectFiles(false);
        }
      }
    },
  },
};
</script>

<style scoped>
.ms-edit-image {
  width: 100%;
  height: 320px;
  display: block;
  background: #407c51;
}

.ms-edit-image:hover {
  border-color: var(--color);
  cursor: pointer;
}

.ms-file-item {
  text-align: center;
  padding-top: 140px;
}

.icon-title {
  color: #fff;
  text-align: center;
  font-size: 16px;
}

.ms-file-item-input {
  width: 100%;
}

.ms-left-col {
  padding-top: 220px;
  margin-right: 15px;
}

.ms-icon-arrow {
  font-size: 20px;
}

.ms-icon-arrow:hover {
  font-size: 21px;
  cursor: pointer;
  color: var(--color);
}

.ms-right-col {
  padding-top: 220px;
  margin-left: 10px;
}

.ms-header-menu {
  font-size: 18px;
  margin-left: 10px;
}

.ms-header-menu:hover {
  cursor: pointer;
  color: var(--color);
}
</style>
