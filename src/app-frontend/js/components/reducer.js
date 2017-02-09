import _ from 'lodash';
import immutable from 'object-path-immutable';
import { defaultMapCenter } from 'constants';

import {
    SET_TARGET_LAYER,
    SET_TARGET_LAYER_OPACITY,
    SET_DATA_SOURCE_TYPE,
    SET_RENDER_METHOD
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
    center: defaultMapCenter,
};

export default function appPage(state = initAppPage, action) {
    var newState = state;

    switch (action.type) {
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
        default:
            return newState;
    }
}
