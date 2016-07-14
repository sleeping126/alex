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

package de.learnlib.alex.annotations;

/**
 * Annotation for LearnAlgorithm factories.
 */
public @interface LearnAlgorithm {

    /**
     * The enum name of the Algorithm.
     * @return The enum name of the Algorithm.
     */
    String name();

    /**
     * A pretty name of the algorithm.
     * @return A pretty name of the algorithm.
     */
    String prettyName() default "";
}