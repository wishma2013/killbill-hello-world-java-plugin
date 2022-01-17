/*
 * Copyright 2020-2022 Equinix, Inc
 * Copyright 2014-2022 The Billing Project, LLC
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

package org.killbill.billing.plugin.helloworld.biz;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.BoundRequestBuilder;
import org.killbill.billing.plugin.util.http.HttpClient;
import org.killbill.billing.plugin.util.http.InvalidRequest;
import org.killbill.billing.plugin.util.http.ResponseFormat;

/**
 * @Description 远程调用业务接口客户端类
 * @Author Toria toria.shi@easemob.com
 * @Date 2022/1/17 上午5:55
 **/
public class RemoteHttpClient extends HttpClient {

    public RemoteHttpClient(final String url, final String username, final String password, final String proxyHost, final Integer proxyPort, final Boolean strictSSL) throws GeneralSecurityException {
        super(url, username, password, proxyHost, proxyPort, strictSSL);
    }

    public RemoteHttpClient(final String url, final String username, final String password, final String proxyHost, final Integer proxyPort, final Boolean strictSSL, final int connectTimeoutMs, final int readTimeoutMs) throws GeneralSecurityException {
        super(url, username, password, proxyHost, proxyPort, strictSSL, connectTimeoutMs, readTimeoutMs);
    }

    @Override
    public <T> T doCall(final String verb, final String uri, final String body, final Map<String, String> queryParams,
                        final Map<String, String> headers, final Class<T> clazz, final ResponseFormat format)
            throws InterruptedException, ExecutionException, TimeoutException, IOException, URISyntaxException, InvalidRequest {
        final String url = getUrl(this.url, uri);

        final BoundRequestBuilder builder = getBuilderWithHeaderAndQuery(verb, url, headers, queryParams);
        if (!GET.equals(verb) && !HEAD.equals(verb)) {
            if (body != null) {
                builder.setBody(body);
            }
        }

        return executeAndWait(builder, DEFAULT_HTTP_TIMEOUT_SEC, clazz, format);
    }

    private String getUrl(final String location, final String uri) throws URISyntaxException {
        if (uri == null) {
            throw new URISyntaxException("(null)", "HttpClient URL misconfigured");
        }

        final URI u = new URI(uri);
        if (u.isAbsolute()) {
            return uri;
        } else {
            return String.format("%s%s", location, uri);
        }
    }

}
