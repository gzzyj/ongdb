/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB.
 *
 * ONgDB is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.api.query;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class WaitingOnQuery extends ExecutingQueryStatus
{
    private final ExecutingQuery query;
    private final long startTimeNanos;

    WaitingOnQuery( ExecutingQuery query, long startTimeNanos )
    {
        this.query = query;
        this.startTimeNanos = startTimeNanos;
    }

    @Override
    long waitTimeNanos( long currentTimeNanos )
    {
        return currentTimeNanos - startTimeNanos;
    }

    @Override
    Map<String,Object> toMap( long currentTimeNanos )
    {
        Map<String,Object> map = new HashMap<>();
        map.put( "queryId", "query-" + query.internalQueryId() );
        map.put( "waitTimeMillis", TimeUnit.NANOSECONDS.toMillis( waitTimeNanos( currentTimeNanos ) ) );
        return map;
    }

    @Override
    String name()
    {
        return WAITING_STATE;
    }
}
