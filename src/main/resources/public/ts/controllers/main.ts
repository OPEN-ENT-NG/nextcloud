import {ng, template} from 'entcore';

declare let window: any;

interface ViewModel {
	$onInit(): any;
	$onDestroy(): any;
}

/**
	Wrapper controller
	------------------
	Main controller.
**/

// we use function instead of arrow function to apply life's cycle hook

export const mainController = ng.controller('MainController', ['$scope', 'route', function ($scope, route) {
	const vm: ViewModel = this;

	// init life's cycle hook
	vm.$onInit = () => {
		console.log("MainController's life cycle");
	};

	// destruction cycle hook
	vm.$onDestroy = () => {

	};

	route({
		list: () => {
			template.open('main', `second-page`);
		},
		defaultView: () => {
			template.open('main', `main`);
		}
	});
}]);
