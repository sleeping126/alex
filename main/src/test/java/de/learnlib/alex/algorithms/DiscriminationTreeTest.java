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

package de.learnlib.alex.algorithms;

import de.learnlib.algorithms.discriminationtree.mealy.DTLearnerMealy;
import de.learnlib.api.LearningAlgorithm;
import de.learnlib.discriminationtree.DTNode;
import de.learnlib.oracles.SULOracle;
import net.automatalib.words.Alphabet;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class DiscriminationTreeTest {

    private DiscriminationTree algorithm;

    @Before
    public void setUp() {
        algorithm = new DiscriminationTree();
    }

    @Test
    public void shouldCreateCorrectLearner() {
        Alphabet<String> sigma = mock(Alphabet.class);
        SULOracle<String, String> oracle = mock(SULOracle.class);

        algorithm.createLearner(sigma, oracle);
    }

    @Test
    public void shouldReturnCorrectInternalData() {
        DTLearnerMealy learner = createDTLearnerMock();

        String json = algorithm.getInternalData(learner);
        assertEquals("{\"discriminator\": \"null\", \"children\": []}", json);
    }

    private DTLearnerMealy createDTLearnerMock() {
        de.learnlib.discriminationtree.DiscriminationTree tree;
        tree = mock(de.learnlib.discriminationtree.DiscriminationTree.class);
        given(tree.getRoot()).willReturn(mock(DTNode.class));
        DTLearnerMealy learner = mock(DTLearnerMealy.class);
        given(learner.getDiscriminationTree()).willReturn(tree);
        return learner;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateInternalDataFromWrongAlgorithmType() {
        LearningAlgorithm.MealyLearner learner = mock(LearningAlgorithm.MealyLearner.class);
        algorithm.getInternalData(learner);
    }

}
