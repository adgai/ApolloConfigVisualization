package com.adgainai.apolloconfigvisualization;

import com.ctrip.framework.apollo.core.utils.ApolloThreadFactory;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class RefreshService {
    private static final ThreadGroup threadGroup = new ThreadGroup("AV");

    private static final AtomicLong threadNumber = new AtomicLong(1L);

    ScheduledExecutorService m_executorService;
    private static Map<String, Integer> ENV_TO_REFRESH_PEROID_MAP = new HashMap<>();

    static {

        ENV_TO_REFRESH_PEROID_MAP = ImmutableMap.of(
                "dev", 10,
                "qa", 20,
                "prod", 60 * 60
        );

        m_executorService = Executors.newScheduledThreadPool(1,
                new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull Runnable r) {
                        Thread thread = new Thread(threadGroup, r, threadGroup.getName() + "-" + "refresh" + "-" + threadNumber.getAndIncrement());
                        thread.setDaemon(true);
                        if (thread.getPriority() != 5) {
                            thread.setPriority(5);
                        }
                        return thread;
                    }
                });
    }

    public void startRefresh() {
        m_executorService.shutdown();


        m_executorService.scheduleAtFixedRate(

        )

    }

    public void getConfig(MyPluginProjectSettings settings) {
        String envs = settings.getEnv();
        List<String> envList = Arrays.stream(envs.split(",")).collect(Collectors.toList());
        ApolloOpenApiClient apolloClient = ApolloOpenApiClient.newBuilder().withToken(settings.getToken()).withPortalUrl(settings.getUrl()).build();


        Runnable runnable = () -> {
            apolloClient.getLatestActiveRelease(
                    settings.getServiceName(),

                    );
        };

    }


}
