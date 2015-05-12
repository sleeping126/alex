package de.learnlib.alex;

import de.learnlib.alex.core.dao.CounterDAO;
import de.learnlib.alex.core.dao.LearnerResultDAO;
import de.learnlib.alex.core.dao.ProjectDAO;
import de.learnlib.alex.core.dao.SymbolDAO;
import de.learnlib.alex.core.dao.SymbolGroupDAO;
import de.learnlib.alex.core.learner.Learner;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class WeblearnerTestApplication extends ResourceConfig {

    public WeblearnerTestApplication(final ProjectDAO projectDAO, CounterDAO counterDAO,
                                     final SymbolGroupDAO symbolGroupDAO,
                                     final SymbolDAO symbolDAO,
                                     final LearnerResultDAO learnerResultDAO,
                                     final Learner learner,
                                     Class<?>... classes) {
        super(classes);

        // register some classes/ objects for IoC.
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(projectDAO).to(ProjectDAO.class);
                bind(counterDAO).to(CounterDAO.class);
                bind(symbolGroupDAO).to(SymbolGroupDAO.class);
                bind(symbolDAO).to(SymbolDAO.class);
                bind(learner).to(Learner.class);
                bind(learnerResultDAO).to(LearnerResultDAO.class);
            }
        });

    }

}
