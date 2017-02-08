import axios from 'axios';
import { map, filter } from 'lodash';

import {
    numericLabel,
    sentenceCase,
    formatCivicInfoUrl,
} from 'utils';

import {
    cartoContainQuery,
    cartoRadiusQuery,
} from 'carto';

import { censusApiQuery, transformData } from 'census';

export const START_FETCH_CARTO = 'START_FETCH_CARTO';
export const COMPLETE_FETCH_CARTO = 'COMPLETE_FETCH_CARTO';
export const FAIL_FETCH_CARTO = 'FAIL_FETCH_CARTO';
export const RESET_ADDRESS_DATA = 'RESET_ADDRESS_DATA';
export const SET_RADIUS = 'SET_RADIUS';
export const START_FETCH_CIVIC_INFO = 'START_FETCH_CIVIC_INFO';
export const COMPLETE_FETCH_CIVIC_INFO = 'COMPLETE_FETCH_CIVIC_INFO';
export const FAIL_FETCH_CIVIC_INFO = 'FAIL_FETCH_CIVIC_INFO';

export const SET_HOVER_GEOM = 'SET_HOVER_GEOM';
export const CLEAR_HOVER_GEOM = 'CLEAR_HOVER_GEOM';
export const SET_HIGHLIGHTED_GEOM = 'SET_HIGHLIGHTED_GEOM';
export const CLEAR_HIGHLIGHTED_GEOM = 'CLEAR_HIGHLIGHTED_GEOM';

export function setHoverGeom(geom) {
    return {
        type: SET_HOVER_GEOM,
        payload: geom,
    };
}

export function clearHoverGeom() {
    return {
        type: CLEAR_HOVER_GEOM,
    };
}

export function setHighlightedGeom(geom, card) {
    return {
        type: SET_HIGHLIGHTED_GEOM,
        payload: {
            geom,
            card,
        },
    };
}

export function clearHighlightedGeom() {
    return {
        type: CLEAR_HIGHLIGHTED_GEOM,
    };
}

export function clearAllGeom() {
    return dispatch => {
        dispatch(clearHoverGeom());
        dispatch(clearHighlightedGeom());
    };
}

export function resetAddressData() {
    return {
        type: RESET_ADDRESS_DATA,
    };
}

export function setRadius(radius) {
    return {
        type: SET_RADIUS,
        payload: radius,
    };
}

export const START_CENSUS_FETCH = 'START_CENSUS_FETCH';
export const COMPLETE_CENSUS_FETCH = 'COMPLETE_CENSUS_FETCH';
export const FAIL_CENSUS_FETCH = 'FAIL_CENSUS_FETCH';
export const UPDATE_DEMOGRAPHICS_DATA = 'UPDATE_DEMOGRAPHICS_DATA';

function startCartoFetch(dataset) {
    return {
        type: START_FETCH_CARTO,
        payload: {
            [dataset]: {
                data: [],
                fetching: true,
                error: {},
            },
        },
    };
}

function doneCartoFetch(dataset, data) {
    return {
        type: COMPLETE_FETCH_CARTO,
        payload: {
            [dataset]: {
                data,
                fetching: false,
                error: {},
            },
        },
    };
}

function failCartoFetch(dataset, error) {
    return {
        type: FAIL_FETCH_CARTO,
        payload: {
            [dataset]: {
                data: [],
                fetching: false,
                error,
            },
        },
        error: true,
    };
}

export function startCensusFetch(dataset) {
    return {
        type: START_CENSUS_FETCH,
        payload: {
            [dataset]: {
                data: [],
                fetching: true,
                error: {},
            },
        },
    };
}

export function completeCensusFetch(dataset, data) {
    const mappedData = transformData(data);
    return {
        type: COMPLETE_CENSUS_FETCH,
        payload: {
            [dataset]: {
                mappedData,
                fetching: false,
                error: {},
            },
        },
    };
}

export function failCensusFetch(dataset, error) {
    return {
        type: FAIL_CENSUS_FETCH,
        payload: {
            [dataset]: {
                data: [],
                fetching: false,
                error,
            },
        },
    };
}

export function updateDemographicsData() {
    return {
        type: UPDATE_DEMOGRAPHICS_DATA,
    };
}

function fetchCartoDataset(dataset, parser) {
    return (center, radius) => (dispatch) => {
        dispatch(startCartoFetch(dataset));
        cartoRadiusQuery(dataset, center, radius)
            .then(({ data }) => dispatch(doneCartoFetch(dataset, map(data.features, parser))))
            .catch(error => dispatch(failCartoFetch(dataset, error)));
    };
}

export function fetchCensusData(dataset, tracts) {
    return dispatch => {
        dispatch(startCensusFetch(dataset, tracts));
        censusApiQuery(tracts)
            .then(({ data }) => dispatch(completeCensusFetch(dataset, data)))
            .then(() => dispatch(updateDemographicsData()))
            .catch(error => dispatch(failCensusFetch(dataset, error)));
    };
}

const fetchCensusTracts = fetchCartoDataset('censusTracts', d => d.properties.tractce10);

const fetchPoliceDistricts = fetchCartoDataset('policeDistricts', ({ properties, geometry }) => ({
    name: `${numericLabel(properties.dist_num)} District`,
    address: properties.location,
    phone: properties.phone,
    geometry,
}));

const fetchCommunityOrgs = fetchCartoDataset('communityOrgs', ({ properties, geometry }) => ({
    name: properties.organizati,
    address: properties.primary_ad,
    phone: properties.primary_ph,
    contactPerson: properties.primary_na,
    description: properties.primary_em,
    geometry,
}));

const fetchFireStations = fetchCartoDataset('fireStations', ({ properties, geometry }) => {
    const { eng, lad, firesta_, location } = properties;
    const engine = eng > 0 ? `Engine ${eng}` : '';
    const ladder = lad > 0 ? `Ladder ${lad}` : '';
    const station = firesta_ > 0 ? `Station ${firesta_}` : '';
    const description = filter([engine, ladder], s => s.length > 0).join(', ');

    return {
        name: sentenceCase(location),
        address: station,
        description,
        geometry,
    };
});

const fetchNec = fetchCartoDataset('nec', ({ properties, geometry }) => ({
    name: properties.name,
    address: properties.address,
    phone: properties.phone,
    geometry,
}));

const fetchNac = fetchCartoDataset('nac', ({ properties, geometry }) => ({
    name: properties.organizati,
    address: sentenceCase(properties.address),
    phone: properties.phone,
    contactPerson: sentenceCase(properties.contact),
    description: properties.contact_ti,
    geometry,
}));

function fetchNeighborhood(center) {
    return (dispatch) => {
        dispatch(startCartoFetch('neighborhoods'));
        cartoContainQuery('neighborhoods', center)
            .then(({ data }) => dispatch(doneCartoFetch('neighborhoods',
                                                        map(data.rows, r => r.listname))))
            .catch(error => dispatch(failCartoFetch('neighborhoods', error)));
    };
}

export function fetchAllData(center, radius) {
    return (dispatch) => {
        dispatch(fetchNeighborhood(center));
        dispatch(fetchCensusTracts(center, radius));
        dispatch(fetchCommunityOrgs(center, radius));
        dispatch(fetchPoliceDistricts(center, radius));
        dispatch(fetchFireStations(center, radius));
        dispatch(fetchNec(center, radius));
        dispatch(fetchNac(center, radius));
    };
}

function startFetchCivicInfo() {
    return {
        type: START_FETCH_CIVIC_INFO,
    };
}

function completeFetchCivicInfo(data) {
    return {
        type: COMPLETE_FETCH_CIVIC_INFO,
        payload: data,
    };
}

function failFetchCivicInfo() {
    return {
        type: FAIL_FETCH_CIVIC_INFO,
    };
}

export function fetchCivicInfo(street) {
    const civicInfoUrl = formatCivicInfoUrl(street);
    return (dispatch) => {
        dispatch(startFetchCivicInfo());
        axios.get(civicInfoUrl)
             .then(({ data }) => dispatch(completeFetchCivicInfo(data)))
             .catch(() => dispatch(failFetchCivicInfo()));
    };
}
