package com.nanxiao.services.mockapi;

import com.advicecoach.server.netty.Server;
import com.advicecoach.server.netty.ServerException;
import com.advicecoach.server.netty.http.HttpServer;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MockApiServiceRunner {
    final Injector injector;

    @Inject
    public MockApiServiceRunner(final Injector injector) {
        this.injector = injector;
    }

    public void start() throws ServerException {
        Server server = injector.getInstance(HttpServer.class);
        server.start();
    }

    public static void main(final String[] args) throws ServerException {
        Guice.createInjector(new MockApiServiceModule())
                .getInstance(MockApiServiceRunner.class)
                .start();
    }
}
