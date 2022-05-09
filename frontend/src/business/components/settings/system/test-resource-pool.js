const daemonSet = `apiVersion: apps/v1
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
        - name: KAFKA_BOOTSTRAP-SERVERS
          value: {kafkaBootstrapServers}
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

const deployment = `apiVersion: apps/v1
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
        - name: KAFKA_BOOTSTRAP-SERVERS
          value: {kafkaBootstrapServers}
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

const sa = `apiVersion: rbac.authorization.k8s.io/v1beta1
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
`

export function getYaml(type, name, namespace, image, kafkaBootstrapServers) {
  if (type === 'Deployment') {
    return deployment
      .replace('{name}', name)
      .replace('{namespace}', namespace)
      .replace('{image}', image)
      .replace('{kafkaBootstrapServers}', kafkaBootstrapServers);
  }
  if (type === 'DaemonSet') {
    return daemonSet
      .replace('{name}', name)
      .replace('{namespace}', namespace)
      .replace('{image}', image)
      .replace('{kafkaBootstrapServers}', kafkaBootstrapServers);
  }
  if (type === 'sa') {
    return sa.replace('{namespace}', namespace);
  }
}

