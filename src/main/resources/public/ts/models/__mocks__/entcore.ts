declare let require: any;

export const moment = require('moment');

interface IController {
    name: string,
    contents: any
}
interface IDirective {
    name: string,
    contents: any
}
interface IService {
    name: string,
    contents: any
}

const controllers: Array<IController> = []
const directives: Array<IDirective> = []
const services: Array<IService> = [];
const workspace = {
    v2: {
        models: {}
    }
};
export const ng = {
    service: jest.fn((name:string, contents: any) => {
        this.ng.services.push({name, contents})
    }),
    directive: jest.fn((name:string, contents: any) => {
        this.ng.directives.push({name, contents})
    }),
    controller: jest.fn((name:string, contents: any) => {
        this.ng.controllers.push({name, contents})
    }),
    // init services, controller and directives
    initMockedModules: jest.fn((app: any) => {
        this.ng.services.forEach((s) => app.service(s.name, s.contents));
        this.ng.directives.forEach((d) => app.directive(d.name, d.contents));
        this.ng.controllers.forEach((c) => app.controller(c.name, c.contents));
    }),
    controllers: controllers,
    directives: directives,
    services: services
};