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

import {events} from "../../constants";
import {Project} from "../../entities/Project";

/**
 * The class of the project create form component.
 */
class ProjectCreateForm {

    /**
     * Constructor.
     *
     * @param {ProjectResource} ProjectResource
     * @param {ToastService} ToastService
     * @param {EventBus} EventBus
     */
    // @ngInject
    constructor(ProjectResource, ToastService, EventBus) {
        this.ProjectResource = ProjectResource;
        this.ToastService = ToastService;
        this.EventBus = EventBus;

        /**
         * The empty project model that is used for the form.
         * @type {Project}
         */
        this.project = new Project();

        /**
         * The mirror urls separated by \n.
         * @type {string}
         */
        this.mirrorUrls = "";
    }

    /**
     * Loads a project from a json file.
     *
     * @param {string} projectAsJson - The project to import.
     */
    loadProjectFromFile(projectAsJson) {
        this.projectToImport = JSON.parse(projectAsJson);
    }

    /**
     * Creates a new project.
     */
    createProject() {
        this.mirrorUrls.split('\n').forEach(url => {
            const trimmedUrl = url.trim();
            if (trimmedUrl !== '') this.project.mirrorUrls.push(trimmedUrl);
        });

        this.ProjectResource.create(this.project)
            .then(createdProject => {
                this.ToastService.success(`Project "${createdProject.name}" created`);
                this.EventBus.emit(events.PROJECT_CREATED, {project: createdProject});
                this.project = new Project();
                this.mirrorUrls = "";

                // set the form to its original state
                this.form.$setPristine();
                this.form.$setUntouched();
            })
            .catch(response => {
                this.ToastService.danger('<p><strong>Creation of project failed</strong></p>' + response.data.message);
            });
    }
}

export const projectCreateForm = {
    templateUrl: 'html/components/forms/project-create-form.html',
    controller: ProjectCreateForm,
    controllerAs: 'vm'
};