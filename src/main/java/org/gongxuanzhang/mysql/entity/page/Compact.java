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

package org.gongxuanzhang.mysql.entity.page;

import lombok.Data;

/**
 * compact行格式
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
@Data
public class Compact implements UserRecord {

    /**
     * 记录头信息 5字节
     **/
    RecordHeader recordHeader;

    /**
     * 变长字段信息
     **/
    int[] variables;

    /**
     * null值列表
     **/
    int[] nullValues;
    /**
     * 真实记录
     **/
    byte[] body;
    /**
     * 6字节  唯一标识
     **/
    int rowId;
    /**
     * 事务id  6字节
     **/
    int transactionId;

    /**
     * 7字节，回滚指针
     **/
    int rollPointer;


}
