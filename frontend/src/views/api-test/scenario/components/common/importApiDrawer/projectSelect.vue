<template>
  <a-select class="w-[260px]" :default-value="innerProject" allow-search @change="selectProject">
    <template #arrow-icon>
      <icon-caret-down />
    </template>
    <a-tooltip v-for="item of projectList" :key="item.id" :mouse-enter-delay="500" :content="item.name">
      <a-option :value="item.id" :class="item.id === innerProject ? 'arco-select-option-selected' : ''">
        {{ item.name }}
      </a-option>
    </a-tooltip>
  </a-select>
</template>

<script setup lang="ts">
  import { getProjectList, getProjectListByOrgAndModule } from '@/api/modules/project-management/project';
  import useAppStore from '@/store/modules/app';

  import type { ProjectListItem } from '@/models/setting/project';

  const props = defineProps<{
    project: string;
  }>();
  const emit = defineEmits<{
    (e: 'update:project', val: string): void;
    (e: 'change', val: string): void;
  }>();

  const appStore = useAppStore();
  const projectList = ref<ProjectListItem[]>([]);
  const innerProject = ref(props.project || appStore.currentProjectId);

  watch(
    () => props.project,
    (val) => {
      innerProject.value = val;
    }
  );

  watch(
    () => innerProject.value,
    (val) => {
      emit('update:project', val);
    }
  );

  onBeforeMount(async () => {
    try {
      if (appStore.currentOrgId) {
        const res = await getProjectListByOrgAndModule(appStore.getCurrentOrgId, 'SCENARIO');
        projectList.value = res;
      } else {
        projectList.value = [];
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });

  function selectProject(
    value: string | number | boolean | Record<string, any> | (string | number | boolean | Record<string, any>)[]
  ) {
    emit('update:project', value as string);
    emit('change', value as string);
  }
</script>

<style lang="less" scoped></style>
