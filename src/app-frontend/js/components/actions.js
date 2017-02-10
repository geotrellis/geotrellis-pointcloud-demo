export const SET_TARGET_LAYER = 'SET_TARGET_LAYER';
export const SET_TARGET_LAYER_OPACITY = 'SET_TARGET_LAYER_OPACITY';
export const SET_DATA_SOURCE_TYPE = 'SET_DATA_SOURCE_TYPE';
export const SET_DEM_ALGORITHM = 'SET_DEM_ALGORITHM';
export const SET_RENDER_METHOD = 'SET_RENDER_METHOD';
export const CLEAR_GEOMETRIES = 'CLEAR_GEOMETRIES';
export const SET_POLYGON = 'SET_POLYGON';
export const SET_POINT = 'SET_POINT';
export const SET_ANALYSIS_ON = 'SET_ANALYSIS_ON';
export const SET_ACTIVE_TAB = 'SET_ACTIVE_TAB';

export function setTargetLayerName(layerName) {
    return {
        type: SET_TARGET_LAYER,
        payload: layerName,
    };
}

export function setTargetLayerOpacity(value) {
    return {
        type: SET_TARGET_LAYER_OPACITY,
        payload: value,
    };
}

export function setDataSourceType(dsType) {
    return {
        type: SET_DATA_SOURCE_TYPE,
        payload: dsType,
    };
}

export function setRenderMethod(method) {
    return {
        type: SET_RENDER_METHOD,
        payload: method,
    };
}

export function setDEMAlgorithm(method) {
    return {
        type: SET_DEM_ALGORITHM,
        payload: method
    };
}

export function clearGeometries() {
    return {
        type: CLEAR_GEOMETRIES,
        payload: null,
    };
}

export function setPolygon(polygon) {
    return {
        type: SET_POLYGON,
        payload: polygon,
    };
}

export function setPoint(point) {
    return {
        type: SET_POINT,
        payload: point,
    };
}

export function setAnalysisOn(flag) {
    return {
        type: SET_ANALYSIS_ON,
        payload: flag
    };
}

export function setActiveTab(idx) {
    return {
        type: SET_ACTIVE_TAB,
        payload: idx
    }
}
