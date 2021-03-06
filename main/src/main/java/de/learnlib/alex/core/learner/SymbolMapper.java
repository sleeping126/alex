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

import com.rits.cloning.Cloner;
import de.learnlib.alex.core.entities.ExecuteResult;
import de.learnlib.alex.core.entities.Symbol;
import de.learnlib.alex.core.learner.connectors.ConnectorManager;
import de.learnlib.api.SULException;
import de.learnlib.mapper.api.ContextExecutableInput;
import de.learnlib.mapper.api.Mapper;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.SimpleAlphabet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class to map the Symbols and their result to the values used in the learning process.
 */
public class SymbolMapper
        implements Mapper<String, String, ContextExecutableInput<ExecuteResult, ConnectorManager>, ExecuteResult> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Map to manage the symbols according to their name in the Alphabet. */
    private Map<String, Symbol> symbols;

    /** Java object cloner. */
    private Cloner cloner;

    /**
     * Constructor.
     * Initialize the map abbreviation -> symbol.
     *
     * @param symbols - The symbols for the learning process.
     */
    public SymbolMapper(Symbol... symbols) {
        this.symbols = new HashMap<>();
        this.cloner = new Cloner();

        for (Symbol s : symbols) {
            this.symbols.put(s.getAbbreviation(), s);
        }
    }

    @Override
    public ContextExecutableInput<ExecuteResult, ConnectorManager> mapInput(String abstractInput) {
        return symbols.get(abstractInput);
    }

    @Override
    public String mapOutput(ExecuteResult concreteOutput) {
        return concreteOutput.toString();
    }

    @Override
    public MappedException<? extends String> mapUnwrappedException(RuntimeException e) throws RuntimeException {
        LOGGER.info("mapper mapped unwrapped exception", e);
        return null;
    }

    @Override
    public MappedException<? extends String> mapWrappedException(SULException e) throws SULException {
        LOGGER.info("mapper mapped wrapped exception", e);
        return null;
    }

    @Override
    public void post() {
        // nothing to do
    }

    @Override
    public void pre() {
        // nothing to do
    }

    /**
     * Get the alphabet for the learning process as required by the LearnLib.
     * Sorted by key ascending.
     *
     * @return The alphabet.
     */
    public Alphabet<String> getAlphabet() {
        Alphabet<String> sigma = new SimpleAlphabet<>();

        // sort the alphabet for a more consistent learning behavior
        List<String> sortedSymbolList = new ArrayList<>(symbols.keySet());
        sortedSymbolList.sort(String::compareTo);
        sigma.addAll(sortedSymbolList);

        return sigma;
    }

    /**
     * Get the list of symbols.
     *
     * @return The list of symbols.
     */
    public List<Symbol> getSymbols() {
        List<Symbol> list = new LinkedList<>();
        list.addAll(symbols.values());
        return list;
    }

    @Override
    public boolean canFork() {
        return true;
    }

    @Nonnull
    @Override
    public Mapper<String, String, ContextExecutableInput<ExecuteResult, ConnectorManager>, ExecuteResult> fork()
            throws UnsupportedOperationException {
        Symbol[] symbolsArray = symbols.values().toArray(new Symbol[symbols.values().size()]);
        return new SymbolMapper(cloner.deepClone(symbolsArray));
    }
}
