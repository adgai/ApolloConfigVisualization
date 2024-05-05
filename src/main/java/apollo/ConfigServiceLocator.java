package apollo;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.core.dto.ServiceDTO;
import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.core.utils.DeferredLoggerFactory;
import com.ctrip.framework.apollo.core.utils.DeprecatedPropertyNotifyUtil;
import com.ctrip.framework.apollo.exceptions.ApolloConfigException;
import com.ctrip.framework.apollo.tracer.Tracer;
import com.ctrip.framework.apollo.tracer.spi.Transaction;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.apollo.util.ExceptionUtil;
import com.ctrip.framework.apollo.util.http.HttpClient;
import com.ctrip.framework.apollo.util.http.HttpRequest;
import com.ctrip.framework.apollo.util.http.HttpResponse;
import com.ctrip.framework.foundation.Foundation;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.net.UrlEscapers;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;

public class ConfigServiceLocator {
    private static final Logger logger = DeferredLoggerFactory.getLogger(com.ctrip.framework.apollo.internals.ConfigServiceLocator.class);
    private HttpClient m_httpClient;
    private ConfigUtil m_configUtil;
    private AtomicReference<List<ServiceDTO>> m_configServices;
    private Type m_responseType;
    private ScheduledExecutorService m_executorService;
    private static final Joiner.MapJoiner MAP_JOINER = Joiner.on("&").withKeyValueSeparator("=");
    private static final Escaper queryParamEscaper = UrlEscapers.urlFormParameterEscaper();

    public ConfigServiceLocator() {
        List<ServiceDTO> initial = Lists.newArrayList();
        this.m_configServices = new AtomicReference(initial);
        this.m_responseType = (new TypeToken<List<ServiceDTO>>() {
        }).getType();
        this.m_httpClient = (HttpClient)ApolloInjector.getInstance(HttpClient.class);
        this.m_configUtil = (ConfigUtil)ApolloInjector.getInstance(ConfigUtil.class);
        this.m_executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("ConfigServiceLocator", true));
        this.initConfigServices();
    }
    public ConfigServiceLocator(IConfigUtil configUtil) {
        List<ServiceDTO> initial = Lists.newArrayList();
        this.m_configServices = new AtomicReference(initial);
        this.m_responseType = (new TypeToken<List<ServiceDTO>>() {
        }).getType();
        this.m_httpClient = (HttpClient)ApolloInjector.getInstance(HttpClient.class);
        this.m_configUtil = configUtil;
        this.m_executorService = Executors.newScheduledThreadPool(1, ApolloThreadFactory.create("ConfigServiceLocator", true));
        this.initConfigServices();
    }

    private void initConfigServices() {
        List<ServiceDTO> customizedConfigServices = this.getCustomizedConfigService();
        if (customizedConfigServices != null) {
            this.setConfigServices(customizedConfigServices);
        } else {
            this.tryUpdateConfigServices();
            this.schedulePeriodicRefresh();
        }
    }

    private List<ServiceDTO> getCustomizedConfigService() {
        String configServices = System.getProperty("apollo.config-service");
        if (Strings.isNullOrEmpty(configServices)) {
            configServices = System.getenv("APOLLO_CONFIG_SERVICE");
        }

        if (Strings.isNullOrEmpty(configServices)) {
            configServices = Foundation.server().getProperty("apollo.config-service", (String)null);
        }

        if (Strings.isNullOrEmpty(configServices)) {
            configServices = this.getDeprecatedCustomizedConfigService();
        }

        if (Strings.isNullOrEmpty(configServices)) {
            return null;
        } else {
            logger.info("Located config services from apollo.config-service configuration: {}, will not refresh config services from remote meta service!", configServices);
            String[] configServiceUrls = configServices.split(",");
            List<ServiceDTO> serviceDTOS = Lists.newArrayList();
            String[] var4 = configServiceUrls;
            int var5 = configServiceUrls.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String configServiceUrl = var4[var6];
                configServiceUrl = configServiceUrl.trim();
                ServiceDTO serviceDTO = new ServiceDTO();
                serviceDTO.setHomepageUrl(configServiceUrl);
                serviceDTO.setAppName("apollo-configservice");
                serviceDTO.setInstanceId(configServiceUrl);
                serviceDTOS.add(serviceDTO);
            }

            return serviceDTOS;
        }
    }

    private String getDeprecatedCustomizedConfigService() {
        String configServices = System.getProperty("apollo.configService");
        if (!Strings.isNullOrEmpty(configServices)) {
            DeprecatedPropertyNotifyUtil.warn("apollo.configService", "apollo.config-service");
        }

        if (Strings.isNullOrEmpty(configServices)) {
            configServices = System.getenv("APOLLO_CONFIGSERVICE");
            if (!Strings.isNullOrEmpty(configServices)) {
                DeprecatedPropertyNotifyUtil.warn("APOLLO_CONFIGSERVICE", "APOLLO_CONFIG_SERVICE");
            }
        }

        if (Strings.isNullOrEmpty(configServices)) {
            configServices = Foundation.server().getProperty("apollo.configService", (String)null);
            if (!Strings.isNullOrEmpty(configServices)) {
                DeprecatedPropertyNotifyUtil.warn("apollo.configService", "apollo.config-service");
            }
        }

        return configServices;
    }

    public List<ServiceDTO> getConfigServices() {
        if (((List)this.m_configServices.get()).isEmpty()) {
            this.updateConfigServices();
        }

        return (List)this.m_configServices.get();
    }

    private boolean tryUpdateConfigServices() {
        try {
            this.updateConfigServices();
            return true;
        } catch (Throwable var2) {
            return false;
        }
    }

    private void schedulePeriodicRefresh() {
//        this.m_executorService.scheduleAtFixedRate(new Runnable() {
//            public void run() {
//                com.ctrip.framework.apollo.internals.ConfigServiceLocator.logger.debug("refresh config services");
//                Tracer.logEvent("Apollo.MetaService", "periodicRefresh");
//                com.ctrip.framework.apollo.internals.ConfigServiceLocator.this.tryUpdateConfigServices();
//            }
//        }, (long)this.m_configUtil.getRefreshInterval(), (long)this.m_configUtil.getRefreshInterval(), this.m_configUtil.getRefreshIntervalTimeUnit());
    }

    private synchronized void updateConfigServices() {
        String url = this.assembleMetaServiceUrl();
        HttpRequest request = new HttpRequest(url);
        int maxRetries = 2;
        Throwable exception = null;

        for(int i = 0; i < maxRetries; ++i) {
            Transaction transaction = Tracer.newTransaction("Apollo.MetaService", "getConfigService");
            transaction.addData("Url", url);

            label84: {
                try {
                    HttpResponse<List<ServiceDTO>> response = this.m_httpClient.doGet(request, this.m_responseType);
                    transaction.setStatus("0");
                    List<ServiceDTO> services = (List)response.getBody();
                    if (services == null || services.isEmpty()) {
                        this.logConfigService("Empty response!");
                        continue;
                    }

                    this.setConfigServices(services);
                } catch (Throwable var14) {
                    Throwable ex = var14;
                    Tracer.logEvent("ApolloConfigException", ExceptionUtil.getDetailMessage(ex));
                    transaction.setStatus(ex);
                    exception = ex;
                    break label84;
                } finally {
                    transaction.complete();
                }

                return;
            }

            try {
                this.m_configUtil.getOnErrorRetryIntervalTimeUnit().sleep(this.m_configUtil.getOnErrorRetryInterval());
            } catch (InterruptedException var13) {
            }
        }

        throw new ApolloConfigException(String.format("Get config services failed from %s", url), exception);
    }

    private void setConfigServices(List<ServiceDTO> services) {
        this.m_configServices.set(services);
        this.logConfigServices(services);
    }

    private String assembleMetaServiceUrl() {
        String domainName = this.m_configUtil.getMetaServerDomainName();
        String appId = this.m_configUtil.getAppId();
        String localIp = this.m_configUtil.getLocalIp();
        Map<String, String> queryParams = Maps.newHashMap();
        queryParams.put("appId", queryParamEscaper.escape(appId));
        if (!Strings.isNullOrEmpty(localIp)) {
            queryParams.put("ip", queryParamEscaper.escape(localIp));
        }

        return domainName + "/services/config?" + MAP_JOINER.join(queryParams);
    }

    private void logConfigServices(List<ServiceDTO> serviceDtos) {
        Iterator var2 = serviceDtos.iterator();

        while(var2.hasNext()) {
            ServiceDTO serviceDto = (ServiceDTO)var2.next();
            this.logConfigService(serviceDto.getHomepageUrl());
        }

    }

    private void logConfigService(String serviceUrl) {
        Tracer.logEvent("Apollo.Config.Services", serviceUrl);
    }
}
