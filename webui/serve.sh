#!/bin/sh

# ng serve --target development --environment dev --sourcemaps true --vendor-chunk true --common-chunk true --base-href http://portfolio.dev.jchein.name:4300/ --serve-path / --verbose true --progress true --named-chunks true --port 4300 --host portfolio.dev.jchein.name --live-reload true --public-host http://portfolio.dev.jchein.name:4300/ --disable-host-check false --hmr false

ng serve --host portfolio.dev.jchein.name --port 4200 --live-reload --open --poll 2500 --progress --proxy-config ./proxy.conf.json --public-host http://portfolio.dev.jchein.name:4200/ --base-href http://portfolio.dev.jchein.name:4200/ --serve-path / --source-map --common-chunk --vendor-chunk --watch

