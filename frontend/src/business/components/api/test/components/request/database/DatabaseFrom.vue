<template>
  <div>
    <el-form :model="config" :rules="rules" label-width="150px" size="small" :disabled="isReadOnly" class="database-from" ref="databaseFrom">

      <el-form-item :label="'连接池名称'" prop="name">
        <el-input v-model="config.name" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="'数据库连接URL'" prop="dbUrl">
        <el-input v-model="config.dbUrl" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="'数据库驱动'" prop="driver">
        <el-select v-model="config.driver" class="select-100" clearable>
          <el-option v-for="p in drivers" :key="p" :label="p" :value="p"/>
        </el-select>
      </el-form-item>

      <el-form-item :label="'用户名'" prop="username">
        <el-input v-model="config.username" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="'密码'" prop="password">
        <el-input v-model="config.password" maxlength="300" show-word-limit
                  :placeholder="$t('commons.input_content')"/>
      </el-form-item>

      <el-form-item :label="'最大连接数'" prop="poolMax">
        <el-input-number size="small" :disabled="isReadOnly" v-model="config.poolMax" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </el-form-item>


      <el-form-item :label="'最大等待时间(ms)'" prop="timeout">
        <el-input-number size="small" :disabled="isReadOnly" v-model="config.timeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" size="small" class="addButton" @click="save">添加</el-button>
      </el-form-item>

    </el-form>

  </div>
</template>

<script>
    import {DatabaseConfig} from "../../../model/ScenarioModel";

    export default {
      name: "MsDatabaseFrom",
      components: {},
      props: {
        isReadOnly: {
          type: Boolean,
          default: false
        },
        config: {
          type: Object,
          default() {
            return new DatabaseConfig();
          }
        },
      },
      data() {
        return {
          drivers: DatabaseConfig.DRIVER_CLASS,
          // config: new DatabaseConfig(),
          rules: {
            name: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 300, message: this.$t('commons.input_limit', [0, 300]), trigger: 'blur'}
            ],
            driver: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
            ],
            password: [
              {max: 200, message: this.$t('commons.input_limit', [0, 200]), trigger: 'blur'}
            ],
            dbUrl: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'}
            ],
            username: [
              {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
              {max: 200, message: this.$t('commons.input_limit', [0, 200]), trigger: 'blur'}
            ]
          }
        }
      },
      methods: {
        save() {
          this.$refs['databaseFrom'].validate((valid) => {
            if (valid) {
              this.$emit('save', this.config);
              // this.config = new DatabaseConfig();
            } else {
              return false;
            }
          });
        }
      }
    }
</script>

<style scoped>

  .addButton {
    float: right;
  }

  .database-from {
    padding: 10px;
    border: #DCDFE6 solid 1px;
    margin: 5px 0;
    border-radius: 5px;
  }

</style>
