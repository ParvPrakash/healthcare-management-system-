package com.healthcare;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Standalone Embedded Tomcat Launcher for 1-command zero-config execution.
 * Allows running the complete Jakarta Servlet + JSP application directly.
 */
public class ServerLauncher {
    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);

    public static void main(String[] args) throws Exception {
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null && !envPort.isEmpty()) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException ignored) {}
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector(); // triggers connector creation

        // Define webapp base
        File docBase = new File("src/main/webapp");
        if (!docBase.exists()) {
            docBase = new File("webapp");
        }

        // Configure RemoteIpValve to honour HTTPS proxy and tunnel headers (Cloudflare, ngrok)
        org.apache.catalina.valves.RemoteIpValve remoteIpValve = new org.apache.catalina.valves.RemoteIpValve();
        remoteIpValve.setRemoteIpHeader("x-forwarded-for");
        remoteIpValve.setProtocolHeader("x-forwarded-proto");
        remoteIpValve.setProtocolHeaderHttpsValue("https");
        remoteIpValve.setHostHeader("x-forwarded-host");
        remoteIpValve.setPortHeader("x-forwarded-port");
        remoteIpValve.setInternalProxies(".*");
        tomcat.getEngine().getPipeline().addValve(remoteIpValve);

        String baseDir = new File("target/tomcat-embed").getAbsolutePath();
        tomcat.setBaseDir(baseDir);

        Context ctx = tomcat.addWebapp("", docBase.getAbsolutePath());
        ctx.setParentClassLoader(ServerLauncher.class.getClassLoader());
        ctx.setReloadable(true);

        // Configure standard RFC6265 Cookie Processor with SameSite=Lax
        org.apache.tomcat.util.http.Rfc6265CookieProcessor cookieProcessor = new org.apache.tomcat.util.http.Rfc6265CookieProcessor();
        cookieProcessor.setSameSiteCookies("lax");
        ctx.setCookieProcessor(cookieProcessor);

        // Tell Tomcat where compiled classes are
        File additionWebInfClasses = new File("target/classes");
        if (additionWebInfClasses.exists()) {
            org.apache.catalina.WebResourceRoot resources = new org.apache.catalina.webresources.StandardRoot(ctx);
            resources.addPreResources(new org.apache.catalina.webresources.DirResourceSet(
                    resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
            ctx.setResources(resources);
        }

        logger.info("==================================================================");
        logger.info("Starting Healthcare Management System on http://localhost:{}", port);
        logger.info("Web application base: {}", docBase.getAbsolutePath());
        logger.info("==================================================================");

        tomcat.start();
        tomcat.getServer().await();
    }
}
