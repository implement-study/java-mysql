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

package org.gongxuanzhang.mysql.service.analysis.dml;

import org.gongxuanzhang.mysql.core.select.As;
import org.gongxuanzhang.mysql.core.select.JsonOrder;
import org.gongxuanzhang.mysql.core.select.SelectCol;
import org.gongxuanzhang.mysql.entity.SingleSelectInfo;
import org.gongxuanzhang.mysql.exception.MySQLException;
import org.gongxuanzhang.mysql.service.analysis.TokenAnalysis;
import org.gongxuanzhang.mysql.service.executor.Executor;
import org.gongxuanzhang.mysql.service.executor.dml.SelectExecutor;
import org.gongxuanzhang.mysql.service.token.SqlToken;
import org.gongxuanzhang.mysql.service.token.TokenKind;
import org.gongxuanzhang.mysql.service.token.TokenSupport;
import org.gongxuanzhang.mysql.storage.StorageEngine;
import org.gongxuanzhang.mysql.tool.Context;
import org.gongxuanzhang.mysql.tool.ExceptionThrower;

import java.util.List;

/**
 * select 解析器
 *
 * @author gxz gongxuanzhang@foxmail.com
 **/
public class SelectAnalysis implements TokenAnalysis {


    @Override
    public Executor analysis(List<SqlToken> sqlTokenList) throws MySQLException {
        SingleSelectInfo singleSelectInfo = new SingleSelectInfo();
        int offset = 1;
        offset += fillAs(singleSelectInfo, sqlTokenList.subList(1, sqlTokenList.size()));
        offset += TokenSupport.fillFrom(singleSelectInfo, sqlTokenList.subList(offset, sqlTokenList.size()));
        offset += TokenSupport.fillWhere(singleSelectInfo, sqlTokenList.subList(offset, sqlTokenList.size()));
        singleSelectInfo.setOrder(new JsonOrder());
        offset += TokenSupport.fillOrderBy(singleSelectInfo, sqlTokenList.subList(offset, sqlTokenList.size()));
        ExceptionThrower.ifNotThrow(offset == sqlTokenList.size(), "sql解析失败");
        StorageEngine engine = Context.selectStorageEngine(singleSelectInfo.getFrom().getMain());
        return new SelectExecutor(engine, singleSelectInfo);
    }


    /**
     * 解析别名
     */
    private int fillAs(SingleSelectInfo singleSelectInfo, List<SqlToken> sqlTokenList) throws MySQLException {
        int offset = 0;
        As as = new As();
        while (offset < sqlTokenList.size() && TokenSupport.isNotTokenKind(sqlTokenList.get(offset), TokenKind.FROM)) {
            //  *
            if (TokenSupport.isTokenKind(sqlTokenList.get(offset), TokenKind.MULTI)) {
                offset += multiAs(as, sqlTokenList, offset);
            } else {
                offset += singleAs(as, sqlTokenList, offset);
            }
        }
        if (as.isEmpty()) {
            throw new MySQLException("无法解析列");
        }
        singleSelectInfo.setAs(as);
        return offset;
    }


    /**
     * 解析带*号的as
     *
     * @return 返回偏移量
     **/
    private int multiAs(As as, List<SqlToken> sqlTokenList, int start) throws MySQLException {
        int offset = 0;
        TokenSupport.mustTokenKind(sqlTokenList.get(offset + start), TokenKind.MULTI);
        offset++;
        if (offset + start < sqlTokenList.size() &&
                TokenSupport.isTokenKind(sqlTokenList.get(offset + start), TokenKind.COMMA)) {
            offset++;
        }
        as.addSelectCol(SelectCol.allCol());
        return offset;
    }


    /**
     * 解析单列
     *
     * @return 返回偏移量
     **/
    private int singleAs(As as, List<SqlToken> sqlTokenList, int start) throws MySQLException {
        int offset = 0;
        String key = TokenSupport.getString(sqlTokenList.get(start + offset));
        offset++;
        if (offset + start < sqlTokenList.size() &&
                TokenSupport.isTokenKind(sqlTokenList.get(start + offset), TokenKind.AS)) {
            offset++;
        }
        String value = null;
        if (offset + start < sqlTokenList.size() &&
                TokenSupport.isTokenKind(sqlTokenList.get(start + offset), TokenKind.LITERACY, TokenKind.VAR)) {
            value = TokenSupport.getString(sqlTokenList.get(start + offset));
            offset++;
        }
        as.addSelectCol(SelectCol.single(key, value));
        //  判断下一个是否是逗号或者结束
        if (offset + start < sqlTokenList.size() && TokenSupport.isTokenKind(sqlTokenList.get(start + offset),
                TokenKind.COMMA)) {
            offset++;
        }
        return offset;
    }


}
