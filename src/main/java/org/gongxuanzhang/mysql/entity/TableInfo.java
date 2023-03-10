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

package org.gongxuanzhang.mysql.entity;

import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.gongxuanzhang.mysql.annotation.DependOnContext;
import org.gongxuanzhang.mysql.core.EngineSelectable;
import org.gongxuanzhang.mysql.core.Refreshable;
import org.gongxuanzhang.mysql.core.SessionManager;
import org.gongxuanzhang.mysql.core.select.As;
import org.gongxuanzhang.mysql.core.select.SelectCol;
import org.gongxuanzhang.mysql.exception.ExecuteException;
import org.gongxuanzhang.mysql.exception.MySQLException;
import org.gongxuanzhang.mysql.tool.TableInfoUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gongxuanzhang.mysql.tool.ExceptionThrower.errorSwap;

/**
 * 表信息
 *
 * @author gxz gongxuanzhang@foxmail.com
 **/
@Data
@DependOnContext
@Slf4j
public class TableInfo implements ExecuteInfo, EngineSelectable, Refreshable {


    /**
     * 在mysql的frm文件上加了个我的姓 嘿嘿
     **/
    public final static String GFRM_SUFFIX = ".gfrm";

    public final static String GIBD_SUFFIX = ".gibd";

    private DatabaseInfo database;

    private String tableName;
    private List<ColumnInfo> columnInfos;
    private List<String> primaryKey;
    private String comment;
    private String engineName;

    /**
     * 自增主键，只能有一个
     */
    private IncrementKey incrementKey;


    public TableInfo() {

    }

    public TableInfo(MySqlCreateTableStatement statement) throws MySQLException {
        List<SQLColumnDefinition> columnDefinitions = statement.getColumnDefinitions();
        if (CollectionUtils.isEmpty(columnDefinitions)) {
            throw new MySQLException("无法获取列信息");
        }
        Set<String> primaryKey = new LinkedHashSet<>(statement.getPrimaryKeyNames());
        columnInfos = new ArrayList<>();
        for (SQLColumnDefinition columnDefinition : columnDefinitions) {
            columnInfos.add(new ColumnInfo(columnDefinition));
            if (columnDefinition.isPrimaryKey() && primaryKey.add(columnDefinition.getColumnName())) {
                throw new MySQLException("主键重复定义");
            }

        }
        TableInfoUtils.fillTableInfo(this, statement.getTableSource().toString());
        if (statement.getComment() != null) {
            this.comment = statement.getComment().toString();
        }
    }


    public void transport(TableInfo tableInfo) {
        this.tableName = tableInfo.tableName;
        this.comment = tableInfo.comment;
        this.database = tableInfo.database;
        this.columnInfos = tableInfo.columnInfos;
        this.primaryKey = tableInfo.primaryKey;
        this.engineName = tableInfo.engineName;
        this.incrementKey = tableInfo.incrementKey;

    }


    /**
     * 表中的唯一键
     *
     * @return 返回个啥
     **/
    public Set<String> uniqueKeys() {
        Set<String> uniqueKey = this.columnInfos.stream()
                .filter(ColumnInfo::isUnique)
                .map(ColumnInfo::getName)
                .collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(primaryKey)) {
            uniqueKey.addAll(primaryKey);
        }
        return uniqueKey;
    }


    /**
     * 表结构文件
     **/
    @DependOnContext
    public File structFile() throws MySQLException {
        File databaseDir = checkDatabase();
        return new File(databaseDir, this.tableName + GFRM_SUFFIX);
    }

    /**
     * 表数据文件
     **/
    @DependOnContext
    public File dataFile() throws MySQLException {
        File databaseDir = checkDatabase();
        return new File(databaseDir, this.tableName + GIBD_SUFFIX);
    }

    /**
     * 校验数据库信息
     *
     * @return 返回数据库文件夹
     * @throws MySQLException 校验失败会抛出异常
     **/
    private File checkDatabase() throws MySQLException {
        if (database == null) {
            this.database = SessionManager.currentSession().getDatabase();
        }
        if (database == null) {
            throw new MySQLException("无法获取database");
        }
        File databaseDir = this.database.sourceFile();
        if (!databaseDir.exists() || !databaseDir.isDirectory()) {
            throw new ExecuteException("数据库[" + database + "]不存在");
        }
        return databaseDir;
    }

    /**
     * 返回带数据库的完整表名
     */
    public String absoluteName() {
        return database.getDatabaseName() + "." + this.tableName;
    }


    /**
     * 根据as把所有需要解析的列都摆出
     * '*' 和重复列都解析
     * 如果别名重复 默认在后面添加(1) (2) 以此类推
     **/
    public List<SelectCol> scatterCol(As as) throws MySQLException {
        List<SelectCol> result = new ArrayList<>();
        Map<String, Integer> aliasCount = new HashMap<>();
        as.forEach(col -> {
            if (col.isAll()) {
                fillAllCol(aliasCount, result);
            } else {
                fillSingleCol(col, aliasCount, result);
            }
        });
        return result;
    }

    private void fillSingleCol(SelectCol col, Map<String, Integer> allCount, List<SelectCol> result) {
        String alias = col.getAlias() == null ? col.getColName() : col.getAlias();
        int count = allCount.merge(alias, 1, Integer::sum);
        String colName = col.getColName();
        if (count > 1) {
            alias += "(" + (count - 1) + ")";
        }
        result.add(SelectCol.single(colName, alias));
    }

    private void fillAllCol(Map<String, Integer> allCount, List<SelectCol> result) {
        for (ColumnInfo columnInfo : this.columnInfos) {
            fillSingleCol(SelectCol.single(columnInfo.getName(), null), allCount, result);
        }
    }

    public List<JSONObject> descTable() {
        List<JSONObject> result = new ArrayList<>();
        Set<String> primary = primaryKey == null ? new HashSet<>() : new HashSet<>(primaryKey);
        for (ColumnInfo columnInfo : columnInfos) {
            JSONObject colInfo = new JSONObject(8);
            colInfo.put("field", columnInfo.getName());
            colInfo.put("type", columnInfo.getType());
            colInfo.put("notNull", Boolean.toString(!columnInfo.isNotNull()));
            colInfo.put("primary key", Boolean.toString(primary.contains(columnInfo.getName())));
            colInfo.put("default", columnInfo.getDefaultValue());
            colInfo.put("auto_increment", Boolean.toString(columnInfo.isAutoIncrement()));
            colInfo.put("unique", Boolean.toString(columnInfo.isUnique()));
            colInfo.put("comment", columnInfo.getComment());
            result.add(colInfo);
        }
        return result;
    }


    public void addPrimaryKey(String primaryKey) {
        if (this.primaryKey == null) {
            this.primaryKey = new ArrayList<>();
        }
        this.primaryKey.add(primaryKey);
    }

    /**
     * 更新TableInfo内容
     **/
    public void refresh() throws MySQLException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(this.structFile());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(this);
            log.info("更新表{}", this.absoluteName());
        } catch (IOException e) {
            errorSwap(e);
        }
    }

}
