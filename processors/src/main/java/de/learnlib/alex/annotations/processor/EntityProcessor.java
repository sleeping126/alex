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

package de.learnlib.alex.annotations.processor;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Processor to collect all entities and create the HibernateUtl class with them.
 */
@SupportedAnnotationTypes("javax.persistence.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EntityProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Map<String, Object> root = new HashMap<>();
        List<TypeElement> entities = new LinkedList<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Entity.class)) {
            TypeElement classElement = (TypeElement) element;
            entities.add(classElement);
        }
        for (Element element : roundEnvironment.getElementsAnnotatedWith(Embeddable.class)) {
            TypeElement classElement = (TypeElement) element;
            entities.add(classElement);
        }

        if (entities.isEmpty()) {
            return false;
        }
        root.put("entities", entities);

        try {
            Configuration templateConfig = FreeMarkerSingleton.getConfiguration();

            Template template = templateConfig.getTemplate("HibernateUtilTemplate.ftl");
            JavaFileObject jfo = processingEnv.getFiler().createSourceFile("de.learnlib.alex.utils.HibernateUtil");

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                                                     "creating source file: " + jfo.toUri());

            Writer writer = jfo.openWriter();
            template.process(root, writer);
            writer.close();
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }

        return true;
    }

}