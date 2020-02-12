<template>
  <el-upload
    accept=".jmx"
    drag
    :action="jmxUploadPath">
    <i class="el-icon-upload"/>
    <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
    <div class="el-upload__tip" slot="tip">只能上传jmx文件</div>
  </el-upload>
</template>

<script>
  export default {
    data() {
      return {
        jmxUploadPath: '/testplan/file/upload',
      };
    },
    methods: {
    }
  }
</script>
