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

import org.gongxuanzhang.mysql.core.ByteSwappable;
import org.gongxuanzhang.mysql.core.factory.ConstantSize;
import org.gongxuanzhang.mysql.entity.ShowLength;

import java.nio.ByteBuffer;

/**
 * 上确界
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
public class Supremum implements ByteSwappable<Supremum>, ShowLength {

    /**
     * 记录头信息 5字节
     **/
    RecordHeader recordHeader;

    /**
     * 定长8字节 "supremum"
     **/
    byte[] body;


    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(ConstantSize.SUPREMUM_SIZE.getSize());
        buffer.put(this.recordHeader.toBytes());
        buffer.put(body);
        return buffer.array();
    }

    @Override
    public Supremum fromBytes(byte[] bytes) {
        ConstantSize.SUPREMUM_SIZE.checkSize(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] headBuffer = new byte[ConstantSize.RECORD_HEADER.getSize()];
        buffer.get(headBuffer);
        this.recordHeader = new RecordHeader(headBuffer);
        this.body = new byte[ConstantSize.SUPREMUM_BODY_SIZE.getSize()];
        buffer.get(this.body);
        return this;
    }

    @Override
    public int length() {
        return ConstantSize.SUPREMUM_SIZE.getSize();
    }
}
