import _ from 'lodash';
import immutable from 'object-path-immutable';
import { defaultMapCenter } from 'constants';

import {
    SET_TARGET_LAYER,
    SET_TARGET_LAYER_OPACITY,
    SET_DATA_SOURCE_TYPE,
    SET_RENDER_METHOD,
    CLEAR_GEOMETRIES,
    SET_POLYGON,
    SET_POINT,
    SET_ANALYSIS_ON
} from './actions';


const initAppPage = {
    singleLayer: {
        idwChecked: true,
        tinChecked: false,
        staticChecked: true,
        dynamicChecked: false,
        targetLayerOpacity: 0.9,
        colorRampChecked: true,
        hillshadeChecked: false,
        snowOnChecked: true,
        snowOffChecked: true,
        renderMethod: "COLORRAMP",
        targetLayerName: "SNOW-ON",
    },
    changeDetection: {
        idwChecked: true,
        tinChecked: false,
        staticChecked: true,
        dynamicChecked: false,
        targetLayerOpacity: 0.9,
    },
    analysis: {
        analysisOn: true,
        results: {
            mean: 0.0,
            min: 0.0,
            max: 0.0,
            diff: 0.0
        },
        polygon: null,
        point: null,
    },
    center: defaultMapCenter,
};

export default function appPage(state = initAppPage, action) {
    var newState = state;

    switch (action.type) {
        case CLEAR_GEOMETRIES:
            console.log("Clearing Geometries");
            newState = immutable.set(newState, 'analysis.polygon', null);
            newState = immutable.set(newState, 'analysis.point', null);
        case SET_POLYGON:
            console.log("Setting polygon");
            newState = immutable.set(newState, 'analysis.polygon', action.payload);
            newState = immutable.set(newState, 'analysis.point', null);
        case SET_POINT:
            console.log("Setting polygon");
            newState = immutable.set(newState, 'analysis.polygon', null);
            newState = immutable.set(newState, 'analysis.point', action.payload);
        case SET_TARGET_LAYER:
            console.log("LAYER NAME: " + action.payload);
            var snowOnChecked = action.payload == "SNOW-ON";
            var snowOffChecked = action.payload == "SNOW-OFF";

            newState = immutable.set(newState, 'singleLayer.targetLayerName', action.payload);
            newState = immutable.set(newState, 'singleLayer.snowOnChecked', snowOnChecked);
            newState = immutable.set(newState, 'singleLayer.snowOffChecked', snowOffChecked);
            return newState;
        case SET_TARGET_LAYER_OPACITY:
            return immutable.set(newState, 'singleLayer.targetLayerOpacity', action.payload);
        case SET_DATA_SOURCE_TYPE:
            switch (action.payload) {
                case 'STATIC':
                    newState = immutable.set(newState, 'singleLayer.staticChecked', true);
                    return immutable.set(newState, 'singleLayer.dynamicChecked', false);
                case 'DYNAMIC':
                    newState = immutable.set(newState, 'singleLayer.staticChecked', false);
                    return immutable.set(newState, 'singleLayer.dynamicChecked', true);
                default:
                    return newState;
            }
        case SET_RENDER_METHOD:
            newState = immutable.set(newState, 'singleLayer.renderMethod', action.payload);
            switch (action.payload) {
                case 'COLORRAMP':
                    newState = immutable.set(newState, 'singleLayer.colorRampChecked', true);
                    return immutable.set(newState, 'singleLayer.hillshadeChecked', false);
                case 'DYNAMIC':
                    newState = immutable.set(newState, 'singleLayer.colorRampCheckd', false);
                    return immutable.set(newState, 'singleLayer.hillshadeChecked', true);
                default:
                    return newState;
            }
        case SET_ANALYSIS_ON:
            newState = immutable.set(newState, 'analysis.analysisOn', action.payload);
            return newState;
        default:
            return newState;
    }

}
