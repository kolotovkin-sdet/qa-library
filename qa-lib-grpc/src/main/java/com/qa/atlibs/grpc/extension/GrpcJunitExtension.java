package com.qa.atlibs.grpc.extension;

import com.qa.atlibs.core.manager.EnvironmentManager;
import com.qa.atlibs.grpc.manager.GrpcManager;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class GrpcJunitExtension implements BeforeAllCallback, AfterAllCallback {
    // todo
    @Override
    public void beforeAll(ExtensionContext context) {
        EnvironmentManager.getEnvironmentVariables().appsConfig().appConfig().entrySet()
                .stream()
                .filter(app -> app.getValue().type().equals("grpc"))
                .forEach(entry -> GrpcManager.setupGrpcChannel(entry.getKey()));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        GrpcManager.shutdownGrpcChannels();
    }
}

