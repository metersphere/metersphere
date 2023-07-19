export const daemonSet = `apiVersion: apps/v1
kind: DaemonSet
metadata:
  labels:
    app: ms-node-controller
  name: {name}
  namespace: {namespace}
spec:
  selector:
    matchLabels:
      app: ms-node-controller
  template:
    metadata:
      labels:
        app: ms-node-controller
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - ms-node-controller
              topologyKey: kubernetes.io/hostname
            weight: 100
      containers:
      - env:
        image: {image}
        imagePullPolicy: IfNotPresent
        name: ms-node-controller
        ports:
        - containerPort: 8082
          protocol: TCP
        resources: {}
        volumeMounts:
        - mountPath: /opt/metersphere/logs
          name: metersphere-logs
      restartPolicy: Always
      volumes:
      - emptyDir: {}
        name: metersphere-logs
`;

export const deployment = `apiVersion: apps/v1
kind: Deployment
metadata:
  labels:
    app: ms-node-controller
  name: {name}
  namespace: {namespace}
spec:
  selector:
    matchLabels:
      app: ms-node-controller
  replicas: 2
  template:
    metadata:
      labels:
        app: ms-node-controller
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - ms-node-controller
              topologyKey: kubernetes.io/hostname
            weight: 100
      containers:
      - env:
        image: {image}
        imagePullPolicy: IfNotPresent
        name: ms-node-controller
        ports:
        - containerPort: 8082
          protocol: TCP
        resources: {}
        volumeMounts:
        - mountPath: /opt/metersphere/logs
          name: metersphere-logs
      restartPolicy: Always
      volumes:
      - emptyDir: {}
        name: metersphere-logs
`;

export const role = `apiVersion: rbac.authorization.k8s.io/v1beta1
kind: Role
metadata:
  name: metersphere
  namespace: {namespace}
rules:
- apiGroups:
  - ""
  resources:
  - pods
  verbs:
  - get
  - watch
  - list
  - create
  - update
  - patch
  - delete
  - exec
- apiGroups:
  - ""
  resources:
  - pods/exec
  verbs:
  - get
  - create
- apiGroups:
  - ""
  resources:
  - namespaces
  verbs:
  - get
  - watch
  - list
- apiGroups:
  - apps
  resources:
  - daemonsets
  - deployments
  verbs:
  - get
  - watch
  - list
  - create
  - update
  - patch
  - delete
- apiGroups:
  - extensions
  resources:
  - deployments
  verbs:
  - get
  - watch
  - list
  - create
  - update
  - patch
  - delete
- apiGroups:
  - batch
  resources:
  - jobs
  verbs:
  - get
  - watch
  - list
  - create
  - update
  - patch
  - delete
`;

export const job = `apiVersion: batch/v1
kind: Job
metadata:
  labels:
    test-id: \${TEST_ID}
  name: \${JOB_NAME}
spec:
  parallelism: 1
  template:
    metadata:
      labels:
        test-id: \${TEST_ID}
    spec:
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
            - podAffinityTerm:
                labelSelector:
                  matchExpressions:
                    - key: test-id
                      operator: In
                      values:
                        - \${TEST_ID}
                topologyKey: kubernetes.io/hostname
              weight: 100
      containers:
        - command:
            - sh
            - -c
            - /run-test.sh
          env:
            - name: START_TIME
              value: "\${START_TIME}"
            - name: GRANULARITY
              value: "\${GRANULARITY}"
            - name: JMETER_REPORTS_TOPIC
              value: \${JMETER_REPORTS_TOPIC}
            - name: METERSPHERE_URL
              value: \${METERSPHERE_URL}
            - name: RESOURCE_ID
              value: \${RESOURCE_ID}
            - name: BACKEND_LISTENER
              value: "\${BACKEND_LISTENER}"
            - name: BOOTSTRAP_SERVERS
              value: \${BOOTSTRAP_SERVERS}
            - name: RATIO
              value: "\${RATIO}"
            - name: REPORT_FINAL
              value: "\${REPORT_FINAL}"
            - name: TEST_ID
              value: \${TEST_ID}
            - name: THREAD_NUM
              value: "\${THREAD_NUM}"
            - name: HEAP
              value: \${HEAP}
            - name: REPORT_ID
              value: \${REPORT_ID}
            - name: REPORT_REALTIME
              value: "\${REPORT_REALTIME}"
            - name: RESOURCE_INDEX
              value: "\${RESOURCE_INDEX}"
            - name: LOG_TOPIC
              value: \${LOG_TOPIC}
            - name: GC_ALGO
              value: \${GC_ALGO}
          image: \${JMETER_IMAGE}
          imagePullPolicy: IfNotPresent
          name: jmeter
          ports:
            - containerPort: 60000
              protocol: TCP
          volumeMounts:
            - mountPath: /test
              name: test-files
            - mountPath: /jmeter-log
              name: log-files
        - command:
            - sh
            - -c
            - /generate-report.sh
          env:
            - name: START_TIME
              value: "\${START_TIME}"
            - name: GRANULARITY
              value: "\${GRANULARITY}"
            - name: JMETER_REPORTS_TOPIC
              value: \${JMETER_REPORTS_TOPIC}
            - name: METERSPHERE_URL
              value: \${METERSPHERE_URL}
            - name: RESOURCE_ID
              value: \${RESOURCE_ID}
            - name: BACKEND_LISTENER
              value: "\${BACKEND_LISTENER}"
            - name: BOOTSTRAP_SERVERS
              value: \${BOOTSTRAP_SERVERS}
            - name: RATIO
              value: "\${RATIO}"
            - name: REPORT_FINAL
              value: "\${REPORT_FINAL}"
            - name: TEST_ID
              value: \${TEST_ID}
            - name: THREAD_NUM
              value: "\${THREAD_NUM}"
            - name: HEAP
              value: \${HEAP}
            - name: REPORT_ID
              value: \${REPORT_ID}
            - name: REPORT_REALTIME
              value: "\${REPORT_REALTIME}"
            - name: RESOURCE_INDEX
              value: "\${RESOURCE_INDEX}"
            - name: LOG_TOPIC
              value: \${LOG_TOPIC}
            - name: GC_ALGO
              value: \${GC_ALGO}
          image: \${JMETER_IMAGE}
          imagePullPolicy: IfNotPresent
          name: report
          volumeMounts:
            - mountPath: /test
              name: test-files
            - mountPath: /jmeter-log
              name: log-files
      restartPolicy: Never
      volumes:
        - emptyDir: {}
          name: test-files
        - emptyDir: {}
          name: log-files
`;

export type YamlType = 'Deployment' | 'DaemonSet' | 'role';

export function getYaml(type: YamlType, name: string, namespace: string, image: string) {
  if (type === 'Deployment') {
    return deployment.replace('{name}', name).replace('{namespace}', namespace).replace('{image}', image);
  }
  if (type === 'DaemonSet') {
    return daemonSet.replace('{name}', name).replace('{namespace}', namespace).replace('{image}', image);
  }
  if (type === 'role') {
    return role.replace('{namespace}', namespace);
  }
  return '';
}
