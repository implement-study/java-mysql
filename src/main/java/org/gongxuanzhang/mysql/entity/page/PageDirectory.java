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
import org.gongxuanzhang.mysql.core.ByteSwappable;
import org.gongxuanzhang.mysql.entity.ShowLength;

import java.nio.ByteBuffer;

/**
 * 页目录
 * 里面有N个slot
 * 每个slot中只记录一个偏移量
 *
 * @author gxz gongxuanzhangmelt@gmail.com
 **/
@Data
public class PageDirectory implements ByteSwappable<PageDirectory>, ShowLength {

    /**
     * slot的每个数字记录每个槽中最大数据的偏移位置
     **/
    short[] slots;


    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(slots.length * 2);
        for (short slot : slots) {
            buffer.putShort(slot);
        }
        return buffer.array();
    }

    @Override
    public PageDirectory fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        this.slots = new short[bytes.length / 2];
        for (int i = bytes.length / 2; i > 0; i--) {
            slots[i] = buffer.getShort();
        }
        return this;
    }

    @Override
    public int length() {
        return this.slots.length * 2;
    }
}
