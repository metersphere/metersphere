import HashTreeElement from "../../hashtree";

const DEFAULT_OPTIONS = {
  options: {
    attributes: {guiclass: "ThreadGroupGui", testclass: "ThreadGroup", testname: "ThreadGroup", enabled: "true"},
  }
};

export const TYPE = "ThreadGroup";

export default class ThreadGroup extends HashTreeElement {
  constructor(options = DEFAULT_OPTIONS) {
    super(options);
    this.$type = TYPE;
    this.type = TYPE;
    this.onSampleError = this.initStringProp('ThreadGroup.on_sample_error', 'continue');
    this.numThreads = this.initStringProp('ThreadGroup.num_threads', 1);
    this.rampTime = this.initStringProp('ThreadGroup.ramp_time', 1);

    let loopController = this.initElementProp('ThreadGroup.main_controller', 'LoopController');
    this.continueForever = loopController.initBoolProp('LoopController.continue_forever', false);
    this.loops = loopController.initStringProp('LoopController.loops', 1);

    this.sameUserOnNextIteration = this.initBoolProp('ThreadGroup.same_user_on_next_iteration', true);
    this.delayedStart = this.initBoolProp('ThreadGroup.delayedStart');
    this.scheduler = this.initBoolProp('ThreadGroup.scheduler', false);
    this.delay = this.initStringProp('ThreadGroup.delay');
    this.duration = this.initStringProp('ThreadGroup.duration');
  }
}

export const schema = {
  ThreadGroup: ThreadGroup
}
