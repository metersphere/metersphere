import EditComp from './edit-comp';
import type { App } from 'vue';

const MsEditComp = Object.assign(EditComp, {
  install: (app: App) => {
    app.component(EditComp.name, EditComp);
  },
});

export type CommentInstance = InstanceType<typeof EditComp>;

export default MsEditComp;
