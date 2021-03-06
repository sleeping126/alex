import {Action} from '../../../../../src/js/entities/actions/Action';
import {CheckPageTitleAction} from '../../../../../src/js/entities/actions/webActions/CheckPageTitleAction';
import {actionType} from '../../../../../src/js/constants';

describe('CheckPageTitleAction', () => {
    beforeEach(angular.mock.module('ALEX'));

    it('should extend the default action and should implement a toString method', () => {
        const action = new CheckPageTitleAction({});
        expect(action instanceof Action).toBe(true);
        expect(angular.isFunction(action.toString)).toBe(true);
    });

    it('should have create a default action', () => {
        const expectedAction = {
            type: actionType.WEB_CHECK_PAGE_TITLE,
            negated: false,
            ignoreFailure: false,
            disabled: false,

            title: '',
            regexp: false
        };
        const action = new CheckPageTitleAction({});
        expect(angular.toJson(action)).toEqual(angular.toJson(expectedAction));
    });
});
