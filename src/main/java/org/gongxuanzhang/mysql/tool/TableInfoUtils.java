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

package org.gongxuanzhang.mysql.tool;

import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import org.gongxuanzhang.mysql.core.FromBox;
import org.gongxuanzhang.mysql.core.SessionManager;
import org.gongxuanzhang.mysql.core.select.From;
import org.gongxuanzhang.mysql.entity.DatabaseInfo;
import org.gongxuanzhang.mysql.entity.TableInfo;
import org.gongxuanzhang.mysql.exception.MySQLException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 表相关工具类
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
public abstract class TableInfoUtils {


    private static final String TABLE_NAME_SEPARATOR = ".";

    /**
     * 拼装已经存在的表
     **/
    public static void assembleTableInfo(FromBox box, String tableName) throws MySQLException {
        TableInfo tableInfo = selectTableInfo(tableName);
        box.setFrom(new From(tableInfo));
    }

    /**
     * 选择已经存在的表信息
     *
     * @param candidate sql解析出的表名  database.table
     * @return 返回已经存在的表信息
     **/
    public static TableInfo selectTableInfo(String candidate) throws MySQLException {
        if (candidate.indexOf(TABLE_NAME_SEPARATOR) != candidate.lastIndexOf(TABLE_NAME_SEPARATOR)) {
            throw new MySQLException(candidate + "无法解析");
        }
        if (candidate.contains(TABLE_NAME_SEPARATOR)) {
            String[] split = candidate.split("\\.");
            return Context.getTableManager().select(split[0] + TABLE_NAME_SEPARATOR + split[1]);
        }
        DatabaseInfo database = SessionManager.currentSession().getDatabase();
        String absoluteName = database + TABLE_NAME_SEPARATOR + candidate;
        return Context.getTableManager().select(absoluteName);
    }

    /**
     * 填充tableInfo
     * 用于还不存在的表
     **/
    public static void fillTableInfo(TableInfo tableInfo, String candidate) throws MySQLException {
        if (candidate.indexOf(TABLE_NAME_SEPARATOR) != candidate.lastIndexOf(TABLE_NAME_SEPARATOR)) {
            throw new MySQLException(candidate + "无法解析");
        }
        DatabaseInfo databaseInfo;
        String tableName;
        if (candidate.contains(TABLE_NAME_SEPARATOR)) {
            String[] split = candidate.split("\\.");
            databaseInfo = new DatabaseInfo(split[0]);
            tableName = split[1];
        } else {
            databaseInfo = SessionManager.currentSession().getDatabase();
            tableName = candidate;
        }
        tableInfo.setDatabase(databaseInfo);
        tableInfo.setTableName(tableName);
    }

    /**
     * 批量查询已经存在的表信息
     *
     * @param tableSources sql解析出的表信息
     * @return 返回填充
     **/
    public static List<TableInfo> batchSelectTableInfo(List<? extends SQLTableSource> tableSources) throws MySQLException {
        if (CollectionUtils.isEmpty(tableSources)) {
            return new ArrayList<>();
        }
        List<TableInfo> result = new ArrayList<>(tableSources.size());
        for (SQLTableSource tableSource : tableSources) {
            result.add(selectTableInfo(tableSource.toString()));
        }
        return result;
    }
}
