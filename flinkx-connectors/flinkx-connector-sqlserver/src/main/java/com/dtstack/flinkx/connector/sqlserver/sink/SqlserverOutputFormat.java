/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.flinkx.connector.sqlserver.sink;

import com.dtstack.flinkx.connector.jdbc.sink.JdbcOutputFormat;
import com.dtstack.flinkx.connector.jdbc.util.JdbcUtil;
import com.dtstack.flinkx.connector.sqlserver.SqlServerDialect;
import com.dtstack.flinkx.connector.sqlserver.converter.SqlserverRawTypeConverter;
import com.dtstack.flinkx.throwable.FlinkxRuntimeException;
import com.dtstack.flinkx.util.TableUtil;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Company：www.dtstack.com
 *
 * @author shitou
 * @date 2021/5/19 20:03
 */
public class SqlserverOutputFormat extends JdbcOutputFormat {

    protected static final Logger LOG = LoggerFactory.getLogger(SqlserverOutputFormat.class);

    @Override
    protected void openInternal(int taskNumber, int numTasks) {
        super.openInternal(taskNumber, numTasks);
        LogicalType rowType =
                TableUtil.createRowType(column, columnType, SqlserverRawTypeConverter::apply);
        setRowConverter(jdbcDialect.getColumnConverter((RowType) rowType));

        Statement statement = null;
        String sql = ((SqlServerDialect)jdbcDialect).getIdentityInsertOnSql(jdbcConf.getSchema(), jdbcConf.getTable());
        try {
            statement = dbConn.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            LOG.error("error to execute {}", sql);
            throw new FlinkxRuntimeException(e);
        }finally {
            JdbcUtil.closeDbResources(null, statement, null, false);
        }
    }
}
