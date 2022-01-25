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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.jooq.tools.json.JSONObject;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.invoice.api.Invoice;
import org.killbill.billing.invoice.api.InvoiceApiException;
import org.killbill.billing.invoice.api.InvoiceUserApi;
import org.killbill.billing.notification.plugin.api.ExtBusEvent;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillEventDispatcher;
import org.killbill.billing.plugin.api.PluginTenantContext;
import org.killbill.billing.plugin.helloworld.biz.ProductLine;
import org.killbill.billing.plugin.helloworld.biz.RemoteHttpClient;
import org.killbill.billing.plugin.util.http.InvalidRequest;
import org.killbill.billing.plugin.util.http.ResponseFormat;
import org.killbill.billing.tenant.api.TenantApiException;
import org.killbill.billing.util.callcontext.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class HelloWorldListener implements OSGIKillbillEventDispatcher.OSGIKillbillEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(HelloWorldListener.class);

    private final OSGIKillbillAPI osgiKillbillAPI;

    public HelloWorldListener(final OSGIKillbillAPI killbillAPI) {
        this.osgiKillbillAPI = killbillAPI;
    }

    @Override
    public void handleKillbillEvent(final ExtBusEvent killbillEvent) {
        logger.info("Received event {} for object id {} of type {}",
                    killbillEvent.getEventType(),
                    killbillEvent.getObjectId(),
                    killbillEvent.getObjectType());
        final TenantContext context = new PluginTenantContext(killbillEvent.getAccountId(), killbillEvent.getTenantId());
        List<String> plugin_properties = new ArrayList<>(1);
        try {
            plugin_properties.addAll(osgiKillbillAPI.getTenantUserApi().getTenantValuesForKey("PLUGIN_CONFIG_hello-world-plugin:billing-host", context));
            logger.info("toria get plugin property PLUGIN_CONFIG_hello-world-plugin:billing-host value:{}", plugin_properties);
        } catch (TenantApiException e) {
            logger.error("toria osgiKillbillAPI.getTenantUserApi().getTenantValuesForKey(\"PLUGIN_CONFIG_hello-world-plugin\", context) error");
            e.printStackTrace();
        }
        switch (killbillEvent.getEventType()) {
            case INVOICE_CREATION:
                try{
                    final Invoice invoice = osgiKillbillAPI.getInvoiceUserApi().getInvoice(killbillEvent.getObjectId(), context);
//                    final List<InvoiceItem> invoiceItem = osgiKillbillAPI.getInvoiceUserApi().getInvoiceItemsByParentInvoice(invoice.getId(), context);

                    logger.info("toria 账单查看invoiceItems[0].productLine：{}", ProductLine.findStartWith(invoice.getInvoiceItems().get(0).getPlanName()));


                    // 1.检查账户余额
                    // 2.调用easemob billing 查询赠送金 api
                    // 3.调用easemob billing 抵扣赠送金 api
                    /**
                     * HttpClient(final String url,
                     *                       final String username,
                     *                       final String password,
                     *                       final String proxyHost,
                     *                       final Integer proxyPort,
                     *                       final Boolean strictSSL,
                     *                       final int connectTimeoutMs,
                     *                       final int readTimeoutMs)
                     */
                    try {
                        //0.查账单金额
                        if (invoice.getBalance().compareTo(BigDecimal.ZERO) > 0 ) {
                            //1.先查账户余额
                            //                                        AuditUserApi auditUserApi = osgiKillbillAPI.getAuditUserApi();
                            //                                        AccountUserApi accountUserApi = osgiKillbillAPI.getAccountUserApi();
                            //                                        final Account account = accountUserApi.getAccountById(killbillEvent.getAccountId(), context);
                            //                                        final AccountAuditLogs accountAuditLogs = auditUserApi.getAccountAuditLogs(account.getId(), AuditLevel.NONE, context);

                            InvoiceUserApi invoiceApi = osgiKillbillAPI.getInvoiceUserApi();
                            final BigDecimal accountBalance = invoiceApi.getAccountBalance(killbillEvent.getAccountId(), context);
                            logger.info("toria get accountBalance:{}", accountBalance);
                            logger.info("toria get invoiceBalance:{}", invoice.getBalance());
                            logger.info("toria get invoiceCreditedAmount:{}", invoice.getCreditedAmount());
                            logger.info("toria get invoiceOriginalChargedAmount:{}", invoice.getOriginalChargedAmount());
                            if (accountBalance.compareTo(BigDecimal.ZERO) > 0) { // 有欠费才抵扣。
//                                String host = helloWorldConfigurationHandler.getConfigurable().getProperty("PLUGIN_CONFIG_hello-world-plugin.remote-billing-host");
//                                logger.info("hello-world-plugin config remote host:", host);
                                RemoteHttpClient httpClient = new RemoteHttpClient(plugin_properties.get(0), "", "", null, null, false, 2000, 60000);
//                                RemoteHttpClient httpClient = new RemoteHttpClient("http://172.16.31.124:31732", "", "", null, null, false, 2000, 60000);
//                                RemoteHttpClient httpClient = new RemoteHttpClient("http://127.0.0.1:8081", "", "", null, null, false, 2000, 60000);
//                                Map querys = new HashMap<String, String>(2);
//                                querys.put("withDetail","true");
//                                querys.put("withExpend","true");
//                                String uri = "/presented/{orgName}";

                                Account account = osgiKillbillAPI.getAccountUserApi().getAccountById(killbillEvent.getAccountId(), context);

//                                uri = uri.replace("{orgName}", account.getExternalKey());
//                                // 查询赠送金账户
//                                JSONObject result = httpClient.doCall("GET", uri, null, querys, Collections.emptyMap(), JSONObject.class, ResponseFormat.JSON);

                                /**
                                 * 遍历如下结果中的 detail -> product == invoiceItem.planName.startWith
                                 * detail -> amount <= invoice.getBalance
                                 * {
                                 *     "status": "ok",
                                 *     "data": {
                                 *         "balance": 198.0,
                                 *         "detail": [
                                 *             {
                                 *                 "amount": 198.00,
                                 *                 "product": "IM"
                                 *             }
                                 *         ]
                                 *     },
                                 *     "error": null,
                                 *     "timestamp": 1642380495574
                                 * }
                                 */
//                                if (null != result && result.get("status").equals("ok") && null != result.get("data")) {
//                                    ArrayList details = (ArrayList)((LinkedHashMap)result.get("data")).get("detail");
//                                    if ((double)result.get("balance") > 0 || (null != details && details.size() > 0)) {
////                                        for (int i=0 ;i < details.size() ; i++) {
////                                            LinkedHashMap detail = (LinkedHashMap)details.get(i);
////                                            double amount = (double)detail.get("amount");
////                                            String product = (String)detail.get("product");
////                                            if (!Strings.isNullOrEmpty(product)) { // 留给billing去判断
////
////                                            }
////                                        }
//
//                                    }
//                                }
                                // 抵扣账单
                                String uri_pay = "/presented/{orgName}/pay/to/{targetId}/internal";
                                uri_pay = uri_pay.replace("{orgName}", account.getExternalKey()).replace("{targetId}", invoice.getId().toString());

                                /**
                                 * {
                                 *     "amount":2,
                                 *     "associatedEntityId":"ac86cb73-46a3-44b6-bc24-6cbf89282c4c",
                                 *     "product":"IM",
                                 *     "reason":"2021-11-11大促A类优惠券抵扣",
                                 *     "salesman":"史梁"
                                 * }
                                 * @return
                                 */

                                StringBuilder products = new StringBuilder();
                                LinkedHashSet productSet = new LinkedHashSet(1);
                                invoice.getInvoiceItems().stream().filter( f -> !Strings.isNullOrEmpty(f.getPlanName()))
                                                                    .forEach(invoiceItem ->
                                                                                     productSet.add(ProductLine.findStartWith(invoiceItem.getProductName())));

                                for (Iterator iterator = productSet.iterator(); iterator.hasNext() ; ) {
                                    products.append(iterator.next()).append("/");
                                }
                                Map payBody = new HashMap(5);
                                payBody.put("amount", accountBalance);//抵扣欠款最高额度
                                payBody.put("associatedEntityId", invoice.getId().toString());
                                payBody.put("product", products.length() > 0 ?
                                                       products.append(ProductLine.ANY.name()).toString() : ProductLine.IM.name());
                                payBody.put("reason", "赠送金账单系统自动抵扣_" + account.getExternalKey() + "_" + invoice.getId().toString());
                                payBody.put("salesman", "killbill");

                                Map<String, String> headers = new HashMap<>(1);
                                headers.put("Content-Type", "application/json");

                                JSONObject result_pay = httpClient.doCall("POST", uri_pay, JSONObject.toJSONString(payBody), Collections.emptyMap(), headers, JSONObject.class, ResponseFormat.JSON);


                                /**
                                 * 抵扣接口返回
                                 *
                                 * {
                                 *     "status": "ok",
                                 *     "data": {
                                 *         "ownerName": null,
                                 *         "type": 0,
                                 *         "reason": "2021-11-11大促A类优惠券抵扣",
                                 *         "associatedEntityId": "ac86cb73-46a3-44b6-bc24-6cbf89282c4c",
                                 *         "rule": null,
                                 *         "product": "IM",
                                 *         "amount": 2.0,
                                 *         "salesman": "史梁",
                                 *         "currency": null,
                                 *         "itemType": null
                                 *     },
                                 *     "error": null,
                                 *     "timestamp": 1642379834731
                                 * }
                                 */
                                if (null != result_pay && result_pay.get("status").equals("ok")) {
                                    // 成功继续
                                } else {
                                    // 失败记录error日志。改由billing记录。
//                                    String uri_logfail = "/presented/{orgName}/pay/fail/{targetId}";
//                                    uri_logfail = uri_logfail.replace("{orgName}", account.getExternalKey()).replace("{targetId}", invoice.getId().toString());
//                                    httpClient.doCall("POST", uri_logfail, null, Collections.emptyMap(), Collections.emptyMap(), JSONObject.class, ResponseFormat.JSON);
                                }
                            }
                        }




                        //                                    httpClient.doCall("GET", "http://hsb-didi-guangzhou-mesos-slave4:31487");
                        /**
                         * (final String verb, final String uri, final String body, final Map<String, String> queryParams,
                         *                         final Map<String, String> headers, final Class<T> clazz, final ResponseFormat format)
                         */

                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (InvalidRequest invalidRequest) {
                        invalidRequest.printStackTrace();
                    } catch (AccountApiException e) {
                        e.printStackTrace();
                    }

//                    invoiceItem.forEach(new Consumer<InvoiceItem>() {
//                        @Override
//                        public void accept(final InvoiceItem invoiceItem) {
//                            if(invoiceItem.getInvoiceItemType().equals(InvoiceItemType.USAGE)) {
////                                if(invoiceItem.getProductName().startsWith("IM-")) //新版计划usage按不足月比例折扣
////                                {
//                                    //查询订阅开始日期 与 账单开始日期对比，如果相同且不等于 BCD 则为不足月
//                                    UUID sId = invoiceItem.getSubscriptionId();
//
//                                    try {
//                                        Subscription subscription = osgiKillbillAPI.getSubscriptionApi().getSubscriptionForExternalKey(sId.toString(), context);
//                                        logger.info("toria 账单查看用量获取订阅：{}", subscription);
//                                        if(subscription != null &&
//                                           !subscription.getEffectiveStartDate().equals(subscription.getBillingStartDate())){// 生效日期 与 计费开始日期
//                                            LocalDate effectiveStartDate = subscription.getEffectiveStartDate();
//                                            LocalDate billingStartDate = subscription.getBillingStartDate();
//                                            if (billingStartDate.minusMonths(1).getMonthOfYear() == effectiveStartDate.getMonthOfYear()) { // 上个月订阅
//
//                                            }
//                                        }
//                                    } catch (SubscriptionApiException e) {
//                                        e.printStackTrace();
//                                    }
//                            }
//                        }
//                    });
                } catch (final InvoiceApiException e) {
                    logger.error("toria error when get invoice", e);
                }
                break;
            //
            // Handle ACCOUNT_CREATION and ACCOUNT_CHANGE only for demo purpose and just print the account
            //
            case ACCOUNT_CREATION:
                try {
                    final Account account = osgiKillbillAPI.getAccountUserApi().getAccountById(killbillEvent.getAccountId(), context);
                    logger.info("Account information: " + account);
                } catch (final AccountApiException e) {
                    logger.warn("Unable to find account", e);
                }
                break;
            case ACCOUNT_CHANGE:
                try {
                    final Account account = osgiKillbillAPI.getAccountUserApi().getAccountById(killbillEvent.getAccountId(), context);
                    logger.info("Account information: " + account);
                } catch (final AccountApiException e) {
                    logger.warn("Unable to find account", e);
                }
                break;

            default:
                break;

        }
    }


}
