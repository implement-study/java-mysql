/*
 * Copyright 2023 java-mysql  and the original author or authors <gongxuanzhangmelt@gmail.com>.
 *
 * Licensed under the GNU Affero General Public License v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://github.com/implement-study/java-mysql/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gongxuanzhang.mysql.component;

import org.gongxuanzhang.mysql.annotation.Engine;
import org.gongxuanzhang.mysql.core.SessionManager;
import org.gongxuanzhang.mysql.storage.StorageEngine;
import org.gongxuanzhang.mysql.tool.Context;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author gxz gongxuanzhang@foxmail.com
 **/
@Component
public class MySqlInitListener implements ApplicationListener<ApplicationStartedEvent> {

    private final Map<String, StorageEngine> engineMap;

    public MySqlInitListener(@Engine Map<String, StorageEngine> engineMap) {
        this.engineMap = engineMap;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        engineMap.forEach((k, v) -> Context.registerEngine(v));
        SessionManager.init();
    }
}
