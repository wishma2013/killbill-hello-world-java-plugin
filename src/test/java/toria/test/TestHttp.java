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

package toria.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jooq.tools.json.JSONObject;
import org.killbill.billing.plugin.helloworld.biz.RemoteHttpClient;
import org.killbill.billing.plugin.util.http.InvalidRequest;
import org.killbill.billing.plugin.util.http.ResponseFormat;

/**
 * @Description TODO
 * @Author Toria toria.shi@easemob.com
 * @Date 2022/1/17 下午7:02
 **/
public class TestHttp {

    public static void main(String[] args) throws GeneralSecurityException, InvalidRequest, InterruptedException, ExecutionException, IOException, TimeoutException, URISyntaxException {
        RemoteHttpClient httpClient = new RemoteHttpClient("http://172.16.31.103:31487", "", "", null, null, false, 2000, 60000);
//        RemoteHttpClient httpClient = new RemoteHttpClient("http://127.0.0.1:8081", "", "", null, null, false, 2000, 60000);
        //
        Map payBody = new HashMap(5);
        payBody.put("amount", 1000);
        payBody.put("associatedEntityId", "567890");
        payBody.put("product", "IM");
        payBody.put("reason", "赠送金账单自动抵扣_");
        payBody.put("salesman", "killbill");

        Map<String, String> headers = new HashMap<>(1);
        headers.put("Content-Type", "application/json");

        // 抵扣账单
        String uri_pay = "/presented/{orgName}/pay/to/{targetId}";
        uri_pay = uri_pay.replace("{orgName}", "batch39").replace("{targetId}", "56789");

        JSONObject result_pay = httpClient.doCall("POST", uri_pay, JSONObject.toJSONString(payBody), Collections.emptyMap(), headers, JSONObject.class, ResponseFormat.JSON);


    }
}
