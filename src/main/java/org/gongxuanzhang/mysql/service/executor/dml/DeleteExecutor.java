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

package org.gongxuanzhang.mysql.service.executor.dml;

import org.gongxuanzhang.mysql.core.result.Result;
import org.gongxuanzhang.mysql.entity.DeleteInfo;
import org.gongxuanzhang.mysql.exception.MySQLException;
import org.gongxuanzhang.mysql.service.executor.EngineExecutor;
import org.gongxuanzhang.mysql.storage.StorageEngine;

/**
 * 删除执行器
 *
 * @author gxz gongxuanzhang@foxmail.com
 **/
public class DeleteExecutor extends EngineExecutor<DeleteInfo> {

    public DeleteExecutor(StorageEngine engine, DeleteInfo info) {
        super(engine, info);
    }

    @Override
    public Result doEngine(StorageEngine engine, DeleteInfo info) throws MySQLException {
        return engine.delete(info);
    }
}
