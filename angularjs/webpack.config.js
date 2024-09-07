var webpack = require('webpack');
var path = require('path');

module.exports = {
    context: path.resolve(__dirname, './src/'),
    entry: {
        application: './ts/app.ts',
        behaviours: './ts/behaviours.ts'
    },
    output: {
        filename: './[name].js'
    },
    externals: {
        "entcore/entcore": "entcore",
        "entcore": "entcore",
        "moment": "entcore",
        "underscore": "entcore",
        "jquery": "entcore",
        "angular": "angular"
    },
    resolve: {
        modulesDirectories: ['node_modules'],
        extensions: ['', '.ts', '.js']
    },
    devtool: "source-map",
    module: {
        loaders: [
            {
                test: /\.(ts|js)$/,
                loader: 'ts-loader',
                include: [
                    path.resolve(__dirname, 'src/ts'),
                    path.resolve(__dirname, 'node_modules/clipboard/src')
                ]
            }
        ]
    }
};
