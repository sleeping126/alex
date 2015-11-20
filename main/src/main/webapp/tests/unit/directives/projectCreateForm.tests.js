describe('projectCreateForm', () => {
    let $rootScope;
    let $compile;
    let ProjectFormModel;
    let renderedElement;
    let controller;
    let ProjectResource;
    let EventBus;
    let events;
    let ToastService;
    let $q;
    let Project;

    beforeEach(module('ALEX'));
    beforeEach(inject((_$rootScope_, _$compile_, _ProjectFormModel_, _ProjectResource_, _EventBus_, _events_,
                       _ToastService_, _$q_, _Project_) => {

        $rootScope = _$rootScope_;
        $compile = _$compile_;
        ProjectFormModel = _ProjectFormModel_;
        ProjectResource = _ProjectResource_;
        EventBus = _EventBus_;
        events = _events_;
        ToastService = _ToastService_;
        $q = _$q_;
        Project = _Project_;

        const element = angular.element("<project-create-form></project-create-form>");
        renderedElement = $compile(element)($rootScope);
        $rootScope.$digest();

        controller = element.controller('projectCreateForm');
    }));

    it('should start with an empty project model', () => {
        expect(controller.project).toEqual(new ProjectFormModel());
    });

    it('should create a project, display a message, emit an event and reset the form', () => {
        const deferred = $q.defer();
        spyOn(ProjectResource, 'create').and.returnValue(deferred.promise);
        spyOn(ToastService, 'success').and.callThrough();
        spyOn(EventBus, 'emit').and.callThrough();

        controller.project.name = 'aNewProject';
        controller.createProject();

        const project = new Project(ENTITIES.projects[0]);
        project.name = 'aNewProject';

        deferred.resolve(project);
        $rootScope.$digest();

        expect(ProjectResource.create).toHaveBeenCalled();
        expect(ToastService.success).toHaveBeenCalled();
        expect(EventBus.emit).toHaveBeenCalledWith(events.PROJECT_CREATED, {project: project});
        expect(controller.project).toEqual(new ProjectFormModel());
    });

    it('should display a fail message if the project could not be created', () => {
        const deferred = $q.defer();
        spyOn(ProjectResource, 'create').and.returnValue(deferred.promise);
        spyOn(ToastService, 'danger').and.callThrough();

        controller.createProject();
        deferred.reject({data: {message: null}});
        $rootScope.$digest();

        expect(ProjectResource.create).toHaveBeenCalled();
        expect(ToastService.danger).toHaveBeenCalled();
    });
});