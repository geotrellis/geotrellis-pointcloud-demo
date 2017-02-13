import _ from 'lodash';
import axios from 'axios';


export const baseUrl = (function() {
    // Should change if deploy or dev
    var hostname = window.location.hostname;
    var baseUrl = 'http://' + hostname + ':7070/';
    return baseUrl;
})();

export const apiUrl = {
    point : {
        single : baseUrl + 'stats/point/single/',
        diff: baseUrl + 'stats/point/diff/',
    },
    poly : {
        single : baseUrl + 'stats/poly/single/',
        diff: baseUrl + 'stats/poly/diff/',
    },
};

export function singlePointStats(layerName, zoom, point) {
    let url = apiUrl.point.single + layerName + '/' + zoom;
    url = url.concat('?lat=' + point.lat + '&lng=' + point.lng);
    return axios.get(url);
};

export function diffPointStats(layer1Name, layer2Name, zoom, point) {
    let url = apiUrl.point.diff + layer1Name + '/' + layer2Name + '/' + zoom;
    url = url.concat('?lat=' + point.lat + '&lng=' + point.lng);
    return axios.get(url);
};

export function singlePolyStats(layerName, zoom, poly) {
    let url = apiUrl.poly.single + layerName + '/' + zoom + '?poly=' + JSON.stringify(poly);
    return axios.get(url);
};

export function diffPolyStats(layer1Name, layer2Name, zoom, poly) {
    let url = apiUrl.poly.diff + layer1Name + '/' + layer2Name + '/' + zoom + '?poly=' + JSON.stringify(poly);
    return axios.get(url);
};


// export function singlePolyStats(layerName, zoom, poly) {
//     let url = apiUrl.poly.single + layerName + '/' + zoom;
//     return axios({
//         method: 'post',
//         url: url,
//         data: poly
//     });
// };

// export function diffPolyStats(layer1Name, layer2Name, zoom, poly) {
//     let url = apiUrl.poly.diff + layer1Name + '/' + layer2Name + '/' + zoom;
//     return axios({
//         method: 'post',
//         url: url,
//         data: poly
//     });
// };
