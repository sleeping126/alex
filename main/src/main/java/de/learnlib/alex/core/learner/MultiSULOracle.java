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

package de.learnlib.alex.core.learner;

import de.learnlib.api.MembershipOracle;
import de.learnlib.api.Query;
import de.learnlib.api.SUL;
import net.automatalib.words.Word;
import net.automatalib.words.WordBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Oracle that allows batched execution of membership queries to multiple suls.
 *
 * @param <I> Input symbol type.
 * @param <O> Output symbol type.
 */
@ParametersAreNonnullByDefault
public class MultiSULOracle<I, O> implements MembershipOracle<I, Word<O>> {

    /** The sul the membership queries should be posed to. */
    private final SUL<I, O> sul;

    /**
     * Constructor.
     *
     * @param sul
     *          The sul the membership queries should be posed to.
     */
    public MultiSULOracle(SUL<I, O> sul) {
        this.sul = sul;
    }

    @Override
    public void processQueries(Collection<? extends Query<I, Word<O>>> queries) {
        if (queries.size() > 0) {
            processQueries(sul, queries);
        }
    }

    private void processQueries(SUL<I, O> sul, Collection<? extends Query<I, Word<O>>> queries) {
        ExecutorService executor = Executors.newFixedThreadPool(queries.size());

        for (Query<I, Word<O>> q : queries) {
            Runnable worker = () -> {

                // forking the sul allows us to pose multiple
                // queries in parallel to multiple suls
                SUL<I, O> forkedSul = sul.fork();
                forkedSul.pre();

                try {

                    // Prefix: Execute symbols, don't record output
                    for (I sym : q.getPrefix()) {
                        forkedSul.step(sym);
                    }

                    // Suffix: Execute symbols, outputs constitute output word
                    WordBuilder<O> wb = new WordBuilder<>(q.getSuffix().length());
                    for (I sym : q.getSuffix()) {
                        wb.add(forkedSul.step(sym));
                    }

                    q.answer(wb.toWord());
                } finally {
                    forkedSul.post();
                }
            };

            executor.submit(worker);
        }

        executor.shutdown();
        while (!executor.isTerminated()) { // wait for all futures to finish
        }
    }

}
