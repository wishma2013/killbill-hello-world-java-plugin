/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.helloworld;

import java.util.Properties;
import java.util.UUID;

import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.plugin.api.notification.PluginTenantConfigurableConfigurationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * When per-tenant config changes are made, the plugin automatically gets notified (and prints a log trace)
 * <pre>
 * {@code
 * curl -v \
 *      -X POST \
 *      -u admin:password \
 *      -H "Content-Type: text/plain" \
 *      -H "X-Killbill-ApiKey: bob" \
 *      -H "X-Killbill-ApiSecret: lazar" \
 *      -H "X-Killbill-CreatedBy: demo" \
 *      -d 'key1=foo1
 * key2=foo2' \
 *      "http://127.0.0.1:8080/1.0/kb/tenants/uploadPluginConfig/hello-world-plugin"
 * }
 * </pre>
 */
public class HelloWorldConfigurationHandler extends PluginTenantConfigurableConfigurationHandler<Properties> {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldConfigurationHandler.class);

    private final String region;

    public HelloWorldConfigurationHandler(final String region,
                                          final String pluginName,
                                          final OSGIKillbillAPI osgiKillbillAPI) {
        super(pluginName, osgiKillbillAPI);
        this.region = region;
    }

    @Override
    protected Properties createConfigurable(final Properties properties) {
        logger.info("New properties for region {}: {}", region, properties);
        return properties;
    }

    public Properties getConfigurable(){
        return super.getConfigurable(UUID.fromString("f7a9cf52-3067-412d-974b-ec328a0216b2"));
    }
}
