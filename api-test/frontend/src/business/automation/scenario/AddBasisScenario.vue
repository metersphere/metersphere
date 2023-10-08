<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="$t('api_test.automation.add_scenario')"
    :visible.sync="visible"
    width="45%"
    :destroy-on-close="true">
    <el-form
      :model="scenarioForm"
      label-position="right"
      label-width="100px"
      size="small"
      :rules="rule"
      ref="scenarioForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="scenarioForm.name" autocomplete="off" :placeholder="$t('commons.name')" />
      </el-form-item>

      <el-form-item :label="$t('test_track.module.module')" prop="apiScenarioModuleId">
        <ms-select-tree
          size="small"
          :data="moduleOptions"
          :defaultKey="scenarioForm.apiScenarioModuleId"
          @getValue="setModule"
          :obj="moduleObj"
          clearable
          checkStrictly />
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.principal')" prop="principal">
        <el-select
          v-model="scenarioForm.principal"
          :placeholder="$t('api_test.automation.scenario.principal')"
          filterable
          size="small"
          style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('api_test.automation.scenario.follow_people')" prop="followPeople">
        <el-select
          v-model="scenarioForm.follows"
          multiple
          :placeholder="$t('api_test.automation.scenario.follow_people')"
          filterable
          size="small"
          style="width: 100%">
          <el-option
            v-for="item in userOptions"
            :key="item.id"
            :label="item.id + ' (' + item.name + ')'"
            :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>

      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input
          class="ms-http-textarea"
          v-model="scenarioForm.description"
          type="textarea"
          :autosize="{ minRows: 2, maxRows: 10 }"
          :rows="2"
          size="small" />
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="visible = false"
        :isShow="true"
        v-loading="loading"
        :title="$t('commons.edit_info')"
        @saveAsEdit="saveScenario(true)"
        @confirm="saveScenario">
      </ms-dialog-footer>
    </template>
  </el-dialog>
</template>

<script>
import { getMaintainer } from '@/api/project';
import { getCurrentProjectID, getCurrentUser } from 'metersphere-frontend/src/utils/token';
import { getUUID } from 'metersphere-frontend/src/utils';
import MsDialogFooter from 'metersphere-frontend/src/components/MsDialogFooter';
import { saveScenario } from '@/business/automation/api-automation';
import MsSelectTree from 'metersphere-frontend/src/components/select-tree/SelectTree';

export default {
  name: 'MsAddBasisScenario',
  components: { MsDialogFooter, MsSelectTree },
  props: {
    moduleOptions: Array,
  },
  data() {
    return {
      scenarioForm: { follows: [] },
      visible: false,
      currentModule: {},
      userOptions: [],
      moduleObj: {
        id: 'id',
        label: 'name',
      },
      rule: {
        name: [
          {
            required: true,
            message: this.$t('test_track.case.input_name'),
            trigger: 'blur',
          },
          {
            max: 100,
            message: this.$t('test_track.length_less_than') + '100',
            trigger: 'blur',
          },
        ],
        apiScenarioModuleId: [
          {
            required: true,
            message: this.$t('test_track.case.input_module'),
            trigger: 'change',
          },
        ],
        principal: [
          {
            required: true,
            message: this.$t('api_test.automation.scenario.select_principal'),
            trigger: 'change',
          },
        ],
      },
      loading: false,
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    setModule(id, data) {
      this.scenarioForm.apiScenarioModuleId = id;
      this.scenarioForm.modulePath = data.path;
    },
    saveScenario(saveAs) {
      this.$refs['scenarioForm'].validate((valid) => {
        if (valid) {
          this.loading = true;
          let path = '/api/automation/create';
          this.setParameter();
          this.scenarioForm.status = 'Underway';
          this.scenarioForm.level = 'P0';
          if (saveAs) {
            this.loading = false;
            this.scenarioForm.request = JSON.stringify(this.scenarioForm.request);
            this.$emit('saveAsEdit', this.scenarioForm);
            this.visible = false;
          } else {
            saveScenario(path, this.scenarioForm, [], this, (response) => {
              this.loading = false;
              this.$success(this.$t('commons.save_success'));
              this.visible = false;
              this.$emit('refresh');
            }).finally(() => {
              this.loading = false;
            });
          }
        } else {
          return false;
        }
      });
    },
    setParameter() {
      this.scenarioForm.projectId = this.projectId;
      this.scenarioForm.id = getUUID().substring(0, 8);
      this.scenarioForm.protocol = this.currentProtocol;

      if (!this.scenarioForm.modulePath) {
        this.scenarioForm.modulePath = this.$t('commons.module_title');
      }
      if (!this.scenarioForm.apiScenarioModuleId) {
        this.scenarioForm.apiScenarioModuleId = 'default-module';
      }
    },
    getMaintainerOptions() {
      getMaintainer().then((response) => {
        this.userOptions = response.data;
      });
    },
    open(currentModule) {
      this.scenarioForm = { principal: getCurrentUser().id };
      this.currentModule = currentModule;
      if (this.scenarioForm.apiScenarioModuleId === undefined && !currentModule.id) {
        this.scenarioForm.apiScenarioModuleId = this.moduleOptions[0].id;
      } else if (currentModule.id) {
        this.scenarioForm.apiScenarioModuleId = currentModule.id;
      }
      this.getMaintainerOptions();
      this.visible = true;
    },
  },
};
</script>
