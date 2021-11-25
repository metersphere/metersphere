UPDATE api_scenario
SET environment_json = api_scenario.scenario_definition -> '$.environmentMap'
WHERE environment_json IS NULL;