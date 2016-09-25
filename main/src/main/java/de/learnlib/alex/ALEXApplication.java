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

package de.learnlib.alex;

import de.learnlib.alex.core.services.LearnAlgorithmService;
import de.learnlib.alex.annotations.LearnAlgorithm;
import de.learnlib.alex.core.dao.SettingsDAO;
import de.learnlib.alex.core.dao.UserDAO;
import de.learnlib.alex.core.entities.Settings;
import de.learnlib.alex.core.entities.User;
import de.learnlib.alex.core.entities.UserRole;
import de.learnlib.alex.security.AuthenticationFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ValidationException;
import javax.ws.rs.ApplicationPath;

/**
 * Main class of the REST API. Implements the Jersey {@link ResourceConfig} and does some configuration and stuff.
 */
@Component
@ApplicationPath("rest")
public class ALEXApplication extends ResourceConfig {

    /** The E-Mail for the default admin, i.e. the admin that will be auto created if no other admin exists. */
    public static final String DEFAULT_ADMIN_EMAIL = "admin@alex.example";

    /** The Password for the default admin, i.e. the admin that will be auto created if no other admin exists. */
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private static final Logger LOGGER = LogManager.getLogger();

    /** The UserDOA to create an admin if needed. */
    @Inject
    private UserDAO userDAO;

    /** The SettingsDAO to create the settings object if needed. */
    @Inject
    private SettingsDAO settingsDAO;

    /** The {@link LearnAlgorithmService} to use. */
    @Inject
    private LearnAlgorithmService algorithms;

    /**
     * Constructor where the magic happens.
     */
    public ALEXApplication() {
        // register REST resources classes
        packages(true, "de.learnlib.alex.rest");

        register(MultiPartFeature.class);
        register(AuthenticationFilter.class);
        register(RolesAllowedDynamicFeature.class); // allow protecting routes with user roles
        register(JacksonConfiguration.class);
    }

    /**
     * Create an admin at the start of th ALEX if no admin is currently in the DB.
     */
    @PostConstruct
    public void createAdminIfNeeded() {
        // create an admin if none exists
        if (userDAO.getAllByRole(UserRole.ADMIN).size() == 0) {
            User admin = new User();
            admin.setEmail(DEFAULT_ADMIN_EMAIL);
            admin.setRole(UserRole.ADMIN);
            admin.setEncryptedPassword(DEFAULT_ADMIN_PASSWORD);

            userDAO.create(admin);
        }
    }

    /**
     * Initialize system properties and create the settings object if needed.
     */
    @PostConstruct
    public void initSettings() {
        Settings settings = settingsDAO.get();
        if (settings == null) {
            try {
                settings = new Settings();

                String chromeDriverPath = System.getProperty("webdriver.chrome.driver", "");
                String geckoDriverPath  = System.getProperty("webdriver.gecko.driver",  "");

                Settings.DriverSettings driverSettings = new Settings.DriverSettings(chromeDriverPath, geckoDriverPath);
                settings.setDriverSettings(driverSettings);

                settingsDAO.create(settings);
            } catch (ValidationException e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            Settings.DriverSettings driverSettings = settings.getDriverSettings();
            System.setProperty("webdriver.chrome.driver", driverSettings.getChrome());
            System.setProperty("webdriver.gecko.driver",  driverSettings.getFirefox());
        }
    }

    /**
     * Search fo LearnAlgorithms in the class path and add them to our {@link LearnAlgorithmService}.
     */
    @PostConstruct
    public void findAlgorithms() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(LearnAlgorithm.class));

        LOGGER.info("Searching for LearnAlgorithms...");
        for (BeanDefinition bd : scanner.findCandidateComponents("")) {
            String beanClassName = bd.getBeanClassName();
            LOGGER.info("Found LearnAlgorithm '{}'.", beanClassName);

            try {
                Class<?> clazz = Class.forName(beanClassName);
                algorithms.addAlgorithm(clazz);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Can not use the LearnAlgorithm '{}'!", beanClassName, e);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not find an already found class!", e);
            }
        }
        LOGGER.info("{} LearnAlgorithms found.", algorithms.size());
    }
}
