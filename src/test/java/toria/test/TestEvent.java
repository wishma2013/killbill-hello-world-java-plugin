/*
 * Copyright 2020-2021 Equinix, Inc
 * Copyright 2014-2021 The Billing Project, LLC
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

import javax.inject.Singleton;

import org.jooby.Jooby;
import org.jooby.json.Jackson;
import org.jooq.tools.json.JSONObject;

/**
 * @Description TODO
 * @Author Toria toria.shi@easemob.com
 * @Date 2021/12/9 下午1:19
 **/
@Singleton
public class TestEvent extends Jooby{
    {
        /** Render JSON: */
        use(new Jackson());

        /**
         * Say hello:
         */
        get(req -> {
            String name = req.param("name").value("Jooby");
            return new JSONObject.SimpleEntry("key", "Hello " + name + "!");
        });

    }
//    @Test
//    public void test1(){
//        runApp("args", app -> {
//            app.get("/", ctx -> "Welcome to Jooby!");
//        });
//
//    }

    public static void main(String[] args) {
        System.out.println("starting ...");
        run(TestEvent::new, args);
    }
}
