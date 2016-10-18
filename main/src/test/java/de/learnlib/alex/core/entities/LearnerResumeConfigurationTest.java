/*
 * Copyright 2016 TU Dortmund
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.learnlib.alex.core.entities;

import de.learnlib.alex.core.entities.learnlibproxies.eqproxies.AbstractEquivalenceOracleProxy;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

public class LearnerResumeConfigurationTest {

    private LearnerResumeConfiguration configuration;

    @Before
    public void setUp() {
        configuration = new LearnerResumeConfiguration();
    }

    @Test
    public void shouldSayThatItIsAValidConfigurationIfItIsOne() {
        configuration.checkConfiguration(); // nothing should happen
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThatAnExceptionIsThrownIfNoEqOracleIsGiven() {
        configuration.setEqOracle(null);

        configuration.checkConfiguration(); // should fail
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureThatAnExceptionIsThrownIfAnInvalidEqOracleIsGiven() {
        AbstractEquivalenceOracleProxy eqOracle = mock(AbstractEquivalenceOracleProxy.class);
        willThrow(IllegalArgumentException.class).given(eqOracle).checkParameters();
        configuration.setEqOracle(eqOracle);

        configuration.checkConfiguration(); // should fail
    }

}