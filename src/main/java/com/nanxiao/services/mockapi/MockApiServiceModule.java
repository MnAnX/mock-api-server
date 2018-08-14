package com.nanxiao.services.mockapi;

import com.advicecoach.server.netty.http.HttpServer;
import com.advicecoach.server.netty.http.responder.HttpResponder;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MockApiServiceModule extends AbstractModule {
    @Override
    protected void configure() {
        Config rootConfig = ConfigFactory.load();
        // Http Responder
        bind(HttpResponder.class).to(MockApiServiceResponder.class);
        // Server Port
        int serverPort = rootConfig.getInt("server.port");
        bind(Integer.class).annotatedWith(Names.named(HttpServer.LISTEN_PORT_NAME))
                .toInstance(serverPort);
    }
}