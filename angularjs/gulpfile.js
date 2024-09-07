var gulp = require('gulp');
var webpack = require('webpack-stream');
var merge = require('merge2');
const replace = require('gulp-replace');
var clean = require('gulp-clean');
var argv = require('yargs').argv;
var fs = require('fs');

gulp.task('drop-cache', function(){
     return gulp.src(['./src/dist'], { read: false })
		.pipe(clean());
});

gulp.task('webpack', ['drop-cache'], () => {
    return gulp.src('./src/**/*.ts')
        .pipe(webpack(require('./webpack.config.js')))
        .on('error', function handleError() {
            this.emit('end'); // Recover from errors
        })
        .pipe(gulp.dest('./src/dist'));
});

gulp.task('build', ['webpack'], () => {
    var refs = gulp.src("./src/view-src/**/*.html")
        .pipe(replace('@@VERSION', Date.now()))
        .pipe(gulp.dest("./src/view"));

    var copyBehaviours = gulp.src('./src/dist/behaviours.js')
        .pipe(gulp.dest('./src/js'));

    return merge[refs, copyBehaviours];
});

function getModName(fileContent){
    var getProp = function(prop){
        return fileContent.split(prop + '=')[1].split(/\\r?\\n/)[0];
    }
    return getProp('modowner') + '~' + getProp('modname') + '~' + getProp('version');
}

gulp.task('watch', () => {
    var springboard = argv.springboard;
    if(!springboard){
        springboard = '../springboard-open-ent/';
    }
    if(springboard[springboard.length - 1] !== '/'){
        springboard += '/';
    }

    gulp.watch('./angularJS/src/ts/**/*.ts', () => gulp.start('build'));

    fs.readFile("./gradle.properties", "utf8", function(error, content){
        var modName = getModName(content);
        gulp.watch(['./angularJS/src/template/**/*.html', '!./angularJS/src/template/entcore/*.html'], () => {
            console.log('Copying resources to ' + springboard + 'mods/' + modName);
            gulp.src('./angularJS/src/**/*')
                .pipe(gulp.dest(springboard + 'mods/' + modName));
        });

        gulp.watch('./angularJS/src/view/**/*.html', () => {
            console.log('Copying resources to ' + springboard + 'mods/' + modName);
            gulp.src('./angularJS/src/**/*')
                .pipe(gulp.dest(springboard + 'mods/' + modName));
        });
    });
});