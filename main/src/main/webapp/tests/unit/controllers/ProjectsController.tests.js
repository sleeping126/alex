describe('ProjectsController', () => {
    let ProjectsController;
    let $state;
    let $q;
    let SessionService;
    let scope;
    let Project;
    let ProjectResource;
    let $controller;
    let EventBus;
    let ToastService;
    let events;

    let deferred;

    beforeEach(module('ALEX'));
    beforeEach(inject((_$controller_, $rootScope, _$state_, _SessionService_, _Project_, _ProjectResource_, _EventBus_,
                       _ToastService_, _$q_, _events_) => {

        $state = _$state_;
        $q = _$q_;
        SessionService = _SessionService_;
        scope = $rootScope.$new();
        Project = _Project_;
        ProjectResource = _ProjectResource_;
        $controller = _$controller_;
        EventBus = _EventBus_;
        ToastService = _ToastService_;
        events = _events_;

        SessionService.user.save(ENTITIES.users[0]);
        deferred = $q.defer();

        $state.go('projects');
        spyOn($state, 'go').and.callThrough();
    }));

    function createController() {
        ProjectsController = $controller('ProjectsController', {
            $scope: scope,
            $state: $state,
            SessionService: SessionService,
            ProjectResource: ProjectResource,
            EventBus: EventBus,
            ToastService: ToastService
        });
    }

    afterEach(() => {
        sessionStorage.removeItem('project');
    });

    it('should initialize the controller with an empty project array', () => {
        spyOn(ProjectResource, 'getAll').and.returnValue(deferred.promise);
        deferred.resolve([]);

        createController();
        scope.$digest();
        expect(ProjectsController.projects.length).toEqual(0);
        expect($state.current.name).toEqual('projects');
    });

    it('should redirect to the project dashboard if there is a project in the session storage', () => {
        SessionService.project.save(ENTITIES.projects[0]);
        createController();
        scope.$digest();
        expect($state.current.name).toEqual('dashboard')
    });

    it('should load projects from the resource', () => {
        spyOn(ProjectResource, 'getAll').and.returnValue(deferred.promise);
        createController();

        deferred.resolve(ENTITIES.projects.map(p => new Project(p)));

        scope.$digest();
        expect(ProjectsController.projects.length).toEqual(3);
        ProjectsController.projects.forEach(p => expect(p instanceof Project));
        expect(ProjectResource.getAll).toHaveBeenCalled();
    });

    it('should show an error toast if loading projects failed', () => {
        spyOn(ProjectResource, 'getAll').and.returnValue(deferred.promise);
        spyOn(ToastService, 'danger').and.callThrough();
        createController();

        deferred.reject({data: {message: null}});

        scope.$digest();
        expect(ProjectsController.projects.length).toEqual(0);
        expect(ProjectResource.getAll).toHaveBeenCalled();
        expect(ToastService.danger).toHaveBeenCalled();
    });

    // used for the next tests
    function prepare() {
        spyOn(ProjectResource, 'getAll').and.returnValue(deferred.promise);
        createController();
        deferred.resolve(ENTITIES.projects.map(p => new Project(p)));
        scope.$digest();
    }

    it('should add a project to the list on project:created event', () => {
        prepare();
        const pre = ProjectsController.projects.length;
        EventBus.emit(events.PROJECT_CREATED, {project: new Project({id: 5})});
        expect(ProjectsController.projects.length).toEqual(pre + 1);
        expect(ProjectsController.projects[pre].id).toEqual(5);
    });

    it('should remove a project from the list on project:deleted event', () => {
        prepare();
        const pre = ProjectsController.projects.length;
        EventBus.emit(events.PROJECT_DELETED, {project: new Project({id: 1})});
        expect(ProjectsController.projects.length).toEqual(pre - 1);
        expect(ProjectsController.projects.find(p => p.id === 1)).toBeUndefined();
    });

    it('should update a project from the list on project:updated event', () => {
        prepare();
        const pre = ProjectsController.projects.length;
        EventBus.emit(events.PROJECT_UPDATED, {project: new Project({id: 1, name: 'updatedName'})});
        expect(ProjectsController.projects.length).toEqual(pre);
        expect(ProjectsController.projects.find(p => p.id === 1).name).toEqual('updatedName');
    })
});