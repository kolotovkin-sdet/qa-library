package com.qa.atlibs.manager;

import com.qa.atlibs.model.EnvironmentModel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class GrpcManager {

    private static final Map<String, ManagedChannel> managedChannels = new HashMap<>();
    @Getter
    private static final MetadataInterceptor metadataInterceptor = new MetadataInterceptor();

    public static void setupGrpcChannel(String application) {
        var config = getApplicationConfig(application);
        managedChannels.put(
                application,
                ManagedChannelBuilder.forAddress(config.host(), Integer.parseInt(config.port()))
                        .usePlaintext()
                        .intercept(metadataInterceptor)
                        .build()
        );
    }

    public static ManagedChannel getGrpcChannel(String application) {
        return managedChannels.get(application);
    }

    public static void shutdownGrpcChannels() {
        managedChannels.values().forEach(ManagedChannel::shutdownNow);
    }

    private static EnvironmentModel.AppConfig getApplicationConfig(String application) {
        return Optional.ofNullable(EnvironmentManager.getEnvironmentVariables().appsConfig().appConfig().get(application))
                .orElseThrow(() ->
                        new NoSuchElementException("""
                                Application %s haven't been set up.
                                Check test-env.yml and set environment variables ${env}.application.${application}
                                """.formatted(application))
                );
    }
}

