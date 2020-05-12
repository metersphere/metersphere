package io.metersphere.performance.engine.kubernetes.provider;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.*;
import io.metersphere.performance.engine.kubernetes.crds.MeterSphereCustomResource;
import io.metersphere.performance.engine.kubernetes.crds.MeterSphereCustomResourceDoneable;
import io.metersphere.performance.engine.kubernetes.crds.MeterSphereCustomResourceList;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

public abstract class AbstractClientProvider {
    private static ObjectMapper objectMapper = new ObjectMapper();

    private String credential;

    public AbstractClientProvider(String credential) {
        setCredential(credential);
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    /**
     * OpenShiftClient继承自KubernetesClient，OpenShiftClient对OpenShift和kubernetes的共有资源也是用KubernetesClient代理的，
     * 所以可以在把client提取到抽象类
     */
    public KubernetesClient getKubernetesClient() {
        ClientCredential providerCredential = JSONObject.parseObject(getCredential(), ClientCredential.class);
        io.fabric8.kubernetes.client.ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder.withMasterUrl(providerCredential.getMasterUrl());
        configBuilder.withOauthToken(providerCredential.getToken());
        configBuilder.withTrustCerts(true);
        //设置默认的 namespace 为 null，
        configBuilder.withNamespace(null);
        return new DefaultKubernetesClient(configBuilder.build());
    }


    public void validateCredential() {
        try (KubernetesClient client = getKubernetesClient()) {
            client.namespaces().list();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 确保指定的namespace存在，不存在直接创建一个
     *
     * @param namespace namespace标识
     * @return
     */
    public synchronized String confirmNamespace(String namespace) {
        KubernetesClient kubernetesClient = getKubernetesClient();
        Namespace currentNamespace = kubernetesClient.namespaces().withName(namespace).get();
        if (currentNamespace == null) {
            Map<String, String> annotations = new HashMap<>();
            Namespace newNamespace = new NamespaceBuilder()
                    .withNewMetadata()
                    .withName(namespace)
                    .withAnnotations(annotations)
                    .endMetadata()
                    .build();
            currentNamespace = kubernetesClient.namespaces().createOrReplace(newNamespace);
        }
        return currentNamespace.getMetadata().getName();
    }


    /**
     * 获取集群UUID，当前集群UUID等于default namespace的UID
     *
     * @return
     */
    public String getClusterUUID() {
        KubernetesClient kubernetesClient = getKubernetesClient();
        Namespace defaultNamespace = kubernetesClient.namespaces().withName("kube-system").get();
        if (defaultNamespace == null) {
            throw new RuntimeException("无法获取集群的kube-system namespace");
        } else {
            return defaultNamespace.getMetadata().getUid();
        }
    }


    /**
     * 确保docker registry存在，
     * 不存在 创建一个harbor-secret，并修改serviceaccount default
     *
     * @param registry
     */
    public void dockerRegistry(DockerRegistry registry) {

        if (StringUtils.isEmpty(registry.getUsername()) ||
                StringUtils.isEmpty(registry.getPassword()) ||
                StringUtils.isEmpty(registry.getUrl())
        ) {
            throw new RuntimeException("Please set the docker registry information");
        }

        String secretName = "docker-registry-ms-secret";
        KubernetesClient kubernetesClient = getKubernetesClient();

        Secret secretRegistry = new SecretBuilder()
                .withNewMetadata().withName(secretName).endMetadata()
                .addToData(".dockerconfigjson", DockerRegistryUtil.getDockerConfig(registry))
                .withType("kubernetes.io/dockerconfigjson")
                .build();
        kubernetesClient.secrets().inNamespace(registry.getNamespace()).createOrReplace(secretRegistry);

        //sa
        ServiceAccount serviceAccount = kubernetesClient.serviceAccounts().inNamespace(registry.getNamespace())
                .withName("default").get();
        List<LocalObjectReference> imagePullSecrets = serviceAccount.getImagePullSecrets();
        for (LocalObjectReference pullSecret : imagePullSecrets) {
            if (secretName.equals(pullSecret.getName())) {
                return;
            }
        }

        LocalObjectReference localObjectReference = new LocalObjectReference(secretName);
        imagePullSecrets.add(localObjectReference);
        serviceAccount.setImagePullSecrets(imagePullSecrets);

        kubernetesClient.serviceAccounts().inNamespace(registry.getNamespace())
                .createOrReplace(serviceAccount);

    }


    public <T> T applyCustomResource(MeterSphereCustomResource customResource) throws Exception {
        try (KubernetesClient kubernetesClient = getKubernetesClient()) {
            CustomResourceDefinition crd = kubernetesClient.customResourceDefinitions().withName(customResource.getCrd()).get();
            if (crd == null) {
                throw new Exception("CRD does not exists.");
            }
            MixedOperation<MeterSphereCustomResource, MeterSphereCustomResourceList, MeterSphereCustomResourceDoneable, Resource<MeterSphereCustomResource, MeterSphereCustomResourceDoneable>>
                    operation = kubernetesClient.customResources(crd, MeterSphereCustomResource.class, MeterSphereCustomResourceList.class, MeterSphereCustomResourceDoneable.class);
            MeterSphereCustomResource replace = operation.inNamespace(customResource.getMetadata().getNamespace()).createOrReplace(customResource);
            return (T) objectMapper.readValue(objectMapper.writeValueAsString(replace), customResource.getClass());
        }
    }


    public boolean deleteCustomResource(MeterSphereCustomResource customResource) {
        try (KubernetesClient kubernetesClient = getKubernetesClient()) {
            CustomResourceDefinition crd = kubernetesClient.customResourceDefinitions().withName(customResource.getCrd()).get();
            MixedOperation<MeterSphereCustomResource, MeterSphereCustomResourceList, MeterSphereCustomResourceDoneable, Resource<MeterSphereCustomResource, MeterSphereCustomResourceDoneable>>
                    operation = kubernetesClient.customResources(crd, MeterSphereCustomResource.class, MeterSphereCustomResourceList.class, MeterSphereCustomResourceDoneable.class);
            Boolean result = operation.inNamespace(customResource.getMetadata().getNamespace()).withName(customResource.getMetadata().getName()).cascading(true).delete();
            return result == null ? false : result;
        }
    }


    public <T> T getCustomResource(MeterSphereCustomResource customResource) throws Exception {
        try (KubernetesClient kubernetesClient = getKubernetesClient()) {
            CustomResourceDefinition crd = kubernetesClient.customResourceDefinitions().withName(customResource.getCrd()).get();
            MixedOperation<MeterSphereCustomResource, MeterSphereCustomResourceList, MeterSphereCustomResourceDoneable, Resource<MeterSphereCustomResource, MeterSphereCustomResourceDoneable>>
                    operation = kubernetesClient.customResources(crd, MeterSphereCustomResource.class, MeterSphereCustomResourceList.class, MeterSphereCustomResourceDoneable.class);
            MeterSphereCustomResource meterSphereCustomResource = operation.inNamespace(customResource.getMetadata().getNamespace()).withName(customResource.getMetadata().getName()).get();
            if (meterSphereCustomResource == null) {
                return null;
            }
            return (T) objectMapper.readValue(objectMapper.writeValueAsString(meterSphereCustomResource), customResource.getClass());
        }
    }


    public <T> List<T> listCustomResource(MeterSphereCustomResource customResource) throws Exception {
        try (KubernetesClient kubernetesClient = getKubernetesClient()) {
            CustomResourceDefinition crd = kubernetesClient.customResourceDefinitions().withName(customResource.getCrd()).get();
            MixedOperation<MeterSphereCustomResource, MeterSphereCustomResourceList, MeterSphereCustomResourceDoneable, Resource<MeterSphereCustomResource, MeterSphereCustomResourceDoneable>>
                    operation = kubernetesClient.customResources(crd, MeterSphereCustomResource.class, MeterSphereCustomResourceList.class, MeterSphereCustomResourceDoneable.class);
            MeterSphereCustomResourceList list;
            if (StringUtils.isNotEmpty(customResource.getMetadata().getNamespace())) {
                list = operation.inNamespace(customResource.getMetadata().getNamespace()).list();
            } else {
                list = operation.inAnyNamespace().list();
            }

            List<T> resultList = new ArrayList<>();
            for (Object cr : list.getItems()) {
                resultList.add((T) objectMapper.readValue(objectMapper.writeValueAsString(cr), customResource.getClass()));
            }
            return resultList;
        }
    }


    public boolean checkPVCNotExists(String namespace, String statefulsetName) {
        KubernetesClient kubernetesClient = getKubernetesClient();
        NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> operation = kubernetesClient.persistentVolumeClaims()
                .inNamespace(namespace);

        PersistentVolumeClaimList pvcList = operation.list(100, null);
        Pattern compile = Pattern.compile(statefulsetName + "-\\d+");
        return checkPVCNotExists(pvcList, compile, operation);
    }

    private boolean checkPVCNotExists(PersistentVolumeClaimList pvcList, Pattern compile, NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> operation) {
        if (pvcList == null || CollectionUtils.isEmpty(pvcList.getItems())) {
            return true;
        }
        Optional<PersistentVolumeClaim> claimOptional = pvcList.getItems().stream().filter(pvc -> compile.matcher(pvc.getMetadata().getName()).matches()).findAny();
        if (claimOptional.isPresent()) {
            return false;
        } else if (StringUtils.isNotEmpty(pvcList.getMetadata().getContinue())) {
            return checkPVCNotExists(operation.list(100, pvcList.getMetadata().getContinue()), compile, operation);
        } else {
            return true;
        }
    }

    public boolean deletePVC(HasMetadata hasMetadata) {
        KubernetesClient kubernetesClient = getKubernetesClient();
        NonNamespaceOperation<PersistentVolumeClaim, PersistentVolumeClaimList, DoneablePersistentVolumeClaim, Resource<PersistentVolumeClaim, DoneablePersistentVolumeClaim>> operation = kubernetesClient.persistentVolumeClaims()
                .inNamespace(hasMetadata.getMetadata().getNamespace());

        if (MapUtils.isNotEmpty(hasMetadata.getMetadata().getLabels())) {
            operation.withLabelSelector(new LabelSelector(null, hasMetadata.getMetadata().getLabels()));
        }
        if (StringUtils.isNotEmpty(hasMetadata.getMetadata().getName())) {
            operation.withName(hasMetadata.getMetadata().getName());
        }
        Boolean delete = operation.delete();
        return delete == null ? false : delete;
    }


    public boolean deleteNamespace(String namespace) {
        KubernetesClient kubernetesClient = getKubernetesClient();
        Boolean delete = kubernetesClient.namespaces().withName(namespace).delete();

        return delete == null ? false : delete;
    }


    public String getLog(String namespace, String pod, String container, int tailingLines) {
        try (KubernetesClient client = getKubernetesClient()) {
            PrettyLoggable<String, LogWatch> loggable;
            if (tailingLines > 0) {
                loggable = client.pods().inNamespace(namespace).withName(pod).inContainer(container).tailingLines(tailingLines);
            } else {
                loggable = client.pods().inNamespace(namespace).withName(pod).inContainer(container);
            }

            return loggable.getLog();
        }
    }
}
