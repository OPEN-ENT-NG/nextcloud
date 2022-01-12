import {ng} from 'entcore';

interface ViewModel {
    $onInit(): any;
    $onDestroy(): any;

    text: string;
}

// we use function instead of arrow function to apply life's cycle hook
export const nextcloudController = ng.controller('NextcloudController', ['$scope', 'route', function ($scope, route) {
    const vm: ViewModel = this;

    // init life's cycle hook
    vm.$onInit = () => {
        vm.text = "test var";
        console.log("vm parent (main): ", $scope.$parent.vm);
    };

    // destruction cycle hook
    vm.$onDestroy = () => {

    };

}]);
