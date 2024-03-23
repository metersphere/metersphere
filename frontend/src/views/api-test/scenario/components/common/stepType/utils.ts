import { ScenarioStepItem } from '@/models/apiTest/scenario';
import { ScenarioStepRefType, ScenarioStepType } from '@/enums/apiEnum';

export default function getStepType(step: ScenarioStepItem) {
  const isCopyApi = step.stepType === ScenarioStepType.API && step.refType === ScenarioStepRefType.COPY;
  const isQuoteApi = step.stepType === ScenarioStepType.API && step.refType === ScenarioStepRefType.REF;
  const isCopyCase = step.stepType === ScenarioStepType.API_CASE && step.refType === ScenarioStepRefType.COPY;
  const isQuoteCase = step.stepType === ScenarioStepType.API_CASE && step.refType === ScenarioStepRefType.REF;
  const isCopyScenario = step.stepType === ScenarioStepType.API_SCENARIO && step.refType === ScenarioStepRefType.COPY;
  const isQuoteScenario =
    step.stepType === ScenarioStepType.API_SCENARIO &&
    [ScenarioStepRefType.REF, ScenarioStepRefType.PARTIAL_REF].includes(step.refType);

  return {
    isCopyApi,
    isQuoteApi,
    isCopyCase,
    isQuoteCase,
    isCopyScenario,
    isQuoteScenario,
  };
}
