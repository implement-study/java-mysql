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

package org.gongxuanzhang.mysql

import org.gongxuanzhang.mysql.core.result.SingleRowResult


fun SingleRowResult.destructuringEquals(other: Collection<String>?): Boolean {
    if (this.data.size != other?.size) {
        return false
    }
    val selectData = this.data.map { jsonObject -> jsonObject.getObject(this.head[0], String::class.java) }
    return selectData.chaosEquals(other)
}
