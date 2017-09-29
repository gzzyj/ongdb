/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
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
package org.neo4j.kernel.api.impl.fulltext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.util.concurrent.ExecutionException;

import org.neo4j.kernel.AvailabilityGuard;
import org.neo4j.kernel.impl.util.Neo4jJobScheduler;
import org.neo4j.kernel.lifecycle.LifeSupport;
import org.neo4j.logging.Log;
import org.neo4j.logging.NullLog;
import org.neo4j.scheduler.JobScheduler;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FulltextUpdateApplierTest
{
    private LifeSupport life;
    private FulltextUpdateApplier applier;
    private AvailabilityGuard availabilityGuard;
    private JobScheduler scheduler;
    private Log log;

    @Before
    public void setUp() throws Throwable
    {
        life = new LifeSupport();
        log = NullLog.getInstance();
        availabilityGuard = new AvailabilityGuard( Clock.systemUTC(), log );
        scheduler = life.add( new Neo4jJobScheduler() );
        life.start();
    }

    private void startApplier( Log log, JobScheduler scheduler )
    {
        applier = life.add( new FulltextUpdateApplier( log, availabilityGuard, scheduler ) );
    }

    @After
    public void tearDown()
    {
        life.shutdown();
    }

    @Test
    public void exceptionsDuringIndexUpdateMustPropagateToTheCaller() throws Exception
    {
        startApplier( log, scheduler );
        AsyncFulltextIndexOperation op = applier.updatePropertyData( null, null );

        try
        {
            op.awaitCompletion();
            fail( "awaitCompletion should have thrown" );
        }
        catch ( ExecutionException e )
        {
            assertThat( e.getCause(), is( instanceOf( NullPointerException.class ) ) );
        }
    }
    // todo exceptions during population most be logged and mark the index as failed
    // todo the applier must shut down if the availability guard is shut down at start
}
